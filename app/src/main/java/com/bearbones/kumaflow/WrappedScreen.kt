package com.bearbones.kumaflow

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun WrappedScreen(
    profile: UserProfile,
    prevMonthTransactions: List<KumaTransaction>,
    monthName: String,
    onClose: () -> Unit
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

    // 🔥 VARIABEL FONT (Udah disiapin biar gampang) 🔥
    // Nanti kalau lu udah dapet file googlesans.ttf dan ditaruh di folder res/font,
    // lu tinggal ganti jadi: val googleFont = FontFamily(Font(R.font.googlesans))
    val googleFont = FontFamily.SansSerif

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

    // --- LOGIKA IG STORY ---
    val pages = 6
    val pagerState = rememberPagerState(pageCount = { pages })
    val coroutineScope = rememberCoroutineScope()

    var isPaused by remember { mutableStateOf(false) }
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(pagerState.currentPage, isPaused) {
        if (!isPaused) {
            progressAnim.snapTo(0f)
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
            )
            if (pagerState.currentPage < pages - 1) {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }

    val neonColors = listOf(
        Color(0xFFFF0055), // Pink
        Color(0xFF00FFCC), // Cyan
        Color(0xFFFFD500), // Yellow
        Color(0xFF00FF33), // Green
        Color(0xFFB000FF), // Purple
        Color(0xFFFF5500)  // Orange
    )

    // DAFTAR GAMBAR BACKGROUND
    val bgImages = listOf(
        R.drawable.bg_slide_1,
        R.drawable.bg_slide_2,
        R.drawable.bg_slide_3,
        R.drawable.bg_slide_4,
        R.drawable.bg_slide_5,
        R.drawable.bg_slide_6
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPaused = true
                            tryAwaitRelease()
                            isPaused = false
                        },
                        onTap = { offset ->
                            coroutineScope.launch {
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

            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

            Box(modifier = Modifier.fillMaxSize()) {
                // GAMBAR BACKGROUND
                Image(
                    painter = painterResource(id = bgImages[page % bgImages.size]),
                    contentDescription = "Background Slide $page",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // FILTER GELAP DI LATAR BELAKANG BIAR CARD LEBIH POP OUT
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )

                // KONTEN KARTU
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                        .graphicsLayer {
                            val scale = 1f - (pageOffset * 0.15f).coerceIn(0f, 0.2f)
                            val alphaFade = 1f - (pageOffset * 1.5f).coerceIn(0f, 1f)
                            scaleX = scale
                            scaleY = scale
                            alpha = alphaFade
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val pageColor = neonColors[page % neonColors.size]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .border(2.dp, pageColor, RoundedCornerShape(32.dp)),
                        // 🔥 INI FIX-NYA: Warna Card jadi Item Solid, kaga transparan lagi! 🔥
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when (page) {
                                0 -> {
                                    Text("BULAN INI", fontFamily = googleFont, fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Kamu udah ngeluarin duit sebanyak...", fontFamily = googleFont, color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text("$curSym ${NumberFormat.getInstance(locale).format(totalExp)}", fontFamily = googleFont, fontSize = 42.sp, fontWeight = FontWeight.Black, color = pageColor, textAlign = TextAlign.Center, lineHeight = 48.sp)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(if (totalExp == 0L) "Lagi puasa jajan ya? Hebat bener \uD83D\uDE31" else "Lumayan sibuk ya dompetmu bulan ini! \uD83D\uDE80", fontFamily = googleFont, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, textAlign = TextAlign.Center)
                                }
                                1 -> {
                                    Text("TOP KATEGORI", fontFamily = googleFont, fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Ternyata, dana kamu paling deres ngalir ke...", fontFamily = googleFont, color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(topCategory.uppercase(), fontFamily = googleFont, fontSize = 42.sp, fontWeight = FontWeight.Black, color = pageColor, textAlign = TextAlign.Center, lineHeight = 48.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("$curSym ${NumberFormat.getInstance(locale).format(topCatAmount)}", fontFamily = googleFont, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text("Asal bikin happy, sesekali gapapa dong! ✨", fontFamily = googleFont, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, textAlign = TextAlign.Center)
                                }
                                2 -> {
                                    Text("TRANSAKSI TERGILA", fontFamily = googleFont, fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Momen pengeluaran paling brutal jatuh kepada...", fontFamily = googleFont, color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(biggestTx?.name ?: "Kosong", fontFamily = googleFont, fontSize = 32.sp, fontWeight = FontWeight.Black, color = pageColor, textAlign = TextAlign.Center, lineHeight = 36.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("$curSym ${NumberFormat.getInstance(locale).format(biggestTx?.amount?.toLongOrNull() ?: 0L)}", fontFamily = googleFont, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text("Semoga beneran kepake dan worth it ya! \uD83D\uDE4F", fontFamily = googleFont, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, textAlign = TextAlign.Center)
                                }
                                3 -> {
                                    Text("PAHLAWAN PEMASUKAN", fontFamily = googleFont, fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Kabar baiknya, ada rezeki nomplok dari...", fontFamily = googleFont, color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(biggestInc?.name ?: "Belum ada rejeki", fontFamily = googleFont, fontSize = 32.sp, fontWeight = FontWeight.Black, color = pageColor, textAlign = TextAlign.Center, lineHeight = 36.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("$curSym ${NumberFormat.getInstance(locale).format(biggestInc?.amount?.toLongOrNull() ?: 0L)}", fontFamily = googleFont, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(if (biggestInc == null) "Bulan depan pasti ada, semangat! \uD83D\uDCAA" else "Kerja keras terbayar lunas! Lanjutkan! \uD83D\uDD25", fontFamily = googleFont, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, textAlign = TextAlign.Center)
                                }
                                4 -> {
                                    Text("FINANCIAL PERSONA", fontFamily = googleFont, fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Berdasarkan gaya jajanmu, gelar yang paling cocok buat kamu adalah...", fontFamily = googleFont, color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(persona, fontFamily = googleFont, fontSize = 36.sp, fontWeight = FontWeight.Black, color = pageColor, textAlign = TextAlign.Center, lineHeight = 42.sp)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text("Kira-kira bulan depan gelarnya bakal berubah kaga nih? \uD83E\uDD14", fontFamily = googleFont, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, textAlign = TextAlign.Center)
                                }
                                5 -> {
                                    Text("THAT'S A WRAP!", fontFamily = googleFont, fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text("Perjalanan keuangan $monthName kamu resmi ditutup.", fontFamily = googleFont, color = Color.White, fontSize = 24.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(32.dp))

                                    Button(
                                        onClick = onClose,
                                        colors = ButtonDefaults.buttonColors(containerColor = pageColor),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth().height(55.dp)
                                    ) {
                                        Text("SIAP BUAT BULAN INI \uD83D\uDCAA", fontFamily = googleFont, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    }
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
                            i < pagerState.currentPage -> 1f
                            i == pagerState.currentPage -> progressAnim.value
                            else -> 0f
                        }
                    },
                    modifier = Modifier.weight(1f).height(3.dp).clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
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