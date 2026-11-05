package uz.egizak.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import uz.egizak.app.ui.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = (application as EgizakApp).repository
        setContent {
            MaterialTheme {
                HomeScreen(repo) { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
            }
        }
    }
}
