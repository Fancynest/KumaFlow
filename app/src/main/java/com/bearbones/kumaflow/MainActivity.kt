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
import com.bearbones.kumaflow.NewUserAnnouncementDialog

// --- DATA CLASSES & OBJECTS ---

object AppStr {
    val isId get() = Locale.getDefault().language == "in" || Locale.getDefault().language == "id"
    val appLocked get() = if(isId) "Aplikasi Terkunci" else "App Locked"
    val secKuma get() = if(isId) "Keamanan KumaFlow" else "KumaFlow Security"
    val scanBio get() = if(isId) "Pindai sidik jari/wajah Anda" else "Scan your fingerprint or face"
    val usePin get() = if(isId) "Gunakan PIN" else "Use PIN"
    val wrongPin get() = if(isId) "PIN yang dimasukkan salah!" else "Incorrect PIN!"
    val curBal get() = if(isId) "Total Saldo" else "Total Balance"
    val inc get() = if(isId) "Pemasukan" else "Income"
    val exp get() = if(isId) "Pengeluaran" else "Expenses"
    val recTx get() = if(isId) "Daftar Transaksi" else "Transaction List"
    val noTx get() = if(isId) "Belum ada transaksi." else "No transactions found."
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
    val editProf get() = if(isId) "Edit Nama" else "Edit Name"
    val appLck get() = if(isId) "Kunci Aplikasi" else "App Lock"
    val finPref get() = if(isId) "Preferensi Keuangan" else "Financial Preference"
    val cur get() = if(isId) "Mata Uang" else "Currency"
    val date get() = if(isId) "Tanggal" else "Date"
    val tar get() = "Target Global"
    val catBudget get() = if(isId) "Budget Kategori" else "Category Budget"
    const val VERSION = "v4.5.1"
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
    val editTx get() = if(isId) "Edit Transaksi" else "Edit Transaction"
    val cat get() = if(isId) "Kategori" else "Category"
    val nme get() = if(isId) "Judul Transaksi" else "Transaction Title"
    val msgInp get() = if(isId) "Catatan Tambahan (Opsional)" else "Notes (Optional)"
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
    val theme get() = if(isId) "Tema Tampilan" else "App Theme"
    val themeSys get() = if(isId) "Ikuti Sistem" else "Use System Setting"
    val themeDark get() = if(isId) "Mode Gelap" else "Dark Mode"
    val themeLight get() = if(isId) "Mode Terang" else "Light Mode"
    val amoledDark get() = if(isId) "AMOLED Gelap" else "AMOLED Dark"
    val amoledDesc get() = if(isId) "Latar belakang hitam murni" else "Pure black background"
    val edit get() = if(isId) "Edit" else "Edit"
    val delete get() = if(isId) "Hapus" else "Delete"
    val delConf get() = if(isId) "Yakin hapus transaksi ini?" else "Delete this transaction?"
    val yes get() = if(isId) "Ya, Hapus" else "Yes, Delete"
    val no get() = if(isId) "Batal" else "Cancel"
    val notif get() = if(isId) "Notifikasi & Pengingat" else "Notifications & Reminders"
    val dailyRem get() = if(isId) "Pengingat 5 Waktu" else "5 Times Reminder"
    val rem get() = if(isId) "Pengingat" else "Reminder"
    val searchTx get() = if(isId) "Cari Transaksi..." else "Search Transactions..."
    val carryOver get() = if(isId) "Bawa Saldo Bulan Lalu" else "Carry-Over Balance"
    val manageCat get() = if(isId) "Kelola Kategori" else "Manage Categories"
    val addCat get() = if(isId) "+ Tambah Kategori" else "+ Add Category"
    val wallet get() = if(isId) "Sumber Dana / Dompet" else "Wallet / Source"
    val manageWallet get() = if(isId) "Kelola Dompet" else "Manage Wallets"
    val addWallet get() = if(isId) "+ Tambah Dompet" else "+ Add Wallet"
    val splitSource get() = if(isId) "Sumber Dana (Split Payment)" else "Funding Source (Split)"
    val addOtherWallet get() = if(isId) "Tambah Dompet Lain" else "Add Another Wallet"
    val total get() = "Total"
    val multiWallet get() = if(isId) "Multi-Dompet" else "Multi-Wallet"
    val mutasi get() = if(isId) "Mutasi" else "Transfer"
    val tarikDari get() = if(isId) "Tarik Dari" else "Withdraw From"
    val simpanKe get() = if(isId) "Simpan Ke" else "Deposit To"
    val jumlahPindah get() = if(isId) "Jumlah Dipindah" else "Transfer Amount"
    val totalTransfer get() = if(isId) "Total Transfer" else "Total Transfer"
    val defMutasiTitle get() = if(isId) "Tarik Tunai / Mutasi" else "Cash Withdrawal / Transfer"
    val newWallet get() = if(isId) "Dompet Baru" else "New Wallet"
    val walletName get() = if(isId) "Nama Dompet" else "Wallet Name"
    val newCat get() = if(isId) "Kategori Baru" else "New Category"
    val catName get() = if(isId) "Nama Kategori" else "Category Name"
    val chooseIcon get() = if(isId) "Pilih Ikon:" else "Choose Icon:"
    val chooseCatIcon get() = if(isId) "Pilih Ikon Kategori:" else "Choose Category Icon:"
    val backupReminderTitle get() = if(isId) "Oit, Backup Dulu! \uD83D\uDC3B\uD83D\uDCBE" else "Hey, Backup Time! \uD83D\uDC3B\uD83D\uDCBE"
    val backupReminderMsg get() = if(isId) "Data kamu udah makin banyak nih. Mending backup dulu filenya biar nggak hilang kalo HP kamu kenapa-kenapa. \uD83D\uDC3B" else "You have a lot of data now. Better backup your file so you don't lose it if something happens to your phone. \uD83D\uDC3B"
    val backupNow get() = if(isId) "Backup Sekarang" else "Backup Now"
    val later get() = if(isId) "Nanti Aja" else "Later"
    val versionInfo get() = if(isId) "Versi: $VERSION\nBuild: Edit Category Icon\nTipe: Standalone Local" else "Version: $VERSION\nBuild: Edit Category Icon\nType: Standalone Local"
}

