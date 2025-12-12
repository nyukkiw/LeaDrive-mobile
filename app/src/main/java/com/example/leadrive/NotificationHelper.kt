package com.example.leadrive

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// Object = Singleton (Bisa dipanggil langsung tanpa 'new')
object NotificationHelper {

    // ID unik untuk channel notifikasi (bebas, tapi harus konsisten)
    private const val CHANNEL_ID = "leadrive_updates_channel"
    private const val CHANNEL_NAME = "Update Kursus"

    // 1. Fungsi Membuat Saluran (Wajib untuk Android 8.0+)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = "Notifikasi perubahan status kursus"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
            }

            // Mendaftarkan channel ke sistem
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 2. Fungsi Menampilkan Notifikasi

    fun showNotification(context: Context, title: String, message: String) {
        try {
            // Membangun tampilan notifikasi
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Ikon bawaan Android (bisa diganti icon app Anda)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) // Hilang otomatis saat diklik

            // Menampilkan notifikasi
            // ID (System.currentTimeMillis) dibuat dinamis agar notifikasi bisa menumpuk, tidak saling timpa
            with(NotificationManagerCompat.from(context)) {
                // Cek Izin secara manual di sini
                if (androidx.core.app.ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    // Jika izin tidak diberikan, jangan lakukan apa-apa (return)
                    return
                }
                with(NotificationManagerCompat.from(context)) {
                    // Merah akan hilang karena sudah di-suppress
                    notify(System.currentTimeMillis().toInt(), builder.build())
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}