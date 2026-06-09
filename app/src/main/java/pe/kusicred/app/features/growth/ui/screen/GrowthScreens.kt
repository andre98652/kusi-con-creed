package pe.kusicred.app.features.growth.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.compose.cartesian.*
import com.patrykandpatrick.vico.compose.cartesian.axis.*
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.*
import com.patrykandpatrick.vico.core.cartesian.axis.*
import com.patrykandpatrick.vico.core.cartesian.data.*
import com.patrykandpatrick.vico.core.cartesian.layer.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.kusicred.app.core.database.dao.WhoTableDao
import pe.kusicred.app.core.util.*
import pe.kusicred.app.features.admission.data.repository.ChildRepository
import pe.kusicred.app.features.growth.data.repository.GrowthRepository
import pe.kusicred.app.ui.theme.*
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// ===== ViewModels =====

@HiltViewModel
class GrowthViewModel @Inject constructor(
    private val growthRepository: GrowthRepository,
    private val childRepository: ChildRepository
) : ViewModel() {
    private val _childId = MutableStateFlow("")

    val child = _childId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(null) else childRepository.getChildById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val records = _childId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else growthRepository.getRecordsByChild(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setChildId(id: String) { _childId.value = id }
}

@HiltViewModel
class AddGrowthViewModel @Inject constructor(
    private val growthRepository: GrowthRepository,
    private val childRepository: ChildRepository
) : ViewModel() {
    private val _childId = MutableStateFlow("")
    val child = _childId.flatMapLatest { if (it.isBlank()) flowOf(null) else childRepository.getChildById(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var weightStr by mutableStateOf("")
    var heightStr by mutableStateOf("")
    var headStr by mutableStateOf("")
    var isSaving by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var savedRecord by mutableStateOf<GrowthRecord?>(null)

    fun setChildId(id: String) { _childId.value = id }

    fun save() {
        val c = child.value ?: return
        val weight = weightStr.toFloatOrNull() ?: run { error = "Peso inválido"; return }
        val height = heightStr.toFloatOrNull() ?: run { error = "Talla inválida"; return }

        viewModelScope.launch {
            isSaving = true
            error = null
            try {
                val age = AgeCalculator.calculate(c.birthDate)
                val record = growthRepository.addRecord(
                    childId = c.id, sex = c.sex,
                    ageMonths = age.totalMonths,
                    weightKg = weight, heightCm = height,
                    headCircumferenceCm = headStr.toFloatOrNull()
                )
                savedRecord = record
            } catch (e: Exception) {
                error = "Error al guardar: ${e.message}"
            }
            isSaving = false
        }
    }
}

// ===== Screens =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthChartScreen(
    childId: String,
    onBack: () -> Unit,
    onAddRecord: () -> Unit,
    onViewHistory: () -> Unit,
    viewModel: GrowthViewModel = hiltViewModel()
) {
    LaunchedEffect(childId) { viewModel.setChildId(childId) }
    val child by viewModel.child.collectAsState()
    val records by viewModel.records.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Curvas de Crecimiento OMS", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Atrás") } },
                actions = {
                    IconButton(onClick = onViewHistory) { Icon(Icons.Default.History, "Historial") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecord, containerColor = KusiGreen40) {
                Icon(Icons.Default.Add, "Agregar control", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            if (records.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShowChart, null, tint = KusiGreen40, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Sin registros aún", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Agrega el primer control de peso y talla", style = MaterialTheme.typography.bodyMedium, color = KusiOnSurfaceVariant)
                    }
                }
            } else {
                // Latest record card
                records.firstOrNull()?.let { latest ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(latest.nutritionalStatus.color).copy(alpha = 0.1f))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Último Control", style = MaterialTheme.typography.labelLarge, color = KusiOnSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                InfoChip("⚖️ Peso", "${latest.weightKg} kg")
                                InfoChip("📏 Talla", "${latest.heightCm} cm")
                                InfoChip("📊 P. Peso", "${latest.weightPercentile.toInt()}%")
                            }
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                color = Color(latest.nutritionalStatus.color).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(latest.nutritionalStatus.displayName,
                                    color = Color(latest.nutritionalStatus.color),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(latest.messageForMother, style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
                        }
                    }
                }

                // Simple weight chart
                if (records.size >= 2) {
                    Text("Evolución del Peso", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    GrowthChart(records = records.reversed())
                }

                // Percentile guide
                PercentileGuideCard()
            }
        }
    }
}

