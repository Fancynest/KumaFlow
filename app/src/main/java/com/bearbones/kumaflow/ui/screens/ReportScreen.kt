@file:Suppress("SpellCheckingInspection", "UNUSED_PARAMETER", "unused", "CanBeVal", "DEPRECATION", "ScheduleExactAlarm")
@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.bearbones.kumaflow

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bearbones.kumaflow.glassCard
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import androidx.compose.ui.draw.blur
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border

// --- DATA CLASSES & OBJECTS ---
@Composable
fun ReportScreen(
    profile: UserProfile,
    monthlyTransactions: List<KumaTransaction>,
    allTransactions: List<KumaTransaction>,
    income: Long,
    expenses: Long,
    balance: Long,
    selectedMonth: Int,
    selectedYear: Int,
    paddingValues: PaddingValues,
    onMonthChange: (Int, Int) -> Unit,
    onOpenWrapped: (Int, Int) -> Unit = { _, _ -> }
) {
    val locale = Locale.forLanguageTag("id-ID")
    val curSym = when(profile.currency) { "USD", "AUD", "CAD", "SGD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; "JPY", "CNY" -> "¥"; "CHF" -> "CHF"; else -> "Rp" }

    fun getCatColor(catName: String): Color {
        val predefined = mapOf("Financial" to Color(0xFF623802), "Food" to Color(0xFFD5641C), "Shopping" to Color(0xFFFEDD60), "Health" to Color(0xFFFEE6B1), "Transport" to Color(0xFFFFFFFF), "Education" to Color(0xFF929292), "Entertainment" to Color(0xFF000000), "Others" to Color(0xFF006064))
        return predefined[catName] ?: Color(android.graphics.Color.HSVToColor(floatArrayOf(abs(catName.hashCode()) % 360f, 0.6f, 0.9f)))
    }

    val expensePerCat = monthlyTransactions.filter { !it.isIncome }.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount.toLongOrNull() ?: 0L } }
    val catTargets = remember(profile.categoryTargets) { try { JSONObject(profile.categoryTargets) } catch (e: Exception) { JSONObject() } }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 24.dp).verticalScroll(rememberScrollState())) {
        Text(AppStr.rep, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = AppText())
        Spacer(modifier = Modifier.height(16.dp))

        MonthYearSelector(selectedMonth, selectedYear, onMonthChange)
        Spacer(modifier = Modifier.height(16.dp))

        val monthNamesList = if (AppStr.isId) listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember") else listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val currentSelectedMonthName = monthNamesList.getOrElse(selectedMonth - 1) { "" }

        OutlinedButton(
            onClick = { onOpenWrapped(selectedMonth, selectedYear) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppPrimary().copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = "Rewatch", tint = AppPrimary())
            Spacer(modifier = Modifier.width(8.dp))
            Text("Putar Ulang Wrapped $currentSelectedMonthName $selectedYear ✨", color = AppPrimary(), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        val isPrideThemeActive = profile.themeMode == 3 || profile.themeMode == 4
        val prideGradient = Brush.linearGradient(colors = listOf(Color(0xFFE40303), Color(0xFFFF8C00), Color(0xFFFFED00), Color(0xFF008026), Color(0xFF24408E), Color(0xFF732982)))
        val defaultSurfaceColor = AppSurface()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 185.dp)
                .border(1.dp, AppText().copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(if (isPrideThemeActive) prideGradient else androidx.compose.ui.graphics.SolidColor(defaultSurfaceColor))
        ) {
            Row(modifier = Modifier.padding(24.dp).fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(AppStr.sum, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = if (isPrideThemeActive) Color.White else AppText())
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(AppStr.net, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    val balPref = if (balance < 0) "- " else "+"
                    AutoSizeText(text = "$curSym $balPref${NumberFormat.getInstance(locale).format(abs(balance))}", modifier = Modifier.fillMaxWidth(), fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White, minimumFallbackSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                    IncomeExpensePill(AppStr.inc, "$curSym ${NumberFormat.getInstance(locale).format(income)}", if (isPrideThemeActive) Color.White else AppGreen(), true)
                    Spacer(modifier = Modifier.height(12.dp))
                    IncomeExpensePill(AppStr.exp, "$curSym ${NumberFormat.getInstance(locale).format(expenses)}", if (isPrideThemeActive) Color.White else AppRed(), false)
                }
            }
        }

        if (profile.monthlyTarget > 0) {
            Spacer(modifier = Modifier.height(20.dp))
            val progress = (expenses.toFloat() / profile.monthlyTarget.toFloat()).coerceIn(0f, 1f)
            val isOver = expenses > profile.monthlyTarget

            Text(AppStr.targetProg, fontWeight = FontWeight.Bold, color = AppText())
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape), color = if(isOver) AppRed() else AppGreen(), trackColor = AppSurfaceVariant())
            Text("${(progress * 100).toInt()}% " + (if(AppStr.isId) "dari" else "of") + " $curSym ${NumberFormat.getInstance(locale).format(profile.monthlyTarget)}", fontSize = 12.sp, color = if(isOver) AppRed() else Color.Gray)
        }

        Spacer(modifier = Modifier.height(30.dp))
        Text(AppStr.spendBreak, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AppText())
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppText().copy(alpha = 0.1f), RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface())
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(180.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                    val bgArcCol = AppSurfaceVariant()
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (expenses == 0L) {
                            drawArc(color = bgArcCol, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(25.dp.toPx()))
                        } else {
                            var start = -90f
                            expensePerCat.forEach { (cat, amt) ->
                                val sweep = (amt.toFloat() / expenses.toFloat()) * 360f
                                drawArc(color = getCatColor(cat), startAngle = start, sweepAngle = sweep, useCenter = false, style = Stroke(25.dp.toPx()))
                                start += sweep
                            }
                        }
                    }
                    AutoSizeText(text = "$curSym ${NumberFormat.getInstance(locale).format(expenses)}", modifier = Modifier.padding(16.dp).fillMaxWidth(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = AppText(), minimumFallbackSize = 12.sp, textAlign = TextAlign.Center)
                }

                if (expenses == 0L) {
                    Text(AppStr.noData, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppText().copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        expensePerCat.forEach { (label, amt) ->
                            val target = catTargets.optLong(label, 0L)
                            val catCol = getCatColor(label)
                            val progress = if (target > 0) (amt.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
                            val isOverLimit = target > 0 && amt > target

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .height(55.dp)
                                    .border(1.dp, AppText().copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AppSurfaceVariant().copy(alpha = 0.5f))
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (target > 0) Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress).background(catCol.copy(alpha = 0.2f)).align(Alignment.CenterStart))
                                    Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Stop, contentDescription = null, tint = catCol, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppText(), modifier = Modifier.weight(1f))
                                        Text("$curSym ${NumberFormat.getInstance(locale).format(amt)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppText())
                                    }
                                    if (target > 0) {
                                        val budgetInfo = if(isOverLimit) "$curSym ${NumberFormat.getInstance(locale).format(amt-target)} OVER!" else "$curSym ${NumberFormat.getInstance(locale).format(target-amt)} left"
                                        Text(budgetInfo, fontSize = 9.sp, color = if(isOverLimit) AppRed() else AppText().copy(alpha=0.7f), modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        Text(AppStr.trends, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AppText())
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .border(1.dp, AppText().copy(alpha = 0.1f), RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface())
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                val greenCol = AppGreen()
                val redCol = AppRed()
                val variantCol = AppSurfaceVariant()
                val textCol = AppText()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    LegendItem(AppStr.inc, greenCol)
                    Spacer(modifier = Modifier.width(16.dp))
                    LegendItem(AppStr.exp, redCol)
                }

                Spacer(modifier = Modifier.height(16.dp))

                val incomeData = FloatArray(5) { 0f }
                val expenseData = FloatArray(5) { 0f }
                val monthLabels = mutableListOf<String>()
                val targetMonths = mutableListOf<Pair<Int, Int>>()

                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, selectedYear)
                cal.set(Calendar.MONTH, selectedMonth - 1)

                for (i in 4 downTo 0) {
                    val tempCal = cal.clone() as Calendar
                    tempCal.add(Calendar.MONTH, -i)
                    val m = tempCal.get(Calendar.MONTH) + 1
                    val y = tempCal.get(Calendar.YEAR)
                    targetMonths.add(Pair(m, y))
                    val monthName = tempCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale) ?: ""
                    monthLabels.add(monthName)
                }

                allTransactions.forEach { t ->
                    try {
                        val dt = LocalDateTime.parse(t.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        val txMonth = dt.monthValue
                        val txYear = dt.year
                        val idx = targetMonths.indexOf(Pair(txMonth, txYear))
                        if (idx != -1) {
                            val amt = t.amount.toFloatOrNull() ?: 0f
                            if (t.isIncome) incomeData[idx] += amt else expenseData[idx] += amt
                        }
                    } catch (_: Exception) {}
                }

                val maxVal = maxOf(incomeData.maxOrNull() ?: 0f, expenseData.maxOrNull() ?: 0f).coerceAtLeast(1f)
                val incPoints = incomeData.map { it / maxVal }
                val expPoints = expenseData.map { it / maxVal }
                val hasData = incomeData.sum() > 0f || expenseData.sum() > 0f

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        for (i in 0..4) {
                            val x = i * size.width / 4
                            drawLine(color = variantCol.copy(alpha = 0.5f), start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1.dp.toPx())
                        }
                        for (i in 0..5) {
                            val y = size.height - (i * size.height / 5)
                            drawLine(color = variantCol.copy(alpha = 0.3f), start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1.dp.toPx())
                        }
                        if (hasData) {
                            drawTrendsArea(incPoints, greenCol)
                            drawTrendsArea(expPoints, redCol)
                        }
                    }
                    if (!hasData) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(AppStr.noTrendData, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textCol.copy(alpha = 0.5f))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    monthLabels.forEachIndexed { index, monthStr ->
                        val isCurrentSelected = index == 4
                        Text(monthStr, fontSize = if(isCurrentSelected) 12.sp else 10.sp, fontWeight = if(isCurrentSelected) FontWeight.Black else FontWeight.Bold, color = if(isCurrentSelected) greenCol else textCol)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}
