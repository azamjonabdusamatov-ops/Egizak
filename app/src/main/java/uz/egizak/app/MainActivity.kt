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
import uz.egizak.app.ui.HomeScreen
import uz.egizak.app.workprofile.WorkProfileManager

class MainActivity : ComponentActivity() {

    private val provisionRequest = 4001

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val repo = (application as EgizakApp).repository
        setContent {
            EgizakTheme {
                HomeScreen(
                    repo = repo,
                    toast = { msg -> toast(msg) },
                    onBankClone = ::bankClone,
                )
            }
        }
    }

    /**
     * Bank/high-security apps are cloned through a real Android work profile instead of the
     * in-app engine, because that is what their integrity checks accept. The first bank clone
     * creates the profile (system dialog); later ones install into the existing profile.
     */
    private fun bankClone(packageName: String) {
        if (!WorkProfileManager.isSupported(this)) {
            toast("Бу телефон иш профилини қўлламайди (кўпинча Honor/Huawei'да шундай).")
            return
        }
        if (WorkProfileManager.hasProfile(this)) {
            val ok = WorkProfileManager.installIntoExistingProfile(this, packageName)
            toast(if (ok) "Иш профилига қўшилди. Иш профили иловаларидан очинг." else "Қўшиб бўлмади.")
        } else {
            toast("Иш профили яратилади — тизим ойнасида тасдиқланг.")
            WorkProfileManager.provision(this, packageName, provisionRequest)
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

private val LightSeed = lightColorScheme(primary = Color(0xFF3F51B5), secondary = Color(0xFF5C6BC0))
private val DarkSeed = darkColorScheme(primary = Color(0xFFAEB6FF), secondary = Color(0xFF9FA8DA))

@Composable
private fun EgizakTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (dark) DarkSeed else LightSeed
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
