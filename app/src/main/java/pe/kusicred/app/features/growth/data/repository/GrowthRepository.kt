package pe.kusicred.app.features.growth.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.kusicred.app.core.database.dao.GrowthRecordDao
import pe.kusicred.app.core.database.dao.WhoTableDao
import pe.kusicred.app.core.database.entity.GrowthRecordEntity
import pe.kusicred.app.core.util.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrowthRepository @Inject constructor(
    private val growthRecordDao: GrowthRecordDao,
    private val whoTableDao: WhoTableDao
) {
    fun getRecordsByChild(childId: String): Flow<List<GrowthRecord>> =
        growthRecordDao.getRecordsByChild(childId).map { list -> list.map { it.toDomain() } }

    suspend fun getLatestRecord(childId: String): GrowthRecord? =
        growthRecordDao.getLatestRecord(childId)?.toDomain()

    suspend fun getRecordsAsc(childId: String): List<GrowthRecord> =
        growthRecordDao.getRecordsByChildAsc(childId).map { it.toDomain() }

    suspend fun addRecord(
        childId: String,
        sex: Sex,
        ageMonths: Int,
        weightKg: Float,
        heightCm: Float,
        headCircumferenceCm: Float?,
        controlDate: LocalDate = LocalDate.now()
    ): GrowthRecord {
        // Get WHO reference data
        val whoWeight = whoTableDao.getRow(sex.code, ageMonths, "WEIGHT")
        val whoHeight = whoTableDao.getRow(sex.code, ageMonths, "HEIGHT")

        val weightPercentile = if (whoWeight != null) {
            PercentileCalculator.calculatePercentile(
                weightKg, whoWeight.p3, whoWeight.p15, whoWeight.p50, whoWeight.p85, whoWeight.p97
            )
        } else 50f

        val heightPercentile = if (whoHeight != null) {
            PercentileCalculator.calculatePercentile(
                heightCm, whoHeight.p3, whoHeight.p15, whoHeight.p50, whoHeight.p85, whoHeight.p97
            )
        } else 50f

        val status = PercentileCalculator.determineNutritionalStatus(weightPercentile)
        val message = PercentileCalculator.generateMessage(status, ageMonths)

        val record = GrowthRecord(
            id = UUID.randomUUID().toString(),
            childId = childId,
            controlDate = controlDate,
            weightKg = weightKg,
            heightCm = heightCm,
            headCircumferenceCm = headCircumferenceCm,
            weightPercentile = weightPercentile,
            heightPercentile = heightPercentile,
            nutritionalStatus = status,
            messageForMother = message
        )

        growthRecordDao.insertRecord(record.toEntity())
        return record
    }

    // ---- WHO Reference Data ----
    suspend fun getWhoData(sex: Sex, ageMonths: Int, type: String) =
        whoTableDao.getRow(sex.code, ageMonths, type)

    // ---- Mappers ----
    private fun GrowthRecordEntity.toDomain(): GrowthRecord = GrowthRecord(
        id = id, childId = childId,
        controlDate = LocalDate.ofEpochDay(controlDateMillis / (24 * 60 * 60 * 1000L)),
        weightKg = weightKg, heightCm = heightCm,
        headCircumferenceCm = headCircumferenceCm,
        weightPercentile = weightPercentile, heightPercentile = heightPercentile,
        nutritionalStatus = NutritionalStatus.values().find { it.name == nutritionalStatus } ?: NutritionalStatus.NORMAL,
        messageForMother = messageForMother
    )

    private fun GrowthRecord.toEntity(): GrowthRecordEntity = GrowthRecordEntity(
        id = id, childId = childId,
        controlDateMillis = controlDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        weightKg = weightKg, heightCm = heightCm,
        headCircumferenceCm = headCircumferenceCm,
        weightPercentile = weightPercentile, heightPercentile = heightPercentile,
        nutritionalStatus = nutritionalStatus.name,
        messageForMother = messageForMother
    )
}
