package pe.kusicred.app.features.milestones.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.kusicred.app.core.util.*
import pe.kusicred.app.features.admission.data.repository.ChildRepository
import pe.kusicred.app.features.milestones.data.repository.MilestoneRepository
import pe.kusicred.app.ui.theme.*
import javax.inject.Inject

// ===== ViewModel =====

@HiltViewModel
class MilestoneViewModel @Inject constructor(
    private val milestoneRepository: MilestoneRepository,
    private val childRepository: ChildRepository
) : ViewModel() {
    private val _childId = MutableStateFlow("")

    val child = _childId.flatMapLatest { if (it.isBlank()) flowOf(null) else childRepository.getChildById(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val milestones = combine(_childId, child) { id, c ->
        if (id.isBlank() || c == null) emptyList()
        else {
            val age = AgeCalculator.calculate(c.birthDate)
            milestoneRepository.getMilestonesForMonth(age.totalMonths).first()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val responses = _childId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else milestoneRepository.getResponsesByChild(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentResponses = MutableStateFlow<Map<String, MilestoneResponseType>>(emptyMap())
    val currentResponses = _currentResponses.asStateFlow()

    var savedSuccessfully by mutableStateOf(false)

    fun setChildId(id: String) { _childId.value = id }

    fun setResponse(milestoneId: String, response: MilestoneResponseType) {
        _currentResponses.update { it + (milestoneId to response) }
    }

    fun saveAllResponses() {
        val id = _childId.value
        if (id.isBlank()) return
        viewModelScope.launch {
            milestoneRepository.saveAllResponses(id, _currentResponses.value)
            savedSuccessfully = true
        }
    }

    // Get a specific milestone for detail screen
    suspend fun getMilestoneById(milestoneId: String, ageMonths: Int): Milestone? {
        return milestoneRepository.getMilestonesForMonth(ageMonths).first()
            .find { it.milestoneId == milestoneId }
    }
}

// ===== Screens =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestoneEvaluationScreen(
    childId: String,
    onBack: () -> Unit,
    onMilestoneDetail: (String) -> Unit,
    onStimulation: (String) -> Unit,
    viewModel: MilestoneViewModel = hiltViewModel()
) {
    LaunchedEffect(childId) { viewModel.setChildId(childId) }
    val child by viewModel.child.collectAsState()
    val milestones by viewModel.milestones.collectAsState()
    val responses by viewModel.currentResponses.collectAsState()

    LaunchedEffect(viewModel.savedSuccessfully) {
        if (viewModel.savedSuccessfully) onBack()
    }

    val grouped = milestones.groupBy { it.area }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hitos del Desarrollo", fontWeight = FontWeight.Bold)
                        child?.let {
                            val age = AgeCalculator.calculate(it.birthDate)
                            Text("Mes ${age.totalMonths}", style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (milestones.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Psychology, null, tint = KusiOrange40, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Sin hitos para este mes", style = MaterialTheme.typography.titleMedium)
                    Text("Los hitos están disponibles para los meses 1, 2, 3, 4, 6, 9, 12, 18 y 24",
                        textAlign = TextAlign.Center, color = KusiOnSurfaceVariant, modifier = Modifier.padding(24.dp))
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MilestoneArea.values().forEach { area ->
                        val areaItems = grouped[area] ?: return@forEach
                        item {
                            AreaHeader(area)
                        }
                        items(areaItems) { milestone ->
                            MilestoneCard(
                                milestone = milestone,
                                currentResponse = responses[milestone.milestoneId],
                                onResponseChange = { viewModel.setResponse(milestone.milestoneId, it) },
                                onInfoClick = { onMilestoneDetail(milestone.milestoneId) }
                            )
                        }
                    }
                }

                // Save button
                val allAnswered = milestones.all { it.milestoneId in responses }
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { viewModel.saveAllResponses() },
                        enabled = allAnswered,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KusiOrange40)
                    ) {
                        Text(
                            text = if (allAnswered) "Guardar evaluación 🎉" else "Completa todos los hitos",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AreaHeader(area: MilestoneArea) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).background(Color(area.colorHex).copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(area.emoji, style = MaterialTheme.typography.bodyLarge) }
        Spacer(Modifier.width(10.dp))
        Text(area.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
            color = Color(area.colorHex))
    }
}

@Composable
fun MilestoneCard(
    milestone: Milestone,
    currentResponse: MilestoneResponseType?,
    onResponseChange: (MilestoneResponseType) -> Unit,
    onInfoClick: () -> Unit
) {
    val areaColor = Color(milestone.area.colorHex)
    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(milestone.question,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f))
                IconButton(onClick = onInfoClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Info, "Ver detalles", tint = areaColor, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            // Response buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MilestoneResponseType.values().forEach { responseType ->
                    val isSelected = currentResponse == responseType
                    val bgColor = when {
                        !isSelected -> MaterialTheme.colorScheme.surfaceVariant
                        responseType == MilestoneResponseType.YES -> KusiStatusNormal
                        responseType == MilestoneResponseType.SOMETIMES -> KusiStatusRisk
                        else -> KusiError
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onResponseChange(responseType) },
                        label = { Text("${responseType.emoji} ${responseType.displayName}", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = bgColor,
                            selectedLabelColor = if (isSelected) Color.White else KusiOnBackground
                        )
                    )
                }
            }

            // Show stimulation tip if NOT_YET
            AnimatedVisibility(visible = currentResponse == MilestoneResponseType.NOT_YET) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = KusiOrange40.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(Modifier.padding(10.dp)) {
                        Text("💡", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(8.dp))
                        Text(milestone.stimulationTip, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B4513))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestoneDetailScreen(
    milestoneId: String,
    childId: String,
    onBack: () -> Unit,
    onStimulation: () -> Unit,
    viewModel: MilestoneViewModel = hiltViewModel()
) {
    LaunchedEffect(childId) { viewModel.setChildId(childId) }
    val milestones by viewModel.milestones.collectAsState()
    val milestone = milestones.find { it.milestoneId == milestoneId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Hito", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        milestone?.let { m ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Area chip
                Surface(
                    color = Color(m.area.colorHex).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("${m.area.emoji} ${m.area.displayName}",
                        color = Color(m.area.colorHex), fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }

                // Question
                Text(m.question, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                // Illustration placeholder
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(m.area.colorHex).copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(m.area.emoji, style = MaterialTheme.typography.displayMedium)
                            Text("Ilustración del hito", style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
                        }
                    }
                }

                // Description
                Card(shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("¿Qué significa este hito?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(m.description, style = MaterialTheme.typography.bodyMedium, color = KusiOnSurfaceVariant)
                    }
                }

                // Stimulation tip
                Card(
                    colors = CardDefaults.cardColors(containerColor = KusiOrange40.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("💡 Cómo estimular", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = KusiOrange40)
                        Spacer(Modifier.height(8.dp))
                        Text(m.stimulationTip, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Button(
                    onClick = onStimulation,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KusiOrange40)
                ) {
                    Icon(Icons.Default.PlayCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ver guía de estimulación", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StimulationGuideScreen(
    milestoneId: String,
    onBack: () -> Unit,
    viewModel: MilestoneViewModel = hiltViewModel()
) {
    val milestones by viewModel.milestones.collectAsState()
    val milestone = milestones.find { it.milestoneId == milestoneId }

    // Stimulation activities based on milestone area
    val activities = when (milestone?.area) {
        MilestoneArea.MOTORA -> listOf(
            "🤸 Ejercicio 1: Tummy Time" to "Coloca al bebé boca abajo sobre una superficie firme por 5-10 minutos, 3 veces al día. Pon un juguete colorido frente a él para motivarlo.",
            "🎯 Ejercicio 2: Alcanzar objetos" to "Cuelga juguetes de colores brillantes a 20-30 cm de distancia. Anima al bebé a extender sus brazos para alcanzarlos.",
            "🦵 Ejercicio 3: Pedaleo" to "Con el bebé boca arriba, mueve suavemente sus piernas en movimiento de pedaleo de bicicleta durante 2-3 minutos.",
            "🏋️ Ejercicio 4: Jalarse para sentarse" to "Toma sus manitos y jala suavemente hacia ti mientras está boca arriba. Esto fortalece los músculos del cuello y abdomen."
        )
        MilestoneArea.LENGUAJE -> listOf(
            "🗣️ Actividad 1: Conversación de turnos" to "Habla con tu bebé y pausa. Espera su respuesta (aunque sea un sonido). Luego responde tú. Imita sus sonidos y gestos.",
            "📚 Actividad 2: Lectura en voz alta" to "Léele libros con imágenes coloridas al menos 10 minutos al día. Señala las imágenes y nómbralas claramente.",
            "🎵 Actividad 3: Cantar y rimar" to "Canta canciones simples con tu bebé. Las rimas y ritmos ayudan al desarrollo del lenguaje. Repite las mismas canciones varias veces.",
            "📣 Actividad 4: Narrar el día" to "Describe en voz alta lo que estás haciendo: 'Ahora lavamos las manos', 'Vamos a comer'. El vocabulario se construye desde el primer día."
        )
        MilestoneArea.SOCIAL -> listOf(
            "😊 Actividad 1: Juego de espejos" to "Muéstrale al bebé un espejo. Habla sobre lo que ve: 'Mira tu nariz, tus ojos'. El reconocimiento propio es un hito social clave.",
            "🤝 Actividad 2: Turnos de juego" to "Rueda una pelota hacia él y espera que la devuelva. Los juegos de dar y recibir enseñan reciprocidad.",
            "👋 Actividad 3: Imitar gestos" to "Haz gestos simples repetidamente: aplausos, adiós, besitos. Celebra cuando el bebé los imite.",
            "🎭 Actividad 4: Juego de roles" to "Juega a dar de comer a un muñeco, a cuidarlo. El juego simbólico es la base del desarrollo emocional."
        )
        MilestoneArea.COGNITIVA -> listOf(
            "🔍 Actividad 1: Juego de esconder" to "Cubre un objeto con un pañuelo frente al bebé y pregunta '¿Dónde está?' Luego descúbrelo. Repite varias veces. Desarrolla la permanencia del objeto.",
            "🧩 Actividad 2: Clasificar objetos" to "Agrupa objetos por color, forma o tamaño. Nómbrale lo que haces. 'Aquí van las rojas, aquí las azules'.",
            "🔢 Actividad 3: Contar con objetos" to "Cuenta objetos en voz alta mientras los tocas: '1 manzana, 2 manzanas'. La conciencia numérica empieza antes del año.",
            "🎨 Actividad 4: Exploración sensorial" to "Permite que el bebé toque diferentes texturas: suave, rugoso, suave, duro. Nómbrale cada textura. La exploración táctil estimula el desarrollo cognitivo."
        )
        null -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guía de Estimulación", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KusiOrange40,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                milestone?.let { m ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = KusiOrange40.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("${m.area.emoji} Hito a trabajar", style = MaterialTheme.typography.labelLarge, color = KusiOrange40)
                            Spacer(Modifier.height(4.dp))
                            Text(m.question, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text("Actividades de Estimulación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Realiza estas actividades diariamente durante 5-10 minutos en un ambiente tranquilo.",
                    style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
            }

            items(activities) { (title, desc) ->
                ElevatedCard(shape = RoundedCornerShape(14.dp), elevation = CardDefaults.elevatedCardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = KusiOrange40)
                        Spacer(Modifier.height(8.dp))
                        Text(desc, style = MaterialTheme.typography.bodyMedium, color = KusiOnSurfaceVariant)
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("✅ Recuerda", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = KusiStatusNormal)
                        Spacer(Modifier.height(4.dp))
                        Text("• Cada bebé tiene su propio ritmo\n• La constancia es más importante que la duración\n• Convierte las actividades en momentos de juego y diversión\n• Si el hito no aparece en 2-3 meses, consulta al pediatra",
                            style = MaterialTheme.typography.bodySmall, color = Color(0xFF1B5E20))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
