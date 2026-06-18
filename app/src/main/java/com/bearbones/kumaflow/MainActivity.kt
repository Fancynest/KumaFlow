@file:Suppress("SpellCheckingInspection", "UNUSED_PARAMETER", "unused", "CanBeVal", "DEPRECATION", "ScheduleExactAlarm")

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
import androidx.compose.ui.graphics.luminance
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
import com.bearbones.kumaflow.ui.components.BokehBackground
import dev.chrisbanes.haze.*
import androidx.compose.ui.draw.blur
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border

// --- DATA CLASSES & OBJECTS ---


val LocalIsDark = compositionLocalOf { true }
val LocalIsAmoled = compositionLocalOf { false }
val LocalIsLiquidGlass = compositionLocalOf { false }
val LocalHazeState = compositionLocalOf { HazeState() }

@Composable
fun AppBg() = MaterialTheme.colorScheme.background

@Composable
fun AppSurface() = MaterialTheme.colorScheme.surface

@Composable
fun AppText() = MaterialTheme.colorScheme.onSurface

@Composable
fun AppPrimary() = MaterialTheme.colorScheme.primary

@Composable
fun AppSurfaceVariant() = if (LocalIsDark.current) {
    if (LocalIsAmoled.current) Color(0xFF1A1A1A) else Color(0xFF333333)
} else {
    Color.White.copy(alpha = 0.5f)
}

@Composable
fun AppGreen() = if (LocalIsDark.current) Color(0xFF66BB6A) else Color(0xFF1B5E20)

@Composable
fun AppRed() = if (LocalIsDark.current) Color(0xFFEF5350) else Color(0xFFB71C1C)

@Composable
fun Modifier.glassmorphic(
    radius: androidx.compose.ui.unit.Dp = 16.dp,
    borderAlpha: Float = 0.2f
): Modifier {
    return if (LocalIsLiquidGlass.current) {
        val glassColor = if (LocalIsDark.current) {
            Color.White.copy(alpha = 0.05f)
        } else {
            Color.White.copy(alpha = 0.4f)
        }
        val borderColor = if (LocalIsDark.current) {
            Color.White.copy(alpha = borderAlpha)
        } else {
            Color.White.copy(alpha = 0.5f)
        }
        
        this
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(radius))
            .background(glassColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(radius)
            )
    } else {
        this
    }
}

@Composable
fun Modifier.glassCard(
    radius: androidx.compose.ui.unit.Dp = 16.dp,
    fallbackColor: Color,
    useHaze: Boolean = false
): Modifier {
    val glassColor = if (LocalIsDark.current) {
        if (fallbackColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.4f) else fallbackColor.copy(alpha = 0.5f)
    } else {
        if (fallbackColor.luminance() < 0.5f) Color.White.copy(alpha = 0.4f) else fallbackColor.copy(alpha = 0.5f)
    }
    return if (LocalIsLiquidGlass.current) {
        this.clip(androidx.compose.foundation.shape.RoundedCornerShape(radius))
            .background(glassColor)
            .border(1.dp, Color.White.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(radius))
    } else {
        this.clip(androidx.compose.foundation.shape.RoundedCornerShape(radius)).background(fallbackColor)
    }
}



