package pe.kusicred.app.features.onboarding.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.kusicred.app.ui.theme.*

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val gradient: List<Color>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.MonitorWeight,
            title = "Curvas de Crecimiento OMS",
            description = "Registra el peso y talla de tu bebé y visualiza en tiempo real cómo está creciendo según los estándares de la Organización Mundial de la Salud.",
            gradient = listOf(Color(0xFF2E8B5A), Color(0xFF4CAF82))
        ),
        OnboardingPage(
            icon = Icons.Default.Vaccines,
            title = "Calendario de Vacunas MINSA",
            description = "Nunca olvides una vacuna. Te enviamos alertas 3 días antes con consejos prácticos para el día de la cita. ¡Sin papel, sin olvidos!",
            gradient = listOf(Color(0xFF3A7EC8), Color(0xFF5B9BD5))
        ),
        OnboardingPage(
            icon = Icons.Default.Psychology,
            title = "Hitos del Desarrollo",
            description = "Monitorea el avance psicomotor de tu bebé mes a mes en las áreas Motora, Lenguaje, Social y Cognitiva con guías visuales interactivas.",
            gradient = listOf(Color(0xFFE07A45), Color(0xFFFF8C69))
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KusiBackground)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            OnboardingPageContent(page = pages[index])
        }

        // Dots indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            pages.forEachIndexed { index, _ ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(if (isSelected) 24.dp else 8.dp, label = "dot_width")
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(if (isSelected) KusiGreen40 else KusiOutline)
                )
            }
        }

        // CTA Button
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onGetStarted()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KusiGreen40)
            ) {
                Text(
                    text = if (pagerState.currentPage < pages.size - 1) "Siguiente →" else "¡Comenzar ahora!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (pagerState.currentPage < pages.size - 1) {
            TextButton(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text("Saltar", color = KusiOnSurfaceVariant)
            }
        } else {
            Spacer(Modifier.height(56.dp))
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .background(Brush.verticalGradient(page.gradient)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }

        // Text content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = KusiOnBackground
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = KusiOnSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}
