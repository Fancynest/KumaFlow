package com.bearbones.kumaflow

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NewUserAnnouncementDialog() {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("KumaFlowPrefs", Context.MODE_PRIVATE)

    // Cek apakah ini pertama kali user buka app (default-nya true)
    var showDialog by remember {
        mutableStateOf(sharedPref.getBoolean("is_first_time_user", true))
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                // Sengaja dikosongin biar user gak bisa tutup dialog sembarangan
                // dengan cara klik di luar area pop-up. Mereka HARUS klik tombol "Paham".
            },
            title = {
                Text(
                    text = "📢 Info Penting Pengingat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(text = "Biar KumaFlow bisa selalu ngingetin kamu nyatat pengeluaran, kamu akan melihat notifikasi \"KumaFlow Aktif\" di atas layar.")
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "🛡️ Pelindung Sistem", fontWeight = FontWeight.Bold)
                    Text(text = "Tanda KumaFlow sedang standby agar alarm tidak dimatikan paksa oleh HP.")
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "🛑 Jangan Dihapus (Di-swipe)", fontWeight = FontWeight.Bold)
                    Text(text = "Tolong biarkan notifikasi ini. Jika dihapus, pengingat berisiko mati.")
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "🔋 Hemat Baterai", fontWeight = FontWeight.Bold)
                    Text(text = "Sistem ini sangat ringan dan aman untuk baterai HP kamu.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Kalau tombol diklik, ubah status "is_first_time_user" jadi false
                        // Terus simpan ke HP, biar next time app dibuka, dialog ini gak muncul lagi
                        sharedPref.edit().putBoolean("is_first_time_user", false).apply()
                        showDialog = false
                    }
                ) {
                    Text("Paham & Lanjutkan")
                }
            }
        )
    }
}