fun showBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onError(errString.toString())
        }
    })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(AppStr.secKuma)
        .setSubtitle(AppStr.scanBio)
        .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK)
        .setNegativeButtonText(AppStr.usePin)
        .build()

    biometricPrompt.authenticate(promptInfo)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userProfileState: UserProfile?,
    dao: TransactionDao,
    onOpenWrapped: (Int, Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val transactionListWithSplits by dao.getAllTransactionsWithSplits().collectAsState(initial = emptyList())
    val userProfile = userProfileState ?: UserProfile(userName = "User")
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedItemIndex) { if (pagerState.currentPage != selectedItemIndex) pagerState.animateScrollToPage(selectedItemIndex) }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) { if (!pagerState.isScrollInProgress) selectedItemIndex = pagerState.currentPage }

    var selectedMonth by remember { mutableIntStateOf(java.time.LocalDateTime.now().monthValue) }
    var selectedYear by remember { mutableIntStateOf(java.time.LocalDateTime.now().year) }
    var forceUpdateTrigger by remember { mutableIntStateOf(0) }

    val homeListState = androidx.compose.foundation.lazy.rememberLazyListState()

    // ðŸ”¥ STATE SELECTION HOISTING ðŸ”¥
    var selectedTxs by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedTxs.isNotEmpty()

    val isFabVisible by remember {
        derivedStateOf {
            homeListState.firstVisibleItemIndex == 0 || !homeListState.isScrollInProgress
        }
    }

    val monthlyTransactionsWithSplits by remember(transactionListWithSplits, selectedMonth, selectedYear, forceUpdateTrigger) {
        derivedStateOf {
            transactionListWithSplits.filter { t ->
                try {
                    val dt = java.time.LocalDateTime.parse(t.transaction.timestamp, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    dt.monthValue == selectedMonth && dt.year == selectedYear
                } catch (e: Exception) { true }
            }
        }
    }

    val walletBalances by remember(transactionListWithSplits, userProfile.wallets, userProfile.useCarryOver, selectedMonth, selectedYear, forceUpdateTrigger) {
        derivedStateOf {
            val balances = userProfile.wallets.split(",").filter { it.isNotBlank() }.associateWith { 0L }.toMutableMap()
            val relevantTxs = if (userProfile.useCarryOver) {
                transactionListWithSplits.filter { t ->
                    try {
                        val dt = java.time.LocalDateTime.parse(t.transaction.timestamp, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        dt.year < selectedYear || (dt.year == selectedYear && dt.monthValue <= selectedMonth)
                    } catch (e: Exception) { false }
                }
            } else monthlyTransactionsWithSplits

            relevantTxs.forEach { txObj ->
                if (txObj.splits.isNotEmpty()) {
                    txObj.splits.forEach { split ->
                        val current = balances[split.splitWallet] ?: 0L
                        balances[split.splitWallet] = current + (if (txObj.transaction.isIncome) split.splitAmount else -split.splitAmount)
                    }
                } else {
                    val amt = txObj.transaction.amount.toLongOrNull() ?: 0L
                    val current = balances[txObj.transaction.wallet] ?: 0L
                    balances[txObj.transaction.wallet] = current + (if (txObj.transaction.isIncome) amt else -amt)
                }
            }
            balances
        }
    }

    val totalBalance by remember(walletBalances, forceUpdateTrigger) { derivedStateOf { walletBalances.values.sum() } }
    val totalIncome by remember(monthlyTransactionsWithSplits, forceUpdateTrigger) { derivedStateOf { monthlyTransactionsWithSplits.filter { it.transaction.isIncome }.sumOf { it.transaction.amount.toLongOrNull() ?: 0L } } }
    val totalExpenses by remember(monthlyTransactionsWithSplits, forceUpdateTrigger) { derivedStateOf { monthlyTransactionsWithSplits.filter { !it.transaction.isIncome }.sumOf { it.transaction.amount.toLongOrNull() ?: 0L } } }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionWithSplits?>(null) }
    var showBackupReminder by remember { mutableStateOf(false) }
    val totalTxCount = transactionListWithSplits.size

    val hazeState = remember { HazeState() }
    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(modifier = Modifier.fillMaxSize().background(AppBg())) {
            BokehBackground()
            Scaffold(
                containerColor = Color.Transparent,
        floatingActionButton = {
            val showFab = selectedItemIndex != 2 && (selectedItemIndex != 0 || isFabVisible) && !isSelectionMode
            if (showFab) {
                FloatingActionButton(
                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); transactionToEdit = null; showBottomSheet = true },
                    containerColor = if (LocalIsLiquidGlass.current) Color.Transparent else AppPrimary(),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = if (LocalIsLiquidGlass.current) 0.dp else 6.dp),
                    contentColor = if (LocalIsLiquidGlass.current) AppPrimary() else Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(70.dp).let { if (LocalIsLiquidGlass.current) it.clip(CircleShape).background(if (LocalIsDark.current) Color.Black.copy(alpha=0.4f) else Color.White.copy(alpha=0.4f), CircleShape).border(1.dp, Color.White.copy(0.3f), CircleShape) else it }
                ) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(40.dp)) }
            }
        },
        bottomBar = { CustomBottomNav(selectedItemIndex, haptic) { selectedItemIndex = it } }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(
            top = paddingValues.calculateTopPadding(),
            start = paddingValues.calculateStartPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
            end = paddingValues.calculateEndPadding(androidx.compose.ui.platform.LocalLayoutDirection.current)
        )) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> HomeScreen(
                        profile = userProfile,
                        transactionsWithSplits = monthlyTransactionsWithSplits,
                        balance = totalBalance,
                        walletBalances = walletBalances,
                        income = totalIncome,
                        expenses = totalExpenses,
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        paddingValues = paddingValues,
                        onMonthChange = { m: Int, y: Int -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); selectedMonth = m; selectedYear = y },
                        onEdit = { t: TransactionWithSplits -> transactionToEdit = t; showBottomSheet = true },
                        onDelete = { t: TransactionWithSplits -> scope.launch { dao.deleteTransaction(t.transaction); updateKumaWidget(context) } },
                        onOpenWrapped = onOpenWrapped,
                        listState = homeListState,
                        selectedTxs = selectedTxs,
                        onToggleSelect = { id: Int ->
                            val newSet = selectedTxs.toMutableSet()
                            if (newSet.contains(id)) newSet.remove(id) else newSet.add(id)
                            selectedTxs = newSet
                        },
                        clearSelection = { selectedTxs = emptySet() },
                        onBulkDelete = { listToDelete: List<TransactionWithSplits> ->
                            scope.launch {
                                listToDelete.forEach { dao.deleteTransaction(it.transaction) }
                                updateKumaWidget(context)
                                Toast.makeText(context, AppStr.txDeleted(listToDelete.size), Toast.LENGTH_SHORT).show()
                            }
                        },
                        onBulkUpdateCategory = { listToUpdate: List<TransactionWithSplits>, newCat: String ->
                            scope.launch {
                                listToUpdate.forEach { txObj ->
                                    val updatedTx = txObj.transaction.copy(category = newCat)
                                    dao.updateFullTransaction(updatedTx, txObj.splits)
                                }
                                forceUpdateTrigger++
                                updateKumaWidget(context)
                                Toast.makeText(context, AppStr.txChangedTo(listToUpdate.size, newCat), Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    1 -> ReportScreen(
                        profile = userProfile, monthlyTransactions = monthlyTransactionsWithSplits.map { it.transaction }, allTransactions = transactionListWithSplits.map { it.transaction }, income = totalIncome, expenses = totalExpenses, balance = totalBalance, selectedMonth = selectedMonth, selectedYear = selectedYear,
                        paddingValues = paddingValues,
                        onMonthChange = { m, y -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); selectedMonth = m; selectedYear = y },
                        onOpenWrapped = onOpenWrapped
                    )
                    2 -> SettingsScreen(
                        currentProfile = userProfile, monthlyTransactionsWithSplits = monthlyTransactionsWithSplits, allTransactionsWithSplits = transactionListWithSplits, dao = dao, selectedMonth = selectedMonth, selectedYear = selectedYear,
                        paddingValues = paddingValues,
                        onForceUpdate = { forceUpdateTrigger++; updateKumaWidget(context) }
                    )
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState, containerColor = AppBg()) {
                TransactionBottomSheet(
                    profile = userProfile, transactionToEdit = transactionToEdit, onDismiss = { showBottomSheet = false },
                    onSave = { txList ->
                        scope.launch {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            txList.forEach { (newTrans, splits) -> if (newTrans.id == 0) dao.insertFullTransaction(newTrans, splits) else dao.updateFullTransaction(newTrans, splits) }
                            if ((totalTxCount + txList.size) % 10 == 0) showBackupReminder = true
                            forceUpdateTrigger++; updateKumaWidget(context)
                            Toast.makeText(context, AppStr.txSaved, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onUpdateProfile = { updatedProfile -> scope.launch { dao.saveProfile(updatedProfile); forceUpdateTrigger++; updateKumaWidget(context) } }
                )
            }
        }

        if (showBackupReminder) {
            AlertDialog(
                onDismissRequest = { showBackupReminder = false },
                title = { Text(AppStr.backupReminderTitle, fontWeight = FontWeight.Black) },
                text = { Text(AppStr.backupReminderMsg) },
                confirmButton = {
                    Button(
                        onClick = { showBackupReminder = false; backupAppToJSON(context, userProfile, transactionListWithSplits) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary())
                    ) { Text(AppStr.backupNow, color = Color.White) }
                },
                dismissButton = { TextButton(onClick = { showBackupReminder = false }) { Text(AppStr.later, color = AppText()) } },
                shape = RoundedCornerShape(28.dp), containerColor = AppSurface(), titleContentColor = AppText(), textContentColor = AppText()
            )
        }
    }
    }
}
}