// ... [KumaIconLibrary] ...
val kumaIconLibrary = mapOf(
    "Kategori" to Icons.Default.Category,
    "Lainnya" to Icons.Default.MoreHoriz,
    "Bintang" to Icons.Default.Star,
    "Favorit" to Icons.Default.Favorite,
    "Bookmark" to Icons.Default.Bookmark,
    "Selesai" to Icons.Default.TaskAlt,
    "Makanan" to Icons.Default.Restaurant,
    "Fastfood" to Icons.Default.Fastfood,
    "Kopi / Nongkrong" to Icons.Default.LocalCafe,
    "Minuman / Boba" to Icons.Default.LocalDrink,
    "Bar / Party" to Icons.Default.LocalBar,
    "Teh / Hangat" to Icons.Default.EmojiFoodBeverage,
    "Cemilan / Jajanan" to Icons.Default.Tapas,
    "Es Krim" to Icons.Default.Icecream,
    "Roti / Kue" to Icons.Default.Cake,
    "Pizza" to Icons.Default.LocalPizza,
    "Buah & Sayur" to Icons.Default.Eco,
    "Mie / Ramen" to Icons.Default.RamenDining,
    "Sup / Kuah" to Icons.Default.SoupKitchen,
    "Set Menu" to Icons.Default.SetMeal,
    "Rice Bowl" to Icons.Default.RiceBowl,
    "Gofood / Delivery" to Icons.Default.DeliveryDining,
    "Sarapan" to Icons.Default.BreakfastDining,
    "Makan Malam" to Icons.Default.DinnerDining,
    "Piknik / Bekal" to Icons.Default.TakeoutDining,
    "Mobil Pribadi" to Icons.Default.DirectionsCar,
    "Motor" to Icons.Default.TwoWheeler,
    "Sepeda" to Icons.Default.PedalBike,
    "Pesawat / Tiket" to Icons.Default.Flight,
    "KRL / Kereta" to Icons.Default.Train,
    "Kereta Cepat" to Icons.Default.DirectionsRailway,
    "Bus / Travel" to Icons.Default.DirectionsBus,
    "Ojol / Taksi" to Icons.Default.LocalTaxi,
    "Kapal / Feri" to Icons.Default.DirectionsBoat,
    "Tram" to Icons.Default.Tram,
    "Skuter" to Icons.Default.ElectricScooter,
    "Bensin / SPBU" to Icons.Default.LocalGasStation,
    "Cas Mobil Listrik" to Icons.Default.EvStation,
    "Tol / Parkir" to Icons.Default.LocalParking,
    "Jalan Kaki" to Icons.Default.DirectionsWalk,
    "Bengkel / Servis" to Icons.Default.Build,
    "Cuci Kendaraan" to Icons.Default.LocalCarWash,
    "Bagasi / Koper" to Icons.Default.Luggage,
    "Paspor / Visa" to Icons.Default.AirplaneTicket,
    "Belanja / Grosir" to Icons.Default.ShoppingBag,
    "Supermarket" to Icons.Default.ShoppingCart,
    "Mall / Thrift" to Icons.Default.LocalMall,
    "Pakaian / Baju" to Icons.Default.Checkroom,
    "Sepatu / Sneakers" to Icons.Default.Snowshoeing,
    "Aksesoris / Jam" to Icons.Default.Watch,
    "Kacamata" to Icons.Default.FaceRetouchingNatural,
    "Skincare / Makeup" to Icons.Default.Brush,
    "Cukur Rambut" to Icons.Default.ContentCut,
    "Spa / Pijat" to Icons.Default.Spa,
    "Toko Kelontong" to Icons.Default.Storefront,
    "Gaji / Uang Masuk" to Icons.Default.AttachMoney,
    "Bank / Mutasi" to Icons.Default.AccountBalance,
    "ATM / Tarik Tunai" to Icons.Default.Atm,
    "Dompet / E-Wallet" to Icons.Default.AccountBalanceWallet,
    "Kartu Kredit / Paylater" to Icons.Default.CreditCard,
    "Tagihan / Bon" to Icons.Default.Receipt,
    "Pajak" to Icons.Default.ReceiptLong,
    "Investasi / Saham" to Icons.Default.TrendingUp,
    "Crypto / Koin" to Icons.Default.CurrencyBitcoin,
    "Tabungan" to Icons.Default.Savings,
    "Brankas" to Icons.Default.Lock,
    "Diskon / Promo" to Icons.Default.Loyalty,
    "Asuransi" to Icons.Default.Shield,
    "Cicilan" to Icons.Default.RequestQuote,
    "Rumah" to Icons.Default.Home,
    "Kosan / Apartemen" to Icons.Default.Apartment,
    "Listrik / Token" to Icons.Default.Bolt,
    "Air / Galon" to Icons.Default.WaterDrop,
    "Internet / Wifi" to Icons.Default.Wifi,
    "Router / Modem" to Icons.Default.Router,
    "Paket Data / Pulsa" to Icons.Default.PhoneIphone,
    "Telepon" to Icons.Default.Call,
    "Gas / Dapur" to Icons.Default.Propane,
    "Alat Kebersihan" to Icons.Default.CleaningServices,
    "Sapu / Pel" to Icons.Default.Sanitizer,
    "Laundry / Cuci Baju" to Icons.Default.LocalLaundryService,
    "Setrika" to Icons.Default.Iron,
    "Perabotan Rumah" to Icons.Default.Chair,
    "Kamar Tidur" to Icons.Default.Bed,
    "Kipas / AC" to Icons.Default.AcUnit,
    "Panci / Wajan" to Icons.Default.Kitchen,
    "Kesehatan / Dokter" to Icons.Default.MedicalServices,
    "Obat / Apotek" to Icons.Default.Medication,
    "Rumah Sakit" to Icons.Default.LocalHospital,
    "Vitamin / Vaksin" to Icons.Default.Vaccines,
    "Gigi / Dokter Gigi" to Icons.Default.CleanHands,
    "Mata / Optik" to Icons.Default.Visibility,
    "Gym / Fitness" to Icons.Default.FitnessCenter,
    "Suplemen / Whey" to Icons.Default.MonitorWeight,
    "Lari / Jogging" to Icons.Default.DirectionsRun,
    "Basket" to Icons.Default.SportsBasketball,
    "Sepak Bola" to Icons.Default.SportsSoccer,
    "Tenis" to Icons.Default.SportsTennis,
    "Berenang" to Icons.Default.Pool,
    "Beladiri" to Icons.Default.SportsMartialArts,
    "Yoga / Meditasi" to Icons.Default.SelfImprovement,
    "Game / Mabar" to Icons.Default.SportsEsports,
    "Top Up / Konsol" to Icons.Default.VideogameAsset,
    "Gacha / Dadu" to Icons.Default.Casino,
    "Film / Bioskop" to Icons.Default.Movie,
    "Streaming / Nonton" to Icons.Default.Subscriptions,
    "YouTube / Video" to Icons.Default.VideoLibrary,
    "Musik / Spotify" to Icons.Default.MusicNote,
    "Audio / IEM" to Icons.Default.Headphones,
    "Speaker" to Icons.Default.Speaker,
    "Konser / Gig" to Icons.Default.LibraryMusic,
    "Gitar / Alat Musik" to Icons.Default.Piano,
    "Buku / Komik" to Icons.Default.AutoStories,
    "Fotografi" to Icons.Default.PhotoCamera,
    "Seni / Melukis" to Icons.Default.Palette,
    "Berkebun" to Icons.Default.Forest,
    "Tech / PC" to Icons.Default.Computer,
    "Laptop" to Icons.Default.Laptop,
    "Gadget / Aksesoris" to Icons.Default.Devices,
    "Keyboard / Mouse" to Icons.Default.Keyboard,
    "Coding / Software" to Icons.Default.Code,
    "Server / Hosting" to Icons.Default.Dns,
    "Cloud / Backup" to Icons.Default.Cloud,
    "Kerja / Kantor" to Icons.Default.Work,
    "Bisnis / Usaha" to Icons.Default.BusinessCenter,
    "Meeting / Zoom" to Icons.Default.VideoCall,
    "Sekolah / Kampus" to Icons.Default.School,
    "Buku Pelajaran" to Icons.Default.MenuBook,
    "SPP / Pendidikan" to Icons.Default.Science,
    "Edukasi Online" to Icons.Default.CastForEducation,
    "Sertifikat / Lulus" to Icons.Default.WorkspacePremium,
    "Ayang / Kencan" to Icons.Default.Favorite,
    "Keluarga" to Icons.Default.Diversity3,
    "Anak / Adik" to Icons.Default.ChildCare,
    "Teman / Circle" to Icons.Default.Groups,
    "Kucing / Anjing" to Icons.Default.Pets,
    "Kondangan / Hadiah" to Icons.Default.CardGiftcard,
    "Pesta / Ulang Tahun" to Icons.Default.Celebration,
    "Sedekah / Donasi" to Icons.Default.VolunteerActivism,
    "Masjid / Ibadah" to Icons.Default.Mosque,
    "Gereja / Ibadah" to Icons.Default.Church,
    "Pura / Vihara" to Icons.Default.TempleBuddhist,
    "Liburan / Staycation" to Icons.Default.BeachAccess,
    "Hotel / Penginapan" to Icons.Default.Hotel,
    "Camping / Alam" to Icons.Default.Terrain,
    "Cuaca / Musim" to Icons.Default.WbSunny,
    "Seneng / Bahagia" to Icons.Default.SentimentSatisfied,
    "Marah / Emosi" to Icons.Default.SentimentVeryDissatisfied
)

