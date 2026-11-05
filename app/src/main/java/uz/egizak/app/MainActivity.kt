package uz.egizak.app

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import uz.egizak.app.ui.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val repo = (application as EgizakApp).repository
        setContent {
            EgizakTheme {
                HomeScreen(repo) { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
            }
        }
    }
}

private val LightSeed = lightColorScheme(primary = Color(0xFF3F51B5), secondary = Color(0xFF5C6BC0))
private val DarkSeed = darkColorScheme(primary = Color(0xFFAEB6FF), secondary = Color(0xFF9FA8DA))

@Composable
private fun EgizakTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    // Material You dynamic colors on Android 12+, a fixed indigo palette below that.
    val scheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (dark) DarkSeed else LightSeed
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