@OptIn(ExperimentalFoundationApi::class)
data class SplitItemUi(var id: String, var wallet: String, var amount: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionBottomSheet(
    profile: UserProfile,
    transactionToEdit: TransactionWithSplits?,
    onDismiss: () -> Unit,
    onSave: (List<Pair<KumaTransaction, List<TransactionSplit>>>) -> Unit,
    onUpdateProfile: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val baseTx = transactionToEdit?.transaction

    var txMode by remember(baseTx) { mutableIntStateOf(if (baseTx != null && baseTx.isIncome) 1 else 0) }

    val calendar = remember { java.util.Calendar.getInstance() }
    var txDateStr by remember(baseTx) {
        mutableStateOf(baseTx?.date ?: java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(if(AppStr.isId) "dd MMM yyyy" else "MMM dd, yyyy", java.util.Locale.getDefault())))
    }
    var txTimestamp by remember(baseTx) {
        mutableStateOf(baseTx?.timestamp ?: java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = java.util.Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                val locale = java.util.Locale.getDefault()
                txDateStr = java.text.SimpleDateFormat(if(AppStr.isId) "dd MMM yyyy" else "MMM dd, yyyy", locale).format(cal.time)

                val now = java.time.LocalDateTime.now()
                txTimestamp = java.time.LocalDateTime.of(year, month + 1, dayOfMonth, now.hour, now.minute).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    val expenseCategories = remember(profile.expenseCats) { profile.expenseCats.split(",").filter { it.isNotBlank() } }
    val incomeCategories = remember(profile.incomeCats) { profile.incomeCats.split(",").filter { it.isNotBlank() } }
    var walletList by remember(profile.wallets) { mutableStateOf(profile.wallets.split(",").filter { it.isNotBlank() }) }

    val currentCategories = if (txMode == 1) incomeCategories else expenseCategories
    var selectedCategory by remember(baseTx, txMode) {
        mutableStateOf(if (baseTx != null && (baseTx.isIncome == (txMode == 1))) baseTx.category else currentCategories.firstOrNull() ?: "Others")
    }

    var name by remember(baseTx) { mutableStateOf(baseTx?.name ?: "") }
    var message by remember(baseTx) { mutableStateOf(baseTx?.message ?: "") }

    var transferFromWallet by remember { mutableStateOf(walletList.firstOrNull() ?: "Cash") }
    var transferToWallet by remember { mutableStateOf(if (walletList.size > 1) walletList[1] else "Cash") }

    val initialSplits = remember(transactionToEdit) {
        if (transactionToEdit != null && transactionToEdit.splits.isNotEmpty()) {
            transactionToEdit.splits.map {
                SplitItemUi(java.util.UUID.randomUUID().toString(), it.splitWallet, it.splitAmount.toString())
            }.toMutableStateList()
        } else {
            mutableStateListOf(
                SplitItemUi(java.util.UUID.randomUUID().toString(), baseTx?.wallet ?: walletList.firstOrNull() ?: "Cash", baseTx?.amount ?: "")
            )
        }
    }

    var expandedCat by remember { mutableStateOf(false) }
    var showNewWalletDialog by remember { mutableStateOf(false) }
    var newWalletName by remember { mutableStateOf("") }

    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    val allowedMathChars = setOf('0','1','2','3','4','5','6','7','8','9','+','-','*','/','(',')',' ','.')

    LaunchedEffect(Unit) {
        if (baseTx == null) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (baseTx == null) AppStr.addTx else AppStr.editTx,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppText()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .glassCard(16.dp, AppSurfaceVariant(), useHaze = false)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (txMode == 1) AppGreen() else Color.Transparent)
                    .clickable { txMode = 1; haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
                contentAlignment = Alignment.Center
            ) {
                Text(AppStr.inc, color = if (txMode == 1) Color.White else AppText(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (txMode == 0) AppRed() else Color.Transparent)
                    .clickable { txMode = 0; haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
                contentAlignment = Alignment.Center
            ) {
                Text(AppStr.exp, color = if (txMode == 0) Color.White else AppText(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            if (baseTx == null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (txMode == 2) Color(0xFF1976D2) else Color.Transparent)
                        .clickable { txMode = 2; haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(AppStr.mutasi, color = if (txMode == 2) Color.White else AppText(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    datePickerDialog.show()
                }
        ) {
            OutlinedTextField(
                value = txDateStr,
                onValueChange = {},
                readOnly = true,
                label = { Text(AppStr.date) },
                trailingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AppPrimary())
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = AppText(),
                    disabledBorderColor = AppSurfaceVariant(),
                    disabledLabelColor = AppText().copy(alpha = 0.7f),
                    disabledTrailingIconColor = AppPrimary()
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (txMode == 2) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                var expFrom by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expFrom,
                    onExpandedChange = { expFrom = !expFrom },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = transferFromWallet,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(AppStr.tarikDari) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expFrom) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expFrom,
                        onDismissRequest = { expFrom = false }
                    ) {
                        walletList.forEach { w ->
                            DropdownMenuItem(
                                text = { Text(w) },
                                onClick = { transferFromWallet = w; expFrom = false }
                            )
                        }
                    }
                }

                var expTo by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expTo,
                    onExpandedChange = { expTo = !expTo },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = transferToWallet,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(AppStr.simpanKe) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expTo) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expTo,
                        onDismissRequest = { expTo = false }
                    ) {
                        walletList.forEach { w ->
                            DropdownMenuItem(
                                text = { Text(w) },
                                onClick = { transferToWallet = w; expTo = false }
                            )
                        }
                    }
                }
            }
        } else {
            ExposedDropdownMenuBox(
                expanded = expandedCat,
                onExpandedChange = { expandedCat = !expandedCat },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(AppStr.cat) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedCat,
                    onDismissRequest = { expandedCat = false }
                ) {
                    currentCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
                                expandedCat = false
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(AppStr.addCat, color = AppPrimary(), fontWeight = FontWeight.Bold) },
                        onClick = {
                            expandedCat = false
                            showNewCategoryDialog = true
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(if (txMode == 2) "${AppStr.nme} (Opsional)" else AppStr.nme) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(AppStr.msgInp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        Spacer(modifier = Modifier.height(24.dp))

        val curSym = when(profile.currency) {
            "USD", "AUD", "CAD", "SGD" -> "$"
            "EUR" -> "â‚¬"
            "GBP" -> "Â£"
            "JPY", "CNY" -> "Â¥"
            "CHF" -> "CHF"
            else -> "Rp"
        }

        if (txMode == 2) {
            OutlinedTextField(
                value = initialSplits[0].amount,
                onValueChange = {
                    if (it.all { c -> c in allowedMathChars }) {
                        initialSplits[0] = initialSplits[0].copy(amount = it)
                    }
                },
                label = { Text(AppStr.jumlahPindah) },
                placeholder = { Text("Cth: 15000+2000") },
                visualTransformation = if (initialSplits[0].amount.any { c -> c in "+-*/()" }) androidx.compose.ui.text.input.VisualTransformation.None else ThousandSeparatorTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            val calcTransfer = evaluateMathExpression(initialSplits[0].amount) ?: 0L
            Text(
                "${AppStr.totalTransfer}: $curSym ${NumberFormat.getInstance(java.util.Locale.getDefault()).format(calcTransfer)}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = AppText()
            )
        } else {
            Text(
                AppStr.splitSource,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AppText()
            )
            Spacer(modifier = Modifier.height(8.dp))

            initialSplits.forEachIndexed { index, splitItem ->
                var expandedWallet by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedWallet,
                        onExpandedChange = { expandedWallet = !expandedWallet },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = splitItem.wallet,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWallet) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedWallet,
                            onDismissRequest = { expandedWallet = false }
                        ) {
                            walletList.forEach { w ->
                                DropdownMenuItem(
                                    text = { Text(w) },
                                    onClick = {
                                        initialSplits[index] = splitItem.copy(wallet = w)
                                        expandedWallet = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(AppStr.addWallet, color = AppPrimary(), fontWeight = FontWeight.Bold) },
                                onClick = {
                                    expandedWallet = false
                                    showNewWalletDialog = true
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = splitItem.amount,
                        onValueChange = {
                            if (it.all { c -> c in allowedMathChars }) {
                                initialSplits[index] = splitItem.copy(amount = it)
                            }
                        },
                        placeholder = { Text("15000+2000") },
                        visualTransformation = if (splitItem.amount.any { c -> c in "+-*/()" }) androidx.compose.ui.text.input.VisualTransformation.None else ThousandSeparatorTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (initialSplits.size > 1) {
                        IconButton(
                            onClick = { initialSplits.removeAt(index) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, null, tint = AppRed())
                        }
                    }
                }
            }

            TextButton(
                onClick = {
                    initialSplits.add(
                        SplitItemUi(
                            java.util.UUID.randomUUID().toString(),
                            walletList.firstOrNull() ?: "Cash",
                            ""
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null, tint = AppPrimary())
                Spacer(modifier = Modifier.width(8.dp))
                Text(AppStr.addOtherWallet, color = AppPrimary(), fontWeight = FontWeight.Bold)
            }

            val totalAmount = initialSplits.sumOf { evaluateMathExpression(it.amount) ?: 0L }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "${AppStr.total}: $curSym ${NumberFormat.getInstance(java.util.Locale.getDefault()).format(totalAmount)}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = AppText()
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        val evaluatedSplits = initialSplits.map { it.copy(amount = (evaluateMathExpression(it.amount) ?: 0L).toString()) }
        val totalAmtFinal = if (txMode == 2) (evaluatedSplits[0].amount.toLongOrNull() ?: 0L) else evaluatedSplits.sumOf { it.amount.toLongOrNull() ?: 0L }
        val isAmountValid = totalAmtFinal > 0L

        Button(
            onClick = {
                val timeStr = txTimestamp
                val dateStr = txDateStr

                if (txMode == 2) {
                    val title = name.ifEmpty { AppStr.defMutasiTitle }
                    val txOut = KumaTransaction(id = 0, name = title, date = dateStr, amount = totalAmtFinal.toString(), isIncome = false, category = "Transfer", wallet = transferFromWallet, timestamp = timeStr, message = message)
                    val txIn = KumaTransaction(id = 0, name = title, date = dateStr, amount = totalAmtFinal.toString(), isIncome = true, category = "Transfer", wallet = transferToWallet, timestamp = timeStr, message = message)

                    onSave(listOf(Pair(txOut, emptyList()), Pair(txIn, emptyList())))
                } else {
                    val parentWalletStr = if(evaluatedSplits.size > 1) "${AppStr.multiWallet} (${evaluatedSplits.size})" else evaluatedSplits[0].wallet
                    val newTx = KumaTransaction(id = baseTx?.id ?: 0, name = name, date = dateStr, amount = totalAmtFinal.toString(), isIncome = (txMode == 1), category = selectedCategory, wallet = parentWalletStr, timestamp = timeStr, message = message)
                    val dbSplits = evaluatedSplits.filter { it.amount.isNotBlank() && (it.amount.toLongOrNull() ?: 0L) > 0 }.map { TransactionSplit(transactionId = 0, splitWallet = it.wallet, splitAmount = it.amount.toLongOrNull() ?: 0L) }

                    onSave(listOf(Pair(newTx, dbSplits)))
                }
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary()),
            shape = RoundedCornerShape(16.dp),
            enabled = isAmountValid && (txMode == 2 || name.isNotEmpty())
        ) {
            Text(AppStr.saveTx, color = Color.White, fontWeight = FontWeight.ExtraBold)
        }
    }

    if (showNewWalletDialog) {
        AlertDialog(
            onDismissRequest = { showNewWalletDialog = false; newWalletName = "" },
            title = { Text(AppStr.newWallet, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newWalletName,
                    onValueChange = { newWalletName = it },
                    label = { Text(AppStr.walletName) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWalletName.isNotBlank() && !walletList.contains(newWalletName.trim())) {
                            val newWallet = newWalletName.trim()
                            val updatedList = walletList + newWallet
                            walletList = updatedList
                            onUpdateProfile(profile.copy(wallets = updatedList.joinToString(",")))
                        }
                        showNewWalletDialog = false
                        newWalletName = ""
                    }
                ) { Text(AppStr.save) }
            },
            dismissButton = {
                TextButton(onClick = { showNewWalletDialog = false }) {
                    Text(AppStr.no, color = AppText())
                }
            }
        )
    }

    if (showNewCategoryDialog) {
        var selectedIconKey by remember { mutableStateOf("Kategori") }

        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false; newCategoryName = "" },
            title = { Text(AppStr.newCat, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text(AppStr.catName) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(AppStr.chooseCatIcon, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppText())
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.height(150.dp).glassCard(8.dp, AppSurfaceVariant(), useHaze = false)
                    ) {
                        items(kumaIconLibrary.keys.toList()) { key ->
                            val icon = kumaIconLibrary[key]!!
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedIconKey == key) AppPrimary() else Color.Transparent)
                                    .clickable {
                                        selectedIconKey = key
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = key, tint = if (selectedIconKey == key) Color.White else AppText(), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank() && !currentCategories.contains(newCategoryName.trim())) {
                            val newCat = newCategoryName.trim()

                            val iconJson = try { JSONObject(profile.categoryIcons) } catch (e: Exception) { JSONObject() }
                            iconJson.put(newCat, selectedIconKey)

                            if (txMode == 1) {
                                val updated = incomeCategories + newCat
                                onUpdateProfile(profile.copy(incomeCats = updated.joinToString(","), categoryIcons = iconJson.toString()))
                            } else {
                                val updated = expenseCategories + newCat
                                onUpdateProfile(profile.copy(expenseCats = updated.joinToString(","), categoryIcons = iconJson.toString()))
                            }
                            selectedCategory = newCat
                        }
                        showNewCategoryDialog = false
                        newCategoryName = ""
                    }
                ) { Text(AppStr.save) }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) {
                    Text(AppStr.no, color = AppText())
                }
            }
        )
    }
}

@Composable
fun MonthYearSelector(currentMonth: Int, currentYear: Int, onMonthChange: (Int, Int) -> Unit) {
    val monthNames = if (AppStr.isId) {
        listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    } else {
        listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(20.dp, AppSurfaceVariant(), useHaze = false)
            .padding(vertical = 6.dp)
    ) {
        IconButton(
            onClick = {
                var m = currentMonth - 1
                var y = currentYear
                if (m < 1) {
                    m = 12
                    y -= 1
                }
                onMonthChange(m, y)
            }
        ) {
            Icon(Icons.Default.ChevronLeft, null, tint = AppText())
        }

        Text(
            text = "${monthNames[currentMonth - 1]} $currentYear",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = AppText(),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        IconButton(
            onClick = {
                var m = currentMonth + 1
                var y = currentYear
                if (m > 12) {
                    m = 1
                    y += 1
                }
                onMonthChange(m, y)
            }
        ) {
            Icon(Icons.Default.ChevronRight, null, tint = AppText())
        }
    }
}


// --- 6. SHARED COMPONENTS (AutoSizeText, PDF, CSV, Item) ---

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    maxLines: Int = 1,
    minimumFallbackSize: TextUnit = 12.sp,
    textAlign: TextAlign? = null
) {
    var scaledTextStyle by remember { mutableStateOf(TextStyle(fontSize = fontSize)) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent { if (readyToDraw) drawContent() },
        color = color,
        fontSize = scaledTextStyle.fontSize,
        fontWeight = fontWeight,
        maxLines = maxLines,
        softWrap = false,
        textAlign = textAlign,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && scaledTextStyle.fontSize > minimumFallbackSize) {
                scaledTextStyle = scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * 0.9f)
            } else {
                readyToDraw = true
            }
        }
    )
}

class ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val locale = Locale.forLanguageTag("id-ID")
        val formattedText = try {
            NumberFormat.getInstance(locale).format(originalText.toLong())
        } catch (_: Exception) {
            originalText
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val originalBefore = originalText.substring(0, offset)
                val transformedBefore = try {
                    NumberFormat.getInstance(locale).format(originalBefore.toLong())
                } catch (_: Exception) {
                    originalBefore
                }
                return transformedBefore.length
            }
            override fun transformedToOriginal(offset: Int): Int {
                val digitsOnly = formattedText.substring(0, offset.coerceAtMost(formattedText.length)).replace(".", "")
                return digitsOnly.length.coerceAtMost(originalText.length)
            }
        }
        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}

fun drawHeaders(
    canvas: android.graphics.Canvas,
    paint: Paint,
    pageNum: Int,
    profile: UserProfile,
    titlePaint: Paint,
    headerPaint: Paint,
    periodStr: String
) {
    canvas.drawText("${AppStr.repPdf} ($pageNum)", 40f, 50f, titlePaint)
    canvas.drawText("${AppStr.cur}: ${profile.currency} | Periode: $periodStr", 40f, 75f, Paint().apply { textSize = 12f })
    canvas.drawLine(40f, 90f, 550f, 90f, paint)
    canvas.drawText(AppStr.date, 40f, 110f, headerPaint)
    canvas.drawText(AppStr.cat, 120f, 110f, headerPaint)
    canvas.drawText("Dompet", 200f, 110f, headerPaint)
    canvas.drawText(AppStr.nme, 280f, 110f, headerPaint)
    canvas.drawText(AppStr.amt, 480f, 110f, headerPaint)
    canvas.drawLine(40f, 120f, 550f, 120f, paint)
}

