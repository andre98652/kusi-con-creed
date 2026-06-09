package pe.kusicred.app.core.database

import pe.kusicred.app.core.database.entity.MilestoneCatalogEntity
import pe.kusicred.app.core.database.entity.VaccineCatalogEntity
import pe.kusicred.app.core.database.entity.WhoTableEntity
import javax.inject.Provider

/**
 * DatabaseSeeder: pobla la base de datos con datos estáticos oficiales
 * - Tablas de percentiles OMS (peso y talla por sexo y edad en meses)
 * - Esquema Nacional de Vacunación MINSA Perú (NT N°141-MINSA/2018/DGIESP)
 * - Banco de hitos TPED (Test Peruano de Evaluación del Desarrollo del Niño)
 *
 * Se ejecuta UNA SOLA VEZ al crear la base de datos por primera vez.
 */
class DatabaseSeeder(private val dbProvider: Provider<AppDatabase>) {

    suspend fun seedDatabase() {
        seedWhoTables()
        seedVaccineCatalog()
        seedMilestoneCatalog()
    }

    // ================================================================
    // TABLAS OMS — Peso para edad (niños 0-24 meses)
    // Fuente: WHO Child Growth Standards (2006)
    // Unidades: kg para peso, cm para talla
    // ================================================================
    private suspend fun seedWhoTables() {
        val whoDao = dbProvider.get().whoTableDao()
        if (whoDao.getCount() > 0) return

        val rows = mutableListOf<WhoTableEntity>()

        // --- PESO NIÑOS (M) kg ---
        val weightBoys = listOf(
            // ageMonths, p3, p15, p50, p85, p97
            listOf(0, 2.5f, 2.9f, 3.3f, 3.9f, 4.4f),
            listOf(1, 3.4f, 3.9f, 4.5f, 5.1f, 5.8f),
            listOf(2, 4.3f, 4.9f, 5.6f, 6.3f, 7.1f),
            listOf(3, 5.0f, 5.7f, 6.4f, 7.2f, 8.0f),
            listOf(4, 5.6f, 6.2f, 7.0f, 7.8f, 8.7f),
            listOf(5, 6.0f, 6.7f, 7.5f, 8.4f, 9.3f),
            listOf(6, 6.4f, 7.1f, 7.9f, 8.8f, 9.8f),
            listOf(7, 6.7f, 7.4f, 8.3f, 9.2f, 10.3f),
            listOf(8, 6.9f, 7.7f, 8.6f, 9.6f, 10.7f),
            listOf(9, 7.1f, 7.9f, 8.9f, 9.9f, 11.0f),
            listOf(10, 7.4f, 8.2f, 9.2f, 10.2f, 11.4f),
            listOf(11, 7.6f, 8.4f, 9.4f, 10.5f, 11.7f),
            listOf(12, 7.7f, 8.6f, 9.6f, 10.8f, 12.0f),
            listOf(14, 8.1f, 9.0f, 10.1f, 11.3f, 12.6f),
            listOf(15, 8.3f, 9.2f, 10.3f, 11.5f, 12.8f),
            listOf(16, 8.4f, 9.4f, 10.5f, 11.7f, 13.1f),
            listOf(18, 8.8f, 9.8f, 10.9f, 12.2f, 13.7f),
            listOf(20, 9.2f, 10.2f, 11.3f, 12.7f, 14.2f),
            listOf(21, 9.3f, 10.4f, 11.5f, 12.9f, 14.5f),
            listOf(22, 9.5f, 10.6f, 11.8f, 13.2f, 14.7f),
            listOf(24, 9.7f, 10.8f, 12.2f, 13.6f, 15.3f)
        )
        weightBoys.forEach { r ->
            rows.add(WhoTableEntity(sex = "M", ageMonths = r[0].toInt(),
                measureType = "WEIGHT",
                p3 = (r[1] as Float), p15 = (r[2] as Float), p50 = (r[3] as Float),
                p85 = (r[4] as Float), p97 = (r[5] as Float)))
        }

        // --- PESO NIÑAS (F) kg ---
        val weightGirls = listOf(
            listOf(0, 2.4f, 2.8f, 3.2f, 3.7f, 4.2f),
            listOf(1, 3.2f, 3.6f, 4.2f, 4.8f, 5.5f),
            listOf(2, 3.9f, 4.5f, 5.1f, 5.8f, 6.6f),
            listOf(3, 4.5f, 5.2f, 5.8f, 6.6f, 7.5f),
            listOf(4, 5.0f, 5.7f, 6.4f, 7.3f, 8.2f),
            listOf(5, 5.4f, 6.1f, 6.9f, 7.8f, 8.8f),
            listOf(6, 5.7f, 6.5f, 7.3f, 8.2f, 9.3f),
            listOf(7, 6.0f, 6.8f, 7.6f, 8.6f, 9.8f),
            listOf(8, 6.3f, 7.0f, 7.9f, 9.0f, 10.2f),
            listOf(9, 6.5f, 7.3f, 8.2f, 9.3f, 10.5f),
            listOf(10, 6.7f, 7.5f, 8.5f, 9.6f, 10.9f),
            listOf(11, 6.9f, 7.7f, 8.7f, 9.9f, 11.2f),
            listOf(12, 7.0f, 7.9f, 8.9f, 10.1f, 11.5f),
            listOf(14, 7.4f, 8.3f, 9.4f, 10.6f, 12.1f),
            listOf(15, 7.6f, 8.5f, 9.6f, 10.9f, 12.4f),
            listOf(16, 7.7f, 8.7f, 9.8f, 11.1f, 12.6f),
            listOf(18, 8.1f, 9.1f, 10.2f, 11.6f, 13.2f),
            listOf(20, 8.4f, 9.5f, 10.7f, 12.1f, 13.7f),
            listOf(21, 8.6f, 9.7f, 10.9f, 12.3f, 14.0f),
            listOf(22, 8.7f, 9.9f, 11.1f, 12.5f, 14.3f),
            listOf(24, 9.0f, 10.2f, 11.5f, 13.0f, 14.8f)
        )
        weightGirls.forEach { r ->
            rows.add(WhoTableEntity(sex = "F", ageMonths = r[0].toInt(),
                measureType = "WEIGHT",
                p3 = (r[1] as Float), p15 = (r[2] as Float), p50 = (r[3] as Float),
                p85 = (r[4] as Float), p97 = (r[5] as Float)))
        }

        // --- TALLA NIÑOS (M) cm ---
        val heightBoys = listOf(
            listOf(0, 46.1f, 48.0f, 49.9f, 51.8f, 53.7f),
            listOf(1, 50.8f, 52.8f, 54.7f, 56.7f, 58.6f),
            listOf(2, 54.4f, 56.4f, 58.4f, 60.4f, 62.4f),
            listOf(3, 57.3f, 59.4f, 61.4f, 63.5f, 65.5f),
            listOf(4, 59.7f, 61.8f, 63.9f, 66.0f, 68.0f),
            listOf(5, 61.7f, 63.8f, 65.9f, 68.0f, 70.1f),
            listOf(6, 63.3f, 65.5f, 67.6f, 69.8f, 71.9f),
            listOf(7, 64.8f, 67.0f, 69.2f, 71.3f, 73.5f),
            listOf(8, 66.2f, 68.4f, 70.6f, 72.8f, 75.0f),
            listOf(9, 67.5f, 69.7f, 72.0f, 74.2f, 76.5f),
            listOf(10, 68.7f, 71.0f, 73.3f, 75.6f, 77.9f),
            listOf(11, 69.9f, 72.2f, 74.5f, 76.9f, 79.2f),
            listOf(12, 71.0f, 73.4f, 75.7f, 78.1f, 80.5f),
            listOf(14, 73.0f, 75.5f, 77.9f, 80.5f, 82.9f),
            listOf(15, 74.0f, 76.6f, 79.1f, 81.7f, 84.2f),
            listOf(16, 75.0f, 77.5f, 80.2f, 82.8f, 85.4f),
            listOf(18, 76.9f, 79.6f, 82.3f, 85.0f, 87.7f),
            listOf(20, 78.6f, 81.4f, 84.2f, 87.0f, 89.8f),
            listOf(21, 79.4f, 82.3f, 85.1f, 87.9f, 90.8f),
            listOf(22, 80.2f, 83.1f, 86.0f, 88.9f, 91.7f),
            listOf(24, 81.7f, 84.8f, 87.8f, 90.9f, 93.9f)
        )
        heightBoys.forEach { r ->
            rows.add(WhoTableEntity(sex = "M", ageMonths = r[0].toInt(),
                measureType = "HEIGHT",
                p3 = (r[1] as Float), p15 = (r[2] as Float), p50 = (r[3] as Float),
                p85 = (r[4] as Float), p97 = (r[5] as Float)))
        }

        // --- TALLA NIÑAS (F) cm ---
        val heightGirls = listOf(
            listOf(0, 45.4f, 47.3f, 49.1f, 51.0f, 52.9f),
            listOf(1, 49.8f, 51.7f, 53.7f, 55.6f, 57.6f),
            listOf(2, 53.0f, 55.0f, 57.1f, 59.1f, 61.1f),
            listOf(3, 55.6f, 57.7f, 59.8f, 61.9f, 64.0f),
            listOf(4, 57.8f, 59.9f, 62.1f, 64.3f, 66.4f),
            listOf(5, 59.6f, 61.8f, 64.0f, 66.2f, 68.5f),
            listOf(6, 61.2f, 63.5f, 65.7f, 68.0f, 70.3f),
            listOf(7, 62.7f, 65.0f, 67.3f, 69.6f, 71.9f),
            listOf(8, 64.0f, 66.4f, 68.7f, 71.1f, 73.5f),
            listOf(9, 65.3f, 67.7f, 70.1f, 72.6f, 75.0f),
            listOf(10, 66.5f, 69.0f, 71.5f, 74.0f, 76.4f),
            listOf(11, 67.7f, 70.2f, 72.8f, 75.3f, 77.8f),
            listOf(12, 68.9f, 71.4f, 74.0f, 76.6f, 79.2f),
            listOf(14, 71.0f, 73.7f, 76.4f, 79.1f, 81.8f),
            listOf(15, 72.0f, 74.8f, 77.5f, 80.3f, 83.1f),
            listOf(16, 73.0f, 75.8f, 78.6f, 81.5f, 84.3f),
            listOf(18, 74.9f, 77.8f, 80.7f, 83.7f, 86.6f),
            listOf(20, 76.7f, 79.7f, 82.7f, 85.7f, 88.7f),
            listOf(21, 77.5f, 80.6f, 83.7f, 86.7f, 89.8f),
            listOf(22, 78.4f, 81.5f, 84.6f, 87.7f, 90.8f),
            listOf(24, 80.0f, 83.2f, 86.4f, 89.6f, 92.9f)
        )
        heightGirls.forEach { r ->
            rows.add(WhoTableEntity(sex = "F", ageMonths = r[0].toInt(),
                measureType = "HEIGHT",
                p3 = (r[1] as Float), p15 = (r[2] as Float), p50 = (r[3] as Float),
                p85 = (r[4] as Float), p97 = (r[5] as Float)))
        }

        whoDao.insertAll(rows)
    }

