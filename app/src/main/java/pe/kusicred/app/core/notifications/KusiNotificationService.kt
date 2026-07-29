package pe.kusicred.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KusiNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "kusi_channel",
                "Notificaciones de Kusi-CRED",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Canal principal para recordatorios de vacunas e hitos"
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showWelcomeNotification() {
        val notification = NotificationCompat.Builder(context, "kusi_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Bienvenida a Kusi-CRED! 🌱")
            .setContentText("Por aquí recibirás notificaciones de vacunas de tu bebé.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