fun generatePDF(context: Context, data: List<KumaTransaction>, profile: UserProfile, month: Int, year: Int) {
    val monthNames = if (AppStr.isId) listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember") else listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val periodStr = "${monthNames[month - 1]} $year"
    val pdfDocument = PdfDocument()
    var pageNum = 1
    var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNum).create())

    val paint = Paint()
    val titlePaint = Paint().apply { isFakeBoldText = true; textSize = 18f; color = android.graphics.Color.BLACK }
    val headerPaint = Paint().apply { isFakeBoldText = true; textSize = 12f; color = android.graphics.Color.DKGRAY }
    val curSym = when(profile.currency) { "USD", "AUD", "CAD", "SGD" -> "$"; "EUR" -> "â‚¬"; "GBP" -> "Â£"; "JPY", "CNY" -> "Â¥"; "CHF" -> "CHF"; else -> "Rp" }

    drawHeaders(page.canvas, paint, pageNum, profile, titlePaint, headerPaint, periodStr)
    var yPos = 145f

    data.forEach { item ->
        if (yPos > 800f) {
            pdfDocument.finishPage(page)
            pageNum++
            page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNum).create())
            drawHeaders(page.canvas, paint, pageNum, profile, titlePaint, headerPaint, periodStr)
            yPos = 145f
        }
        val amountPrefix = if (item.isIncome) "+" else "-"
        val amountColor = if (item.isIncome) android.graphics.Color.parseColor("#1B5E20") else android.graphics.Color.parseColor("#B71C1C")

        paint.color = android.graphics.Color.BLACK
        page.canvas.drawText(item.date.take(12), 40f, yPos, paint)
        page.canvas.drawText(item.category.take(10), 120f, yPos, paint)
        page.canvas.drawText(item.wallet.take(10), 200f, yPos, paint)
        page.canvas.drawText(item.name.take(25), 280f, yPos, paint)

        paint.color = amountColor
        page.canvas.drawText("$amountPrefix $curSym ${item.amount}", 480f, yPos, paint)
        yPos += 25f
    }

    if (yPos > 750f) {
        pdfDocument.finishPage(page)
        pageNum++
        page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNum).create())
        yPos = 50f
    }

    paint.color = android.graphics.Color.BLACK
    page.canvas.drawLine(40f, yPos, 550f, yPos, paint)
    yPos += 20f
    val inc = data.filter { it.isIncome }.sumOf { it.amount.toLongOrNull() ?: 0L }
    val exp = data.filter { !it.isIncome }.sumOf { it.amount.toLongOrNull() ?: 0L }
    page.canvas.drawText("Pemasukan: $curSym $inc", 40f, yPos, titlePaint.apply { textSize = 12f; color = android.graphics.Color.parseColor("#1B5E20") })
    yPos += 20f
    page.canvas.drawText("Pengeluaran: $curSym $exp", 40f, yPos, titlePaint.apply { textSize = 12f; color = android.graphics.Color.parseColor("#B71C1C") })

    pdfDocument.finishPage(page)
    val file = File(context.cacheDir, "KumaFlow_Report_${month}_${year}.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, AppStr.sharePdf))
    } catch (_: Exception) {
        Toast.makeText(context, AppStr.failPdf, Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}

fun generateCSV(context: Context, data: List<KumaTransaction>, profile: UserProfile, month: Int, year: Int) {
    val file = File(context.cacheDir, "KumaFlow_Report_${month}_${year}.csv")
    try {
        file.bufferedWriter().use { out ->
            out.write("${AppStr.date},${AppStr.cat},Wallet,${AppStr.type},${AppStr.nme},${AppStr.msgInp},${AppStr.cur},${AppStr.amt}\n")
            data.forEach { t ->
                val type = if (t.isIncome) AppStr.inc else AppStr.exp
                out.write("${t.date},${t.category},${t.wallet},$type,\"${t.name}\",\"${t.message}\",${profile.currency},${t.amount}\n")
            }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, AppStr.shareCsv))
    } catch (_: Exception) {
        Toast.makeText(context, AppStr.failCsv, Toast.LENGTH_SHORT).show()
    }
}

