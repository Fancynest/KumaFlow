package com.bearbones.kumaflow

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

class KumaService : Service() {

    private var serviceJob: Job? = null
    private var lastTriggeredMinute = -1 // Prevent duplicate notification triggers within the same minute

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        startInternalTimer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Return START_STICKY to ensure automatic restart if the OS kills the service
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel() // Terminate the background loop when the service is destroyed
    }

    private fun startInternalTimer() {
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val db = KumaDatabase.getDatabase(this@KumaService)
                    val profile = db.transactionDao().getUserProfile().firstOrNull()

                    if (profile != null && profile.isReminderOn) {
                        val now = Calendar.getInstance()
                        val currentHour = now.get(Calendar.HOUR_OF_DAY)
                        val currentMin = now.get(Calendar.MINUTE)

                        val times = profile.reminderTimes.split(",")
                        for (timeStr in times) {
                            val parts = timeStr.split(":")
                            if (parts.size == 2) {
                                val targetHour = parts[0].toIntOrNull() ?: 0
                                val targetMin = parts[1].toIntOrNull() ?: 0

                                // Trigger reminder if the exact time matches and has not been triggered yet
                                if (currentHour == targetHour && currentMin == targetMin && currentMin != lastTriggeredMinute) {
                                    lastTriggeredMinute = currentMin
                                    showReminderNotification(this@KumaService)
                                }
                            }
                        }

                        // Reset the trigger lock once the minute changes
                        if (currentMin != lastTriggeredMinute) {
                            lastTriggeredMinute = -1
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Polling every 15 seconds. Designed to be lightweight with minimal battery impact.
                delay(15000L)
            }
        }
    }

    private fun showReminderNotification(context: Context) {
        val channelId = "kumaflow_reminder_channel_v7"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.kumaflownotification}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "KumaFlow Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat untuk mencatat pengeluaran"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val messages = listOf(
            Pair("Satu bulan saldonya ke mana? 🥀", "Duit abis berlebihan itu nggak baik. Yuk catat pengeluaran hari ini."),
            Pair("Takut tambah dewasa... dan miskin 😭", "Yuk absen dulu hari ini sebelum duitnya nguap nggak jelas."),
            Pair("Sialnya ku kenal Paylater 💔", "Sisa rasa penyesalan doang kalau nggak dicatat dari sekarang."),
            Pair("Lantas mengapa ku masih jajan? 🎧", "Mending evaluasi dulu pengeluaran kamu hari ini deh."),
            Pair("Berapa harga kewarasan ini? 🥺", "Catat pengeluaran kamu sekarang yuk ah!"),
            Pair("Kita usahakan backup itu... ⛈️", "Biar kalau HP error, catatan keuangan nggak ikut ngilang."),
            Pair("Sedia aku sebelum hujan ☔", "Backup file KumaFlow menjagamu tak kehilangan Data."),
            Pair("Tapi anehnya saldo cepat hilang", "Oh ternyata jajan kopi tiap malam."),
            Pair("Soal hemat ternyata aku masih amatir 💸", "Saatnya evaluasi pengeluaran hari ini."),
            Pair("Gengsi menyelimutiku 😔", "Padahal dompet lagi butuh bantuan."),
            Pair("Tak lagi sama arah dompetnya 😭", "Keinginan banyak, dompet berkata tidak")
        )

        val randomMsg = messages.random()

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_kuma_notif) // Fallback to R.mipmap.ic_launcher if the custom notification icon fails
            .setContentTitle(randomMsg.first)
            .setContentText(randomMsg.second)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setSound(soundUri)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup("REMINDER_GROUP")


        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun startForegroundServiceNotification() {
        val channelId = "kumaflow_foreground_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "KumaFlow Background Sync",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("KumaFlow Aktif")
            .setContentText("Menjaga pengingat agar tetap berjalan...")
            .setSmallIcon(R.drawable.ic_kuma_notif) // Fallback to R.mipmap.ic_launcher if the custom notification icon fails
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setGroup("SERVICE_GROUP")
            .build()

        startForeground(101, notification)
    }
}