package pe.kusicred.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val KusiLightColorScheme = lightColorScheme(
    primary = KusiGreen40,
    onPrimary = KusiOnPrimary,
    primaryContainer = KusiSurfaceVariant,
    onPrimaryContainer = KusiGreen40,

    secondary = KusiOrange40,
    onSecondary = KusiOnPrimary,
    secondaryContainer = KusiOrange80,
    onSecondaryContainer = Color(0xFF5C2000),

    tertiary = KusiBlue40,
    onTertiary = KusiOnPrimary,
    tertiaryContainer = KusiBlue80,
    onTertiaryContainer = Color(0xFF001D35),

    background = KusiBackground,
    onBackground = KusiOnBackground,

    surface = KusiSurface,
    onSurface = KusiOnSurface,
    surfaceVariant = KusiSurfaceVariant,
    onSurfaceVariant = KusiOnSurfaceVariant,

    error = KusiError,
    errorContainer = KusiErrorContainer,
    onError = Color.White,
    onErrorContainer = Color(0xFF410E0B),

    outline = KusiOutline,
    outlineVariant = KusiDivider
)

@Composable
fun KUSICREDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // KUSI-CRED usa siempre el tema claro para consistencia visual
    val colorScheme = KusiLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KusiTypography,
        content = content
    )
}
