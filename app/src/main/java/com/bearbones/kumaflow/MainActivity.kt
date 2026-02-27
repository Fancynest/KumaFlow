@file:Suppress("SpellCheckingInspection", "UNUSED_PARAMETER", "unused", "CanBeVal", "DEPRECATION")

package com.bearbones.kumaflow

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

// --- 0. LANGUAGE TRANSLATION ENGINE ---
object AppStr {
    val isId get() = Locale.getDefault().language == "in" || Locale.getDefault().language == "id"
    val appLocked get() = if(isId) "Aplikasi Terkunci" else "App Locked"
    val secKuma get() = if(isId) "Keamanan KumaFlow" else "KumaFlow Security"
    val scanBio get() = if(isId) "Pindai sidik jari/wajah Anda" else "Scan your fingerprint or face"
    val usePin get() = if(isId) "Gunakan PIN" else "Use PIN"
    val wrongPin get() = if(isId) "PIN yang dimasukkan salah!" else "Incorrect PIN!"
    val curBal get() = if(isId) "Saldo Saat Ini" else "Current Balance"
    val inc get() = if(isId) "Pemasukan" else "Income"
    val exp get() = if(isId) "Pengeluaran" else "Expenses"
    val recTx get() = if(isId) "Transaksi Terakhir (7 Hari)" else "Recent Transactions (7 Days)"
    val noTx get() = if(isId) "Belum ada transaksi minggu ini." else "No transactions this week."
    val rep get() = if(isId) "Laporan" else "Report"
    val sum get() = if(isId) "Ringkasan" else "Summary"
    val net get() = if(isId) "Tabungan Bersih" else "Net Savings"
    val targetProg get() = if(isId) "Progres Target Bulanan" else "Monthly Target Progress"
    val spendBreak get() = if(isId) "Rincian Pengeluaran" else "Spending Breakdown"
    val noData get() = if(isId) "Tidak Ada Data" else "No Data"
    val trends get() = if(isId) "Tren Bulanan" else "Monthly Trends"
    val noTrendData get() = if(isId) "Belum ada data bulanan" else "No monthly data yet"
    val set get() = if(isId) "Pengaturan" else "Settings"
    val accSec get() = if(isId) "Akun & Keamanan" else "Account & Security"
    val editProf get() = if(isId) "Edit Profil" else "Edit Profile"
    val appLck get() = if(isId) "Kunci Aplikasi" else "App Lock"
    val finPref get() = if(isId) "Preferensi Keuangan" else "Financial Preference"
    val cur get() = if(isId) "Mata Uang" else "Currency"
    val date get() = if(isId) "Tanggal" else "Date"
    val tar get() = "Target"
    val dat get() = "Data"
    val expPdf get() = if(isId) "Ekspor ke PDF" else "Export to PDF"
    val expCsv get() = if(isId) "Ekspor ke CSV" else "Export to CSV"
    val expDrive get() = if(isId) "Ekspor ke Drive" else "Export to Drive"
    val backApp get() = if(isId) "Cadangkan Aplikasi" else "Backup App"
    val rest get() = if(isId) "Pulihkan Data" else "Restore"
    val abt get() = if(isId) "Tentang" else "About"
    val appVer get() = if(isId) "Versi Aplikasi" else "App Version"
    val priv get() = if(isId) "Kebijakan Privasi" else "Privacy Policy"
    val trms get() = if(isId) "Syarat & Ketentuan" else "Terms"
    val contDev get() = if(isId) "Hubungi Pengembang" else "Contact Developer"
    val save get() = if(isId) "Simpan" else "Save"
    val usr get() = if(isId) "Nama Pengguna" else "Username"
    val selCur get() = if(isId) "Pilih Mata Uang" else "Select Currency"
    val setTar get() = if(isId) "Set Target Bulanan" else "Set Monthly Target"
    val limExp get() = if(isId) "Limit Pengeluaran" else "Expense Limit"
    val btnSet get() = if(isId) "Atur" else "Set"
    val selDat get() = if(isId) "Pilih Format Tanggal" else "Select Date Format"
    val setPin get() = if(isId) "Atur PIN 6 Digit" else "Set 6-Digit PIN"
    val confPin get() = if(isId) "Konfirmasi PIN" else "Confirm PIN"
    val pinAct get() = if(isId) "Sistem Keamanan Aktif!" else "Security System Activated!"
    val pinDeact get() = if(isId) "Sistem Keamanan Dinonaktifkan!" else "Security System Disabled!"
    val info get() = if(isId) "Informasi KumaFlow" else "KumaFlow Info"
    val close get() = if(isId) "Tutup" else "Close"
    val privDesc get() = if(isId) "Semua data transaksi dan profil disimpan secara lokal di perangkat Anda. KumaFlow tidak membagikan data Anda ke server eksternal, sehingga privasi Anda 100% aman." else "All data is stored locally on your device. KumaFlow does not share your data to external servers, ensuring 100% privacy."
    val gotIt get() = if(isId) "Mengerti" else "Got it"
    val termDesc get() = if(isId) "1. Penggunaan aplikasi sepenuhnya tanggung jawab pengguna.\n\n2. Karena data disimpan secara lokal (offline), kehilangan perangkat berarti kehilangan data kecuali Anda rutin melakukan pencadangan.\n\n3. Pengembang tidak bertanggung jawab atas kerugian finansial pengguna." else "1. Use of this app is strictly the user's responsibility.\n\n2. Data is stored offline. Losing your device means losing your data unless backed up regularly.\n\n3. Developers are not liable for any financial discrepancies."
    val agree get() = if(isId) "Setuju" else "Agree"
    val home get() = if(isId) "Beranda" else "Home"
    val addTx get() = if(isId) "Tambah Transaksi" else "Add New Transaction"
    val cat get() = if(isId) "Kategori" else "Category"
    val nme get() = if(isId) "Nama" else "Name"
    val msgInp get() = if(isId) "Pesan / Catatan" else "Message / Notes"
    val amt get() = if(isId) "Jumlah" else "Amount"
    val saveTx get() = if(isId) "Simpan Transaksi" else "Save Transaction"
    val txSaved get() = if(isId) "Transaksi Disimpan!" else "Transaction Saved!"
    val resOk get() = if(isId) "Restore Data Berhasil!" else "Data Restored Successfully!"
    val resFail get() = if(isId) "Gagal Restore: File korup/salah format" else "Restore Failed: Corrupted/invalid file"
    val repPdf get() = if(isId) "LAPORAN TRANSAKSI" else "TRANSACTION REPORT"
    val sharePdf get() = if(isId) "Bagikan PDF" else "Share PDF"
    val failPdf get() = if(isId) "Gagal membuat PDF" else "Failed to generate PDF"
    val type get() = if(isId) "Tipe" else "Type"
    val shareCsv get() = if(isId) "Bagikan CSV" else "Share CSV"
    val failCsv get() = if(isId) "Gagal membuat CSV" else "Failed to generate CSV"
    val noDrive get() = if(isId) "Google Drive tidak ditemukan, membuka bagikan standar..." else "Google Drive not found, opening standard share..."
    val saveBak get() = if(isId) "Simpan File Backup (.kuma)" else "Save Backup File (.kuma)"
    val failBak get() = if(isId) "Gagal membackup aplikasi" else "Failed to backup app"
    val noFileMgr get() = if(isId) "Aplikasi File Manager tidak ditemukan" else "File Manager app not found"
}