    // ================================================================
    // ESQUEMA NACIONAL DE VACUNACIÓN — MINSA PERÚ
    // Fuente: Norma Técnica N°141-MINSA/2018/DGIESP
    // ================================================================
    private suspend fun seedVaccineCatalog() {
        val vaccineDao = dbProvider.get().vaccineCatalogDao()
        if (vaccineDao.getCount() > 0) return

        val vaccines = listOf(
            VaccineCatalogEntity(
                vaccineId = "BCG",
                name = "BCG (Bacilo Calmette-Guérin)",
                shortName = "BCG",
                description = "Protege contra las formas graves de tuberculosis en niños.",
                sideEffects = "Puede aparecer una pequeña pústula en el lugar de la inyección. Es normal y desaparece sola.",
                scheduledAgeMonths = 0,
                scheduledAgeDays = 0,
                doseNumber = 1,
                contextualTip = "Esta vacuna se aplica al nacer. No requiere preparación especial.",
                isNeonatal = true
            ),
            VaccineCatalogEntity(
                vaccineId = "HVB_RN",
                name = "Hepatitis B (Recién Nacido)",
                shortName = "HvB RN",
                description = "Primera dosis de protección contra la Hepatitis B, aplicada en las primeras horas de vida.",
                sideEffects = "Leve dolor en el lugar de la inyección. Es temporal.",
                scheduledAgeMonths = 0,
                scheduledAgeDays = 0,
                doseNumber = 1,
                contextualTip = "Se aplica en las primeras 12 horas de vida del bebé en la maternidad.",
                isNeonatal = true
            ),
            VaccineCatalogEntity(
                vaccineId = "PENTAVALENTE_1",
                name = "Pentavalente 1ra Dosis",
                shortName = "Pentavalente 1",
                description = "Protege contra 5 enfermedades: Difteria, Tos ferina, Tétanos, Hepatitis B y Haemophilus influenzae tipo b.",
                sideEffects = "Fiebre leve, irritabilidad y enrojecimiento en el lugar de la de la inyección durante 1-2 días.",
                scheduledAgeMonths = 2,
                scheduledAgeDays = 60,
                doseNumber = 1,
                contextualTip = "Faltan pocos días para la Pentavalente 1. Lleva un polo de manga corta o fácil de retirar. Ten a la mano paracetamol pediátrico por si le da fiebre."
            ),
            VaccineCatalogEntity(
                vaccineId = "IPV_1",
                name = "Polio Inactivada (IPV) 1ra Dosis",
                shortName = "IPV 1",
                description = "Protege contra la poliomielitis. Se aplica inyectable.",
                sideEffects = "Mínimos: leve sensibilidad en el lugar de la inyección.",
                scheduledAgeMonths = 2,
                scheduledAgeDays = 60,
                doseNumber = 1,
                contextualTip = "Se aplica junto con la Pentavalente a los 2 meses."
            ),
            VaccineCatalogEntity(
                vaccineId = "ROTAVIRUS_1",
                name = "Rotavirus 1ra Dosis",
                shortName = "Rotavirus 1",
                description = "Protege contra la diarrea severa por rotavirus, principal causa de hospitalización en bebés.",
                sideEffects = "Puede causar leve irritabilidad o diarrea transitoria.",
                scheduledAgeMonths = 2,
                scheduledAgeDays = 60,
                doseNumber = 1,
                contextualTip = "Es oral (gotitas). No dar de lactar 1 hora antes para que el bebé la tome bien."
            ),
            VaccineCatalogEntity(
                vaccineId = "NEUMO_1",
                name = "Neumocócica 1ra Dosis",
                shortName = "Neumocócica 1",
                description = "Protege contra la neumonía, meningitis y otitis causadas por Streptococcus pneumoniae.",
                sideEffects = "Fiebre leve, hinchazón en el sitio de aplicación.",
                scheduledAgeMonths = 2,
                scheduledAgeDays = 60,
                doseNumber = 1,
                contextualTip = "Si nota al bebé con fiebre después, aplica paracetamol y paños de agua tibia."
            ),
            VaccineCatalogEntity(
                vaccineId = "PENTAVALENTE_2",
                name = "Pentavalente 2da Dosis",
                shortName = "Pentavalente 2",
                description = "Segunda dosis de refuerzo contra las 5 enfermedades.",
                sideEffects = "Fiebre leve, irritabilidad. Similar a la primera dosis.",
                scheduledAgeMonths = 4,
                scheduledAgeDays = 120,
                doseNumber = 2,
                contextualTip = "Recuerda llevar el carnet de vacunas y un polo fácil de retirar. ¡Ya falta poco para los 4 meses!"
            ),
            VaccineCatalogEntity(
                vaccineId = "IPV_2",
                name = "Polio Inactivada (IPV) 2da Dosis",
                shortName = "IPV 2",
                description = "Segunda dosis de protección contra la polio.",
                sideEffects = "Mínimos.",
                scheduledAgeMonths = 4,
                scheduledAgeDays = 120,
                doseNumber = 2,
                contextualTip = "Se aplica junto con la Pentavalente a los 4 meses."
            ),
            VaccineCatalogEntity(
                vaccineId = "ROTAVIRUS_2",
                name = "Rotavirus 2da Dosis",
                shortName = "Rotavirus 2",
                description = "Segunda y última dosis de protección contra rotavirus.",
                sideEffects = "Mínima irritabilidad.",
                scheduledAgeMonths = 4,
                scheduledAgeDays = 120,
                doseNumber = 2,
                contextualTip = "Última dosis de rotavirus. No dar de lactar 1 hora antes."
            ),
            VaccineCatalogEntity(
                vaccineId = "NEUMO_2",
                name = "Neumocócica 2da Dosis",
                shortName = "Neumocócica 2",
                description = "Segunda dosis contra enfermedades neumocócicas.",
                sideEffects = "Fiebre leve posible.",
                scheduledAgeMonths = 4,
                scheduledAgeDays = 120,
                doseNumber = 2,
                contextualTip = "Prepara paracetamol por si acaso."
            ),
            VaccineCatalogEntity(
                vaccineId = "PENTAVALENTE_3",
                name = "Pentavalente 3ra Dosis",
                shortName = "Pentavalente 3",
                description = "Tercera y última dosis de la serie primaria de Pentavalente.",
                sideEffects = "Fiebre leve, irritabilidad.",
                scheduledAgeMonths = 6,
                scheduledAgeDays = 180,
                doseNumber = 3,
                contextualTip = "¡Última dosis de la Pentavalente! Tu bebé estará completamente protegido. ¡Gran logro!"
            ),
            VaccineCatalogEntity(
                vaccineId = "APO_1",
                name = "Antipolio Oral (APO)",
                shortName = "APO",
                description = "Vacuna oral de refuerzo contra la poliomielitis.",
                sideEffects = "Prácticamente ninguno.",
                scheduledAgeMonths = 6,
                scheduledAgeDays = 180,
                doseNumber = 1,
                contextualTip = "Son gotitas orales. Se aplica junto con las demás vacunas de los 6 meses."
            ),
            VaccineCatalogEntity(
                vaccineId = "INFLUENZA_1",
                name = "Influenza 1ra Dosis",
                shortName = "Influenza 1",
                description = "Protege contra la gripe estacional. Fundamental para menores de 2 años.",
                sideEffects = "Dolor leve en el sitio de inyección, fiebre baja.",
                scheduledAgeMonths = 6,
                scheduledAgeDays = 180,
                doseNumber = 1,
                contextualTip = "La influenza puede ser peligrosa en bebés. ¡Esta vacuna los protege! Se repite cada año."
            ),
            VaccineCatalogEntity(
                vaccineId = "INFLUENZA_2",
                name = "Influenza 2da Dosis",
                shortName = "Influenza 2",
                description = "Segunda dosis de refuerzo de influenza (solo para niños que reciben por primera vez).",
                sideEffects = "Mínimos.",
                scheduledAgeMonths = 7,
                scheduledAgeDays = 210,
                doseNumber = 2,
                contextualTip = "Esta es la segunda y última dosis inicial de influenza. ¡Ya tiene protección completa!"
            ),
            VaccineCatalogEntity(
                vaccineId = "SPR_1",
                name = "SPR (Sarampión, Parotiditis, Rubéola)",
                shortName = "SPR",
                description = "Triple viral. Protege contra el sarampión, paperas y rubéola.",
                sideEffects = "Entre el 5to y 12vo día puede aparecer fiebre o erupción leve. Es normal.",
                scheduledAgeMonths = 12,
                scheduledAgeDays = 365,
                doseNumber = 1,
                contextualTip = "¡Primer cumpleaños y primera dosis de la triple viral! Entre 5 y 12 días después puede tener fiebre leve. Es normal, es la vacuna actuando."
            ),
            VaccineCatalogEntity(
                vaccineId = "NEUMO_R",
                name = "Neumocócica Refuerzo",
                shortName = "Neumocócica R",
                description = "Dosis de refuerzo de la vacuna neumocócica al año de vida.",
                sideEffects = "Fiebre leve posible.",
                scheduledAgeMonths = 12,
                scheduledAgeDays = 365,
                doseNumber = 3,
                contextualTip = "Refuerzo de neumocócica al año. ¡Lleva el carnet actualizado!"
            ),
            VaccineCatalogEntity(
                vaccineId = "HVA_1",
                name = "Hepatitis A 1ra Dosis",
                shortName = "Hepatitis A",
                description = "Protege contra la Hepatitis A, transmitida por agua o alimentos contaminados.",
                sideEffects = "Dolor en el sitio de inyección, malestar leve.",
                scheduledAgeMonths = 12,
                scheduledAgeDays = 365,
                doseNumber = 1,
                contextualTip = "Importante para proteger al bebé de la Hepatitis A, muy común en Perú."
            ),
            VaccineCatalogEntity(
                vaccineId = "VARICELA_1",
                name = "Varicela 1ra Dosis",
                shortName = "Varicela",
                description = "Protege contra la varicela (chickenpox). Una sola dosis da alta protección.",
                sideEffects = "Puede aparecer erupción leve similar a la varicela entre 7-21 días después.",
                scheduledAgeMonths = 12,
                scheduledAgeDays = 365,
                doseNumber = 1,
                contextualTip = "¡Adiós a la varicela! Una sola dosis al año de vida."
            ),
            VaccineCatalogEntity(
                vaccineId = "APO_2",
                name = "Antipolio Oral Refuerzo (APO)",
                shortName = "APO Refuerzo",
                description = "Dosis de refuerzo de la vacuna oral contra la polio.",
                sideEffects = "Ninguno.",
                scheduledAgeMonths = 18,
                scheduledAgeDays = 540,
                doseNumber = 2,
                contextualTip = "Refuerzo de polio al año y medio. ¡Casi completamos el esquema!"
            ),
            VaccineCatalogEntity(
                vaccineId = "PENTAVALENTE_R",
                name = "DPT Refuerzo (Pentavalente)",
                shortName = "DPT Refuerzo",
                description = "Refuerzo de Difteria, Pertussis y Tétanos al año y medio.",
                sideEffects = "Posible fiebre y malestar leve.",
                scheduledAgeMonths = 18,
                scheduledAgeDays = 540,
                doseNumber = 4,
                contextualTip = "Refuerzo importante. Asegúrate de tenerlo en el carnet."
            ),
            VaccineCatalogEntity(
                vaccineId = "SPR_2",
                name = "SPR Segunda Dosis",
                shortName = "SPR 2",
                description = "Segunda dosis de triple viral para refuerzo completo.",
                sideEffects = "Similar a la primera dosis.",
                scheduledAgeMonths = 18,
                scheduledAgeDays = 540,
                doseNumber = 2,
                contextualTip = "Segunda dosis de la triple viral. ¡Protección reforzada contra sarampión, paperas y rubéola!"
            )
        )

        vaccineDao.insertAll(vaccines)
    }

