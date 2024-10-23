package com.example.monitorco
import android.content.Context
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.monitorco.ui.theme.MonitorCOTheme
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var manometro: ProgressBar
    private lateinit var janelaIcon: ImageView
    private lateinit var alertButton: Button
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Usando Compose para a interface
        setContent {
            MonitorCOTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }

        // Definindo o layout da activity_main.xml
        setContentView(R.layout.activity_main)

        manometro = findViewById(R.id.manometro)
        janelaIcon = findViewById(R.id.janelaIcon)
        alertButton = findViewById(R.id.alertButton) // Botão para parar o alerta

        updateManometro(75) // Exemplo de valor do sensor
        updateJanelaState(true)

        alertButton.setOnClickListener {
            stopAlert()
        }

        // Aqui você deve verificar a lógica de CO e iniciar o alerta
        checkCOLevel(75) // Use o valor real do sensor
    }

    private fun checkCOLevel(value: Int) {
        if (value >= 70) { // Nível vermelho
            startAlert()
        }
    }

    private fun startAlert() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)

        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    private fun stopAlert() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun updateManometro(value: Int) {
        manometro.progress = value
        when {
            value < 40 -> manometro.progressTintList = ContextCompat.getColorStateList(this, R.color.green)
            value < 70 -> manometro.progressTintList = ContextCompat.getColorStateList(this, R.color.yellow)
            else -> {
                manometro.progressTintList = ContextCompat.getColorStateList(this, R.color.red)
                startAlert() // Iniciar alerta se o nível for vermelho
            }
        }
    }

    private fun updateJanelaState(isOpen: Boolean) {
        val icon: Drawable? = if (isOpen) {
            ContextCompat.getDrawable(this, R.drawable.ic_window_open)
        } else {
            ContextCompat.getDrawable(this, R.drawable.ic_window_closed)
        }
        janelaIcon.setImageDrawable(icon)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MonitorCOTheme {
        Greeting("Android")
    }
}
