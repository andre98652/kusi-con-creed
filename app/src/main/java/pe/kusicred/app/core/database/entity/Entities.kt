package pe.kusicred.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// ============================================================
// CHILD — Perfil del niño
// ============================================================
@Entity(tableName = "children")
data class ChildEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val docType: String,           // "DNI" | "CNV"
    val docNumber: String,
    val birthDateMillis: Long,     // fecha+hora exacta en millis
    val sex: String,               // "M" | "F"
    val birthWeightGrams: Float,
    val birthHeightCm: Float,
    val gestationWeeks: Int,
    val isPremature: Boolean,      // gestationWeeks < 37
    val guardianName: String,
    val guardianDni: String,
    val guardianPhone: String,
    val guardianEmail: String,
    val insuranceType: String,     // "SIS" | "ESSALUD" | "PRIVADO"
    val isPremium: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

// ============================================================
// GROWTH RECORD — Registro de control de crecimiento
// ============================================================
@Entity(tableName = "growth_records")
data class GrowthRecordEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val controlDateMillis: Long,
    val weightKg: Float,
    val heightCm: Float,
    val headCircumferenceCm: Float?,
    val weightPercentile: Float,
    val heightPercentile: Float,
    val nutritionalStatus: String, // NutritionalStatus enum name
    val messageForMother: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

// ============================================================
// WHO TABLE — Tabla de percentiles de la OMS
// ============================================================
@Entity(tableName = "who_table")
data class WhoTableEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sex: String,               // "M" | "F"
    val ageMonths: Int,
    val measureType: String,       // "WEIGHT" | "HEIGHT" | "HEAD"
    val p3: Float,
    val p15: Float,
    val p50: Float,
    val p85: Float,
    val p97: Float
)

// ============================================================
// VACCINE CATALOG — Catálogo estático de vacunas MINSA
// ============================================================
@Entity(tableName = "vaccine_catalog")
data class VaccineCatalogEntity(
    @PrimaryKey val vaccineId: String,
    val name: String,
    val shortName: String,
    val description: String,
    val sideEffects: String,
    val scheduledAgeMonths: Int,   // meses desde nacimiento
    val scheduledAgeDays: Int,     // días desde nacimiento (para RN)
    val doseNumber: Int,
    val contextualTip: String,     // mensaje para la madre
    val isNeonatal: Boolean = false
)

// ============================================================
// VACCINE RECORD — Vacunas programadas por niño
// ============================================================
@Entity(tableName = "vaccine_records")
data class VaccineRecordEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val vaccineId: String,
    val scheduledDateMillis: Long,
    val appliedDateMillis: Long?,
    val status: String,            // "PENDING" | "APPLIED" | "RESCHEDULED" | "OVERDUE"
    val rescheduledDateMillis: Long?,
    val notes: String?
)

// ============================================================
// IRON SUPPLEMENT — Registro diario de gotas de hierro
// ============================================================
@Entity(tableName = "iron_records")
data class IronRecordEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val dateMillis: Long,
    val taken: Boolean,
    val createdAtMillis: Long = System.currentTimeMillis()
)

// ============================================================
// MILESTONE CATALOG — Catálogo de hitos TPED MINSA
// ============================================================
@Entity(tableName = "milestone_catalog")
data class MilestoneCatalogEntity(
    @PrimaryKey val milestoneId: String,
    val ageMonths: Int,            // mes de vida donde aplica
    val area: String,              // "MOTORA" | "LENGUAJE" | "SOCIAL" | "COGNITIVA"
    val question: String,
    val description: String,
    val illustration: String?,     // nombre del drawable resource
    val stimulationTip: String     // consejo si "Aún no"
)

// ============================================================
// MILESTONE RESPONSE — Respuestas de la madre por evaluación
// ============================================================
@Entity(tableName = "milestone_responses")
data class MilestoneResponseEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val milestoneId: String,
    val evaluationDateMillis: Long,
    val response: String,          // "YES" | "SOMETIMES" | "NOT_YET"
    val createdAtMillis: Long = System.currentTimeMillis()
)

// ============================================================
// APP PREFERENCES — Configuración de la app
// ============================================================
@Entity(tableName = "app_preferences")
data class AppPreferenceEntity(
    @PrimaryKey val key: String,
    val value: String
)