val LocalIsDark = compositionLocalOf { true }
val LocalIsAmoled = compositionLocalOf { false }

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


@Entity(tableName = "transactions")
data class KumaTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val date: String,
    val amount: String,
    val isIncome: Boolean,
    val category: String,
    val wallet: String,
    val timestamp: String,
    val message: String = ""
)

@Entity(
    tableName = "transaction_splits",
    foreignKeys = [
        ForeignKey(
            entity = KumaTransaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId")]
)
data class TransactionSplit(
    @PrimaryKey(autoGenerate = true)
    val splitId: Int = 0,
    val transactionId: Int,
    val splitWallet: String,
    val splitAmount: Long
)

data class TransactionWithSplits(
    @Embedded
    val transaction: KumaTransaction,
    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val splits: List<TransactionSplit>
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
    val themeMode: Int = 0,
    val isReminderOn: Boolean = false,
    val reminderTimes: String = "05:00,12:30,15:30,18:00,20:00",
    val useCarryOver: Boolean = false,
    val expenseCats: String = "Food,Shopping,Health,Transport,Education,Entertainment,Others",
    val incomeCats: String = "Financial,Others",
    val wallets: String = "Cash,Bank BCA,GoPay",
    val categoryTargets: String = "{}",
    val isAmoledMode: Boolean = false,
    val categoryIcons: String = "{}"
)

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsWithSplits(): Flow<List<TransactionWithSplits>>

    @Insert
    suspend fun insertTransaction(transaction: KumaTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: KumaTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: KumaTransaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<KumaTransaction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplits(splits: List<TransactionSplit>)

    @Query("DELETE FROM transaction_splits WHERE transactionId = :txId")
    suspend fun deleteSplitsByTxId(txId: Int)

    @Transaction
    suspend fun insertFullTransaction(transaction: KumaTransaction, splits: List<TransactionSplit>) {
        val parentId = insertTransaction(transaction).toInt()
        val splitsWithParentId = splits.map { it.copy(transactionId = parentId) }
        insertSplits(splitsWithParentId)
    }

    @Transaction
    suspend fun updateFullTransaction(transaction: KumaTransaction, splits: List<TransactionSplit>) {
        updateTransaction(transaction)
        deleteSplitsByTxId(transaction.id)
        val splitsWithParentId = splits.map { it.copy(transactionId = transaction.id) }
        insertSplits(splitsWithParentId)
    }

    @Query("SELECT * FROM user_profile WHERE id = 0")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_profile ADD COLUMN isAmoledMode INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `transaction_splits` (
                `splitId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `transactionId` INTEGER NOT NULL, 
                `splitWallet` TEXT NOT NULL, 
                `splitAmount` INTEGER NOT NULL, 
                FOREIGN KEY(`transactionId`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_splits_transactionId` ON `transaction_splits` (`transactionId`)")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_profile ADD COLUMN categoryIcons TEXT NOT NULL DEFAULT '{}'")
    }
}

@Database(
    entities = [
        KumaTransaction::class,
        UserProfile::class,
        TransactionSplit::class
    ],
    version = 15,
    exportSchema = false
)
abstract class KumaDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: KumaDatabase? = null

        fun getDatabase(context: Context): KumaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KumaDatabase::class.java,
                    "kuma_database"
                )
                    .addMigrations(MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
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

@Composable
fun LockScreen(correctPin: String, activity: FragmentActivity, onSuccess: () -> Unit) {
    var inputPin by remember { mutableStateOf("") }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        showBiometricPrompt(activity, onSuccess, {})
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg())
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = AppText()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            AppStr.appLocked,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AppText()
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(6) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            if (index < inputPin.length) AppText() else AppSurfaceVariant(),
                            CircleShape
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(48.dp))

        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "Finger", "0", "Del")
        keys.chunked(3).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                row.forEach { label ->
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(AppSurface())
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                when (label) {
                                    "Del" -> if (inputPin.isNotEmpty()) inputPin = inputPin.dropLast(1)
                                    "Finger" -> showBiometricPrompt(activity, onSuccess, {})
                                    else -> {
                                        if (inputPin.length < 6) {
                                            inputPin += label
                                        }
                                        if (inputPin.length == 6) {
                                            if (inputPin == correctPin) {
                                                onSuccess()
                                            } else {
                                                inputPin = ""
                                                Toast.makeText(context, AppStr.wrongPin, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (label) {
                            "Finger" -> Icon(Icons.Default.Fingerprint, contentDescription = null, tint = AppText())
                            "Del" -> Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null, tint = AppText())
                            else -> Text(label, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppText())
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

class MainActivity : FragmentActivity() {
    var pendingRestoreJson: String? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 12345 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                try {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val display = windowManager.defaultDisplay
            val modes = display.supportedModes
            val bestMode = modes.maxByOrNull { it.refreshRate }
            bestMode?.let { mode ->
                val params = window.attributes
                params.preferredDisplayModeId = mode.modeId
                window.attributes = params
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    11
                )
            }
        }

        val serviceIntent = Intent(this, KumaService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        setContent {
            val context = LocalContext.current
            val db = remember { KumaDatabase.getDatabase(context) }
            val dao = db.transactionDao()
            val userProfile by dao.getUserProfile().collectAsState(initial = null)

            val sharedPrefs = remember { context.getSharedPreferences("kumaflow_prefs", android.content.Context.MODE_PRIVATE) }

            // 🔥 STATE BARU BUAT NANGKEP BULAN & TAHUN 🔥
            var wrappedTarget by remember { mutableStateOf<Pair<Int, Int>?>(null) }

            LaunchedEffect(userProfile?.userName) {
                checkAndApplyPrideEasterEgg(context, userProfile?.userName)
            }

            var isAuthenticated by rememberSaveable { mutableStateOf(false) }

            val systemDark = isSystemInDarkTheme()
            val isAmoled = userProfile?.isAmoledMode == true

            val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
            val isJune = currentMonth == java.util.Calendar.JUNE
            val isPrideTriggered = userProfile?.userName?.contains("#pride", ignoreCase = true) == true
            val isBearTriggered = userProfile?.userName?.contains("#bear", ignoreCase = true) == true

            val activeThemeMode = if (isJune) userProfile?.themeMode ?: 0 else {
                if ((userProfile?.themeMode ?: 0) > 2) 0 else userProfile?.themeMode ?: 0
            }

            val isDark = when(activeThemeMode) {
                1, 3, 5 -> false
                2, 4, 6 -> true
                else -> systemDark
            }

            CompositionLocalProvider(
                LocalIsDark provides isDark,
                LocalIsAmoled provides isAmoled
            ) {
                val colorScheme = when {
                    // 1. Easter Egg Pride & Bear (Prioritas paling tinggi kalau lagi bulan Juni)
                    isPrideTriggered && activeThemeMode == 3 -> lightColorScheme(background = Color(0xFFFCE4EC), surface = Color(0xFFF8BBD0), primary = Color(0xFFD81B60), onPrimary = Color.White, onBackground = Color(0xFF212121), onSurface = Color(0xFF212121))
                    isPrideTriggered && activeThemeMode == 4 -> darkColorScheme(background = Color(0xFF121212), surface = Color(0xFF263238), primary = Color(0xFFAA00FF), onPrimary = Color.White, onBackground = Color.White, onSurface = Color.White)
                    isBearTriggered && activeThemeMode == 5 -> lightColorScheme(background = Color(0xFFFFF3E0), surface = Color(0xFFFFE0B2), primary = Color(0xFFBF360C), onPrimary = Color.White, onBackground = Color(0xFF3E2723), onSurface = Color(0xFF3E2723))
                    isBearTriggered && activeThemeMode == 6 -> darkColorScheme(background = Color(0xFF3E2723), surface = Color(0xFF4E342E), primary = Color(0xFFFFCA28), onPrimary = Color.Black, onBackground = Color(0xFFEFEBE9), onSurface = Color(0xFFEFEBE9))

                    // 2. 🔥 MATERIAL YOU (Dynamic Color) BUAT ANDROID 12+ 🔥
                    activeThemeMode == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        if (isDark) androidx.compose.material3.dynamicDarkColorScheme(context)
                        else androidx.compose.material3.dynamicLightColorScheme(context)
                    }

                    // 3. Fallback buat OS Jadul atau Tema Default (Light/Dark/Amoled)
                    isDark -> if (isAmoled) darkColorScheme(background = Color(0xFF000000), surface = Color(0xFF0F0F0F), onBackground = Color(0xFFE0E0E0), onSurface = Color(0xFFE0E0E0), primary = Color(0xFFD5641C), onPrimary = Color.White) else darkColorScheme(background = Color(0xFF121212), surface = Color(0xFF1E1E1E), onBackground = Color(0xFFE0E0E0), onSurface = Color(0xFFE0E0E0), primary = Color(0xFFD5641C), onPrimary = Color.White)
                    else -> lightColorScheme(background = Color(0xFFD9D2C5), surface = Color(0xFFC7BCAC), onBackground = Color(0xFF4A2F1D), onSurface = Color(0xFF4A2F1D), primary = Color(0xFF4A2F1D), onPrimary = Color.White)
                }

                MaterialTheme(colorScheme = colorScheme) {
                    if (userProfile?.isAppLocked == true && !isAuthenticated) {
                        LockScreen(userProfile?.appPin ?: "", this@MainActivity) {
                            isAuthenticated = true
                        }
                    } else {
                        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                            Box(modifier = Modifier.fillMaxSize()) {

                                MainScreen(
                                    userProfileState = userProfile,
                                    dao = dao,
                                    onOpenWrapped = { m, y -> wrappedTarget = Pair(m, y) }
                                )

                                NewUserAnnouncementDialog()

                                // 🔥 LOGIKA WRAPPED DINAMIS 🔥
                                if (wrappedTarget != null && userProfile != null) {
                                    val targetMonth = wrappedTarget!!.first
                                    val targetYear = wrappedTarget!!.second
                                    val allTxs by dao.getAllTransactionsWithSplits().collectAsState(initial = emptyList())

                                    val cal = java.util.Calendar.getInstance()
                                    cal.set(java.util.Calendar.MONTH, targetMonth - 1)
                                    cal.set(java.util.Calendar.YEAR, targetYear)
                                    val targetMonthName = cal.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, java.util.Locale.forLanguageTag("id-ID")) ?: ""

                                    val targetMonthTxs = allTxs
                                        .map { it.transaction }
                                        .filter { tx ->
                                            try {
                                                val dt = java.time.LocalDateTime.parse(tx.timestamp, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                                dt.monthValue == targetMonth && dt.year == targetYear
                                            } catch (e: Exception) { false }
                                        }

                                    WrappedScreen(
                                        profile = userProfile!!,
                                        prevMonthTransactions = targetMonthTxs,
                                        monthName = "$targetMonthName $targetYear",
                                        onClose = {
                                            wrappedTarget = null
                                            val todayCal = java.util.Calendar.getInstance()
                                            todayCal.add(java.util.Calendar.MONTH, -1)
                                            val pMonth = todayCal.get(java.util.Calendar.MONTH) + 1
                                            val pYear = todayCal.get(java.util.Calendar.YEAR)

                                            // Cuma ngilangin banner kalau yang dibuka beneran bulan lalu
                                            if (targetMonth == pMonth && targetYear == pYear) {
                                                sharedPrefs.edit().putString("last_viewed_wrapped", "$pMonth-$pYear").apply()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
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

    // 🔥 STATE FAB YANG UDAH DIANGKAT KE MAINSCREEN 🔥
    val homeListState = androidx.compose.foundation.lazy.rememberLazyListState()
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

    Scaffold(
        containerColor = AppBg(),
        floatingActionButton = {
            // 🔥 LOGIKA AUTO-HIDE NYALA DI SINI 🔥
            val showFab = selectedItemIndex != 2 && (selectedItemIndex != 0 || isFabVisible)
            if (showFab) {
                FloatingActionButton(
                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); transactionToEdit = null; showBottomSheet = true },
                    containerColor = AppPrimary(), contentColor = Color.White, shape = CircleShape, modifier = Modifier.size(70.dp)
                ) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(40.dp)) }
            }
        },
        bottomBar = { CustomBottomNav(selectedItemIndex, haptic) { selectedItemIndex = it } }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> HomeScreen(
                        profile = userProfile, transactionsWithSplits = monthlyTransactionsWithSplits, balance = totalBalance, walletBalances = walletBalances, income = totalIncome, expenses = totalExpenses, selectedMonth = selectedMonth, selectedYear = selectedYear,
                        onMonthChange = { m, y -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); selectedMonth = m; selectedYear = y },
                        onEdit = { t -> transactionToEdit = t; showBottomSheet = true },
                        onDelete = { t -> scope.launch { dao.deleteTransaction(t.transaction); updateKumaWidget(context) } },
                        onOpenWrapped = onOpenWrapped,
                        listState = homeListState // 🔥 LEMPAR STATE NYA KE SINI 🔥
                    )
                    1 -> ReportScreen(
                        profile = userProfile, monthlyTransactions = monthlyTransactionsWithSplits.map { it.transaction }, allTransactions = transactionListWithSplits.map { it.transaction }, income = totalIncome, expenses = totalExpenses, balance = totalBalance, selectedMonth = selectedMonth, selectedYear = selectedYear,
                        onMonthChange = { m, y -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); selectedMonth = m; selectedYear = y },
                        onOpenWrapped = onOpenWrapped
                    )
                    2 -> SettingsScreen(
                        currentProfile = userProfile, monthlyTransactionsWithSplits = monthlyTransactionsWithSplits, allTransactionsWithSplits = transactionListWithSplits, dao = dao, selectedMonth = selectedMonth, selectedYear = selectedYear,
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
    val haptic = LocalHapticFeedback.current
    val baseTx = transactionToEdit?.transaction

    var txMode by remember(baseTx) { mutableIntStateOf(if (baseTx != null && baseTx.isIncome) 1 else 0) }

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

    // 🔥 SMART NUMPAD: Daftar karakter matematika yang diizinkan
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppSurfaceVariant())
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
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY", "CNY" -> "¥"
            "CHF" -> "CHF"
            else -> "Rp"
        }

        if (txMode == 2) {
            // 🔥 SMART NUMPAD TRANSFER MODE 🔥
            OutlinedTextField(
                value = initialSplits[0].amount,
                onValueChange = {
                    if (it.all { c -> c in allowedMathChars }) {
                        initialSplits[0] = initialSplits[0].copy(amount = it)
                    }
                },
                label = { Text(AppStr.jumlahPindah) },
                placeholder = { Text("Cth: 15000+2000") },
                // Matikan ThousandSeparator kalau ada simbol matematika biar kaga meledak
                visualTransformation = if (initialSplits[0].amount.any { c -> c in "+-*/()" }) androidx.compose.ui.text.input.VisualTransformation.None else ThousandSeparatorTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            val calcTransfer = evaluateMathExpression(initialSplits[0].amount) ?: 0L
            Text(
                "${AppStr.totalTransfer}: $curSym ${NumberFormat.getInstance(Locale.getDefault()).format(calcTransfer)}",
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

                    // 🔥 SMART NUMPAD SPLIT/NORMAL MODE 🔥
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

            // Hitung total pakai otak kalkulator secara live
            val totalAmount = initialSplits.sumOf { evaluateMathExpression(it.amount) ?: 0L }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "${AppStr.total}: $curSym ${NumberFormat.getInstance(Locale.getDefault()).format(totalAmount)}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = AppText()
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 🔥 LOGIKA SAVE YG UDAH DI-EVALUATE MATEMATIKANYA 🔥
        val evaluatedSplits = initialSplits.map { it.copy(amount = (evaluateMathExpression(it.amount) ?: 0L).toString()) }
        val totalAmtFinal = if (txMode == 2) (evaluatedSplits[0].amount.toLongOrNull() ?: 0L) else evaluatedSplits.sumOf { it.amount.toLongOrNull() ?: 0L }
        val isAmountValid = totalAmtFinal > 0L

        Button(
            onClick = {
                val now = LocalDateTime.now()
                val timeStr = baseTx?.timestamp ?: now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val dateStr = baseTx?.date ?: now.format(DateTimeFormatter.ofPattern(if(AppStr.isId) "dd MMM yyyy" else "MMM dd, yyyy", Locale.getDefault()))

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
                        modifier = Modifier.height(150.dp).clip(RoundedCornerShape(8.dp)).background(AppSurfaceVariant())
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
            .clip(RoundedCornerShape(20.dp))
            .background(AppSurfaceVariant())
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

@OptIn(ExperimentalFoundationApi::class)
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
    listState: androidx.compose.foundation.lazy.LazyListState // 🔥 PARAMETER BARU 🔥
) {
    val context = LocalContext.current
    val locale = java.util.Locale.forLanguageTag("id-ID")
    val curSym = when(profile.currency) { "USD", "AUD", "CAD", "SGD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; "JPY", "CNY" -> "¥"; "CHF" -> "CHF"; else -> "Rp" }

    val haptic = LocalHapticFeedback.current

    var isPrivacyMode by rememberSaveable { mutableStateOf(false) }
    val blurRadius by androidx.compose.animation.core.animateDpAsState(targetValue = if (isPrivacyMode) 12.dp else 0.dp, label = "blur_anim")

    var searchQuery by remember { mutableStateOf("") }
    val filteredTx = transactionsWithSplits.filter {
        it.transaction.name.contains(searchQuery, ignoreCase = true) || it.transaction.category.contains(searchQuery, ignoreCase = true) || it.transaction.message.contains(searchQuery, ignoreCase = true)
    }

    // 🔥 LOGIKA STICKY HEADERS: Kelompokkin transaksi berdasarkan tanggal
    val groupedTx = remember(filteredTx) { filteredTx.groupBy { it.transaction.date } }

    val sharedPrefs = remember { context.getSharedPreferences("kumaflow_prefs", android.content.Context.MODE_PRIVATE) }
    val cal = java.util.Calendar.getInstance()
    cal.add(java.util.Calendar.MONTH, -1)
    val prevMonth = cal.get(java.util.Calendar.MONTH) + 1
    val prevYear = cal.get(java.util.Calendar.YEAR)
    val wrappedKey = "$prevMonth-$prevYear"

    var showWrappedBanner by remember { mutableStateOf(sharedPrefs.getString("last_viewed_wrapped", "") != wrappedKey) }

    LazyColumn(
        state = listState, // 🔥 PAKE PARAMETER DI SINI 🔥
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
                                    Text("KumaFlow Wrapped ✨", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
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
                    modifier = Modifier.fillMaxWidth().heightIn(min = 250.dp).clip(RoundedCornerShape(32.dp)).background(if (isPrideThemeActive) prideGradient else androidx.compose.ui.graphics.SolidColor(defaultSurfaceColor))
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppPrimary(), unfocusedBorderColor = AppSurfaceVariant())
                )

                Text(AppStr.recTx, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = AppText())
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
            // 🔥 EKSEKUSI STICKY HEADERS 🔥
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
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        TransactionItem(profile, item, isPrivacyMode, onEdit, onDelete)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

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
    onMonthChange: (Int, Int) -> Unit,
    onOpenWrapped: (Int, Int) -> Unit = { _, _ -> } // 🔥 UPDATE PARAMETER
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

        // 🔥 LOGIKA NAMA BULAN DINAMIS & TOMBOL REWATCH 🔥
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

        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 185.dp).clip(RoundedCornerShape(32.dp)).background(if (isPrideThemeActive) prideGradient else androidx.compose.ui.graphics.SolidColor(defaultSurfaceColor))) {
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

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = AppSurface())) {
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

                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(55.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = AppSurfaceVariant().copy(alpha = 0.5f))) {
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

        Card(modifier = Modifier.fillMaxWidth().height(280.dp), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = AppSurface())) {
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
                modifier = Modifier.fillMaxWidth(),
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppSurfaceVariant())
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
                        AppStr.rest to Icons.Default.History
                    )
                ) { label ->
                    val plainMonthlyTxs = monthlyTransactionsWithSplits.map { it.transaction }
                    when (label) {
                        AppStr.expPdf -> generatePDF(context, plainMonthlyTxs, currentProfile, selectedMonth, selectedYear)
                        AppStr.expCsv -> generateCSV(context, plainMonthlyTxs, currentProfile, selectedMonth, selectedYear)
                        AppStr.expDrive -> exportToDrive(context, plainMonthlyTxs, currentProfile, selectedMonth, selectedYear)
                        AppStr.backApp -> backupAppToJSON(context, currentProfile, allTransactionsWithSplits)
                        AppStr.rest -> { mainActivity?.openSafeFilePicker() }
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
                                    Text("• $w", color = AppText(), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppSurfaceVariant())
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
                                    Text("• $cat", color = AppText(), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))

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
                                modifier = Modifier.height(120.dp).clip(RoundedCornerShape(8.dp)).background(AppSurfaceVariant())
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
            text = "KumaFlow ${AppStr.VERSION}$easterEggEmoji\nLocal Data Only • Privacy First",
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
} // Ini kurung penutup fungsi SettingsScreen ya, awas kehapus wkwk

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
    val curSym = when(profile.currency) { "USD", "AUD", "CAD", "SGD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; "JPY", "CNY" -> "¥"; "CHF" -> "CHF"; else -> "Rp" }

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
        page.canvas.drawText(item.date.take(10), 40f, yPos, paint)
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
        modifier = modifier.heightIn(min = 230.dp),
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
            .clip(RoundedCornerShape(24.dp))
            .background(AppSurface())
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
            .widthIn(max = 80.dp) // Biar kaga nabrak menu sebelahnya
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
    onEdit: (TransactionWithSplits) -> Unit,
    onDelete: (TransactionWithSplits) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val trans = obj.transaction
    val haptic = LocalHapticFeedback.current

    val blurRadius by androidx.compose.animation.core.animateDpAsState(targetValue = if (isPrivacyMode) 12.dp else 0.dp, label = "blur_tx_anim")

    val curSym = when(profile.currency) {
        "USD", "AUD", "CAD", "SGD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY", "CNY" -> "¥"
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

    // 🔥 LOGIKA SWIPE TO DISMISS STATE 🔥
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe kiri = Hapus
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDeleteDialog = true
                    false
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe kanan = Edit
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
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF1976D2) // Biru pas geser kanan
                SwipeToDismissBoxValue.EndToStart -> AppRed() // Merah pas geser kiri
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
        // 🔥 ISI CONTENT CARD UTAMA 🔥
        Card(
            modifier = Modifier
                .fillMaxWidth()
                // Ganti clickable biasa pake combinedClickable
                .combinedClickable(
                    onClick = {
                        // Klik biasa sekarang langsung ngebuka menu Edit biar cepet
                        onEdit(obj)
                    },
                    onLongClick = {
                        // 🔥 JURUS KAGEBUNSHIN (DUPLICATE) 🔥
                        // Kloning datanya, set ID = 0 biar pas di-save ke-insert sebagai transaksi baru!
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val duplicateTx = obj.copy(transaction = trans.copy(id = 0, name = "${trans.name} (Copy)"))
                        onEdit(duplicateTx)
                    }
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (trans.category == "Transfer") Color(0xFF1976D2) else Color(0xFFD5641C))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(trans.name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

                    if (trans.message.isNotEmpty()) {
                        Text(trans.message, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 2.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    // Kita hapus teks trans.date di sini karena udah diwakilin sama Sticky Header di atas layar!
                    Text("${trans.wallet} • ${trans.category}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                val formatted = try {
                    NumberFormat.getInstance(Locale.forLanguageTag("id-ID")).format(trans.amount.toLong())
                } catch (_: Exception) {
                    trans.amount
                }

                AutoSizeText(
                    text = "${if (trans.isIncome) "+ " else "- "} $curSym $formatted",
                    color = if (trans.isIncome) Color.White else AppText(),
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

// --- EASTER EGG LOGIC ---
fun checkAndApplyPrideEasterEgg(context: android.content.Context, userName: String?) {
    if (userName.isNullOrEmpty()) return

    val pm = context.packageManager
    val pkg = context.packageName

    // Cocokin sama nama alias di AndroidManifest.xml
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

            // Biar gak spam force close kalau iconnya udah bener
            if (pm.getComponentEnabledSetting(icon) != state) {
                pm.setComponentEnabledSetting(
                    icon,
                    state,
                    android.content.pm.PackageManager.DONT_KILL_APP
                )
            }
        }
    }

    // Cuma aktif kalau bulan Juni (Calendar.JUNE = 5)
    val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
    if (currentMonth == java.util.Calendar.JUNE) {
        when {
            // Kita pake emoji Pelangi biasa atau hashtag biar 100% aman masuk database
            userName.contains("🌈") || userName.contains("#pride", ignoreCase = true) -> setIcon(
                prideIcon
            )

            userName.contains("🐻") || userName.contains("#bear", ignoreCase = true) -> setIcon(
                bearIcon
            )

            else -> setIcon(normalIcon)
        }
    } else {
        setIcon(normalIcon) // Paksa balik ke normal kalau udah lewat Juni
    }

}

// Taruh di luar class, bebas di paling bawah file
fun evaluateMathExpression(input: String): Long? {
    return try {
        // Ilangin semua spasi biar kaga error
        val expression = input.replace("\\s".toRegex(), "")
        if (expression.isEmpty()) return null

        // Logika parser ringan buat ngitung +, -, *, /
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
                    if (eat('+'.code)) x += parseTerm() // Tambah
                    else if (eat('-'.code)) x -= parseTerm() // Kurang
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // Kali
                    else if (eat('/'.code)) x /= parseFactor() // Bagi
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
        null // Kalau user ngetik ngawur (misal "15000+"), balikin null aja kaga usah error
    }
}