fun DrawScope.drawTrendsArea(points: List<Float>, color: Color) {
    if (points.size < 2) return
    val w = size.width
    val h = size.height
    val path = Path()
    val fillPath = Path()

    path.moveTo(0f, h - (points[0] * h))
    fillPath.moveTo(0f, h)
    fillPath.lineTo(0f, h - (points[0] * h))

    val step = w / (points.size - 1)

    for (i in 0 until points.size - 1) {
        val x1 = i * step
        val y1 = h - (points[i] * h)
        val x2 = (i + 1) * step
        val y2 = h - (points[i+1] * h)
        val cX = (x1 + x2) / 2f
        path.cubicTo(cX, y1, cX, y2, x2, y2)
        fillPath.cubicTo(cX, y1, cX, y2, x2, y2)
    }

    fillPath.lineTo(w, h)
    fillPath.lineTo(0f, h)
    fillPath.close()

    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(color.copy(alpha = 0.4f), color.copy(alpha = 0.0f)),
            startY = 0f,
            endY = h
        )
    )
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    val lastX = w
    val lastY = h - (points.last() * h)
    drawCircle(color, radius = 5.dp.toPx(), center = Offset(lastX, lastY))
    drawCircle(Color.White, radius = 2.5f.dp.toPx(), center = Offset(lastX, lastY))
}