// --- 0. THEME ENGINE ---
val LocalIsDark = compositionLocalOf { true }
@Composable fun AppBg() = MaterialTheme.colorScheme.background
@Composable fun AppSurface() = MaterialTheme.colorScheme.surface
@Composable fun AppText() = MaterialTheme.colorScheme.onSurface
@Composable fun AppPrimary() = MaterialTheme.colorScheme.primary
@Composable fun AppSurfaceVariant() = if (LocalIsDark.current) Color(0xFF333333) else Color.White.copy(alpha = 0.5f)
@Composable fun AppGreen() = if (LocalIsDark.current) Color(0xFF66BB6A) else Color(0xFF1B5E20)
@Composable fun AppRed() = if (LocalIsDark.current) Color(0xFFEF5350) else Color(0xFFB71C1C)

// --- 1. ROOM DATABASE ENGINE ---
@Entity(tableName = "transactions")
data class KumaTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, val date: String, val amount: String,
    val isIncome: Boolean, val category: String, val timestamp: String,
    val message: String = ""
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 0,
    val userName: String,
    val isAppLocked: Boolean = false,
    val appPin: String = "",
    val currency: String = "IDR",
    val dateFormat: String = "dd MMM yyyy",
    val monthlyTarget: Long = 0L,
    val isDarkMode: Boolean = true
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<KumaTransaction>>

    @Insert
    suspend fun insertTransaction(transaction: KumaTransaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<KumaTransaction>)

    @Query("SELECT * FROM user_profile WHERE id = 0")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}

@Database(entities = [KumaTransaction::class, UserProfile::class], version = 6, exportSchema = false)
abstract class KumaDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    companion object {
        @Volatile private var INSTANCE: KumaDatabase? = null
        fun getDatabase(context: Context): KumaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, KumaDatabase::class.java, "kuma_database")
                    .fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- 2. BIOMETRIC LOGIC ---
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
        .setNegativeButtonText(AppStr.usePin)
        .build()
    biometricPrompt.authenticate(promptInfo)
}

// --- 3. LOCK SCREEN UI COMPONENT ---
@Composable
fun LockScreen(correctPin: String, activity: FragmentActivity, onSuccess: () -> Unit) {
    var inputPin by remember { mutableStateOf("") }
    val context = LocalContext.current
    LaunchedEffect(Unit) { showBiometricPrompt(activity, onSuccess, {}) }

    Column(modifier = Modifier.fillMaxSize().background(AppBg()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = AppText())
        Spacer(modifier = Modifier.height(16.dp))
        Text(AppStr.appLocked, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppText())
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(6) { index -> Box(modifier = Modifier.size(16.dp).background(if (index < inputPin.length) AppText() else AppSurfaceVariant(), CircleShape)) }
        }
        Spacer(modifier = Modifier.height(48.dp))
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "Finger", "0", "Del")
        keys.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                row.forEach { label ->
                    Box(modifier = Modifier.size(70.dp).clip(CircleShape).background(AppSurface()).clickable {
                        when (label) {
                            "Del" -> if (inputPin.isNotEmpty()) inputPin = inputPin.dropLast(1)
                            "Finger" -> showBiometricPrompt(activity, onSuccess, {})
                            else -> {
                                if (inputPin.length < 6) {
                                    inputPin += label
                                }
                                if (inputPin.length == 6) {
                                    if (inputPin == correctPin) onSuccess()
                                    else {
                                        inputPin = ""
                                        Toast.makeText(context, AppStr.wrongPin, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }, contentAlignment = Alignment.Center) {
                        when (label) {
                            "Finger" -> Icon(Icons.Default.Fingerprint, contentDescription = null, tint = AppText())
                            "Del" -> Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null, tint = AppText())
                            else -> Text(label, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppText())
                        }
                    }
                }
            }
        }
    }
}

// --- 4. MAIN ACTIVITY (DILENGKAPI NATIVE FILE PICKER UNTUK BYPASS BUG 16-BIT) ---
class MainActivity : FragmentActivity() {

