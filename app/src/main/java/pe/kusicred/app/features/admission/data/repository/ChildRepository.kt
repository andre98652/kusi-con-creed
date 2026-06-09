package pe.kusicred.app.features.admission.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.kusicred.app.core.database.dao.ChildDao
import pe.kusicred.app.core.database.entity.ChildEntity
import pe.kusicred.app.core.util.*
import pe.kusicred.app.core.util.AgeCalculator.toEpochMillis
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildRepository @Inject constructor(
    private val childDao: ChildDao
) {
    fun getAllChildren(): Flow<List<Child>> = childDao.getAllChildren().map { list ->
        list.map { it.toDomain() }
    }

    fun getChildById(id: String): Flow<Child?> = childDao.getChildByIdFlow(id).map { it?.toDomain() }

    suspend fun saveChild(child: Child) {
        childDao.insertChild(child.toEntity())
    }

    suspend fun updateChild(child: Child) {
        childDao.updateChild(child.toEntity())
    }

    suspend fun deleteChild(child: Child) {
        childDao.deleteChild(child.toEntity())
    }

    suspend fun getChildCount(): Int = childDao.getChildCount()

    // ---- Mappers ----
    private fun ChildEntity.toDomain(): Child = Child(
        id = id, fullName = fullName,
        docType = DocType.values().find { it.name == docType } ?: DocType.DNI,
        docNumber = docNumber,
        birthDate = AgeCalculator.epochMillisToLocalDateTime(birthDateMillis),
        sex = if (sex == "M") Sex.MALE else Sex.FEMALE,
        birthWeightGrams = birthWeightGrams, birthHeightCm = birthHeightCm,
        gestationWeeks = gestationWeeks, isPremature = isPremature,
        guardianName = guardianName, guardianDni = guardianDni,
        guardianPhone = guardianPhone, guardianEmail = guardianEmail,
        insuranceType = InsuranceType.values().find { it.name == insuranceType } ?: InsuranceType.SIS,
        isPremium = isPremium
    )

    private fun Child.toEntity(): ChildEntity = ChildEntity(
        id = id, fullName = fullName, docType = docType.name,
        docNumber = docNumber, birthDateMillis = birthDate.toEpochMillis(),
        sex = sex.code, birthWeightGrams = birthWeightGrams, birthHeightCm = birthHeightCm,
        gestationWeeks = gestationWeeks, isPremature = isPremature,
        guardianName = guardianName, guardianDni = guardianDni,
        guardianPhone = guardianPhone, guardianEmail = guardianEmail,
        insuranceType = insuranceType.name, isPremium = isPremium
    )
}

fun createNewChildId(): String = UUID.randomUUID().toString()