@Composable
fun GrowthChart(records: List<GrowthRecord>) {
    val weights = records.map { it.weightKg }
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(records) {
        modelProducer.runTransaction {
            lineSeries { series(weights) }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(200.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = remember { LineCartesianLayer.LineFill.single(fill(KusiGreen40)) }
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom()
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxSize().padding(8.dp)
        )
    }
}

@Composable
fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = KusiOnSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PercentileGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Guía de Percentiles OMS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            listOf(
                Triple("< P3", "Desnutrición severa", KusiError),
                Triple("P3 - P15", "Desnutrición / Riesgo", KusiStatusRisk),
                Triple("P15 - P85", "Rango normal ✓", KusiStatusNormal),
                Triple("P85 - P97", "Sobrepeso", KusiStatusOverweight),
                Triple("> P97", "Obesidad", KusiStatusAlert)
            ).forEach { (range, label, color) ->
                Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp), modifier = Modifier.width(60.dp)) {
                        Text(range, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(4.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(label, style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGrowthRecordScreen(
    childId: String,
    onRecordSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddGrowthViewModel = hiltViewModel()
) {
    LaunchedEffect(childId) { viewModel.setChildId(childId) }
    val child by viewModel.child.collectAsState()

    LaunchedEffect(viewModel.savedRecord) {
        if (viewModel.savedRecord != null) onRecordSaved()
    }

    var showResult by remember { mutableStateOf(false) }
    val savedRecord = viewModel.savedRecord

    if (savedRecord != null && !showResult) {
        // Show result before navigating
        GrowthResultDialog(record = savedRecord, onDismiss = { showResult = true; onRecordSaved() })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Control", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            child?.let { c ->
                val age = AgeCalculator.calculate(c.birthDate)
                Card(
                    colors = CardDefaults.cardColors(containerColor = KusiSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (c.sex == Sex.MALE) "👦" else "👧", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(c.fullName.split(" ").take(2).joinToString(" "), fontWeight = FontWeight.Bold)
                            Text(age.asReadableString(), style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
                        }
                    }
                }
            }

            Text("Medidas del Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = viewModel.weightStr,
                onValueChange = { viewModel.weightStr = it },
                label = { Text("Peso (kg) *") },
                leadingIcon = { Icon(Icons.Default.MonitorWeight, null, tint = KusiGreen40) },
                trailingIcon = { Text("kg", modifier = Modifier.padding(end = 8.dp), color = KusiOnSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KusiGreen40)
            )

            OutlinedTextField(
                value = viewModel.heightStr,
                onValueChange = { viewModel.heightStr = it },
                label = { Text("Talla (cm) *") },
                leadingIcon = { Icon(Icons.Default.Height, null, tint = KusiGreen40) },
                trailingIcon = { Text("cm", modifier = Modifier.padding(end = 8.dp), color = KusiOnSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KusiGreen40)
            )

            OutlinedTextField(
                value = viewModel.headStr,
                onValueChange = { viewModel.headStr = it },
                label = { Text("Perímetro cefálico (cm) — Opcional") },
                leadingIcon = { Icon(Icons.Default.Face, null, tint = KusiGreen40) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KusiGreen40)
            )

            viewModel.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = { viewModel.save() },
                enabled = !viewModel.isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KusiGreen40)
            ) {
                if (viewModel.isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Calcular y Guardar 📊", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GrowthResultDialog(record: GrowthRecord, onDismiss: () -> Unit) {
    val statusColor = Color(record.nutritionalStatus.color)
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("📊", style = MaterialTheme.typography.headlineLarge) },
        title = { Text("Resultado del Control", fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoChip("⚖️ Peso", "${record.weightKg} kg")
                    InfoChip("📏 Talla", "${record.heightCm} cm")
                }
                Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text(record.nutritionalStatus.displayName, color = statusColor, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp))
                }
                Text(record.messageForMother, style = MaterialTheme.typography.bodyMedium,
                    color = KusiOnSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = KusiGreen40)) {
                Text("Entendido ✓")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthHistoryScreen(
    childId: String,
    onBack: () -> Unit,
    viewModel: GrowthViewModel = hiltViewModel()
) {
    LaunchedEffect(childId) { viewModel.setChildId(childId) }
    val records by viewModel.records.collectAsState()
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Controles", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (records.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Sin registros aún", color = KusiOnSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records) { record ->
                    val statusColor = Color(record.nutritionalStatus.color)
                    ElevatedCard(shape = RoundedCornerShape(14.dp), elevation = CardDefaults.elevatedCardElevation(2.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(44.dp).background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.ShowChart, null, tint = statusColor, modifier = Modifier.size(24.dp)) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(record.controlDate.format(fmt), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text("${record.weightKg} kg • ${record.heightCm} cm", style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
                            }
                            Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                                Text("P${record.weightPercentile.toInt()}", style = MaterialTheme.typography.labelMedium,
                                    color = statusColor, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