    // ================================================================
    // HITOS DEL DESARROLLO — TPED MINSA (Test Peruano de Evaluación)
    // Fuente: MINSA Perú - Test de Evaluación del Desarrollo del Niño
    // ================================================================
    private suspend fun seedMilestoneCatalog() {
        val milestoneDao = dbProvider.get().milestoneCatalogDao()
        if (milestoneDao.getCount() > 0) return

        val milestones = mutableListOf<MilestoneCatalogEntity>()

        // ============ MES 1 ============
        milestones.addAll(listOf(
            MilestoneCatalogEntity("M1_MOT1", 1, "MOTORA",
                "¿Levanta la cabeza brevemente cuando está boca abajo?",
                "Al poner al bebé boca abajo, debería poder levantar la cabeza por un momento.",
                null, "Practica el 'tummy time' (boca abajo) por períodos cortos varias veces al día sobre una superficie firme."),
            MilestoneCatalogEntity("M1_LEN1", 1, "LENGUAJE",
                "¿Reacciona a los sonidos girando la cabeza?",
                "El bebé debe responder a sonidos como tu voz o un sonajero girando la cabeza.",
                null, "Habla constantemente con tu bebé, cántale y pon música suave. La estimulación auditiva es clave."),
            MilestoneCatalogEntity("M1_SOC1", 1, "SOCIAL",
                "¿Fija la mirada en el rostro de su cuidador?",
                "El bebé debe poder mirar fijamente el rostro de mamá o papá cuando están cerca.",
                null, "Acerca tu cara a unos 20-30 cm de sus ojos. Háblale suavemente. El contacto visual es el primer vínculo."),
            MilestoneCatalogEntity("M1_COG1", 1, "COGNITIVA",
                "¿Calma al ser alzado o cuando escucha tu voz?",
                "El bebé se calma ante el tacto familiar o la voz conocida.",
                null, "Cárgalo con frecuencia, no lo malacostumbras. El contacto físico y la voz calman y estimulan su cerebro.")
        ))

        // ============ MES 2 ============
        milestones.addAll(listOf(
            MilestoneCatalogEntity("M2_MOT1", 2, "MOTORA",
                "¿Levanta la cabeza 45° cuando está boca abajo?",
                "Ahora levanta la cabeza a un ángulo mayor y la mantiene por más tiempo.",
                null, "Continúa el tummy time. Usa un rollo pequeño bajo el pecho para ayudarlo."),
            MilestoneCatalogEntity("M2_LEN1", 2, "LENGUAJE",
                "¿Hace sonidos guturales o 'arrullos'?",
                "Empieza a hacer soniditos más allá del llanto.",
                null, "Imita sus sonidos. Cuando él hace 'aaa', tú repite 'aaa'. Así aprende que comunicarse tiene respuesta."),
            MilestoneCatalogEntity("M2_SOC1", 2, "SOCIAL",
                "¿Sonríe en respuesta a tu sonrisa o tu voz?",
                "La sonrisa social aparece al mes y medio o dos meses. Es un hito muy importante.",
                null, "¡Sonríele constantemente! Si no aparece la sonrisa social a los 2 meses, coméntalo al pediatra."),
            MilestoneCatalogEntity("M2_COG1", 2, "COGNITIVA",
                "¿Sigue con la vista un objeto que se mueve lentamente?",
                "Puede seguir un juguete o tu dedo que se mueve de lado a lado.",
                null, "Usa un juguete de colores brillantes (rojo, negro) y muévelo lentamente frente a sus ojos.")
        ))

        // ============ MES 3 ============
        milestones.addAll(listOf(
            MilestoneCatalogEntity("M3_MOT1", 3, "MOTORA",
                "¿Sostiene la cabeza erguida cuando está en posición vertical?",
                "Cuando lo alzas verticalmente, controla la cabeza sin que se caiga.",
                null, "Carga al bebé en posición vertical apoyando su cabeza. Poco a poco retira el apoyo por segundos."),
            MilestoneCatalogEntity("M3_LEN1", 3, "LENGUAJE",
                "¿Hace sonidos como respuesta cuando le hablas?",
                "Inicia una 'conversación' básica: tú hablas, él responde con sonidos.",
                null, "Pausa después de hablarle y espera su respuesta. Este 'turno de conversación' es el primer paso del lenguaje."),
            MilestoneCatalogEntity("M3_SOC1", 3, "SOCIAL",
                "¿Reconoce el rostro de su cuidador principal?",
                "Se emociona claramente cuando ve a mamá o papá.",
                null, "Muéstrale fotos de la familia. Nombra a cada persona: 'Esta es la abuela'."),
            MilestoneCatalogEntity("M3_COG1", 3, "COGNITIVA",
                "¿Abre y cierra las manos, y las mira?",
                "Descubre sus manos y las observa con curiosidad.",
                null, "Pon un sonajero suave en su mano y deja que lo explore. Las manos son el primer 'juguete'.")
        ))

        // ============ MES 4 ============
        milestones.addAll(listOf(
            MilestoneCatalogEntity("M4_MOT1", 4, "MOTORA",
                "¿Empuja hacia abajo con las piernas cuando está parado sobre una superficie?",
                "Cuando lo sostienes de pie, empuja hacia abajo con sus piernas.",
                null, "Sosteniéndolo firmemente, apoya sus pies en una superficie dura. Es un juego, no lo fuerces."),
            MilestoneCatalogEntity("M4_MOT2", 4, "MOTORA",
                "¿Alcanza objetos con ambas manos?",
                "Intenta alcanzar y agarrar juguetes que están frente a él.",
                null, "Cuelga juguetes coloridos a su alcance cuando está boca arriba. Lo motivará a extender los brazos."),
            MilestoneCatalogEntity("M4_LEN1", 4, "LENGUAJE",
                "¿Balbucea cadenas de sonidos (ba-ba, da-da)?",
                "Empieza el balbuceo más elaborado con consonantes.",
                null, "Repite sus balbuceos y agrega palabras simples. '¡Ba-ba-ba! ¡Muy bien!'"),
            MilestoneCatalogEntity("M4_SOC1", 4, "SOCIAL",
                "¿Ríe a carcajadas?",
                "La risa fuerte aparece alrededor de los 4 meses.",
                null, "Juega a las cosquillas suaves, cánticos y caras graciosas. La risa es señal de vínculo y bienestar."),
            MilestoneCatalogEntity("M4_COG1", 4, "COGNITIVA",
                "¿Reconoce rostros familiares a distancia?",
                "Se emociona cuando ve a personas conocidas desde lejos.",
                null, "Visita a la familia regularmente para ampliar su círculo de afecto.")
        ))

        // ============ MES 6 ============
        milestones.addAll(listOf(
            MilestoneCatalogEntity("M6_MOT1", 6, "MOTORA",
                "¿Se sienta con apoyo?",
                "Puede mantener la posición sentada cuando lo apoyamos con cojines o nuestras manos.",
                null, "Siéntalo rodeado de cojines en el piso. Nunca lo dejes solo en esta posición."),
            MilestoneCatalogEntity("M6_MOT2", 6, "MOTORA",
                "¿Se da vuelta de boca arriba a boca abajo?",
                "Logra girar solo de una posición a la otra.",
                null, "Pon un juguete llamativo a un lado para motivarlo a girar hacia él."),
            MilestoneCatalogEntity("M6_LEN1", 6, "LENGUAJE",
                "¿Responde a su nombre girando la cabeza?",
                "Reconoce que su nombre lo llama a él.",
                null, "Llámalo por su nombre frecuentemente. Cuando gire, celébralo con aplausos."),
            MilestoneCatalogEntity("M6_SOC1", 6, "SOCIAL",
                "¿Muestra preferencia por las personas conocidas?",
                "Puede llorar o voltearse cuando lo carga una persona desconocida. Es normal.",
                null, "La ansiedad ante extraños es señal de vínculo saludable. No lo fuerce a interactuar."),
            MilestoneCatalogEntity("M6_COG1", 6, "COGNITIVA",
                "¿Busca un juguete que cayó o quedó fuera de su vista?",
                "Empieza a entender que los objetos existen aunque no los vea (permanencia del objeto).",
                null, "Tapa un juguete con un pañuelo y pregunta '¿Dónde está?'. Destápalo y celébralo.")
        ))

        // ============ MES 9 ============
        milestones.addAll(listOf(
            MilestoneCatalogEntity("M9_MOT1", 9, "MOTORA",
                "¿Se sienta solo sin apoyo?",
                "Se mantiene sentado de forma independiente.",
                null, "Pon juguetes alrededor de él cuando está sentado para que practique el equilibrio."),
            MilestoneCatalogEntity("M9_MOT2", 9, "MOTORA",
                "¿Gatea o se arrastra para desplazarse?",
                "Usa alguna forma de locomoción en el piso.",
                null, "Crea un espacio seguro en el piso para que explore libremente. El gateo es fundamental."),
            MilestoneCatalogEntity("M9_LEN1", 9, "LENGUAJE",
                "¿Dice 'mamá' o 'papá' con intención?",
                "Usa estas palabras para llamar a sus cuidadores.",
                null, "Responde siempre cuando te llama. Refuerza: '¡Sí! ¡Aquí está mamá!'"),
            MilestoneCatalogEntity("M9_SOC1", 9, "SOCIAL",
                "¿Juega a dar y recibir objetos?",
                "Entiende el intercambio y disfruta el juego conjunto.",
                null, "Juega a pasarse una pelota o bloques. Es el primer juego cooperativo."),
            MilestoneCatalogEntity("M9_COG1", 9, "COGNITIVA",
                "¿Señala objetos con el dedo índice?",
                "Usa el gesto de señalar para comunicar interés.",
                null, "Cuando señale, nombra lo que señala: '¡Sí! ¡Es un perro!'")
        ))

        // ============ MES 12 ============
        milestones.addAll(listOf(
            MilestoneCatalogEntity("M12_MOT1", 12, "MOTORA",
                "¿Se pone de pie solo agarrándose de algo?",
                "Se incorpora usando muebles o paredes de apoyo.",
                null, "Asegura los muebles a la pared. A esta edad todo le sirve de apoyo."),
            MilestoneCatalogEntity("M12_MOT2", 12, "MOTORA",
                "¿Da algunos pasos solo o caminando apoyado?",
                "Primeros pasos independientes o caminando de la mano.",
                null, "No uses andadores, retrasan el desarrollo. Déjalo caminar descalzo sobre el piso."),
            MilestoneCatalogEntity("M12_LEN1", 12, "LENGUAJE",
                "¿Dice al menos 2 palabras con significado?",
                "Tiene un vocabulario inicial de palabras simples con sentido.",
                null, "Léele libros con imágenes. Nombra todo lo que ve: 'perro', 'gato', 'pelota'."),
            MilestoneCatalogEntity("M12_SOC1", 12, "SOCIAL",
                "¿Imita acciones de adultos (aplaudir, decir adiós)?",
                "Copia gestos simples.",
                null, "Haz gestos repetidamente: aplausos, adiós, besos. Los imitará con entusiasmo."),
            MilestoneCatalogEntity("M12_COG1", 12, "COGNITIVA",
                "¿Usa objetos correctamente (peine en el pelo, taza para beber)?",
                "Entiende para qué sirve cada objeto.",
                null, "Dale juguetes funcionales: cucharas de juguete, tazas. Muéstrale cómo usarlos.")
        ))

        // ============ MES 18 ============
        milestones.addAll(listOf(
            MilestoneCatalogEntity("M18_MOT1", 18, "MOTORA",
                "¿Camina solo con buen equilibrio?",
                "Camina de forma independiente y estable.",
                null, "Anímalo a caminar en diferentes superficies: pasto, arena, adoquines."),
            MilestoneCatalogEntity("M18_MOT2", 18, "MOTORA",
                "¿Sube escaleras gateando o con apoyo?",
                "Intenta subir escalones con ayuda.",
                null, "Supervisa siempre. Practica en escalones bajos con tu mano extendida."),
            MilestoneCatalogEntity("M18_LEN1", 18, "LENGUAJE",
                "¿Dice al menos 10 palabras?",
                "Vocabulario de al menos 10 palabras diferentes.",
                null, "Habla mucho con él. Describe todo lo que hacen: 'Vamos a lavar las manos'."),
            MilestoneCatalogEntity("M18_SOC1", 18, "SOCIAL",
                "¿Juega junto a otros niños (juego paralelo)?",
                "Juega al lado de otros niños aunque no necesariamente con ellos.",
                null, "Llévalo a parques y grupos de juego. La interacción con pares es clave."),
            MilestoneCatalogEntity("M18_COG1", 18, "COGNITIVA",
                "¿Señala partes del cuerpo cuando se le pregunta?",
                "Señala nariz, ojos, boca, orejas al pedirlo.",
                null, "Juega al 'Simón dice' con partes del cuerpo. '¿Dónde está tu nariz?'")
        ))

        // ============ MES 24 ============
        milestones.addAll(listOf(
            MilestoneCatalogEntity("M24_MOT1", 24, "MOTORA",
                "¿Corre sin caerse frecuentemente?",
                "Corre con mejor coordinación.",
                null, "Juega a correr con él en espacios abiertos y seguros."),
            MilestoneCatalogEntity("M24_MOT2", 24, "MOTORA",
                "¿Patea una pelota?",
                "Coordina el movimiento para patear una pelota.",
                null, "Juega a patear pelotas de diferentes tamaños."),
            MilestoneCatalogEntity("M24_LEN1", 24, "LENGUAJE",
                "¿Une 2 palabras para comunicarse (más agua, mamá ven)?",
                "Forma frases simples de dos palabras.",
                null, "Si aún no une palabras, habla más pausado y completa sus frases: '¿Quieres más agua?'"),
            MilestoneCatalogEntity("M24_SOC1", 24, "SOCIAL",
                "¿Juega juegos de simulación (da de comer a un muñeco, habla por teléfono)?",
                "Inicio del juego simbólico o de roles.",
                null, "Regálale un set de cocina de juguete o muñecas para estimular el juego simbólico."),
            MilestoneCatalogEntity("M24_COG1", 24, "COGNITIVA",
                "¿Puede seguir instrucciones de 2 pasos?",
                "Entiende y ejecuta órdenes dobles.",
                null, "Da instrucciones de 2 pasos: 'Busca tu zapato y dámelo'. Celebra cuando lo logra.")
        ))

        milestoneDao.insertAll(milestones)
    }
}
