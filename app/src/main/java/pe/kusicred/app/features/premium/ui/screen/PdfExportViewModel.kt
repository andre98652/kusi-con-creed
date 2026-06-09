package pe.kusicred.app.features.premium.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.kusicred.app.core.util.Child
import pe.kusicred.app.core.util.GrowthRecord
import pe.kusicred.app.core.util.Milestone
import pe.kusicred.app.core.util.MilestoneResponse
import pe.kusicred.app.features.admission.data.repository.ChildRepository
import pe.kusicred.app.features.growth.data.repository.GrowthRepository
import pe.kusicred.app.features.milestones.data.repository.MilestoneRepository
import pe.kusicred.app.features.premium.util.ClinicalReportBuilder
import pe.kusicred.app.features.vaccines.data.repository.VaccineRepository
import pe.kusicred.app.features.vaccines.data.repository.VaccineWithRecord
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PdfExportViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val growthRepository: GrowthRepository,
    private val vaccineRepository: VaccineRepository,
    private val milestoneRepository: MilestoneRepository
) : ViewModel() {

    private val _childId = MutableStateFlow("")
    val childId: StateFlow<String> = _childId.asStateFlow()

    fun setChildId(id: String) {
        _childId.value = id
    }

    val childState = _childId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(null) else childRepository.getChildById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val growthState = _childId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else growthRepository.getRecordsByChild(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaccinesState = _childId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else vaccineRepository.getVaccineSchedule(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val milestoneResponsesState = _childId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else milestoneRepository.getResponsesByChild(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMilestonesState = milestoneRepository.getAllMilestones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined UI state
    val uiState = combine(
        childState,
        growthState,
        vaccinesState,
        milestoneResponsesState,
        allMilestonesState
    ) { child, growth, vaccines, responses, milestones ->
        if (child == null) {
            PdfExportUiState.Loading
        } else {
            val milestoneMap = milestones.associateBy { it.milestoneId }
            val mappedMilestones = responses.mapNotNull { response ->
                val milestone = milestoneMap[response.milestoneId] ?: return@mapNotNull null
                milestone to response
            }
            PdfExportUiState.Success(
                child = child,
                growthRecords = growth,
                vaccines = vaccines,
                milestones = mappedMilestones
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PdfExportUiState.Loading)

    var isGenerating = MutableStateFlow(false)
    var exportError = MutableStateFlow<String?>(null)

    fun exportPdf(context: Context, onComplete: (File) -> Unit) {
        val state = uiState.value
        if (state !is PdfExportUiState.Success) return

        viewModelScope.launch {
            isGenerating.value = true
            exportError.value = null
            try {
                val cacheDir = context.cacheDir
                val file = File(cacheDir, "reporte_kusi_cred_${state.child.docNumber}.pdf")
                if (file.exists()) file.delete()
                
                ClinicalReportBuilder.generateReport(
                    context = context,
                    file = file,
                    child = state.child,
                    growthRecords = state.growthRecords,
                    vaccineRecords = state.vaccines,
                    milestoneResponses = state.milestones
                )
                onComplete(file)
            } catch (e: Exception) {
                exportError.value = "Error al generar PDF: ${e.message}"
            } finally {
                isGenerating.value = false
            }
        }
    }
}

sealed interface PdfExportUiState {
    object Loading : PdfExportUiState
    data class Success(
        val child: Child,
        val growthRecords: List<GrowthRecord>,
        val vaccines: List<VaccineWithRecord>,
        val milestones: List<Pair<Milestone, MilestoneResponse>>
    ) : PdfExportUiState
}
