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
        val predefined = mapOf(
            "Financial" to Color(0xFF4CAF50),
            "Food" to Color(0xFFFF9800),
            "Shopping" to Color(0xFFE91E63),
            "Health" to Color(0xFFF44336),
            "Transport" to Color(0xFF2196F3),
            "Education" to Color(0xFF9C27B0),
            "Entertainment" to Color(0xFF673AB7),
            "Transfer" to Color(0xFF00BCD4),
            "Others" to Color(0xFF607D8B)
        )
        return predefined[catName] ?: Color(android.graphics.Color.HSVToColor(floatArrayOf(abs(catName.hashCode()) % 360f, 0.7f, 0.8f)))
    }

    val expensePerCat = monthlyTransactions.filter { !it.isIncome }.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount.toLongOrNull() ?: 0L } }.toList().sortedByDescending { it.second }
    val catTargets = remember(profile.categoryTargets) { try { JSONObject(profile.categoryTargets) } catch (e: Exception) { JSONObject() } }
    val savedIcons = remember(profile.categoryIcons) { try { JSONObject(profile.categoryIcons) } catch (e: Exception) { JSONObject() } }
    var showAllCategories by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 24.dp).verticalScroll(rememberScrollState())) {
        Text(AppStr.rep, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = AppText())
        Spacer(modifier = Modifier.height(16.dp))

        MonthYearSelector(selectedMonth, selectedYear, onMonthChange)
        Spacer(modifier = Modifier.height(16.dp))

        val monthNamesList = if (AppStr.isId) listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember") else listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val currentSelectedMonthName = monthNamesList.getOrElse(selectedMonth - 1) { "" }

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)
        val isCurrentOrFutureMonth = selectedYear > currentYear || (selectedYear == currentYear && selectedMonth >= currentMonth)

        val context = LocalContext.current

        OutlinedButton(
            onClick = {
                if (isCurrentOrFutureMonth) {
                    Toast.makeText(context, "Wrapped $currentSelectedMonthName $selectedYear is Coming Soon!", Toast.LENGTH_SHORT).show()
                } else {
                    onOpenWrapped(selectedMonth, selectedYear)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrentOrFutureMonth) AppText().copy(alpha = 0.2f) else AppPrimary().copy(alpha = 0.5f))
        ) {
            Icon(if (isCurrentOrFutureMonth) Icons.Default.Lock else Icons.Default.AutoAwesome, contentDescription = "Rewatch", tint = if (isCurrentOrFutureMonth) AppText().copy(alpha = 0.5f) else AppPrimary())
            Spacer(modifier = Modifier.width(8.dp))
            val btnText = if (isCurrentOrFutureMonth) "Wrapped $currentSelectedMonthName $selectedYear (Coming Soon) ✨" else "Putar Ulang Wrapped $currentSelectedMonthName $selectedYear ✨"
            Text(btnText, color = if (isCurrentOrFutureMonth) AppText().copy(alpha = 0.5f) else AppPrimary(), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        val isPrideThemeActive = profile.themeMode == 3 || profile.themeMode == 4
        val prideGradient = Brush.linearGradient(colors = listOf(Color(0xFFE40303), Color(0xFFFF8C00), Color(0xFFFFED00), Color(0xFF008026), Color(0xFF24408E), Color(0xFF732982)))
        val defaultSurfaceColor = AppSurface()

        val boxModifier = if (isPrideThemeActive) {
            Modifier
                .fillMaxWidth()
                .heightIn(min = 185.dp)
                .border(1.dp, AppText().copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(prideGradient)
        } else {
            Modifier
                .fillMaxWidth()
                .heightIn(min = 185.dp)
                .glassCard(32.dp, defaultSurfaceColor)
        }

        Box(modifier = boxModifier) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Text(AppStr.sum, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = if (isPrideThemeActive) Color.White else AppText())
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(AppStr.net, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isPrideThemeActive) Color.White.copy(alpha = 0.8f) else AppText().copy(alpha = 0.8f))
                val balPref = if (balance < 0) "- " else "+"
                AutoSizeText(text = "$curSym $balPref${NumberFormat.getInstance(locale).format(abs(balance))}", modifier = Modifier.fillMaxWidth(), fontSize = 32.sp, fontWeight = FontWeight.Black, color = if (isPrideThemeActive) Color.White else AppText(), minimumFallbackSize = 18.sp)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f).glassCard(16.dp, if (isPrideThemeActive) Color.White.copy(alpha = 0.1f) else AppSurfaceVariant()).padding(12.dp)) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowUpward, null, tint = if (isPrideThemeActive) Color.White else AppGreen(), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStr.inc, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isPrideThemeActive) Color.White.copy(alpha = 0.8f) else AppText().copy(alpha = 0.8f))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            AutoSizeText(text = "$curSym ${NumberFormat.getInstance(locale).format(income)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = if (isPrideThemeActive) Color.White else AppText(), minimumFallbackSize = 10.sp)
                        }
                    }
                    Box(modifier = Modifier.weight(1f).glassCard(16.dp, if (isPrideThemeActive) Color.White.copy(alpha = 0.1f) else AppSurfaceVariant()).padding(12.dp)) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDownward, null, tint = if (isPrideThemeActive) Color.White else AppRed(), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStr.exp, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isPrideThemeActive) Color.White.copy(alpha = 0.8f) else AppText().copy(alpha = 0.8f))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            AutoSizeText(text = "$curSym ${NumberFormat.getInstance(locale).format(expenses)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = if (isPrideThemeActive) Color.White else AppText(), minimumFallbackSize = 10.sp)
                        }
                    }
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
                .glassCard(32.dp, AppSurface()),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassCard(24.dp, AppSurfaceVariant(), useHaze = true)
                            .padding(vertical = 8.dp)
                    ) {
                        var index = 0
                        val itemsToShow = if (showAllCategories) expensePerCat else expensePerCat.take(5)
                        
                        itemsToShow.forEach { (label, amt) ->
                            val target = catTargets.optLong(label, 0L)
                            val catCol = getCatColor(label)
                            val progress = if (target > 0) (amt.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
                            val isOverLimit = target > 0 && amt > target

                            val iconKey = savedIcons.optString(label, "")
                            val icon = kumaIconLibrary[iconKey] ?: when(label) {
                                "Financial" -> Icons.Default.AccountBalance
                                "Food" -> Icons.Default.Restaurant
                                "Shopping" -> Icons.Default.LocalMall
                                "Health" -> Icons.Default.Favorite
                                "Transport" -> Icons.Default.DirectionsCar
                                "Education" -> Icons.Default.School
                                "Entertainment" -> Icons.Default.Gamepad
                                "Transfer" -> Icons.Default.SyncAlt
                                else -> Icons.Default.DashboardCustomize
                            }

                            Box(modifier = Modifier.fillMaxWidth().height(65.dp)) {
                                if (target > 0) Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress).background(catCol.copy(alpha = 0.15f)).align(Alignment.CenterStart))
                                Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(catCol.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                        Icon(icon, contentDescription = null, tint = catCol, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppText(), modifier = Modifier.weight(1f))
                                    Text("$curSym ${NumberFormat.getInstance(locale).format(amt)}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = AppText())
                                }
                                if (target > 0) {
                                    val budgetInfo = if(isOverLimit) "$curSym ${NumberFormat.getInstance(locale).format(amt-target)} OVER!" else "$curSym ${NumberFormat.getInstance(locale).format(target-amt)} left"
                                    Text(budgetInfo, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if(isOverLimit) AppRed() else AppText().copy(alpha=0.6f), modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 6.dp))
                                }
                            }

                            if (index < itemsToShow.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 60.dp),
                                    color = AppText().copy(alpha = 0.05f)
                                )
                            }
                            index++
                        }
                        
                        if (expensePerCat.size > 5) {
                            TextButton(onClick = { showAllCategories = !showAllCategories }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (showAllCategories) "Show Less" else "Show More (${expensePerCat.size - 5})", color = AppPrimary(), fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(if (showAllCategories) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = AppPrimary())
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
                .glassCard(32.dp, AppSurface()),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
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
        Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding() + 24.dp))
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



