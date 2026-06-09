package pe.kusicred.app.features.premium.util

import android.content.Context
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import pe.kusicred.app.core.util.Child
import pe.kusicred.app.core.util.GrowthRecord
import pe.kusicred.app.core.util.Milestone
import pe.kusicred.app.core.util.MilestoneResponse
import pe.kusicred.app.features.vaccines.data.repository.VaccineWithRecord
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ClinicalReportBuilder {

    fun generateReport(
        context: Context,
        file: File,
        child: Child,
        growthRecords: List<GrowthRecord>,
        vaccineRecords: List<VaccineWithRecord>,
        milestoneResponses: List<Pair<Milestone, MilestoneResponse>>
    ) {
        val document = Document(PageSize.A4, 36f, 36f, 36f, 36f)
        val writer = PdfWriter.getInstance(document, FileOutputStream(file))
        document.open()

        val mainColor = BaseColor(76, 175, 130) // KusiGreen40 (#4CAF82)
        val secondaryColor = BaseColor(91, 155, 213) // KusiBlue (#5B9BD5)
        val textColor = BaseColor(30, 30, 30)

        // Fonts
        val titleFont = Font(Font.FontFamily.HELVETICA, 22f, Font.BOLD, mainColor)
        val subtitleFont = Font(Font.FontFamily.HELVETICA, 10f, Font.ITALIC, BaseColor.GRAY)
        val sectionHeaderFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, secondaryColor)
        val labelFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, textColor)
        val valueFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, textColor)
        val tableHeaderFont = Font(Font.FontFamily.HELVETICA, 9f, Font.BOLD, BaseColor.WHITE)
        val tableBodyFont = Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL, textColor)

        // Header Title
        val title = Paragraph("REPORTE CLÍNICO DE SEGUIMIENTO (CRED)", titleFont)
        title.alignment = Element.ALIGN_CENTER
        title.spacingAfter = 2f
        document.add(title)

        // Date generated
        val dateGenerated = Paragraph(
            "Generado el: ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} a través de Kusi-CRED",
            subtitleFont
        )
        dateGenerated.alignment = Element.ALIGN_CENTER
        dateGenerated.spacingAfter = 16f
        document.add(dateGenerated)

        // Horizontal Line
        val line = Paragraph("----------------------------------------------------------------------------------------------------------------------------------", subtitleFont)
        line.spacingAfter = 10f
        document.add(line)

        // Section 1: Datos Personales
        val sec1Header = Paragraph("1. DATOS PERSONALES DEL NIÑO Y TUTOR", sectionHeaderFont)
        sec1Header.spacingAfter = 8f
        document.add(sec1Header)

        val personalTable = PdfPTable(4)
        personalTable.widthPercentage = 100f
        personalTable.setWidths(floatArrayOf(1.2f, 1.8f, 1.2f, 1.8f))

        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        // Rows
        addCell(personalTable, "Nombre Completo:", labelFont)
        addCell(personalTable, child.fullName, valueFont)
        addCell(personalTable, "Sexo:", labelFont)
        addCell(personalTable, child.sex.displayName, valueFont)

        addCell(personalTable, "Documento:", labelFont)
        addCell(personalTable, "${child.docType.name}: ${child.docNumber}", valueFont)
        addCell(personalTable, "Fecha de Nacimiento:", labelFont)
        addCell(personalTable, child.birthDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), valueFont)

        addCell(personalTable, "Peso al nacer:", labelFont)
        addCell(personalTable, "${child.birthWeightGrams / 1000} kg", valueFont)
        addCell(personalTable, "Talla al nacer:", labelFont)
        addCell(personalTable, "${child.birthHeightCm} cm", valueFont)

        addCell(personalTable, "Semanas Gestación:", labelFont)
        addCell(personalTable, "${child.gestationWeeks} sem (${if (child.isPremature) "Prematuro" else "A término"})", valueFont)
        addCell(personalTable, "Seguro de Salud:", labelFont)
        addCell(personalTable, child.insuranceType.displayName, valueFont)

        addCell(personalTable, "Nombre del Tutor:", labelFont)
        addCell(personalTable, child.guardianName, valueFont)
        addCell(personalTable, "DNI Tutor / Teléf:", labelFont)
        addCell(personalTable, "${child.guardianDni} / ${child.guardianPhone}", valueFont)

        document.add(personalTable)
        document.add(Paragraph(" ", valueFont))

        // Section 2: Historial de Crecimiento
        val sec2Header = Paragraph("2. CONTROL DE CRECIMIENTO Y ESTADO NUTRICIONAL (OMS)", sectionHeaderFont)
        sec2Header.spacingAfter = 8f
        document.add(sec2Header)

        if (growthRecords.isEmpty()) {
            document.add(Paragraph("No hay registros de crecimiento registrados.", valueFont))
        } else {
            val growthTable = PdfPTable(6)
            growthTable.widthPercentage = 100f
            growthTable.setWidths(floatArrayOf(1.2f, 1.0f, 1.0f, 1.2f, 1.2f, 2.4f))

            // Headers
            addHeaderCell(growthTable, "Fecha", tableHeaderFont, mainColor)
            addHeaderCell(growthTable, "Peso (kg)", tableHeaderFont, mainColor)
            addHeaderCell(growthTable, "Talla (cm)", tableHeaderFont, mainColor)
            addHeaderCell(growthTable, "P. Peso", tableHeaderFont, mainColor)
            addHeaderCell(growthTable, "P. Talla", tableHeaderFont, mainColor)
            addHeaderCell(growthTable, "Estado Nutricional", tableHeaderFont, mainColor)

            for (record in growthRecords) {
                addCell(growthTable, record.controlDate.format(dateFormatter), tableBodyFont)
                addCell(growthTable, "${record.weightKg} kg", tableBodyFont)
                addCell(growthTable, "${record.heightCm} cm", tableBodyFont)
                addCell(growthTable, "P${record.weightPercentile.toInt()}", tableBodyFont)
                addCell(growthTable, "P${record.heightPercentile.toInt()}", tableBodyFont)
                addCell(growthTable, record.nutritionalStatus.displayName, tableBodyFont)
            }
            document.add(growthTable)
        }
        document.add(Paragraph(" ", valueFont))

        // Section 3: Calendario de Vacunación
        val sec3Header = Paragraph("3. ESQUEMA NACIONAL DE VACUNACIÓN (MINSA)", sectionHeaderFont)
        sec3Header.spacingAfter = 8f
        document.add(sec3Header)

        if (vaccineRecords.isEmpty()) {
            document.add(Paragraph("No hay registros de vacunación disponibles.", valueFont))
        } else {
            val vaccineTable = PdfPTable(5)
            vaccineTable.widthPercentage = 100f
            vaccineTable.setWidths(floatArrayOf(2.5f, 1.0f, 1.2f, 1.2f, 1.1f))

            addHeaderCell(vaccineTable, "Vacuna", tableHeaderFont, mainColor)
            addHeaderCell(vaccineTable, "Dosis", tableHeaderFont, mainColor)
            addHeaderCell(vaccineTable, "Prog. Edad", tableHeaderFont, mainColor)
            addHeaderCell(vaccineTable, "Fecha Prog.", tableHeaderFont, mainColor)
            addHeaderCell(vaccineTable, "Estado", tableHeaderFont, mainColor)

            for (record in vaccineRecords) {
                addCell(vaccineTable, record.vaccine.name, tableBodyFont)
                addCell(vaccineTable, "Dosis ${record.vaccine.doseNumber}", tableBodyFont)
                addCell(
                    vaccineTable,
                    if (record.vaccine.isNeonatal) "RN" else "${record.vaccine.scheduledAgeMonths} meses",
                    tableBodyFont
                )
                addCell(vaccineTable, record.scheduledDate.format(dateFormatter), tableBodyFont)
                addCell(vaccineTable, record.status.displayName, tableBodyFont)
            }
            document.add(vaccineTable)
        }
        document.add(Paragraph(" ", valueFont))

        // Section 4: Hitos del Desarrollo
        val sec4Header = Paragraph("4. HITOS DEL DESARROLLO EVALUADOS (TPED MINSA)", sectionHeaderFont)
        sec4Header.spacingAfter = 8f
        document.add(sec4Header)

        if (milestoneResponses.isEmpty()) {
            document.add(Paragraph("No hay evaluaciones de hitos del desarrollo registradas.", valueFont))
        } else {
            val milestoneTable = PdfPTable(4)
            milestoneTable.widthPercentage = 100f
            milestoneTable.setWidths(floatArrayOf(1.2f, 1.5f, 3.5f, 1.0f))

            addHeaderCell(milestoneTable, "Edad Eval.", tableHeaderFont, mainColor)
            addHeaderCell(milestoneTable, "Área", tableHeaderFont, mainColor)
            addHeaderCell(milestoneTable, "Hito / Pregunta", tableHeaderFont, mainColor)
            addHeaderCell(milestoneTable, "Respuesta", tableHeaderFont, mainColor)

            for ((milestone, response) in milestoneResponses) {
                addCell(milestoneTable, "${milestone.ageMonths} meses", tableBodyFont)
                addCell(milestoneTable, milestone.area.displayName, tableBodyFont)
                addCell(milestoneTable, milestone.question, tableBodyFont)
                addCell(milestoneTable, response.response.displayName, tableBodyFont)
            }
            document.add(milestoneTable)
        }

        document.close()
    }

    private fun addCell(table: PdfPTable, text: String, font: Font) {
        val cell = PdfPCell(Phrase(text, font))
        cell.border = Rectangle.NO_BORDER
        cell.setPadding(4f)
        table.addCell(cell)
    }

    private fun addHeaderCell(table: PdfPTable, text: String, font: Font, bgColor: BaseColor) {
        val cell = PdfPCell(Phrase(text, font))
        cell.backgroundColor = bgColor
        cell.border = Rectangle.BOX
        cell.setPadding(6f)
        cell.horizontalAlignment = Element.ALIGN_CENTER
        table.addCell(cell)
    }
}