    // Variabel penampung string JSON yang dibaca langsung secara sinkron
    var pendingRestoreJson: String? = null

    // Override fungsi jadul Android buat nangkap hasil File Manager dengan aman
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 12345 adalah angka aman, jauh di bawah batas 65535 (16-bit)
        if (requestCode == 12345 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                try {
                    // FIX: Baca isi file SECARA SINKRON di Main Thread sebelum URI Expired
                    contentResolver.openInputStream(uri)?.use { stream ->
                        pendingRestoreJson = stream.bufferedReader().readText()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal baca file: ${e.message}", Toast.LENGTH_LONG).show()
                    pendingRestoreJson = null
                }
            } else {
                pendingRestoreJson = null
            }
        }
    }

    // Fungsi trigger File Manager murni (bukan lewat Jetpack Compose)
    fun openSafeFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        try {
            startActivityForResult(intent, 12345)
        } catch (e: Exception) {
            Toast.makeText(this, AppStr.noFileMgr, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val db = remember { KumaDatabase.getDatabase(context) }
            val dao = db.transactionDao()
            val userProfile by dao.getUserProfile().collectAsState(initial = null)
            var isAuthenticated by remember { mutableStateOf(false) }

            val isDark = userProfile?.isDarkMode ?: true

            CompositionLocalProvider(LocalIsDark provides isDark) {
                val colorScheme = if (isDark) darkColorScheme(
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    onBackground = Color(0xFFE0E0E0),
                    onSurface = Color(0xFFE0E0E0),
                    primary = Color(0xFFD5641C),
                    onPrimary = Color.White
                ) else lightColorScheme(
                    background = Color(0xFFD9D2C5),
                    surface = Color(0xFFC7BCAC),
                    onBackground = Color(0xFF4A2F1D),
                    onSurface = Color(0xFF4A2F1D),
                    primary = Color(0xFF4A2F1D),
                    onPrimary = Color.White
                )

                MaterialTheme(colorScheme = colorScheme) {
                    if (userProfile?.isAppLocked == true && !isAuthenticated) {
                        LockScreen(userProfile?.appPin ?: "", this) { isAuthenticated = true }
                    } else {
                        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                            MainScreen(userProfile, dao)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(userProfileState: UserProfile?, dao: TransactionDao) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val transactionList by dao.getAllTransactions().collectAsState(initial = emptyList())
    val userProfile = userProfileState ?: UserProfile(userName = "User")
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    val totalIncome by remember { derivedStateOf { transactionList.filter { it.isIncome }.sumOf { it.amount.toLongOrNull() ?: 0L } } }
    val totalExpenses by remember { derivedStateOf { transactionList.filter { !it.isIncome }.sumOf { it.amount.toLongOrNull() ?: 0L } } }
    val totalBalance by remember { derivedStateOf { totalIncome - totalExpenses } }
    val recentTransactions by remember { derivedStateOf {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val oneWeekAgo = LocalDateTime.now().minusDays(7)
        transactionList.filter { try { LocalDateTime.parse(it.timestamp, formatter).isAfter(oneWeekAgo) } catch (_: Exception) { true } }
    } }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppBg(),
        floatingActionButton = {
            if (selectedItemIndex != 2) {
                FloatingActionButton(onClick = { showBottomSheet = true }, containerColor = AppPrimary(), contentColor = Color.White, shape = CircleShape, modifier = Modifier.size(70.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(40.dp))
                }
            }
        },
        bottomBar = { CustomBottomNav(selectedItemIndex) { selectedItemIndex = it } }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedItemIndex) {
                0 -> HomeScreen(userProfile, recentTransactions, totalBalance, totalIncome, totalExpenses) {
                    scope.launch { dao.saveProfile(userProfile.copy(isDarkMode = !userProfile.isDarkMode)) }
                }
                1 -> ReportScreen(userProfile, transactionList, totalIncome, totalExpenses, totalBalance)
                2 -> SettingsScreen(userProfile, transactionList, dao)
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState, containerColor = AppBg()) {
                TransactionBottomSheet(onDismiss = { showBottomSheet = false }, onSave = { newTrans ->
                    scope.launch {
                        dao.insertTransaction(newTrans)
                        Toast.makeText(context, AppStr.txSaved, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionBottomSheet(onDismiss: () -> Unit, onSave: (KumaTransaction) -> Unit) {
    var isIncome by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Food") }
    var name by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Financial", "Food", "Shopping", "Health", "Transport", "Education", "Entertainment", "Others")

    Column(modifier = Modifier.fillMaxWidth().imePadding().padding(horizontal = 24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(AppStr.addTx, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = AppText())
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(16.dp)).background(AppSurfaceVariant())) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(if (isIncome) AppGreen() else Color.Transparent).clickable { isIncome = true }, contentAlignment = Alignment.Center) { Text(AppStr.inc, color = if (isIncome) Color.White else AppText(), fontWeight = FontWeight.Bold) }
            Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(if (!isIncome) AppRed() else Color.Transparent).clickable { isIncome = false }, contentAlignment = Alignment.Center) { Text(AppStr.exp, color = if (!isIncome) Color.White else AppText(), fontWeight = FontWeight.Bold) }
        }
        Spacer(modifier = Modifier.height(20.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = selectedCategory, onValueChange = {}, readOnly = true, label = { Text(AppStr.cat) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            selectedCategory = cat
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(AppStr.nme) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text(AppStr.msgInp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = amount, onValueChange = { if (it.all { c -> c.isDigit() }) amount = it }, label = { Text(AppStr.amt) }, visualTransformation = ThousandSeparatorTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(30.dp))
        Button(onClick = {
            val now = LocalDateTime.now()
            onSave(KumaTransaction(name = name, date = now.format(DateTimeFormatter.ofPattern(if(AppStr.isId) "dd MMM yyyy" else "MMM dd, yyyy", Locale.getDefault())), amount = amount, isIncome = isIncome, category = selectedCategory, timestamp = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), message = message))
            onDismiss()
        }, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = AppPrimary()), shape = RoundedCornerShape(16.dp), enabled = amount.isNotEmpty() && name.isNotEmpty()) {
            Text(AppStr.saveTx, color = Color.White, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun HomeScreen(profile: UserProfile, transactions: List<KumaTransaction>, balance: Long, income: Long, expenses: Long, onToggleMode: () -> Unit) {
    val locale = Locale.forLanguageTag("id-ID")
    val curSym = when(profile.currency) { "USD", "AUD", "CAD", "SGD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; "JPY", "CNY" -> "¥"; "CHF" -> "CHF"; else -> "Rp" }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(if (AppStr.isId) "Halo, ${profile.userName}!" else "Hello, ${profile.userName}!", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = AppText())
            IconButton(onClick = onToggleMode) {
                Icon(imageVector = if (LocalIsDark.current) Icons.Default.WbSunny else Icons.Default.Brightness3, contentDescription = null, tint = AppText(), modifier = Modifier.size(32.dp))
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Card(modifier = Modifier.fillMaxWidth().height(250.dp), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = AppSurface())) {
            Column(modifier = Modifier.padding(32.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
                Text(AppStr.curBal, color = AppText(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(16.dp))
                val balPref = if (balance < 0) "- " else ""

                // FIX: Pakai AutoSizeText buat Balance
                AutoSizeText(
                    text = "$balPref$curSym ${NumberFormat.getInstance(locale).format(abs(balance))}",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    minimumFallbackSize = 24.sp
                )

                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = AppGreen(), modifier = Modifier.size(20.dp))

                    // FIX: Pakai AutoSizeText buat Income
                    AutoSizeText(
                        text = "${AppStr.inc} $curSym ${NumberFormat.getInstance(locale).format(income)}",
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        minimumFallbackSize = 8.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = AppRed(), modifier = Modifier.size(20.dp))

                    // FIX: Pakai AutoSizeText buat Expenses
                    AutoSizeText(
                        text = "${AppStr.exp} $curSym ${NumberFormat.getInstance(locale).format(expenses)}",
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        minimumFallbackSize = 8.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(AppStr.recTx, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = AppText())
        Spacer(modifier = Modifier.height(16.dp))
        if (transactions.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(AppStr.noTx, textAlign = TextAlign.Center, color = AppText().copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
            }
        }
        else { LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) { items(transactions) { TransactionItem(profile, it) } } }
    }
}

@Composable
fun ReportScreen(profile: UserProfile, allTransactions: List<KumaTransaction>, income: Long, expenses: Long, balance: Long) {
    val locale = Locale.forLanguageTag("id-ID")
    val curSym = when(profile.currency) { "USD", "AUD", "CAD", "SGD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; "JPY", "CNY" -> "¥"; "CHF" -> "CHF"; else -> "Rp" }
    val categoryColors = mapOf("Financial" to Color(0xFF623802), "Food" to Color(0xFFD5641C), "Shopping" to Color(0xFFFEDD60), "Health" to Color(0xFFFEE6B1), "Transport" to Color(0xFFFFFFFF), "Education" to Color(0xFF929292), "Entertainment" to Color(0xFF000000), "Others" to Color(0xFF006064))
    val expensePerCat = allTransactions.filter { !it.isIncome }.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount.toLongOrNull() ?: 0L } }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 24.dp).verticalScroll(rememberScrollState())) {
        Text(AppStr.rep, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = AppText())
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth().height(185.dp), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = AppSurface())) {
            Row(modifier = Modifier.padding(24.dp).fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(AppStr.sum, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = AppText())
                    Spacer(modifier = Modifier.height(12.dp)); Text(AppStr.net, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    val balPref = if (balance < 0) "- " else "+";

                    // FIX: Pakai AutoSizeText buat Net Savings
                    AutoSizeText(
                        text = "$curSym $balPref${NumberFormat.getInstance(locale).format(abs(balance))}",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        minimumFallbackSize = 14.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                    IncomeExpensePill(AppStr.inc, "$curSym ${NumberFormat.getInstance(locale).format(income)}", AppGreen(), true)
                    Spacer(modifier = Modifier.height(12.dp)); IncomeExpensePill(AppStr.exp, "$curSym ${NumberFormat.getInstance(locale).format(expenses)}", AppRed(), false)
                }
            }
        }

        if (profile.monthlyTarget > 0) {
            Spacer(modifier = Modifier.height(20.dp))
            val progress = (expenses.toFloat() / profile.monthlyTarget.toFloat()).coerceIn(0f, 1f)
            val isOver = expenses > profile.monthlyTarget
            Text(AppStr.targetProg, fontWeight = FontWeight.Bold, color = AppText())
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = if(isOver) AppRed() else AppGreen(),
                trackColor = AppSurfaceVariant()
            )
            Text("${(progress * 100).toInt()}% " + (if(AppStr.isId) "dari" else "of") + " $curSym ${NumberFormat.getInstance(locale).format(profile.monthlyTarget)}", fontSize = 12.sp, color = if(isOver) AppRed() else Color.Gray)
        }

        Spacer(modifier = Modifier.height(30.dp))
        Text(AppStr.spendBreak, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AppText())
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth().height(220.dp), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = AppSurface())) {
            Row(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    val bgArcCol = AppSurfaceVariant()
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (expenses == 0L) { drawArc(bgArcCol, -90f, 360f, false, style = Stroke(25.dp.toPx())) }
                        else { var start = -90f; expensePerCat.forEach { (cat, amt) -> val sweep = (amt.toFloat() / expenses.toFloat()) * 360f; drawArc(categoryColors[cat] ?: Color.Gray, start, sweep, false, style = Stroke(25.dp.toPx())); start += sweep } }
                    }
                    if (expenses == 0L) Text(AppStr.noData, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppText())
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    categoryColors.forEach { (label, color) ->
                        val catAmt = expensePerCat[label] ?: 0L
                        ReportLegendItem(label, color, NumberFormat.getInstance(locale).format(catAmt))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp)); Text(AppStr.trends, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AppText())
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth().height(280.dp), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = AppSurface())) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                val greenCol = AppGreen()
                val redCol = AppRed()
                val variantCol = AppSurfaceVariant()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { LegendItem(AppStr.inc, greenCol); Spacer(modifier = Modifier.width(16.dp)); LegendItem(AppStr.exp, redCol) }

                val incomeData = FloatArray(5) { 0f }
                val expenseData = FloatArray(5) { 0f }
                allTransactions.forEach { t ->
                    try {
                        val dt = LocalDateTime.parse(t.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        val m = dt.monthValue
                        if (m in 1..5) {
                            val amt = t.amount.toFloatOrNull() ?: 0f
                            if (t.isIncome) incomeData[m-1] += amt else expenseData[m-1] += amt
                        }
                    } catch (_: Exception) {}
                }
                val maxVal = maxOf(incomeData.maxOrNull() ?: 0f, expenseData.maxOrNull() ?: 0f).coerceAtLeast(1f)
                val incPoints = incomeData.map { it / maxVal }
                val expPoints = expenseData.map { it / maxVal }
                val hasData = incomeData.sum() > 0f || expenseData.sum() > 0f

                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        for (i in 0..5) { val y = size.height - (i * size.height / 5); drawLine(variantCol, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 1.dp.toPx()) }
                        if (hasData) {
                            drawTrendsLine(incPoints, greenCol)
                            drawTrendsLine(expPoints, redCol)
                        }
                    }
                    if (!hasData) { Text(AppStr.noTrendData, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppText().copy(alpha = 0.5f)) }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) { listOf("Jan", "Feb", "Mar", "Apr", "May").forEach { Text(it, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppText()) } }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTrendsLine(points: List<Float>, color: Color) {
    val path = Path().apply { points.forEachIndexed { index, value -> val x = index * size.width / (points.size - 1); val y = size.height - (value * size.height); if (index == 0) moveTo(x, y) else lineTo(x, y) } }
    drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
}

@Composable
fun SettingsScreen(currentProfile: UserProfile, transactionList: List<KumaTransaction>, dao: TransactionDao) {
    val context = LocalContext.current
    val mainActivity = context as? MainActivity
    val scope = rememberCoroutineScope()

    var showVersionDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }

    var pinInput by remember { mutableStateOf("") }
    var targetInput by remember { mutableStateOf(currentProfile.monthlyTarget.toString()) }
    var isTurningOn by remember { mutableStateOf(true) }
    var newName by remember { mutableStateOf(currentProfile.userName) }

    // --- FIX: Logic Restore di-trigger manual ---
    // Di sini kita cek apakah mainActivity punya pending data (pendingRestoreJson)
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
                        isDarkMode = pObj.optBoolean("isDarkMode", true)
                    )
                    dao.saveProfile(newProfile)

                    val txsArr = root.getJSONArray("transactions")
                    val parsedTxs = mutableListOf<KumaTransaction>()
                    for (i in 0 until txsArr.length()) {
                        val tObj = txsArr.getJSONObject(i)
                        parsedTxs.add(
                            KumaTransaction(
                                name = tObj.optString("name", ""),
                                date = tObj.optString("date", ""),
                                amount = tObj.optString("amount", "0"),
                                isIncome = tObj.optBoolean("isIncome", false),
                                category = tObj.optString("category", "Others"),
                                timestamp = tObj.optString("timestamp", ""),
                                message = tObj.optString("message", "")
                            )
                        )
                    }

                    dao.clearTransactions()
                    parsedTxs.chunked(100).forEach { chunk ->
                        dao.insertTransactions(chunk)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, AppStr.resOk, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        // INI AKAN NAMPILIN ERROR ASLINYA KALAU GAGAL
                        Toast.makeText(context, "Error Restore: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } finally {
                    // Bersihin state
                    mainActivity.pendingRestoreJson = null
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 24.dp)) {
        Text(AppStr.set, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = AppText())
        Spacer(modifier = Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsGroupCard(
                    title = AppStr.accSec,
                    modifier = Modifier.weight(1f),
                    items = listOf(AppStr.editProf to Icons.Default.Edit, AppStr.appLck to Icons.Default.Fingerprint),
                    hasSwitch = true,
                    isSwitchOn = currentProfile.isAppLocked,
                    onSwitchChange = { isTurningOn = it; showPinDialog = true }
                ) { label ->
                    if (label == AppStr.editProf) showEditProfileDialog = true
                }

                SettingsGroupCard(
                    title = AppStr.finPref,
                    modifier = Modifier.weight(1f),
                    items = listOf(AppStr.cur to Icons.Default.Sync, AppStr.date to Icons.Default.DateRange, AppStr.tar to Icons.Default.Adjust),
                    onClick = { label ->
                        when(label) {
                            AppStr.cur -> showCurrencyDialog = true
                            AppStr.date -> showDateDialog = true
                            AppStr.tar -> showTargetDialog = true
                        }
                    }
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsGroupCard(AppStr.dat, Modifier.weight(1f), listOf(AppStr.expPdf to Icons.Default.PictureAsPdf, AppStr.expCsv to Icons.Default.Description, AppStr.expDrive to Icons.Default.AddToDrive, AppStr.backApp to Icons.Default.CloudUpload, AppStr.rest to Icons.Default.History)) { label ->
                    when (label) {
                        AppStr.expPdf -> generatePDF(context, transactionList, currentProfile)
                        AppStr.expCsv -> generateCSV(context, transactionList, currentProfile)
                        AppStr.expDrive -> exportToDrive(context, transactionList, currentProfile)
                        AppStr.backApp -> backupAppToJSON(context, currentProfile, transactionList)
                        AppStr.rest -> {
                            mainActivity?.openSafeFilePicker()
                        }
                    }
                }
                SettingsGroupCard(AppStr.abt, Modifier.weight(1f), listOf(AppStr.appVer to Icons.Default.Info, AppStr.priv to Icons.Default.PrivacyTip, AppStr.trms to Icons.AutoMirrored.Filled.MenuBook, AppStr.contDev to Icons.Default.SupportAgent)) { label ->
                    when (label) {
                        AppStr.appVer -> showVersionDialog = true
                        AppStr.priv -> showPrivacyDialog = true
                        AppStr.trms -> showTermsDialog = true
                        AppStr.contDev -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/6285173220524")))
                    }
                }
            }
        }

        // --- DIALOGS ---
        if (showEditProfileDialog) {
            AlertDialog(onDismissRequest = { showEditProfileDialog = false }, title = { Text(AppStr.editProf, fontWeight = FontWeight.Bold) }, text = { OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text(AppStr.usr) }, shape = RoundedCornerShape(12.dp)) }, confirmButton = { Button(onClick = { scope.launch { dao.saveProfile(currentProfile.copy(userName = newName)); showEditProfileDialog = false } }) { Text(AppStr.save) } })
        }
        if (showCurrencyDialog) {
            AlertDialog(onDismissRequest = { showCurrencyDialog = false }, title = { Text(AppStr.selCur) }, text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) { listOf("IDR", "USD", "EUR", "JPY", "GBP", "AUD", "CAD", "CHF", "CNY", "SGD").forEach { c -> TextButton(onClick = { scope.launch { dao.saveProfile(currentProfile.copy(currency = c)); showCurrencyDialog = false } }) { Text(c) } } }
            }, confirmButton = {})
        }
        if (showTargetDialog) {
            AlertDialog(onDismissRequest = { showTargetDialog = false }, title = { Text(AppStr.setTar) }, text = { OutlinedTextField(value = targetInput, onValueChange = { if (it.all { c -> c.isDigit() }) targetInput = it }, label = { Text(AppStr.limExp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }, confirmButton = { Button(onClick = { scope.launch { dao.saveProfile(currentProfile.copy(monthlyTarget = targetInput.toLongOrNull() ?: 0L)); showTargetDialog = false } }) { Text(AppStr.btnSet) } })
        }
        if (showDateDialog) {
            AlertDialog(onDismissRequest = { showDateDialog = false }, title = { Text(AppStr.selDat) }, text = {
                Column { listOf("dd MMM yyyy", "dd/MM/yyyy", "yyyy-MM-dd").forEach { f -> TextButton(onClick = { scope.launch { dao.saveProfile(currentProfile.copy(dateFormat = f)); showDateDialog = false } }) { Text(f) } } }
            }, confirmButton = {})
        }
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false; pinInput = "" },
                title = { Text(if(isTurningOn) AppStr.setPin else AppStr.confPin) },
                text = { OutlinedTextField(value = pinInput, onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pinInput = it }, label = { Text("PIN") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), shape = RoundedCornerShape(12.dp)) },
                confirmButton = {
                    Button(
                        enabled = pinInput.length == 6,
                        onClick = {
                            when {
                                isTurningOn -> { scope.launch { dao.saveProfile(currentProfile.copy(isAppLocked = true, appPin = pinInput)); showPinDialog = false; pinInput = ""; Toast.makeText(context, AppStr.pinAct, Toast.LENGTH_SHORT).show() } }
                                pinInput == currentProfile.appPin -> { scope.launch { dao.saveProfile(currentProfile.copy(isAppLocked = false)); showPinDialog = false; pinInput = ""; Toast.makeText(context, AppStr.pinDeact, Toast.LENGTH_SHORT).show() } }
                                else -> { Toast.makeText(context, AppStr.wrongPin, Toast.LENGTH_SHORT).show() }
                            }
                        }
                    ) { Text("OK") }
                }
            )
        }
        if (showVersionDialog) {
            AlertDialog(onDismissRequest = { showVersionDialog = false }, title = { Text(AppStr.info, fontWeight = FontWeight.Bold) }, text = { Text("Versi: 1.0.0-beta\nBuild: 20260224\nTipe: Standalone Local") }, confirmButton = { TextButton(onClick = { showVersionDialog = false }) { Text(AppStr.close) } }, shape = RoundedCornerShape(24.dp), containerColor = AppBg())
        }
        if (showPrivacyDialog) {
            AlertDialog(onDismissRequest = { showPrivacyDialog = false }, title = { Text(AppStr.priv, fontWeight = FontWeight.Bold) }, text = { Column(modifier = Modifier.verticalScroll(rememberScrollState())) { Text(AppStr.privDesc) } }, confirmButton = { TextButton(onClick = { showPrivacyDialog = false }) { Text(AppStr.gotIt) } }, shape = RoundedCornerShape(24.dp), containerColor = AppBg())
        }
        if (showTermsDialog) {
            AlertDialog(onDismissRequest = { showTermsDialog = false }, title = { Text(AppStr.trms, fontWeight = FontWeight.Bold) }, text = { Column(modifier = Modifier.verticalScroll(rememberScrollState())) { Text(AppStr.termDesc) } }, confirmButton = { TextButton(onClick = { showTermsDialog = false }) { Text(AppStr.agree) } }, shape = RoundedCornerShape(24.dp), containerColor = AppBg())
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "KumaFlow v1.0.0-beta\nLocal Data Only • Privacy First",
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            textAlign = TextAlign.Center,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AppText().copy(alpha = 0.5f)
        )
    }
}

