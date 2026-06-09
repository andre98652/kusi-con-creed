package pe.kusicred.app.features.admission.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pe.kusicred.app.features.admission.ui.screen.ChildRegistrationViewModel
import pe.kusicred.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildRegistrationScreen(
    onRegistrationComplete: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ChildRegistrationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()

    LaunchedEffect(state.savedChildId) {
        state.savedChildId?.let { onRegistrationComplete(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Registrar Bebé", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Paso ${currentStep + 1} de 3", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { if (currentStep > 0) viewModel.previousStep() else onBack() }) {
                        Icon(Icons.Default.ArrowBack, "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Progress bar
            LinearProgressIndicator(
                progress = { (currentStep + 1) / 3f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = KusiGreen40,
                trackColor = KusiDivider
            )

            // Step content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    0 -> Step1ChildData(state, viewModel)
                    1 -> Step2BirthData(state, viewModel)
                    2 -> Step3GuardianData(state, viewModel)
                }
            }

            // Next button
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        if (currentStep < 2) viewModel.nextStep()
                        else viewModel.saveChild()
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KusiGreen40)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (currentStep < 2) "Siguiente →" else "¡Registrar Bebé! 🎉",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1ChildData(state: RegistrationState, vm: ChildRegistrationViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("👶 Datos del Niño/Niña", "Información básica de identificación")

        KusiTextField(value = state.fullName, onValueChange = vm::onFullNameChange,
            label = "Nombres y apellidos", icon = Icons.Default.Person)

        // Doc type selector
        Text("Tipo de Documento", style = MaterialTheme.typography.labelLarge, color = KusiOnSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("DNI", "CNV").forEach { type ->
                FilterChip(
                    selected = state.docType == type,
                    onClick = { vm.onDocTypeChange(type) },
                    label = { Text(type) }
                )
            }
        }

        KusiTextField(value = state.docNumber, onValueChange = vm::onDocNumberChange,
            label = "Número de documento", icon = Icons.Default.Badge,
            keyboardType = KeyboardType.Number)

        // Date + Time of birth
        Text("Fecha y hora de nacimiento *", style = MaterialTheme.typography.labelLarge, color = KusiOnSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KusiTextField(value = state.birthDate, onValueChange = vm::onBirthDateChange,
                label = "DD/MM/AAAA", icon = Icons.Default.DateRange,
                keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
            KusiTextField(value = state.birthTime, onValueChange = vm::onBirthTimeChange,
                label = "HH:MM", icon = Icons.Default.Schedule,
                keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
        }

        // Sex selector
        Text("Sexo *", style = MaterialTheme.typography.labelLarge, color = KusiOnSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("M" to "👦 Masculino", "F" to "👧 Femenino").forEach { (code, label) ->
                val selected = state.sex == code
                ElevatedCard(
                    modifier = Modifier.weight(1f).clickable { vm.onSexChange(code) },
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (selected) KusiGreen40 else KusiSurface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(if (selected) 4.dp else 1.dp)
                ) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(label, color = if (selected) Color.White else KusiOnBackground, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun Step2BirthData(state: RegistrationState, vm: ChildRegistrationViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("🏥 Antecedentes de Nacimiento", "Datos del nacimiento para control de prematuridad")

        KusiTextField(value = state.birthWeightStr, onValueChange = vm::onBirthWeightChange,
            label = "Peso al nacer (gramos)", icon = Icons.Default.MonitorWeight,
            keyboardType = KeyboardType.Decimal)

        KusiTextField(value = state.birthHeightStr, onValueChange = vm::onBirthHeightChange,
            label = "Talla al nacer (cm)", icon = Icons.Default.Height,
            keyboardType = KeyboardType.Decimal)

        KusiTextField(value = state.gestationWeeksStr, onValueChange = vm::onGestationWeeksChange,
            label = "Semanas de gestación", icon = Icons.Default.PregnantWoman,
            keyboardType = KeyboardType.Number)

        if (state.gestationWeeksStr.toIntOrNull()?.let { it < 37 } == true) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Prematuridad detectada. Se usará edad corregida para los cálculos.",
                        style = MaterialTheme.typography.bodySmall, color = Color(0xFF856404))
                }
            }
        }
    }
}

@Composable
private fun Step3GuardianData(state: RegistrationState, vm: ChildRegistrationViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("👩 Datos del Responsable", "Información de contacto del tutor")

        KusiTextField(value = state.guardianName, onValueChange = vm::onGuardianNameChange,
            label = "Nombres completos", icon = Icons.Default.Person)

        KusiTextField(value = state.guardianDni, onValueChange = vm::onGuardianDniChange,
            label = "DNI del responsable", icon = Icons.Default.Badge,
            keyboardType = KeyboardType.Number)

        KusiTextField(value = state.guardianPhone, onValueChange = vm::onGuardianPhoneChange,
            label = "Teléfono / Celular", icon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone)

        KusiTextField(value = state.guardianEmail, onValueChange = vm::onGuardianEmailChange,
            label = "Correo electrónico", icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email)

        Text("Tipo de seguro", style = MaterialTheme.typography.labelLarge, color = KusiOnSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("SIS", "ESSALUD", "PRIVADO", "NINGUNO").forEach { insurance ->
                FilterChip(
                    selected = state.insuranceType == insurance,
                    onClick = { vm.onInsuranceTypeChange(insurance) },
                    label = { Text(insurance) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KusiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = KusiGreen40) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = KusiGreen40,
            focusedLabelColor = KusiGreen40
        )
    )
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = KusiOnBackground)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = KusiOnSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = KusiDivider)
    }
}
