package pe.kusicred.app.features.admission.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.kusicred.app.core.util.*
import pe.kusicred.app.core.util.AgeCalculator.toEpochMillis
import pe.kusicred.app.features.admission.data.repository.ChildRepository
import pe.kusicred.app.features.admission.data.repository.createNewChildId
import pe.kusicred.app.features.vaccines.data.repository.VaccineRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class RegistrationState(
    // Step 1
    val fullName: String = "",
    val docType: String = "DNI",
    val docNumber: String = "",
    val birthDate: String = "",
    val birthTime: String = "",
    val sex: String = "",
    // Step 2
    val birthWeightStr: String = "",
    val birthHeightStr: String = "",
    val gestationWeeksStr: String = "",
    // Step 3
    val guardianName: String = "",
    val guardianDni: String = "",
    val guardianPhone: String = "",
    val guardianEmail: String = "",
    val insuranceType: String = "SIS",
    // Meta
    val isLoading: Boolean = false,
    val error: String? = null,
    val savedChildId: String? = null
)

@HiltViewModel
class ChildRegistrationViewModel @Inject constructor(
    private val childRepository: ChildRepository,
    private val vaccineRepository: VaccineRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state = _state.asStateFlow()

    private val _currentStep = MutableStateFlow(0)
    val currentStep = _currentStep.asStateFlow()

    fun nextStep() {
        val error = validateCurrentStep()
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }
        _state.update { it.copy(error = null) }
        if (_currentStep.value < 2) _currentStep.value++
    }

    fun previousStep() {
        if (_currentStep.value > 0) _currentStep.value--
    }

    private fun validateCurrentStep(): String? = when (_currentStep.value) {
        0 -> when {
            _state.value.fullName.isBlank() -> "Ingresa el nombre completo del bebé"
            _state.value.birthDate.isBlank() -> "Ingresa la fecha de nacimiento"
            _state.value.sex.isBlank() -> "Selecciona el sexo del bebé"
            else -> null
        }
        1 -> when {
            _state.value.birthWeightStr.toFloatOrNull() == null -> "Ingresa el peso al nacer en gramos"
            _state.value.birthHeightStr.toFloatOrNull() == null -> "Ingresa la talla al nacer en cm"
            _state.value.gestationWeeksStr.toIntOrNull() == null -> "Ingresa las semanas de gestación"
            else -> null
        }
        2 -> when {
            _state.value.guardianName.isBlank() -> "Ingresa el nombre del responsable"
            _state.value.guardianPhone.isBlank() -> "Ingresa un número de teléfono"
            else -> null
        }
        else -> null
    }

    fun saveChild() {
        val error = validateCurrentStep()
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val s = _state.value
                val birthDateTime = parseBirthDateTime(s.birthDate, s.birthTime)
                val gestationWeeks = s.gestationWeeksStr.toIntOrNull() ?: 40
                val childId = createNewChildId()

                val child = Child(
                    id = childId,
                    fullName = s.fullName,
                    docType = DocType.values().find { it.name == s.docType } ?: DocType.DNI,
                    docNumber = s.docNumber,
                    birthDate = birthDateTime,
                    sex = if (s.sex == "M") Sex.MALE else Sex.FEMALE,
                    birthWeightGrams = s.birthWeightStr.toFloatOrNull() ?: 0f,
                    birthHeightCm = s.birthHeightStr.toFloatOrNull() ?: 0f,
                    gestationWeeks = gestationWeeks,
                    isPremature = gestationWeeks < 37,
                    guardianName = s.guardianName,
                    guardianDni = s.guardianDni,
                    guardianPhone = s.guardianPhone,
                    guardianEmail = s.guardianEmail,
                    insuranceType = InsuranceType.values().find { it.name == s.insuranceType } ?: InsuranceType.SIS
                )

                childRepository.saveChild(child)
                vaccineRepository.createVaccineScheduleForChild(childId, birthDateTime.toLocalDate())

                _state.update { it.copy(isLoading = false, savedChildId = childId) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Error al guardar: ${e.message}") }
            }
        }
    }

    private fun parseBirthDateTime(dateStr: String, timeStr: String): LocalDateTime {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val date = LocalDate.parse(dateStr.trim(), formatter)
            val timeParts = timeStr.split(":").map { it.trim().toIntOrNull() ?: 0 }
            LocalDateTime.of(date, java.time.LocalTime.of(timeParts.getOrElse(0) { 0 }, timeParts.getOrElse(1) { 0 }))
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    // State update helpers
    fun onFullNameChange(v: String) = _state.update { it.copy(fullName = v) }
    fun onDocTypeChange(v: String) = _state.update { it.copy(docType = v) }
    fun onDocNumberChange(v: String) = _state.update { it.copy(docNumber = v) }
    fun onBirthDateChange(v: String) = _state.update { it.copy(birthDate = v) }
    fun onBirthTimeChange(v: String) = _state.update { it.copy(birthTime = v) }
    fun onSexChange(v: String) = _state.update { it.copy(sex = v) }
    fun onBirthWeightChange(v: String) = _state.update { it.copy(birthWeightStr = v) }
    fun onBirthHeightChange(v: String) = _state.update { it.copy(birthHeightStr = v) }
    fun onGestationWeeksChange(v: String) = _state.update { it.copy(gestationWeeksStr = v) }
    fun onGuardianNameChange(v: String) = _state.update { it.copy(guardianName = v) }
    fun onGuardianDniChange(v: String) = _state.update { it.copy(guardianDni = v) }
    fun onGuardianPhoneChange(v: String) = _state.update { it.copy(guardianPhone = v) }
    fun onGuardianEmailChange(v: String) = _state.update { it.copy(guardianEmail = v) }
    fun onInsuranceTypeChange(v: String) = _state.update { it.copy(insuranceType = v) }
}
