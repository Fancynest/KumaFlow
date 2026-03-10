package com.bearbones.kumaflow

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Manager class responsible for scheduling, canceling, and displaying daily local notifications.
 * It utilizes AlarmManager to ensure precise delivery times.
 */
object KumaReminderManager {
    private const val CHANNEL_ID = "kumaflow_reminder_channel"
    const val REMINDER_1_REQ_CODE = 101
    const val REMINDER_2_REQ_CODE = 102

    /**
     * Schedules an exact alarm for the specified hour and minute.
     */
    fun scheduleReminder(context: Context, hour: Int, minute: Int, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, KumaReminderReceiver::class.java).apply {
            putExtra("REQUEST_CODE", requestCode)
        }

        // Enforce FLAG_IMMUTABLE for PendingIntent security compliance in modern Android
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            // If the specified time has already passed today, schedule it for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        // Use setExactAndAllowWhileIdle to ensure the alarm triggers even if the device is in Doze mode
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Failsafe for Android 14+ if the user explicitly revokes exact alarm permission
            e.printStackTrace()
        }
    }

    /**
     * Cancels an existing scheduled alarm based on its request code.
     */
    fun cancelReminder(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, KumaReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Builds and fires the local notification with a randomized copy array.
     */
    fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the NotificationChannel, required on Android 8.0 (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "KumaFlow Daily Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders for expense tracking"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Define the pool of randomized reminder messages
        val messages = listOf(
            Pair("Waktunya rekap hari ini! \uD83D\uDCDD", "Mari luangkan waktu 1 menit buat nyatet pengeluaran hari ini biar keuangan tetap aman."),
            Pair("Saldo bulan ini masih aman? \uD83D\uDCB0", "Jangan sampai ada pengeluaran yang terlewat. Yuk, catat dulu di KumaFlow sebelum lupa!"),
            Pair("Kuma nungguin catatanmu nih \uD83D\uDC3B✨", "Hari ini ada transaksi apa saja? Sini setor catatannya ke KumaFlow."),
            Pair("Persiapan sebelum istirahat \uD83C\uDF19", "Catat pengeluaran hari ini biar besok bangun dengan pikiran tenang. Selamat istirahat!"),
            Pair("Satu langkah kecil buat nabung! \uD83D\uDCC8", "Disiplin mencatat adalah kunci. Yuk absen dulu pengeluaran atau pemasukanmu hari ini.")
        )

        // Select a random message to prevent notification fatigue
        val randomMsg = messages.random()

        // Construct the intent to open the application when the notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(randomMsg.first)
            .setContentText(randomMsg.second)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Dispatch the notification
        notificationManager.notify(1001, builder.build())
    }
}

/**
 * BroadcastReceiver responsible for triggering the notification and rescheduling the next exact alarm.
 */
class KumaReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        KumaReminderManager.showNotification(context)

        val reqCode = intent.getIntExtra("REQUEST_CODE", -1)

        // Asynchronously fetch user preferences from Room to reschedule the next occurrence
        CoroutineScope(Dispatchers.IO).launch {
            val db = KumaDatabase.getDatabase(context)
            val profile = db.transactionDao().getUserProfile().firstOrNull()

            if (profile != null && profile.isReminderOn) {
                if (reqCode == KumaReminderManager.REMINDER_1_REQ_CODE) {
                    val parts = profile.reminderTime1.split(":")
                    if (parts.size == 2) KumaReminderManager.scheduleReminder(context, parts[0].toInt(), parts[1].toInt(), reqCode)
                } else if (reqCode == KumaReminderManager.REMINDER_2_REQ_CODE) {
                    val parts = profile.reminderTime2.split(":")
                    if (parts.size == 2) KumaReminderManager.scheduleReminder(context, parts[0].toInt(), parts[1].toInt(), reqCode)
                }
            }
        }
    }
}

/**
 * BroadcastReceiver responsible for restoring alarms after a device reboot.
 * This ensures reminders persist across system restarts.
 */
class KumaBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = KumaDatabase.getDatabase(context)
                val profile = db.transactionDao().getUserProfile().firstOrNull()

                if (profile != null && profile.isReminderOn) {
                    val parts1 = profile.reminderTime1.split(":")
                    if (parts1.size == 2) KumaReminderManager.scheduleReminder(context, parts1[0].toInt(), parts1[1].toInt(), KumaReminderManager.REMINDER_1_REQ_CODE)

                    val parts2 = profile.reminderTime2.split(":")
                    if (parts2.size == 2) KumaReminderManager.scheduleReminder(context, parts2[0].toInt(), parts2[1].toInt(), KumaReminderManager.REMINDER_2_REQ_CODE)
                }
            }
        }
    }
}