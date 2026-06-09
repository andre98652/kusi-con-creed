package pe.kusicred.app.features.premium.ui.screen

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import pe.kusicred.app.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumPaywallScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kusi Premium ⭐", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero
            Box(
                modifier = Modifier.fillMaxWidth().background(
                    Brush.verticalGradient(listOf(Color(0xFFF9A825), Color(0xFFFF8C69), Color(0xFFF3E5F5)))
                ).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⭐", fontSize = 64.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Lleva el control al siguiente nivel",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center, color = Color.White)
                    Text("Todo lo que tu bebé necesita en una sola app",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Features
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("¿Qué incluye Premium?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp))

                PremiumFeatureItem(
                    icon = Icons.Default.PictureAsPdf,
                    title = "Reportes PDF Clínicos",
                    description = "Exporta el historial completo de tu bebé en formato profesional. Compártelo por WhatsApp con el pediatra.",
                    color = Color(0xFFE53935)
                )

                PremiumFeatureItem(
                    icon = Icons.Default.Warning,
                    title = "Alertas de Bandera Roja",
                    description = "El sistema detecta automáticamente si tu bebé arrastra múltiples hitos sin cumplir y te notifica para consulta médica oportuna.",
                    color = Color(0xFFE53935)
                )

                PremiumFeatureItem(
                    icon = Icons.Default.PlayCircle,
                    title = "Videotutoriales con Especialistas",
                    description = "Videos de 2 minutos con pediatras y nutricionistas sobre alimentación complementaria, masajes anticólicos y más.",
                    color = Color(0xFF3A7EC8)
                )

                PremiumFeatureItem(
                    icon = Icons.Default.Group,
                    title = "Modo Multi-Perfil",
                    description = "Registra más de un bebé en la misma cuenta. Ideal para familias con varios hijos o profesionales de guarderías.",
                    color = Color(0xFF2E8B5A)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Pricing
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Plan Mensual", style = MaterialTheme.typography.titleMedium, color = Color(0xFF5C4000))
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("S/ 9", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5C4000))
                        Text(".90/mes", style = MaterialTheme.typography.titleMedium, color = Color(0xFF5C4000), modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Text("Cancela cuando quieras", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B7000))
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Button(
                    onClick = { /* Google Play Billing */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF9A825)
                    )
                ) {
                    Text("Suscribirme ahora ⭐", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("7 días de prueba gratuita • Sin compromiso",
                style = MaterialTheme.typography.bodySmall,
                color = KusiOnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun PremiumFeatureItem(icon: ImageVector, title: String, description: String, color: Color) {
    ElevatedCard(shape = RoundedCornerShape(14.dp), elevation = CardDefaults.elevatedCardElevation(2.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = color, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = KusiOnSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfExportScreen(
    childId: String,
    onBack: () -> Unit,
    viewModel: PdfExportViewModel = hiltViewModel()
) {
    LaunchedEffect(childId) {
        viewModel.setChildId(childId)
    }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val error by viewModel.exportError.collectAsState()

    fun sharePdfFile(file: File) {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Reporte PDF"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exportar Reporte PDF", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.PictureAsPdf, null, tint = KusiError, modifier = Modifier.size(96.dp))
            Spacer(Modifier.height(24.dp))
            
            Text("Reporte Clínico CRED", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Genera un documento PDF oficial con el historial completo de crecimiento (OMS), vacunas (MINSA) e hitos del desarrollo (TPED) de tu bebé. Perfecto para compartir con su pediatra.",
                textAlign = TextAlign.Center,
                color = KusiOnSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(Modifier.height(32.dp))

            when (val state = uiState) {
                is PdfExportUiState.Loading -> {
                    CircularProgressIndicator(color = KusiGreen40)
                }
                is PdfExportUiState.Success -> {
                    Text(
                        "Bebé: ${state.child.fullName}",
                        fontWeight = FontWeight.Bold,
                        color = KusiGreen40,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.exportPdf(context) { file ->
                                sharePdfFile(file)
                            }
                        },
                        enabled = !isGenerating,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KusiError)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Download, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Generar y Compartir PDF", fontWeight = FontWeight.Bold)
                        }
                    }

                    error?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
