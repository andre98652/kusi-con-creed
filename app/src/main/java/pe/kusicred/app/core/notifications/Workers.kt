package pe.kusicred.app.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import pe.kusicred.app.core.database.AppDatabase
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

// ============================================================
// VACCINE NOTIFICATION WORKER
// Checks daily if any vaccine is due in the next 3 days
// ============================================================
@HiltWorker
class VaccineNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: AppDatabase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val today = LocalDate.now()
            val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            // Mark overdue vaccines
            database.vaccineRecordDao().markOverdueVaccines(todayMillis)

            // Query all children
            val children = database.childDao().getAllChildren().first()
            val threeDaysFromNow = todayMillis + (3 * 24 * 60 * 60 * 1000L)

            var notifId = 1000
            for (child in children) {
                val upcoming = database.vaccineRecordDao().getVaccinesInRange(child.id, todayMillis, threeDaysFromNow)
                val pendingUpcoming = upcoming.filter { it.status == "PENDING" || it.status == "RESCHEDULED" }

                for (record in pendingUpcoming) {
                    val vaccine = database.vaccineCatalogDao().getVaccineById(record.vaccineId)
                    if (vaccine != null) {
                        val daysUntil = ((record.scheduledDateMillis - todayMillis) / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(0)
                        val firstName = child.fullName.split(" ").firstOrNull() ?: child.fullName
                        NotificationHelper.sendVaccineReminder(
                            context = applicationContext,
                            childName = firstName,
                            vaccineName = vaccine.shortName,
                            daysUntil = daysUntil,
                            contextualTip = vaccine.contextualTip,
                            notifId = notifId++
                        )
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "vaccine_notification_worker"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<VaccineNotificationWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
            )
        }
    }
}

// ============================================================
// IRON REMINDER WORKER
// Fires daily for iron supplement reminder
// ============================================================
@HiltWorker
class IronReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: AppDatabase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val childId = inputData.getString("child_id") ?: return Result.failure()
            val childName = inputData.getString("child_name") ?: "tu bebé"

            val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L)
            val inactiveDays = database.ironRecordDao().getInactiveDaysCount(childId, threeDaysAgo)

            if (inactiveDays >= 3) {
                NotificationHelper.sendIronInactivityAlert(applicationContext, childName, inactiveDays)
            } else {
                NotificationHelper.sendIronReminder(applicationContext, childName)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME_PREFIX = "iron_reminder_"

        fun schedule(context: Context, childId: String, childName: String) {
            val inputData = workDataOf("child_id" to childId, "child_name" to childName)
            val request = PeriodicWorkRequestBuilder<IronReminderWorker>(1, TimeUnit.DAYS)
                .setInputData(inputData).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "$WORK_NAME_PREFIX$childId", ExistingPeriodicWorkPolicy.KEEP, request
            )
        }
    }
}
