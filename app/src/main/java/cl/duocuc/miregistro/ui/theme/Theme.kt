package cl.duocuc.miregistro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColoresPagoFlex = lightColorScheme(
    primary = Color(0xFF1455A3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEAF4FF),
    onPrimaryContainer = Color(0xFF0B2D52),
    secondary = Color(0xFF4A90D9),
    background = Color(0xFFF5F8FC),
    surface = Color.White,
    onSurface = Color(0xFF18324D),
    error = Color(0xFFB3261E)
)

@Composable
fun PagoFlexTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColoresPagoFlex, content = content)
}
@Composable
fun MiRegistroTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    PagoFlexTheme(content)
}