// --- 6. SHARED COMPONENTS ---

// FIX: Komponen pintar buat Auto-Size Text
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    maxLines: Int = 1,
    minimumFallbackSize: TextUnit = 12.sp
) {
    var scaledTextStyle by remember { mutableStateOf(androidx.compose.ui.text.TextStyle(fontSize = fontSize)) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        color = color,
        fontSize = scaledTextStyle.fontSize,
        fontWeight = fontWeight,
        maxLines = maxLines,
        softWrap = false,
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
        val formattedText = try { NumberFormat.getInstance(locale).format(originalText.toLong()) } catch (_: Exception) { originalText }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val originalBefore = originalText.substring(0, offset)
                val transformedBefore = try { NumberFormat.getInstance(locale).format(originalBefore.toLong()) } catch (_: Exception) { originalBefore }
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

fun drawHeaders(canvas: android.graphics.Canvas, paint: Paint, pageNum: Int, profile: UserProfile, titlePaint: Paint, headerPaint: Paint) {
    canvas.drawText("${AppStr.repPdf} ($pageNum)", 40f, 50f, titlePaint)
    canvas.drawText("${AppStr.cur}: ${profile.currency}", 40f, 75f, Paint().apply { textSize = 12f })
    canvas.drawLine(40f, 90f, 550f, 90f, paint)
    canvas.drawText(AppStr.date, 40f, 110f, headerPaint)
    canvas.drawText(AppStr.cat, 130f, 110f, headerPaint)
    canvas.drawText(AppStr.nme, 220f, 110f, headerPaint)
    canvas.drawText(AppStr.msgInp, 350f, 110f, headerPaint)
    canvas.drawText(AppStr.amt, 480f, 110f, headerPaint)
    canvas.drawLine(40f, 120f, 550f, 120f, paint)
}

fun generatePDF(context: Context, data: List<KumaTransaction>, profile: UserProfile) {
    val pdfDocument = PdfDocument()
    var pageNum = 1
    var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNum).create())
    val paint = Paint()
    val titlePaint = Paint().apply { isFakeBoldText = true; textSize = 18f; color = android.graphics.Color.BLACK }
    val headerPaint = Paint().apply { isFakeBoldText = true; textSize = 12f; color = android.graphics.Color.DKGRAY }
    val curSym = when(profile.currency) { "USD", "AUD", "CAD", "SGD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; "JPY", "CNY" -> "¥"; "CHF" -> "CHF"; else -> "Rp" }

    drawHeaders(page.canvas, paint, pageNum, profile, titlePaint, headerPaint)
    var yPos = 145f

    data.forEach { item ->
        if (yPos > 800f) {
            pdfDocument.finishPage(page)
            pageNum++
            page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNum).create())
            drawHeaders(page.canvas, paint, pageNum, profile, titlePaint, headerPaint)
            yPos = 145f
        }
        val amountPrefix = if (item.isIncome) "+" else "-"
        val amountColor = if (item.isIncome) android.graphics.Color.parseColor("#1B5E20") else android.graphics.Color.parseColor("#B71C1C")

        paint.color = android.graphics.Color.BLACK
        page.canvas.drawText(item.date.take(12), 40f, yPos, paint)
        page.canvas.drawText(item.category.take(12), 130f, yPos, paint)
        page.canvas.drawText(item.name.take(18), 220f, yPos, paint)
        page.canvas.drawText(item.message.take(18), 350f, yPos, paint)

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
    val lblInc = if (AppStr.isId) "TOTAL PEMASUKAN" else "TOTAL INCOME"
    val lblExp = if (AppStr.isId) "TOTAL PENGELUARAN" else "TOTAL EXPENSES"
    page.canvas.drawText("$lblInc: $curSym $inc", 40f, yPos, titlePaint.apply { textSize = 12f; color = android.graphics.Color.parseColor("#1B5E20") })
    yPos += 20f
    page.canvas.drawText("$lblExp: $curSym $exp", 40f, yPos, titlePaint.apply { textSize = 12f; color = android.graphics.Color.parseColor("#B71C1C") })

    pdfDocument.finishPage(page)
    val fileName = "KumaFlow_Report_${System.currentTimeMillis()}.pdf"
    val file = File(context.cacheDir, fileName)
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        context.startActivity(Intent.createChooser(intent, AppStr.sharePdf))
    } catch (_: Exception) { Toast.makeText(context, AppStr.failPdf, Toast.LENGTH_SHORT).show() } finally { pdfDocument.close() }
}

