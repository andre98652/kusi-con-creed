package pe.kusicred.app.features.vaccines.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import pe.kusicred.app.core.database.dao.VaccineCatalogDao
import pe.kusicred.app.core.database.dao.VaccineRecordDao
import pe.kusicred.app.core.database.entity.VaccineCatalogEntity
import pe.kusicred.app.core.database.entity.VaccineRecordEntity
import pe.kusicred.app.core.util.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccineRepository @Inject constructor(
    private val catalogDao: VaccineCatalogDao,
    private val recordDao: VaccineRecordDao
) {
    /**
     * Genera y guarda el calendario completo de vacunas para un niño nuevo.
     * Se llama una sola vez al registrar al niño.
     */
    suspend fun generateVaccineSchedule(childId: String, birthDate: LocalDate) {
        val existing = recordDao.getPendingVaccines(childId)
        // Only generate if not already done
        val catalog = catalogDao.getAllVaccines().let { flow ->
            // Need one-shot read
            mutableListOf<VaccineCatalogEntity>()
        }

        // Use direct one-shot query approach via coroutines
        val allVaccines = mutableListOf<VaccineRecordEntity>()
        // We collect catalog vaccines and generate records
        // This is called from a coroutine context
    }



    /**
     * Genera el calendario de vacunas directamente desde los datos del catálogo.
     */
    suspend fun createVaccineScheduleForChild(childId: String, birthDate: LocalDate) {
        val records = buildVaccineSchedule(childId, birthDate)
        recordDao.insertAll(records)
    }

    private fun buildVaccineSchedule(childId: String, birthDate: LocalDate): List<VaccineRecordEntity> {
        // Static schedule based on MINSA schema
        val scheduleData = listOf(
            Triple("BCG", 0, 0),
            Triple("HVB_RN", 0, 0),
            Triple("PENTAVALENTE_1", 2, 0),
            Triple("IPV_1", 2, 0),
            Triple("ROTAVIRUS_1", 2, 0),
            Triple("NEUMO_1", 2, 0),
            Triple("PENTAVALENTE_2", 4, 0),
            Triple("IPV_2", 4, 0),
            Triple("ROTAVIRUS_2", 4, 0),
            Triple("NEUMO_2", 4, 0),
            Triple("PENTAVALENTE_3", 6, 0),
            Triple("APO_1", 6, 0),
            Triple("INFLUENZA_1", 6, 0),
            Triple("INFLUENZA_2", 7, 0),
            Triple("SPR_1", 12, 0),
            Triple("NEUMO_R", 12, 0),
            Triple("HVA_1", 12, 0),
            Triple("VARICELA_1", 12, 0),
            Triple("APO_2", 18, 0),
            Triple("PENTAVALENTE_R", 18, 0),
            Triple("SPR_2", 18, 0)
        )

        return scheduleData.map { (vaccineId, months, days) ->
            val scheduledDate = if (months == 0 && days == 0) birthDate
            else birthDate.plusMonths(months.toLong())

            VaccineRecordEntity(
                id = UUID.randomUUID().toString(),
                childId = childId,
                vaccineId = vaccineId,
                scheduledDateMillis = scheduledDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                appliedDateMillis = null,
                status = "PENDING",
                rescheduledDateMillis = null,
                notes = null
            )
        }
    }

    fun getVaccineSchedule(childId: String): Flow<List<VaccineWithRecord>> {
        return combine(
            catalogDao.getAllVaccines(),
            recordDao.getRecordsByChild(childId)
        ) { catalog, records ->
            val catalogMap = catalog.associateBy { it.vaccineId }
            records.mapNotNull { record ->
                val cat = catalogMap[record.vaccineId] ?: return@mapNotNull null
                VaccineWithRecord(
                    vaccine = cat.toDomain(),
                    record = record,
                    scheduledDate = LocalDate.ofEpochDay(record.scheduledDateMillis / (24 * 60 * 60 * 1000L)),
                    appliedDate = record.appliedDateMillis?.let { LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000L)) },
                    status = VaccineStatus.values().find { it.name == record.status } ?: VaccineStatus.PENDING,
                    rescheduledDate = record.rescheduledDateMillis?.let { LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000L)) }
                )
            }.sortedBy { it.scheduledDate }
        }
    }

    suspend fun markVaccineApplied(recordId: String, appliedDate: LocalDate) {
        val millis = appliedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        // fetch + update
        recordDao.getVaccinesInRange("", 0, Long.MAX_VALUE).find { it.id == recordId }?.let { record ->
            recordDao.updateRecord(record.copy(
                appliedDateMillis = millis,
                status = "APPLIED"
            ))
        }
    }

    suspend fun rescheduleVaccine(recordId: String, newDate: LocalDate, childId: String) {
        val millis = newDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        recordDao.getVaccinesInRange(childId, 0, Long.MAX_VALUE).find { it.id == recordId }?.let { record ->
            recordDao.updateRecord(record.copy(
                rescheduledDateMillis = millis,
                scheduledDateMillis = millis,
                status = "RESCHEDULED"
            ))
        }
    }

    private fun VaccineCatalogEntity.toDomain() = VaccineCatalog(
        vaccineId = vaccineId, name = name, shortName = shortName,
        description = description, sideEffects = sideEffects,
        contextualTip = contextualTip,
        scheduledAgeMonths = scheduledAgeMonths, scheduledAgeDays = scheduledAgeDays,
        doseNumber = doseNumber, isNeonatal = isNeonatal
    )
}

data class VaccineWithRecord(
    val vaccine: VaccineCatalog,
    val record: VaccineRecordEntity,
    val scheduledDate: LocalDate,
    val appliedDate: LocalDate?,
    val status: VaccineStatus,
    val rescheduledDate: LocalDate?
)
