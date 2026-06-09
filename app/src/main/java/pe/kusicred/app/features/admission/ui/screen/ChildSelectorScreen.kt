package pe.kusicred.app.features.admission.ui.screen

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.kusicred.app.core.util.*
import pe.kusicred.app.features.admission.data.repository.ChildRepository
import pe.kusicred.app.ui.theme.*
import javax.inject.Inject

@HiltViewModel
class ChildSelectorViewModel @Inject constructor(
    private val childRepository: ChildRepository
) : ViewModel() {
    val children = childRepository.getAllChildren().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildSelectorScreen(
    onChildSelected: (String) -> Unit,
    onAddNewChild: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ChildSelectorViewModel = hiltViewModel()
) {
    val children by viewModel.children.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kusi-CRED", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión", tint = KusiGreen40)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNewChild,
                containerColor = KusiGreen40,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Agregar Bebé", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        if (children.isEmpty()) {
            // Empty state
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ChildCare, null, tint = KusiGreen40, modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("¡Bienvenida a Kusi-CRED!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Registra a tu bebé para comenzar\nsu seguimiento de salud",
                        style = MaterialTheme.typography.bodyLarge, color = KusiOnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = onAddNewChild,
                        colors = ButtonDefaults.buttonColors(containerColor = KusiGreen40),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Registrar mi bebé", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Selecciona un perfil", style = MaterialTheme.typography.titleMedium,
                        color = KusiOnSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                }
                items(children) { child ->
                    ChildCard(child = child, onClick = { onChildSelected(child.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildCard(child: Child, onClick: () -> Unit) {
    val age = AgeCalculator.calculate(child.birthDate)
    val sexColor = if (child.sex == Sex.MALE) KusiBlue40 else KusiOrange40
    val sexEmoji = if (child.sex == Sex.MALE) "👦" else "👧"

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .then(Modifier.padding(0.dp))
                    .run {
                        this
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = sexColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(sexEmoji, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(child.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(age.asReadableString(), style = MaterialTheme.typography.bodyMedium, color = KusiOnSurfaceVariant)
                if (child.isPremature) {
                    Text("⚡ Prematuro (${child.gestationWeeks} sem.)", style = MaterialTheme.typography.bodySmall, color = KusiOrange40)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = KusiOutline)
        }
    }
}
