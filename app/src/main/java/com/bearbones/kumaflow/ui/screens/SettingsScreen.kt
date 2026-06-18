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
fun SettingsScreen(
    currentProfile: UserProfile,
    monthlyTransactionsWithSplits: List<TransactionWithSplits>,
    allTransactionsWithSplits: List<TransactionWithSplits>,
    dao: TransactionDao,
    selectedMonth: Int,
    selectedYear: Int,
    onForceUpdate: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val mainActivity = context as? MainActivity
    val scope = rememberCoroutineScope()

    var showVersionDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showCatBudgetDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showWalletDialog by remember { mutableStateOf(false) }

    var pinInput by remember { mutableStateOf("") }
    var targetInput by remember { mutableStateOf(currentProfile.monthlyTarget.toString()) }
    var isTurningOn by remember { mutableStateOf(true) }
    var newName by remember { mutableStateOf(currentProfile.userName) }

    LaunchedEffect(mainActivity?.pendingRestoreJson) {
        val jsonToRestore = mainActivity?.pendingRestoreJson
        if (jsonToRestore != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val root = JSONObject(jsonToRestore)
                    val pObj = root.getJSONObject("profile")
                    val newProfile = UserProfile(
                        userName = pObj.optString("userName", "User"),
                        isAppLocked = pObj.optBoolean("isAppLocked", false),
                        appPin = pObj.optString("appPin", ""),
                        currency = pObj.optString("currency", "IDR"),
                        dateFormat = pObj.optString("dateFormat", "dd MMM yyyy"),
                        monthlyTarget = pObj.optLong("monthlyTarget", 0L),
                        themeMode = pObj.optInt("themeMode", 0),
                        isReminderOn = pObj.optBoolean("isReminderOn", false),
                        reminderTimes = pObj.optString("reminderTimes", "05:00,12:30,15:30,18:00,20:00"),
                        useCarryOver = pObj.optBoolean("useCarryOver", false),
                        expenseCats = pObj.optString("expenseCats", "Food,Shopping,Health,Transport,Education,Entertainment,Others"),
                        incomeCats = pObj.optString("incomeCats", "Financial,Others"),
                        wallets = pObj.optString("wallets", "Cash,Bank BCA,GoPay"),
                        categoryTargets = pObj.optString("categoryTargets", "{}"),
                        isAmoledMode = pObj.optBoolean("isAmoledMode", false),
                        categoryIcons = pObj.optString("categoryIcons", "{}")
                    )
                    dao.saveProfile(newProfile)

                    val txsArr = root.getJSONArray("transactions")
                    dao.clearTransactions()

                    for (i in 0 until txsArr.length()) {
                        try {
                            val tObj = txsArr.getJSONObject(i)
                            var safeTimestamp = tObj.optString("timestamp", "")
                            if (safeTimestamp.isBlank()) {
                                safeTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            }

                            val baseTx = KumaTransaction(
                                name = tObj.optString("name", "Unknown"),
                                date = tObj.optString("date", ""),
                                amount = tObj.optString("amount", "0"),
                                isIncome = tObj.optBoolean("isIncome", false),
                                category = tObj.optString("category", "Others"),
                                wallet = tObj.optString("wallet", "Cash"),
                                timestamp = safeTimestamp,
                                message = tObj.optString("message", "")
                            )

                            val splitsArr = tObj.optJSONArray("splits")
                            val dbSplits = mutableListOf<TransactionSplit>()
                            if (splitsArr != null) {
                                for (j in 0 until splitsArr.length()) {
                                    val sObj = splitsArr.getJSONObject(j)
                                    dbSplits.add(
                                        TransactionSplit(
                                            transactionId = 0,
                                            splitWallet = sObj.optString("w", "Cash"),
                                            splitAmount = sObj.optLong("a", 0L)
                                        )
                                    )
                                }
                            }

                            dao.insertFullTransaction(baseTx, dbSplits)

                        } catch (e: Exception) {
                            android.util.Log.e("RestoreDebug", "Gagal insert transaksi ke-$i: ${e.message}")
                        }
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, AppStr.resOk, Toast.LENGTH_SHORT).show()
                        updateKumaWidget(context)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error Restore: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } finally {
                    mainActivity.pendingRestoreJson = null
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(AppStr.set, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = AppText())
        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsGroupCard(
                    title = AppStr.accSec,
                    modifier = Modifier.weight(1f),
                    items = listOf(
                        AppStr.editProf to Icons.Default.Edit,
                        AppStr.theme to Icons.Default.Palette,
                        AppStr.appLck to Icons.Default.Fingerprint
                    ),
                    hasSwitch = true,
                    isSwitchOn = currentProfile.isAppLocked,
                    onSwitchChange = {
                        isTurningOn = it
                        showPinDialog = true
                    }
                ) { label ->
                    when(label) {
                        AppStr.editProf -> showEditProfileDialog = true
                        AppStr.theme -> showThemeDialog = true
                    }
                }

                SettingsGroupCard(
                    title = AppStr.finPref,
                    modifier = Modifier.weight(1f),
                    items = listOf(
                        AppStr.cur to Icons.Default.Sync,
                        AppStr.manageWallet to Icons.Default.AccountBalanceWallet,
                        AppStr.manageCat to Icons.Default.Category,
                        AppStr.tar to Icons.Default.Adjust,
                        AppStr.catBudget to Icons.Default.PieChart
                    ),
                    onClick = { label ->
                        when(label) {
                            AppStr.cur -> showCurrencyDialog = true
                            AppStr.manageWallet -> showWalletDialog = true
                            AppStr.tar -> showTargetDialog = true
                            AppStr.manageCat -> showCategoryDialog = true
                            AppStr.catBudget -> showCatBudgetDialog = true
                        }
                    }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AppText().copy(alpha = 0.15f), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = AppSurface())
            ) {
                var expandReminders by remember { mutableStateOf(false) }

                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        AppStr.notif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = AppText(),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = AppText(), modifier = Modifier.size(20.dp))
                        Text(
                            AppStr.carryOver,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppText(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Switch(
                            checked = currentProfile.useCarryOver,
                            onCheckedChange = { isChecked ->
                                scope.launch {
                                    dao.saveProfile(currentProfile.copy(useCarryOver = isChecked))
                                    onForceUpdate()
                                }
                            },
                            modifier = Modifier.scale(0.8f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, null, tint = AppText(), modifier = Modifier.size(20.dp))
                        Text(
                            AppStr.dailyRem,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppText(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (currentProfile.isReminderOn) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    expandReminders = !expandReminders
                                },
                                modifier = Modifier.size(28.dp).padding(end = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (expandReminders) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand",
                                    tint = AppText()
                                )
                            }
                        }

                        Switch(
                            checked = currentProfile.isReminderOn,
                            onCheckedChange = { isChecked ->
                                scope.launch {
                                    val newProf = currentProfile.copy(isReminderOn = isChecked)
                                    dao.saveProfile(newProf)
                                    onForceUpdate()
                                }
                                if (!isChecked) expandReminders = false
                            },
                            modifier = Modifier.scale(0.8f)
                        )
                    }

                    if (currentProfile.isReminderOn && expandReminders) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .glassCard(12.dp, AppSurfaceVariant())
                                .padding(12.dp)
                        ) {
                            val timesList = currentProfile.reminderTimes.split(",")
                            timesList.forEachIndexed { index, time ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val parts = time.split(":")
                                            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
                                            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

                                            TimePickerDialog(context, { _, h, m ->
                                                val newTimes = timesList.toMutableList()
                                                newTimes[index] = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                                                val newProf = currentProfile.copy(reminderTimes = newTimes.joinToString(","))
                                                scope.launch {
                                                    dao.saveProfile(newProf)
                                                    onForceUpdate()
                                                }
                                            }, hour, minute, true).show()
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AccessTime, null, tint = AppText().copy(alpha=0.7f), modifier = Modifier.size(16.dp))
                                    Text(
                                        "${AppStr.rem} ${index + 1}",
                                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                                        fontSize = 12.sp,
                                        color = AppText().copy(alpha=0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        time,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppPrimary()
                                    )
                                }
                                if (index < timesList.size - 1) {
                                    HorizontalDivider(color = AppText().copy(alpha = 0.1f))
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsGroupCard(
                    title = AppStr.dat,
                    modifier = Modifier.weight(1f),
                    items = listOf(
                        AppStr.expPdf to Icons.Default.PictureAsPdf,
                        AppStr.expCsv to Icons.Default.Description,
                        AppStr.expDrive to Icons.Default.AddToDrive,
                        AppStr.backApp to Icons.Default.CloudUpload,
                        AppStr.rest to Icons.Default.History,
                        AppStr.optDb to Icons.Default.CleaningServices
                    )
                ) { label ->
                    val plainMonthlyTxs = monthlyTransactionsWithSplits.map { it.transaction }
                    when (label) {
                        AppStr.expPdf -> generatePDF(context, plainMonthlyTxs, currentProfile, selectedMonth, selectedYear)
                        AppStr.expCsv -> generateCSV(context, plainMonthlyTxs, currentProfile, selectedMonth, selectedYear)
                        AppStr.expDrive -> exportToDrive(context, plainMonthlyTxs, currentProfile, selectedMonth, selectedYear)
                        AppStr.backApp -> backupAppToJSON(context, currentProfile, allTransactionsWithSplits)
                        AppStr.rest -> { mainActivity?.openSafeFilePicker() }
                        AppStr.optDb -> {
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val db = KumaDatabase.getDatabase(context)
                                    db.openHelper.writableDatabase.execSQL("VACUUM")
                                    withContext(Dispatchers.Main) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        Toast.makeText(context, AppStr.optSuccess, Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "${AppStr.optFail} ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }

                SettingsGroupCard(
                    title = AppStr.abt,
                    modifier = Modifier.weight(1f),
                    items = listOf(
                        AppStr.appVer to Icons.Default.Info,
                        AppStr.priv to Icons.Default.PrivacyTip,
                        AppStr.trms to Icons.AutoMirrored.Filled.MenuBook,
                        AppStr.contDev to Icons.Default.SupportAgent
                    )
                ) { label ->
                    when (label) {
                        AppStr.appVer -> showVersionDialog = true
                        AppStr.priv -> showPrivacyDialog = true
                        AppStr.trms -> showTermsDialog = true
                        AppStr.contDev -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/6285173220524")))
                    }
                }
            }
        }

        if (showCatBudgetDialog) {
            val expenseCatsList = currentProfile.expenseCats.split(",").filter { it.isNotBlank() }
            var targetMap by remember {
                mutableStateOf(
                    try {
                        val json = JSONObject(currentProfile.categoryTargets)
                        val map = mutableMapOf<String, Long>()
                        val keys = json.keys()
                        while(keys.hasNext()) {
                            val k = keys.next()
                            map[k] = json.optLong(k, 0L)
                        }
                        map
                    } catch (e: Exception) {
                        mutableMapOf<String, Long>()
                    }
                )
            }
            AlertDialog(
                onDismissRequest = { showCatBudgetDialog = false },
                title = { Text(AppStr.catBudget, fontWeight = FontWeight.Bold) },
                text = {
                    Box(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                            expenseCatsList.forEach { catName ->
                                val currentVal = targetMap[catName] ?: 0L
                                var inputValue by remember { mutableStateOf(if (currentVal == 0L) "" else currentVal.toString()) }
                                OutlinedTextField(
                                    value = inputValue,
                                    onValueChange = { input ->
                                        if (input.isEmpty() || input.all { char -> char.isDigit() }) {
                                            inputValue = input
                                            targetMap = targetMap.toMutableMap().apply { put(catName, input.toLongOrNull() ?: 0L) }
                                        }
                                    },
                                    label = { Text(catName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val json = JSONObject()
                                targetMap.forEach { (k, v) -> json.put(k, v) }
                                dao.saveProfile(currentProfile.copy(categoryTargets = json.toString()))
                                onForceUpdate()
                                showCatBudgetDialog = false
                            }
                        }
                    ) { Text(AppStr.save) }
                },
                dismissButton = {
                    TextButton(onClick = { showCatBudgetDialog = false }) { Text(AppStr.close, color = AppText()) }
                },
                containerColor = AppSurface()
            )
        }

        if (showWalletDialog) {
            var newWalletName by remember { mutableStateOf("") }
            var activeWallets by remember { mutableStateOf(currentProfile.wallets.split(",").filter { it.isNotBlank() }) }
            AlertDialog(
                onDismissRequest = {
                    showWalletDialog = false
                    onForceUpdate()
                },
                title = { Text(AppStr.manageWallet, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(activeWallets) { w ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("â€¢ $w", color = AppText(), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    if (activeWallets.size > 1) {
                                        IconButton(
                                            onClick = {
                                                val newList = activeWallets.filter { it != w }
                                                activeWallets = newList
                                                scope.launch { dao.saveProfile(currentProfile.copy(wallets = newList.joinToString(","))) }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) { Icon(Icons.Default.Close, null, tint = AppRed()) }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newWalletName,
                                onValueChange = { newWalletName = it },
                                label = { Text(AppStr.addWallet) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (newWalletName.isNotBlank() && !activeWallets.contains(newWalletName.trim())) {
                                        val newList = activeWallets.toMutableList().apply { add(newWalletName.trim()) }
                                        activeWallets = newList
                                        scope.launch { dao.saveProfile(currentProfile.copy(wallets = newList.joinToString(","))) }
                                        newWalletName = ""
                                    }
                                },
                                modifier = Modifier.background(AppPrimary(), CircleShape)
                            ) { Icon(Icons.Default.Add, null, tint = Color.White) }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showWalletDialog = false
                            onForceUpdate()
                        }
                    ) { Text(AppStr.close) }
                }
            )
        }

        if (showCategoryDialog) {
            var isIncomeTab by remember { mutableStateOf(false) }
            var newCatName by remember { mutableStateOf("") }
            var activeIncomeCats by remember { mutableStateOf(currentProfile.incomeCats.split(",").filter { it.isNotBlank() }) }
            var activeExpenseCats by remember { mutableStateOf(currentProfile.expenseCats.split(",").filter { it.isNotBlank() }) }

            var selectedIconKey by remember { mutableStateOf("Kategori") }
            var editingCatOldName by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = {
                    showCategoryDialog = false
                    onForceUpdate()
                },
                title = { Text(AppStr.manageCat, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .glassCard(12.dp, AppSurfaceVariant(), useHaze = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (!isIncomeTab) AppRed() else Color.Transparent)
                                    .clickable {
                                        isIncomeTab = false
                                        editingCatOldName = null
                                        newCatName = ""
                                        selectedIconKey = "Kategori"
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                contentAlignment = Alignment.Center
                            ) { Text(AppStr.exp, color = if (!isIncomeTab) Color.White else AppText(), fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isIncomeTab) AppGreen() else Color.Transparent)
                                    .clickable {
                                        isIncomeTab = true
                                        editingCatOldName = null
                                        newCatName = ""
                                        selectedIconKey = "Kategori"
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                contentAlignment = Alignment.Center
                            ) { Text(AppStr.inc, color = if (isIncomeTab) Color.White else AppText(), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        val currentList = if (isIncomeTab) activeIncomeCats else activeExpenseCats

                        LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                            items(currentList) { cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("â€¢ $cat", color = AppText(), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))

                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingCatOldName = cat
                                                newCatName = cat
                                                val iconJson = try { JSONObject(currentProfile.categoryIcons) } catch (e: Exception) { JSONObject() }
                                                selectedIconKey = iconJson.optString(cat, "Kategori")
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) { Icon(Icons.Default.Edit, null, tint = AppPrimary()) }

                                        if (currentList.size > 1) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = {
                                                    if (isIncomeTab) {
                                                        val newList = activeIncomeCats.filter { it != cat }
                                                        activeIncomeCats = newList
                                                        scope.launch { dao.saveProfile(currentProfile.copy(incomeCats = newList.joinToString(","))) }
                                                    } else {
                                                        val newList = activeExpenseCats.filter { it != cat }
                                                        activeExpenseCats = newList
                                                        scope.launch { dao.saveProfile(currentProfile.copy(expenseCats = newList.joinToString(","))) }
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) { Icon(Icons.Default.Close, null, tint = AppRed()) }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (editingCatOldName != null) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                                    Text("Editing: $editingCatOldName", fontSize = 10.sp, color = AppPrimary(), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cancel", fontSize = 10.sp, color = AppRed(), fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                                        editingCatOldName = null
                                        newCatName = ""
                                        selectedIconKey = "Kategori"
                                    })
                                }
                            }

                            OutlinedTextField(
                                value = newCatName,
                                onValueChange = { newCatName = it },
                                label = { Text(AppStr.catName) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(AppStr.chooseCatIcon, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppText())
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                modifier = Modifier.height(120.dp).glassCard(8.dp, AppSurfaceVariant(), useHaze = false)
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

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (newCatName.isNotBlank()) {
                                        val newCat = newCatName.trim()
                                        val iconJson = try { JSONObject(currentProfile.categoryIcons) } catch (e: Exception) { JSONObject() }

                                        if (editingCatOldName != null && editingCatOldName != newCat) {
                                            iconJson.remove(editingCatOldName)
                                        }
                                        iconJson.put(newCat, selectedIconKey)

                                        if (isIncomeTab) {
                                            val newList = activeIncomeCats.toMutableList()
                                            if (editingCatOldName != null) {
                                                val idx = newList.indexOf(editingCatOldName)
                                                if (idx != -1) newList[idx] = newCat
                                            } else if (!newList.contains(newCat)) {
                                                newList.add(newCat)
                                            }
                                            activeIncomeCats = newList
                                            scope.launch { dao.saveProfile(currentProfile.copy(incomeCats = newList.joinToString(","), categoryIcons = iconJson.toString())) }
                                        } else {
                                            val newList = activeExpenseCats.toMutableList()
                                            if (editingCatOldName != null) {
                                                val idx = newList.indexOf(editingCatOldName)
                                                if (idx != -1) newList[idx] = newCat
                                            } else if (!newList.contains(newCat)) {
                                                newList.add(newCat)
                                            }
                                            activeExpenseCats = newList
                                            scope.launch { dao.saveProfile(currentProfile.copy(expenseCats = newList.joinToString(","), categoryIcons = iconJson.toString())) }
                                        }
                                        newCatName = ""
                                        selectedIconKey = "Kategori"
                                        editingCatOldName = null
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(45.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text(if (editingCatOldName != null) AppStr.save else AppStr.addCat) }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCategoryDialog = false
                            onForceUpdate()
                        }
                    ) { Text(AppStr.close) }
                }
            )
        }

        if (showEditProfileDialog) {
            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                title = { Text(AppStr.editProf, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(AppStr.usr) },
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                dao.saveProfile(currentProfile.copy(userName = newName))
                                onForceUpdate()
                                showEditProfileDialog = false
                            }
                        }
                    ) { Text(AppStr.save) }
                }
            )
        }

        if (showCurrencyDialog) {
            AlertDialog(
                onDismissRequest = { showCurrencyDialog = false },
                title = { Text(AppStr.selCur) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        listOf("IDR", "USD", "EUR", "JPY", "GBP", "AUD", "CAD", "CHF", "CNY", "SGD").forEach { c ->
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        dao.saveProfile(currentProfile.copy(currency = c))
                                        onForceUpdate()
                                        showCurrencyDialog = false
                                    }
                                }
                            ) { Text(c) }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        if (showTargetDialog) {
            AlertDialog(
                onDismissRequest = { showTargetDialog = false },
                title = { Text(AppStr.setTar) },
                text = {
                    OutlinedTextField(
                        value = targetInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) targetInput = it },
                        label = { Text(AppStr.limExp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                dao.saveProfile(currentProfile.copy(monthlyTarget = targetInput.toLongOrNull() ?: 0L))
                                onForceUpdate()
                                showTargetDialog = false
                            }
                        }
                    ) { Text(AppStr.btnSet) }
                }
            )
        }

        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text(AppStr.theme) },
                text = {
                    Column {
                        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
                        val isJune = currentMonth == java.util.Calendar.JUNE
                        val hasPride = currentProfile.userName.contains("#pride", ignoreCase = true)
                        val hasBear = currentProfile.userName.contains("#bear", ignoreCase = true)

                        val themeOptions = mutableListOf(
                            0 to AppStr.themeSys,
                            1 to AppStr.themeLight,
                            2 to AppStr.themeDark
                        )

                        if (isJune) {
                            if (hasPride) {
                                themeOptions.add(3 to "Pride Light \uD83C\uDFF3\uFE0F\u200D\uD83C\uDF08")
                                themeOptions.add(4 to "Pride Dark \uD83C\uDFF3\uFE0F\u200D\uD83C\uDF08")
                            }
                            if (hasBear) {
                                themeOptions.add(5 to "Bear Light \uD83D\uDC3B")
                                themeOptions.add(6 to "Bear Dark \uD83D\uDC3B")
                            }
                        }

                        themeOptions.forEach { (value, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            dao.saveProfile(currentProfile.copy(themeMode = value))
                                            onForceUpdate()
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = currentProfile.themeMode == value, onClick = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(label, color = AppText(), fontWeight = if (value > 2) FontWeight.ExtraBold else FontWeight.Normal)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = AppSurfaceVariant()
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)) {
                                Text(AppStr.amoledDark, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppText())
                                Text(AppStr.amoledDesc, fontSize = 10.sp, color = AppText().copy(alpha=0.6f))
                            }
                            Switch(
                                checked = currentProfile.isAmoledMode,
                                onCheckedChange = { isChecked ->
                                    scope.launch {
                                        dao.saveProfile(currentProfile.copy(isAmoledMode = isChecked))
                                        onForceUpdate()
                                    }
                                },
                                modifier = Modifier.scale(0.8f)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)) {
                                Text(AppStr.liquidGlass, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppText())
                                Text(AppStr.liquidGlassDesc, fontSize = 10.sp, color = AppText().copy(alpha=0.6f))
                            }
                            Switch(
                                checked = currentProfile.isLiquidGlass,
                                onCheckedChange = { isChecked ->
                                    scope.launch {
                                        dao.saveProfile(currentProfile.copy(isLiquidGlass = isChecked))
                                        onForceUpdate()
                                    }
                                },
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) { Text(AppStr.close) }
                }
            )
        }

        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false; pinInput = "" },
                title = { Text(if(isTurningOn) AppStr.setPin else AppStr.confPin) },
                text = {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                confirmButton = {
                    Button(
                        enabled = pinInput.length == 6,
                        onClick = {
                            when {
                                isTurningOn -> {
                                    scope.launch {
                                        dao.saveProfile(currentProfile.copy(isAppLocked = true, appPin = pinInput))
                                        showPinDialog = false
                                        pinInput = ""
                                        Toast.makeText(context, AppStr.pinAct, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                pinInput == currentProfile.appPin -> {
                                    scope.launch {
                                        dao.saveProfile(currentProfile.copy(isAppLocked = false))
                                        showPinDialog = false
                                        pinInput = ""
                                        Toast.makeText(context, AppStr.pinDeact, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                else -> {
                                    Toast.makeText(context, AppStr.wrongPin, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) { Text("OK") }
                }
            )
        }

        if (showVersionDialog) {
            AlertDialog(
                onDismissRequest = { showVersionDialog = false },
                title = { Text(AppStr.info, fontWeight = FontWeight.Bold) },
                text = { Text(AppStr.versionInfo) },
                confirmButton = { TextButton(onClick = { showVersionDialog = false }) { Text(AppStr.close) } },
                shape = RoundedCornerShape(24.dp),
                containerColor = AppBg()
            )
        }

        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = { Text(AppStr.priv, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(AppStr.privDesc)
                    }
                },
                confirmButton = { TextButton(onClick = { showPrivacyDialog = false }) { Text(AppStr.gotIt) } },
                shape = RoundedCornerShape(24.dp),
                containerColor = AppBg()
            )
        }

        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                title = { Text(AppStr.trms, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(AppStr.termDesc)
                    }
                },
                confirmButton = { TextButton(onClick = { showTermsDialog = false }) { Text(AppStr.agree) } },
                shape = RoundedCornerShape(24.dp),
                containerColor = AppBg()
            )
        }

        Spacer(modifier = Modifier.height(50.dp))

        val easterEggEmoji = when (currentProfile.themeMode) {
            3, 4 -> " \uD83C\uDFF3\uFE0F\u200D\uD83C\uDF08"
            5, 6 -> " \uD83D\uDC3B"
            else -> ""
        }

        Text(
            text = "KumaFlow ${AppStr.VERSION}$easterEggEmoji\nLocal Data Only â€¢ Privacy First",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Center,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AppText().copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(100.dp))
    }
}