fun generateCSV(context: Context, data: List<KumaTransaction>, profile: UserProfile) {
    val file = File(context.cacheDir, "KumaFlow_Report_${System.currentTimeMillis()}.csv")
    try {
        file.bufferedWriter().use { out ->
            out.write("${AppStr.date},${AppStr.cat},${AppStr.type},${AppStr.nme},${AppStr.msgInp},${AppStr.cur},${AppStr.amt}\n")
            data.forEach { t ->
                val type = if (t.isIncome) AppStr.inc else AppStr.exp
                out.write("${t.date},${t.category},$type,\"${t.name}\",\"${t.message}\",${profile.currency},${t.amount}\n")
            }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/csv"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        context.startActivity(Intent.createChooser(intent, AppStr.shareCsv))
    } catch (_: Exception) { Toast.makeText(context, AppStr.failCsv, Toast.LENGTH_SHORT).show() }
}

fun exportToDrive(context: Context, data: List<KumaTransaction>, profile: UserProfile) {
    val file = File(context.cacheDir, "KumaFlow_DriveBackup_${System.currentTimeMillis()}.csv")
    try {
        file.bufferedWriter().use { out ->
            out.write("${AppStr.date},${AppStr.cat},${AppStr.type},${AppStr.nme},${AppStr.msgInp},${AppStr.cur},${AppStr.amt}\n")
            data.forEach { t ->
                val type = if (t.isIncome) AppStr.inc else AppStr.exp
                out.write("${t.date},${t.category},$type,\"${t.name}\",\"${t.message}\",${profile.currency},${t.amount}\n")
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
        generateCSV(context, data, profile)
    }
}

fun backupAppToJSON(context: Context, profile: UserProfile, txs: List<KumaTransaction>) {
    try {
        val root = JSONObject()
        val pJson = JSONObject().apply {
            put("userName", profile.userName)
            put("isAppLocked", profile.isAppLocked)
            put("appPin", profile.appPin)
            put("currency", profile.currency)
            put("dateFormat", profile.dateFormat)
            put("monthlyTarget", profile.monthlyTarget)
            put("isDarkMode", profile.isDarkMode)
        }
        root.put("profile", pJson)
        val tArr = JSONArray()
        txs.forEach { t ->
            val tJson = JSONObject().apply {
                put("name", t.name)
                put("date", t.date)
                put("amount", t.amount)
                put("isIncome", t.isIncome)
                put("category", t.category)
                put("timestamp", t.timestamp)
                put("message", t.message)
            }
            tArr.put(tJson)
        }
        root.put("transactions", tArr)

        val file = File(context.cacheDir, "KumaFlow_Backup_${System.currentTimeMillis()}.kuma")
        file.writeText(root.toString())
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply { type = "*/*"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        context.startActivity(Intent.createChooser(intent, AppStr.saveBak))
    } catch (_: Exception) { Toast.makeText(context, AppStr.failBak, Toast.LENGTH_SHORT).show() }
}

@Composable
fun SettingsGroupCard(title: String, modifier: Modifier = Modifier, items: List<Pair<String, ImageVector>>, hasSwitch: Boolean = false, isSwitchOn: Boolean = false, onSwitchChange: (Boolean) -> Unit = {}, onClick: (String) -> Unit) {
    Card(modifier = modifier.heightIn(min = 230.dp), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = AppSurface())) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = AppText(), modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(18.dp))
            items.forEach { (label, icon) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onClick(label) }, verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = AppText(), modifier = Modifier.size(20.dp))
                    Text(label, modifier = Modifier.weight(1f).padding(start = 10.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppText())
                    if (hasSwitch && label == AppStr.appLck) Switch(checked = isSwitchOn, onCheckedChange = onSwitchChange, modifier = Modifier.scale(0.6f))
                }
            }
        }
    }
}

