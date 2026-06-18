package com.bearbones.kumaflow

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class KumaWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Sync widget data on every update request
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Intercept broadcast actions emitted by MainActivity for manual updates
        if (intent.action == "com.bearbones.kumaflow.UPDATE_WIDGET") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, KumaWidgetProvider::class.java))
            ids.forEach { id -> updateWidget(context, appWidgetManager, id) }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_kumaflow)

        // Set onClick listener to launch the main application interface
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.tv_widget_balance, pendingIntent)

        // Fetch application data asynchronously in the background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = KumaDatabase.getDatabase(context)
                val profile = db.transactionDao().getUserProfile().firstOrNull() ?: return@launch

                // Utilize the updated DAO query (WithSplits) for complex transaction structures
                val transactionsWithSplits = db.transactionDao().getAllTransactionsWithSplits().firstOrNull() ?: emptyList()

                val locale = Locale.forLanguageTag("id-ID")
                val curSym = when(profile.currency) { "USD" -> "$"; "EUR" -> "€"; "JPY" -> "¥"; else -> "Rp" }

                val currentMonth = LocalDateTime.now().monthValue
                val currentYear = LocalDateTime.now().year

                var income = 0L
                var expenses = 0L
                val walletBalances = mutableMapOf<String, Long>()

                profile.wallets.split(",").filter { it.isNotBlank() }.forEach { walletBalances[it] = 0L }

                transactionsWithSplits.forEach { txObj ->
                    try {
                        val t = txObj.transaction
                        val dt = LocalDateTime.parse(t.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        val amt = t.amount.toLongOrNull() ?: 0L

                        // Compute and allocate split transaction values across different wallets
                        if (txObj.splits.isNotEmpty()) {
                            txObj.splits.forEach { split ->
                                val current = walletBalances[split.splitWallet] ?: 0L
                                walletBalances[split.splitWallet] = current + (if (t.isIncome) split.splitAmount else -split.splitAmount)
                            }
                        } else {
                            val current = walletBalances[t.wallet] ?: 0L
                            walletBalances[t.wallet] = current + (if(t.isIncome) amt else -amt)
                        }

                        // Calculate the total income and expenses exclusively for the current month
                        if (dt.monthValue == currentMonth && dt.year == currentYear) {
                            if (t.isIncome) income += amt else expenses += amt
                        }
                    } catch (_: Exception) {}
                }

                val totalBal = walletBalances.values.sum()
                val top3Wallets = walletBalances.entries.toList().take(3)

                // Update widget UI components with the newly calculated data
                views.setTextViewText(R.id.tv_widget_balance, "$curSym ${NumberFormat.getInstance(locale).format(totalBal)}")
                views.setTextViewText(R.id.tv_widget_income, "$curSym ${NumberFormat.getInstance(locale).format(income)}")
                views.setTextViewText(R.id.tv_widget_expense, "$curSym ${NumberFormat.getInstance(locale).format(expenses)}")

                if (top3Wallets.isNotEmpty()) {
                    views.setTextViewText(R.id.tv_w1_name, top3Wallets[0].key)
                    views.setTextViewText(R.id.tv_w1_bal, "$curSym ${NumberFormat.getInstance(locale).format(abs(top3Wallets[0].value))}")
                }
                if (top3Wallets.size > 1) {
                    views.setTextViewText(R.id.tv_w2_name, top3Wallets[1].key)
                    views.setTextViewText(R.id.tv_w2_bal, "$curSym ${NumberFormat.getInstance(locale).format(abs(top3Wallets[1].value))}")
                }
                if (top3Wallets.size > 2) {
                    views.setTextViewText(R.id.tv_w3_name, top3Wallets[2].key)
                    views.setTextViewText(R.id.tv_w3_bal, "$curSym ${NumberFormat.getInstance(locale).format(abs(top3Wallets[2].value))}")
                }

                // Apply the updated views to the homescreen widget
                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}