package pe.kusicred.app.core.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId

// ============================================================
// DOMAIN MODELS — Data classes de dominio
// ============================================================

data class Child(
    val id: String,
    val fullName: String,
    val docType: DocType,
    val docNumber: String,
    val birthDate: LocalDateTime,
    val sex: Sex,
    val birthWeightGrams: Float,
    val birthHeightCm: Float,
    val gestationWeeks: Int,
    val isPremature: Boolean,
    val guardianName: String,
    val guardianDni: String,
    val guardianPhone: String,
    val guardianEmail: String,
    val insuranceType: InsuranceType,
    val isPremium: Boolean = false
)

data class GrowthRecord(
    val id: String,
    val childId: String,
    val controlDate: LocalDate,
    val weightKg: Float,
    val heightCm: Float,
    val headCircumferenceCm: Float?,
    val weightPercentile: Float,
    val heightPercentile: Float,
    val nutritionalStatus: NutritionalStatus,
    val messageForMother: String
)

data class VaccineScheduleItem(
    val id: String,
    val childId: String,
    val vaccine: VaccineCatalog,
    val scheduledDate: LocalDate,
    val appliedDate: LocalDate?,
    val status: VaccineStatus,
    val rescheduledDate: LocalDate?,
    val notes: String?
)

data class VaccineCatalog(
    val vaccineId: String,
    val name: String,
    val shortName: String,
    val description: String,
    val sideEffects: String,
    val contextualTip: String,
    val scheduledAgeMonths: Int,
    val scheduledAgeDays: Int,
    val doseNumber: Int,
    val isNeonatal: Boolean
)

data class IronRecord(
    val id: String,
    val childId: String,
    val date: LocalDate,
    val taken: Boolean
)

data class Milestone(
    val milestoneId: String,
    val ageMonths: Int,
    val area: MilestoneArea,
    val question: String,
    val description: String,
    val illustration: String?,
    val stimulationTip: String
)

data class MilestoneResponse(
    val id: String,
    val childId: String,
    val milestoneId: String,
    val evaluationDate: LocalDate,
    val response: MilestoneResponseType
)

data class ChildAge(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalMonths: Int,
    val totalDays: Int
) {
    fun asReadableString(): String = when {
        years > 0 -> "$years año${if (years > 1) "s" else ""} y $months mes${if (months != 1) "es" else ""}"
        months > 0 -> "$months mes${if (months != 1) "es" else ""} y $days día${if (days != 1) "s" else ""}"
        else -> "$days día${if (days != 1) "s" else ""}"
    }
}

// ============================================================
// ENUMS
// ============================================================

enum class DocType(val displayName: String) {
    DNI("DNI"),
    CNV("CNV (Certificado de Nacido Vivo)")
}

enum class Sex(val displayName: String, val code: String) {
    MALE("Masculino", "M"),
    FEMALE("Femenino", "F")
}

enum class InsuranceType(val displayName: String) {
    SIS("SIS"),
    ESSALUD("EsSalud"),
    PRIVADO("Seguro Privado"),
    NINGUNO("Sin seguro")
}

enum class NutritionalStatus(val displayName: String, val color: Long) {
    SEVERE_MALNUTRITION("Desnutrición Severa", 0xFF8B0000),
    MALNUTRITION("Desnutrición", 0xFFE53935),
    AT_RISK("Riesgo de Desnutrición", 0xFFFFB347),
    NORMAL("Normal", 0xFF4CAF82),
    OVERWEIGHT("Sobrepeso", 0xFFFF8C00),
    OBESITY("Obesidad", 0xFFE53935),
    UNKNOWN("No determinado", 0xFFB0C9B8)
}

enum class VaccineStatus(val displayName: String) {
    PENDING("Pendiente"),
    APPLIED("Aplicada"),
    RESCHEDULED("Reprogramada"),
    OVERDUE("Vencida")
}

enum class MilestoneArea(val displayName: String, val emoji: String, val colorHex: Long) {
    MOTORA("Área Motora", "🏃", 0xFF4CAF82),
    LENGUAJE("Área de Lenguaje", "💬", 0xFF5B9BD5),
    SOCIAL("Área Social", "🤝", 0xFFFF8C69),
    COGNITIVA("Área Cognitiva", "🧠", 0xFFFFB347)
}

enum class MilestoneResponseType(val displayName: String, val emoji: String) {
    YES("Sí", "✅"),
    SOMETIMES("A veces", "🔄"),
    NOT_YET("Aún no", "❌")
}

// ============================================================
// AGE CALCULATOR
// ============================================================