@Composable
fun CustomBottomNav(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp).height(85.dp).clip(RoundedCornerShape(24.dp)).background(AppSurface())) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            NavItem(Icons.Default.Home, AppStr.home, selectedIndex == 0) { onItemSelected(0) }
            NavItem(Icons.Default.Equalizer, AppStr.rep, selectedIndex == 1) { onItemSelected(1) }
            NavItem(Icons.Default.Settings, AppStr.set, selectedIndex == 2) { onItemSelected(2) }
        }
    }
}

@Composable
fun NavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(icon, contentDescription = null, tint = if (isSelected) AppText() else AppText().copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) AppText() else AppText().copy(alpha = 0.5f))
    }
}

@Composable
fun IncomeExpensePill(label: String, amount: String, color: Color, isUp: Boolean) {
    Column(horizontalAlignment = Alignment.End) {
        Row(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = if (isUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, tint = color, contentDescription = null, modifier = Modifier.size(12.dp))
            Text(" $label", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        }

        // FIX: Pakai AutoSizeText buat di dalem Pill
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
fun ReportLegendItem(label: String, color: Color, amount: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)).background(AppSurfaceVariant()).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Text(" $label", modifier = Modifier.weight(1f).padding(start = 12.dp), color = AppText(), fontWeight = FontWeight.Bold)
        Text(amount, color = AppText(), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppText())
    }
}