fun exportToDrive(context: Context, data: List<KumaTransaction>, profile: UserProfile, month: Int, year: Int) {
    val file = File(context.cacheDir, "KumaFlow_Drive_${month}_${year}.csv")
    try {
        file.bufferedWriter().use { out ->
            out.write("${AppStr.date},${AppStr.cat},Wallet,${AppStr.type},${AppStr.nme},${AppStr.msgInp},${AppStr.cur},${AppStr.amt}\n")
            data.forEach { t ->
                val type = if (t.isIncome) AppStr.inc else AppStr.exp
                out.write("${t.date},${t.category},${t.wallet},$type,\"${t.name}\",\"${t.message}\",${profile.currency},${t.amount}\n")
            }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.google.android.apps.docs")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, AppStr.noDrive, Toast.LENGTH_LONG).show()
        generateCSV(context, data, profile, month, year)
    }
}

fun backupAppToJSON(context: Context, profile: UserProfile, txsWithSplits: List<TransactionWithSplits>) {
    try {
        val root = JSONObject()
        root.put("backupVersion", 5)

        val pJson = JSONObject().apply {
            put("userName", profile.userName)
            put("isAppLocked", profile.isAppLocked)
            put("appPin", profile.appPin)
            put("currency", profile.currency)
            put("dateFormat", profile.dateFormat)
            put("monthlyTarget", profile.monthlyTarget)
            put("themeMode", profile.themeMode)
            put("isReminderOn", profile.isReminderOn)
            put("reminderTimes", profile.reminderTimes)
            put("useCarryOver", profile.useCarryOver)
            put("expenseCats", profile.expenseCats)
            put("incomeCats", profile.incomeCats)
            put("wallets", profile.wallets)
            put("categoryTargets", profile.categoryTargets)
            put("isAmoledMode", profile.isAmoledMode)
            put("categoryIcons", profile.categoryIcons)
        }
        root.put("profile", pJson)

        val tArr = JSONArray()
        txsWithSplits.forEach { obj ->
            val tJson = JSONObject().apply {
                put("name", obj.transaction.name)
                put("date", obj.transaction.date)
                put("amount", obj.transaction.amount)
                put("isIncome", obj.transaction.isIncome)
                put("category", obj.transaction.category)
                put("wallet", obj.transaction.wallet)
                put("timestamp", obj.transaction.timestamp)
                put("message", obj.transaction.message)
            }
            if (obj.splits.isNotEmpty()) {
                val splitArr = JSONArray()
                obj.splits.forEach { s ->
                    splitArr.put(JSONObject().apply {
                        put("w", s.splitWallet)
                        put("a", s.splitAmount)
                    })
                }
                tJson.put("splits", splitArr)
            }
            tArr.put(tJson)
        }

        root.put("transactions", tArr)
        val file = File(context.cacheDir, "KumaFlow_Backup_${System.currentTimeMillis()}.kuma")
        file.writeText(root.toString())

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, AppStr.saveBak))
    } catch (_: Exception) {
        Toast.makeText(context, AppStr.failBak, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun SettingsGroupCard(
    title: String,
    modifier: Modifier = Modifier,
    items: List<Pair<String, ImageVector>>,
    hasSwitch: Boolean = false,
    isSwitchOn: Boolean = false,
    onSwitchChange: (Boolean) -> Unit = {},
    onClick: (String) -> Unit
) {
    Card(
        modifier = modifier
            .heightIn(min = 230.dp)
            .border(
                width = 1.dp,
                color = AppText().copy(alpha = 0.15f),
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface())
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = AppText(),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(18.dp))
            items.forEach { (label, icon) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onClick(label) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = AppText(), modifier = Modifier.size(20.dp))
                    Text(
                        label,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppText(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (hasSwitch && label == AppStr.appLck) {
                        Switch(
                            checked = isSwitchOn,
                            onCheckedChange = onSwitchChange,
                            modifier = Modifier.scale(0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomBottomNav(
    selectedIndex: Int,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onItemSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .height(85.dp)
            .border(1.dp, AppText().copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .glassCard(24.dp, AppSurface(), useHaze = false)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Rounded.Home, AppStr.home, selectedIndex == 0) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onItemSelected(0)
            }
            NavItem(Icons.Rounded.Equalizer, AppStr.rep, selectedIndex == 1) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onItemSelected(1)
            }
            NavItem(Icons.Rounded.Settings, AppStr.set, selectedIndex == 2) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onItemSelected(2)
            }
        }
    }
}

@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .widthIn(max = 80.dp)
    ) {
        Icon(
            icon,
            null,
            tint = if (isSelected) AppText() else AppText().copy(alpha = 0.5f),
            modifier = Modifier.size(32.dp)
        )
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) AppText() else AppText().copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun IncomeExpensePill(label: String, amount: String, color: Color, isUp: Boolean) {
    Column(horizontalAlignment = Alignment.End) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                " $label",
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp
            )
        }
        AutoSizeText(
            text = amount,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            minimumFallbackSize = 8.sp
        )
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AppText()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    profile: UserProfile,
    obj: TransactionWithSplits,
    isPrivacyMode: Boolean,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onEdit: (TransactionWithSplits) -> Unit,
    onDelete: (TransactionWithSplits) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val trans = obj.transaction
    val haptic = LocalHapticFeedback.current

    val blurRadius by androidx.compose.animation.core.animateDpAsState(targetValue = if (isPrivacyMode) 12.dp else 0.dp, label = "blur_tx_anim")

    val curSym = when(profile.currency) {
        "USD", "AUD", "CAD", "SGD" -> "$"
        "EUR" -> "â‚¬"
        "GBP" -> "Â£"
        "JPY", "CNY" -> "Â¥"
        "CHF" -> "CHF"
        else -> "Rp"
    }

    val savedIcons = remember(profile.categoryIcons) {
        try { JSONObject(profile.categoryIcons) } catch (e: Exception) { JSONObject() }
    }

    val iconKey = savedIcons.optString(trans.category, "")
    val icon = kumaIconLibrary[iconKey] ?: when(trans.category) {
        "Financial" -> Icons.Default.AccountBalance
        "Food" -> Icons.Default.Restaurant
        "Shopping" -> Icons.Default.LocalMall
        "Health" -> Icons.Default.Favorite
        "Transport" -> Icons.Default.DirectionsCar
        "Education" -> Icons.Default.School
        "Entertainment" -> Icons.Default.Gamepad
        "Transfer" -> Icons.Default.SyncAlt
        else -> Icons.Default.Category
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(AppStr.delConf) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(obj)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppRed())
                ) { Text(AppStr.yes) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(AppStr.no, color = AppText())
                }
            }
        )
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (isSelectionMode) return@rememberSwipeToDismissBoxState false
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDeleteDialog = true
                    false
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEdit(obj)
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !isSelectionMode,
        enableDismissFromEndToStart = !isSelectionMode,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF1976D2)
                SwipeToDismissBoxValue.EndToStart -> AppRed()
                else -> Color.Transparent
            }
            val alignment = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            val iconSwipe = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> Icons.Default.Category
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                Icon(iconSwipe, contentDescription = null, tint = Color.White)
            }
        }
    ) {
        val baseCardColor = if (trans.category == "Transfer") Color(0xFF1976D2) else Color(0xFFD5641C)
        val finalCardColor = if (isSelected) AppPrimary().copy(alpha = 0.5f) else baseCardColor

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) AppPrimary() else Color.Transparent,
                    shape = RoundedCornerShape(20.dp)
                )
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleSelect()
                        } else {
                            onEdit(obj)
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (!isSelectionMode) {
                            onToggleSelect()
                        } else {
                            val duplicateTx = obj.copy(transaction = trans.copy(id = 0, name = "${trans.name} (Copy)"))
                            onEdit(duplicateTx)
                        }
                    }
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = finalCardColor)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(if (isSelected) AppPrimary() else Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(trans.name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

                    if (trans.message.isNotEmpty()) {
                        Text(trans.message, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 2.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Text("${trans.wallet} â€¢ ${trans.category}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                val formatted = try {
                    NumberFormat.getInstance(Locale.forLanguageTag("id-ID")).format(trans.amount.toLong())
                } catch (_: Exception) {
                    trans.amount
                }

                AutoSizeText(
                    text = "${if (trans.isIncome) "+ " else "- "} $curSym $formatted",
                    color = if (trans.isIncome) Color.White else AppText(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.widthIn(max = 120.dp).padding(start = 8.dp).blur(blurRadius),
                    minimumFallbackSize = 10.sp
                )
            }
        }
    }
}

fun updateKumaWidget(context: Context) {
    try {
        val updateIntent = Intent(context, Class.forName("com.bearbones.kumaflow.KumaWidgetProvider")).apply {
            action = "com.bearbones.kumaflow.UPDATE_WIDGET"
        }
        context.sendBroadcast(updateIntent)
    } catch (_: Exception) {}
}

fun checkAndApplyPrideEasterEgg(context: android.content.Context, userName: String?) {
    if (userName.isNullOrEmpty()) return

    val pm = context.packageManager
    val pkg = context.packageName

    val normalIcon = android.content.ComponentName(context, "$pkg.MainActivityAliasNormal")
    val prideIcon = android.content.ComponentName(context, "$pkg.MainActivityAliasPride")
    val bearIcon = android.content.ComponentName(context, "$pkg.MainActivityAliasBear")

    fun setIcon(targetIcon: android.content.ComponentName) {
        val allIcons = listOf(normalIcon, prideIcon, bearIcon)
        for (icon in allIcons) {
            val state = if (icon == targetIcon) {
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }

            if (pm.getComponentEnabledSetting(icon) != state) {
                pm.setComponentEnabledSetting(
                    icon,
                    state,
                    android.content.pm.PackageManager.DONT_KILL_APP
                )
            }
        }
    }

    val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
    if (currentMonth == java.util.Calendar.JUNE) {
        when {
            userName.contains("ðŸŒˆ") || userName.contains("#pride", ignoreCase = true) -> setIcon(
                prideIcon
            )
            userName.contains("ðŸ»") || userName.contains("#bear", ignoreCase = true) -> setIcon(
                bearIcon
            )
            else -> setIcon(normalIcon)
        }
    } else {
        setIcon(normalIcon)
    }
}

fun evaluateMathExpression(input: String): Long? {
    return try {
        val expression = input.replace("\\s".toRegex(), "")
        if (expression.isEmpty()) return null

        object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm()
                    else if (eat('-'.code)) x -= parseTerm()
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor()
                    else if (eat('/'.code)) x /= parseFactor()
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()
                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                return x
            }
        }.parse().toLong()
    } catch (e: Exception) {
        null
    }
}