object AgeCalculator {
    fun calculate(birthDate: LocalDateTime, referenceDate: LocalDate = LocalDate.now()): ChildAge {
        val birth = birthDate.toLocalDate()
        val period = Period.between(birth, referenceDate)
        val totalDays = birth.until(referenceDate, java.time.temporal.ChronoUnit.DAYS).toInt()
        val totalMonths = birth.until(referenceDate, java.time.temporal.ChronoUnit.MONTHS).toInt()
        return ChildAge(
            years = period.years,
            months = period.months,
            days = period.days,
            totalMonths = totalMonths,
            totalDays = totalDays
        )
    }

    fun calculateCorrected(birthDate: LocalDateTime, gestationWeeks: Int): ChildAge {
        val correctionDays = (40 - gestationWeeks) * 7L
        val correctedBirth = birthDate.toLocalDate().plusDays(correctionDays)
        val today = LocalDate.now()
        val period = Period.between(correctedBirth, today)
        val totalMonths = correctedBirth.until(today, java.time.temporal.ChronoUnit.MONTHS).toInt()
        val totalDays = correctedBirth.until(today, java.time.temporal.ChronoUnit.DAYS).toInt()
        return ChildAge(
            years = period.years,
            months = period.months,
            days = period.days,
            totalMonths = totalMonths.coerceAtLeast(0),
            totalDays = totalDays.coerceAtLeast(0)
        )
    }

    fun LocalDateTime.toEpochMillis(): Long =
        atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun epochMillisToLocalDateTime(millis: Long): LocalDateTime =
        LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(millis),
            ZoneId.systemDefault()
        )
}

// ============================================================
// PERCENTILE CALCULATOR
// ============================================================

object PercentileCalculator {

    /**
     * Calcula el percentil de una medida comparando contra la tabla OMS.
     * Retorna un valor entre 0.0 y 100.0 aproximado.
     */
    fun calculatePercentile(
        value: Float,
        p3: Float, p15: Float, p50: Float, p85: Float, p97: Float
    ): Float {
        return when {
            value <= p3 -> lerp(0f, 3f, 0f, p3, value)
            value <= p15 -> lerp(3f, 15f, p3, p15, value)
            value <= p50 -> lerp(15f, 50f, p15, p50, value)
            value <= p85 -> lerp(50f, 85f, p50, p85, value)
            value <= p97 -> lerp(85f, 97f, p85, p97, value)
            else -> lerp(97f, 100f, p97, p97 * 1.1f, value).coerceAtMost(100f)
        }
    }

    private fun lerp(pMin: Float, pMax: Float, vMin: Float, vMax: Float, v: Float): Float {
        if (vMax == vMin) return pMin
        return pMin + (pMax - pMin) * ((v - vMin) / (vMax - vMin))
    }

    /**
     * Determina el estado nutricional según los percentiles OMS para peso.
     */
    fun determineNutritionalStatus(weightPercentile: Float): NutritionalStatus = when {
        weightPercentile < 3f -> NutritionalStatus.SEVERE_MALNUTRITION
        weightPercentile < 15f -> NutritionalStatus.MALNUTRITION
        weightPercentile < 85f -> NutritionalStatus.NORMAL
        weightPercentile < 97f -> NutritionalStatus.OVERWEIGHT
        else -> NutritionalStatus.OBESITY
    }

    /**
     * Genera el mensaje empático para la madre según el estado nutricional.
     */
    fun generateMessage(status: NutritionalStatus, ageMonths: Int): String = when (status) {
        NutritionalStatus.NORMAL ->
            "¡Genial! Tu bebé está en el rango ideal de crecimiento para sus $ageMonths meses. ¡Sigue así, mamá! 🌟"
        NutritionalStatus.AT_RISK ->
            "Tu bebé está creciendo, pero está cerca del límite inferior. Es bueno comentarlo con el médico en el próximo control. 💛"
        NutritionalStatus.MALNUTRITION ->
            "El peso de tu bebé está por debajo del rango normal. Te recomendamos buscar orientación nutricional pronto. El equipo de salud puede ayudarte. 🩺"
        NutritionalStatus.SEVERE_MALNUTRITION ->
            "⚠️ El peso de tu bebé necesita atención médica urgente. Por favor, visita un centro de salud lo antes posible."
        NutritionalStatus.OVERWEIGHT ->
            "Tu bebé está un poco por encima del rango normal. Puedes comentarlo con el pediatra en el próximo control. 💛"
        NutritionalStatus.OBESITY ->
            "El peso de tu bebé está por encima del rango saludable. El médico puede orientarte sobre la alimentación. 🩺"
        NutritionalStatus.UNKNOWN ->
            "No pudimos calcular el estado nutricional. Asegúrate de ingresar datos correctos."
    }
}