@Composable
fun TransactionItem(profile: UserProfile, trans: KumaTransaction) {
    val curSym = when(profile.currency) {
        "USD", "AUD", "CAD", "SGD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY", "CNY" -> "¥"
        "CHF" -> "CHF"
        else -> "Rp"
    }
    val icon = when(trans.category) { "Financial" -> Icons.Default.AccountBalance; "Food" -> Icons.Default.Restaurant; "Shopping" -> Icons.Default.LocalMall; "Health" -> Icons.Default.Favorite; "Transport" -> Icons.Default.DirectionsCar; "Education" -> Icons.Default.School; "Entertainment" -> Icons.Default.Gamepad; else -> Icons.Default.Category }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFD5641C))) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(45.dp).background(Color.White.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp)) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(trans.name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                if (trans.message.isNotEmpty()) { Text(trans.message, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)) }
                Text(trans.date, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
            val formatted = try { NumberFormat.getInstance(Locale.forLanguageTag("id-ID")).format(trans.amount.toLong()) } catch (_: Exception) { trans.amount }

            // FIX: Buat item list juga dibikin AutoSize biar aman kalau ada yang nabung 1 Triliun
            AutoSizeText(
                text = "${if (trans.isIncome) "+ " else "- "} $curSym $formatted",
                color = if (trans.isIncome) Color.White else AppText(),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(max = 120.dp),
                minimumFallbackSize = 10.sp
            )
        }
    }
}