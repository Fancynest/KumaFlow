import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WrappedScreen(
    profile: UserProfile,
    prevMonthTransactions: List<KumaTransaction>, // Kirim data transaksi bulan kemaren ke sini
    monthName: String, // Contoh: "Mei 2026"
    onClose: () -> Unit // Pas ditutup, update sharedprefs di MainActivity
) {
    val locale = Locale.forLanguageTag("id-ID")
    val curSym = when(profile.currency) {
        "USD", "AUD", "CAD", "SGD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY", "CNY" -> "¥"
        "CHF" -> "CHF"
        else -> "Rp"
    }

    // --- KALKULASI DATA WRAPPED ---
    val expenses = prevMonthTransactions.filter { !it.isIncome }
    val incomes = prevMonthTransactions.filter { it.isIncome }

    val totalExp = expenses.sumOf { it.amount.toLongOrNull() ?: 0L }
    val totalInc = incomes.sumOf { it.amount.toLongOrNull() ?: 0L }

    val categoryGroup = expenses.groupBy { it.category }.mapValues { it.value.sumOf { t -> t.amount.toLongOrNull() ?: 0L } }
    val topCategory = categoryGroup.maxByOrNull { it.value }?.key ?: "Belum ada"
    val topCatAmount = categoryGroup[topCategory] ?: 0L

    val biggestTx = expenses.maxByOrNull { it.amount.toLongOrNull() ?: 0L }
    val biggestInc = incomes.maxByOrNull { it.amount.toLongOrNull() ?: 0L }

    val persona = when {
        totalExp == 0L -> "Sepuh Frugal Living \uD83E\uDDDD\u200D♂\uFE0F"
        categoryGroup["Food"] ?: 0L > totalExp / 3 -> "Foodie Sejati \uD83C\uDF54"
        categoryGroup["Shopping"] ?: 0L > totalExp / 3 -> "Trendsetter FOMO \uD83D\uDECD\uFE0F"
        totalExp > totalInc && totalInc > 0L -> "Donatur Tetap Kafe ☕"
        else -> "Si Paling Bijak \uD83E\uDD13"
    }

    // --- LOGIKA IG STORY (PAGER & TIMER) ---
    val pages = 6
    val pagerState = rememberPagerState(pageCount = { pages })
    val coroutineScope = rememberCoroutineScope()

    var isPaused by remember { mutableStateOf(false) }
    var progressAnimation by remember { mutableStateOf(0f) }

    LaunchedEffect(pagerState.currentPage, isPaused) {
        progressAnimation = 0f
        if (!isPaused) {
            val animationDuration = 5000L // 5 detik per slide
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < animationDuration) {
                delay(16) // ~60fps refresh rate
                progressAnimation = (System.currentTimeMillis() - startTime) / animationDuration.toFloat()
            }
            if (pagerState.currentPage < pages - 1) {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }

    val neonColors = listOf(
        Color(0xFFFF0055), // Neon Pink
        Color(0xFF00FFCC), // Neon Cyan
        Color(0xFFFFD500), // Neon Yellow
        Color(0xFF00FF33), // Neon Green
        Color(0xFFB000FF), // Neon Purple
        Color(0xFFFF5500)  // Neon Orange
    )

    // --- UI UTAMA WRAPPED ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Vibe AMOLED Dark
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPaused = true // Kalo layar ditahan, timer pause
                            tryAwaitRelease()
                            isPaused = false
                        },
                        onTap = { offset ->
                            coroutineScope.launch {
                                // Tap kiri = mundur, Tap kanan = maju
                                if (offset.x < size.width / 3) {
                                    if (pagerState.currentPage > 0) pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                } else {
                                    if (pagerState.currentPage < pages - 1) pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        }
                    )
                }
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                // KARTU NEO-BRUTALISM + GLASSMORPHISM
                val activeNeon = neonColors[page % neonColors.size]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(4.dp, activeNeon, RoundedCornerShape(32.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF)), // Transparan efek kaca
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (page) {
                            0 -> {
                                Text("BULAN INI", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Kamu udah ngeluarin duit sebanyak...", color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("$curSym ${NumberFormat.getInstance(locale).format(totalExp)}", fontSize = 42.sp, fontWeight = FontWeight.Black, color = activeNeon, textAlign = TextAlign.Center, lineHeight = 48.sp)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Lumayan sibuk ya dompetmu bulan ini! \uD83D\uDE80", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, textAlign = TextAlign.Center)
                            }
                            1 -> {
                                Text("TOP KATEGORI", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Ternyata, dana kamu paling deres ngalir ke...", color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(topCategory.uppercase(), fontSize = 48.sp, fontWeight = FontWeight.Black, color = activeNeon, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("$curSym ${NumberFormat.getInstance(locale).format(topCatAmount)}", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Asal bikin happy, sesekali gapapa dong! ✨", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, textAlign = TextAlign.Center)
                            }
                            2 -> {
                                Text("TRANSAKSI TERGILA", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Momen pengeluaran paling brutal jatuh kepada...", color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(biggestTx?.name ?: "Kosong", fontSize = 32.sp, fontWeight = FontWeight.Black, color = activeNeon, textAlign = TextAlign.Center, lineHeight = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("$curSym ${NumberFormat.getInstance(locale).format(biggestTx?.amount?.toLongOrNull() ?: 0L)}", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Semoga beneran kepake dan worth it ya! \uD83D\uDE4F", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, textAlign = TextAlign.Center)
                            }
                            3 -> {
                                Text("PAHLAWAN PEMASUKAN", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Kabar baiknya, ada rezeki nomplok dari...", color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(biggestInc?.name ?: "Belum ada rejeki", fontSize = 32.sp, fontWeight = FontWeight.Black, color = activeNeon, textAlign = TextAlign.Center, lineHeight = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("$curSym ${NumberFormat.getInstance(locale).format(biggestInc?.amount?.toLongOrNull() ?: 0L)}", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Kerja keras terbayar lunas! Lanjutkan! \uD83D\uDD25", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, textAlign = TextAlign.Center)
                            }
                            4 -> {
                                Text("FINANCIAL PERSONA", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Berdasarkan gaya jajanmu, gelar yang paling cocok buat kamu adalah...", color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(persona, fontSize = 36.sp, fontWeight = FontWeight.Black, color = activeNeon, textAlign = TextAlign.Center, lineHeight = 42.sp)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Kira-kira bulan depan gelarnya bakal berubah kaga nih? \uD83E\uDD14", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, textAlign = TextAlign.Center)
                            }
                            5 -> {
                                Text("THAT'S A WRAP!", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Perjalanan keuangan $monthName kamu resmi ditutup.", color = Color.White, fontSize = 24.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(32.dp))

                                Button(
                                    onClick = onClose,
                                    colors = ButtonDefaults.buttonColors(containerColor = activeNeon),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth().height(55.dp)
                                ) {
                                    Text("SIAP BUAT BULAN INI \uD83D\uDCAA", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- PROGRESS BAR IG STORY DI ATAS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0 until pages) {
                LinearProgressIndicator(
                    progress = {
                        when {
                            i < pagerState.currentPage -> 1f // Kalo udah kelewat, full 100%
                            i == pagerState.currentPage -> progressAnimation // Slide yg lagi jalan
                            else -> 0f // Slide belum kebuka
                        }
                    },
                    modifier = Modifier.weight(1f).height(3.dp).clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }

        // --- TOMBOL CLOSE (SILANG) ---
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
        }
    }
}