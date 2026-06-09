package pe.kusicred.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import pe.kusicred.app.MainActivity
import pe.kusicred.app.R

object NotificationHelper {

    const val CHANNEL_VACCINES = "kusicred_vaccines"
    const val CHANNEL_IRON = "kusicred_iron"
    const val CHANNEL_ALERTS = "kusicred_alerts"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Canal de vacunas
        NotificationChannel(
            CHANNEL_VACCINES,
            "Recordatorio de Vacunas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas para las vacunas programadas de tu bebé"
            manager.createNotificationChannel(this)
        }

        // Canal de hierro
        NotificationChannel(
            CHANNEL_IRON,
            "Gotas de Hierro",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Recordatorio diario de suplementación de hierro"
            manager.createNotificationChannel(this)
        }

        // Canal de alertas
        NotificationChannel(
            CHANNEL_ALERTS,
            "Alertas de Salud",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas importantes sobre la salud de tu bebé"
            manager.createNotificationChannel(this)
        }
    }

    fun sendVaccineReminder(
        context: Context,
        childName: String,
        vaccineName: String,
        daysUntil: Int,
        contextualTip: String,
        notifId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "vaccines")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (daysUntil) {
            0 -> "💉 ¡Hoy es el día de la vacuna de ${childName}!"
            1 -> "💉 ¡Mañana toca vacuna!"
            else -> "💉 Faltan $daysUntil días para la vacuna de $childName"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_VACCINES)
            .setSmallIcon(R.drawable.ic_vaccine_notif)
            .setContentTitle(title)
            .setContentText("$vaccineName — $contextualTip")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$vaccineName\n\n$contextualTip"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, notification)
    }

    fun sendIronReminder(context: Context, childName: String, notifId: Int = 9001) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "iron_tracker")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_IRON)
            .setSmallIcon(R.drawable.ic_iron_drop)
            .setContentTitle("💊 Gotas de hierro de $childName")
            .setContentText("¡Buenos días! Recuerda darle las gotas de hierro. Previenen la anemia. ✨")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, notification)
    }

    fun sendIronInactivityAlert(context: Context, childName: String, inactiveDays: Int, notifId: Int = 9002) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_alert)
            .setContentTitle("⚠️ Hierro de $childName")
            .setContentText("Han pasado $inactiveDays días sin registrar las gotas de hierro. La constancia es clave para prevenir la anemia.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Han pasado $inactiveDays días sin registrar las gotas de hierro.\n\nLa anemia afecta el desarrollo cerebral de tu bebé. La suplementación diaria hace una gran diferencia. ¡Puedes lograrlo! 💪"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, notification)
    }
}
