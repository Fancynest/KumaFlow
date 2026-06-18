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
fun HomeScreen(
    profile: UserProfile,
    transactionsWithSplits: List<TransactionWithSplits>,
    balance: Long,
    walletBalances: Map<String, Long>,
    income: Long,
    expenses: Long,
    selectedMonth: Int,
    selectedYear: Int,
    onMonthChange: (Int, Int) -> Unit,
    onEdit: (TransactionWithSplits) -> Unit,
    onDelete: (TransactionWithSplits) -> Unit,
    onOpenWrapped: (Int, Int) -> Unit = { _, _ -> },
    listState: androidx.compose.foundation.lazy.LazyListState,
    selectedTxs: Set<Int>,
    onToggleSelect: (Int) -> Unit,
    clearSelection: () -> Unit,
    onBulkDelete: (List<TransactionWithSplits>) -> Unit,
    onBulkUpdateCategory: (List<TransactionWithSplits>, String) -> Unit
) {
    val context = LocalContext.current
    val locale = java.util.Locale.forLanguageTag("id-ID")
    val curSym = when(profile.currency) { "USD", "AUD", "CAD", "SGD" -> "$"; "EUR" -> "â‚¬"; "GBP" -> "Â£"; "JPY", "CNY" -> "Â¥"; "CHF" -> "CHF"; else -> "Rp" }

    val haptic = LocalHapticFeedback.current

    var isPrivacyMode by rememberSaveable { mutableStateOf(false) }
    val blurRadius by androidx.compose.animation.core.animateDpAsState(targetValue = if (isPrivacyMode) 12.dp else 0.dp, label = "blur_anim")

    var searchQuery by remember { mutableStateOf("") }
    val filteredTx = transactionsWithSplits.filter {
        it.transaction.name.contains(searchQuery, ignoreCase = true) || it.transaction.category.contains(searchQuery, ignoreCase = true) || it.transaction.message.contains(searchQuery, ignoreCase = true)
    }

    val groupedTx = remember(filteredTx) { filteredTx.groupBy { it.transaction.date } }

    val sharedPrefs = remember { context.getSharedPreferences("kumaflow_prefs", android.content.Context.MODE_PRIVATE) }
    val cal = java.util.Calendar.getInstance()
    cal.add(java.util.Calendar.MONTH, -1)
    val prevMonth = cal.get(java.util.Calendar.MONTH) + 1
    val prevYear = cal.get(java.util.Calendar.YEAR)
    val wrappedKey = "$prevMonth-$prevYear"

    var showWrappedBanner by remember { mutableStateOf(sharedPrefs.getString("last_viewed_wrapped", "") != wrappedKey) }

    val isSelectionMode = selectedTxs.isNotEmpty()
    var showBulkCatDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(top = 24.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    if (showWrappedBanner) {
                        val bannerGradient = androidx.compose.ui.graphics.Brush.linearGradient(colors = listOf(Color(0xFFE40303), Color(0xFF732982)))
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp).clickable { onOpenWrapped(prevMonth, prevYear) },
                            shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().background(bannerGradient).padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("KumaFlow Wrapped âœ¨", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val monthName = cal.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, locale) ?: "Bulan Lalu"
                                        Text("Rapor keuanganmu di bulan $monthName udah siap! Yuk intip pengeluaranmu.", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, lineHeight = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(Icons.Default.ArrowForwardIos, contentDescription = "Buka Wrapped", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    Text(if (AppStr.isId) "Halo, ${profile.userName}!" else "Hello, ${profile.userName}!", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = AppText())
                    Spacer(modifier = Modifier.height(16.dp))
                    MonthYearSelector(selectedMonth, selectedYear, onMonthChange)
                    Spacer(modifier = Modifier.height(24.dp))

                    val isPrideThemeActive = profile.themeMode == 3 || profile.themeMode == 4
                    val prideGradient = androidx.compose.ui.graphics.Brush.linearGradient(colors = listOf(Color(0xFFE40303), Color(0xFFFF8C00), Color(0xFFFFED00), Color(0xFF008026), Color(0xFF24408E), Color(0xFF732982)))
                    val defaultSurfaceColor = AppSurface()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 250.dp)
                            .border(1.dp, AppText().copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                            .clip(RoundedCornerShape(32.dp))
                            .background(if (isPrideThemeActive) prideGradient else androidx.compose.ui.graphics.SolidColor(defaultSurfaceColor))
                    ) {
                        Column(modifier = Modifier.padding(vertical = 32.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
                            Column(modifier = Modifier.padding(horizontal = 32.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(AppStr.curBal, color = if (isPrideThemeActive) Color.White else AppText(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Privacy",
                                        tint = if (isPrideThemeActive) Color.White else AppText(),
                                        modifier = Modifier.clip(CircleShape).clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            isPrivacyMode = !isPrivacyMode
                                        }.padding(4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                val balPref = if (balance < 0) "- " else ""
                                AutoSizeText(text = "$balPref$curSym ${NumberFormat.getInstance(locale).format(abs(balance))}", modifier = Modifier.fillMaxWidth().blur(blurRadius), fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White, minimumFallbackSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(walletBalances.toList()) { (walletName, amt) ->
                                    val wBalPref = if (amt < 0) "- " else ""
                                    Column(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(AppBg().copy(alpha = 0.2f)).padding(horizontal = 16.dp, vertical = 10.dp)) {
                                        Text(walletName, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("$wBalPref$curSym ${NumberFormat.getInstance(locale).format(abs(amt))}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.blur(blurRadius))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowUpward, null, tint = if (isPrideThemeActive) Color.White else AppGreen(), modifier = Modifier.size(20.dp))
                                AutoSizeText(text = "${AppStr.inc} $curSym ${NumberFormat.getInstance(locale).format(income)}", modifier = Modifier.weight(1f).padding(start = 4.dp).blur(blurRadius), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, minimumFallbackSize = 8.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowDownward, null, tint = if (isPrideThemeActive) Color.White else AppRed(), modifier = Modifier.size(20.dp))
                                AutoSizeText(text = "${AppStr.exp} $curSym ${NumberFormat.getInstance(locale).format(expenses)}", modifier = Modifier.weight(1f).padding(start = 4.dp).blur(blurRadius), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, minimumFallbackSize = 8.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = searchQuery, onValueChange = { searchQuery = it }, label = { Text(AppStr.searchTx) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = AppText().copy(alpha = 0.5f)) },
                        trailingIcon = { if (searchQuery.isNotEmpty()) { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null, tint = AppText()) } } },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(16.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppPrimary(),
                            unfocusedBorderColor = AppText().copy(alpha = 0.25f)
                        )
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(AppStr.recTx, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = AppText())
                        if (isSelectionMode) {
                            TextButton(onClick = { clearSelection() }) {
                                Text(AppStr.cancelBulk, color = AppRed(), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (filteredTx.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val composition by com.airbnb.lottie.compose.rememberLottieComposition(com.airbnb.lottie.compose.LottieCompositionSpec.RawRes(R.raw.beruang_kosong))
                        val progress by com.airbnb.lottie.compose.animateLottieCompositionAsState(composition = composition, iterations = com.airbnb.lottie.compose.LottieConstants.IterateForever)
                        com.airbnb.lottie.compose.LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(150.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(AppStr.noTx, textAlign = TextAlign.Center, color = AppText().copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                groupedTx.forEach { (date, txs) ->
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppBg())
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = date,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = AppPrimary()
                            )
                        }
                    }

                    items(txs) { item ->
                        val isSelected = selectedTxs.contains(item.transaction.id)
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            TransactionItem(
                                profile = profile,
                                obj = item,
                                isPrivacyMode = isPrivacyMode,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onToggleSelect = { onToggleSelect(item.transaction.id) },
                                onEdit = onEdit,
                                onDelete = onDelete
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(if (isSelectionMode) 180.dp else 100.dp)) }
        }

        // ðŸ”¥ BULK ACTION OVERLAY BAR ðŸ”¥
        androidx.compose.animation.AnimatedVisibility(
            visible = isSelectionMode,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .border(1.dp, AppText().copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(AppSurfaceVariant())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            showBulkCatDialog = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = AppPrimary())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(AppStr.changeCat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppText())
                    }

                    Text("${selectedTxs.size} ${AppStr.selected}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = AppText())

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val txsToDelete = transactionsWithSplits.filter { selectedTxs.contains(it.transaction.id) }
                            onBulkDelete(txsToDelete)
                            clearSelection()
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = AppRed())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(AppStr.bulkDel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppRed())
                    }
                }
            }
        }

        if (showBulkCatDialog) {
            val allCats = (profile.expenseCats.split(",") + profile.incomeCats.split(",")).filter { it.isNotBlank() }.distinct()
            AlertDialog(
                onDismissRequest = { showBulkCatDialog = false },
                title = { Text(AppStr.chooseNewCat, fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(allCats) { catName ->
                            Text(
                                text = catName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val txsToUpdate = transactionsWithSplits.filter { selectedTxs.contains(it.transaction.id) }
                                        onBulkUpdateCategory(txsToUpdate, catName)
                                        clearSelection()
                                        showBulkCatDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                color = AppText(),
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(color = AppText().copy(alpha = 0.1f))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBulkCatDialog = false }) { Text(AppStr.close, color = AppRed()) }
                },
                containerColor = AppSurface()
            )
        }
    }
}

