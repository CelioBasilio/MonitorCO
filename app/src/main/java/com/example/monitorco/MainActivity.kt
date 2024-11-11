package com.example.monitorco

import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HospedagemAdapter
    private var mediaPlayer: MediaPlayer? = null
    var isAlertActive: Boolean = false

    private val updateInterval = 5000L // Intervalo de atualização (5 segundos)
    private val handler = Handler(Looper.getMainLooper()) // Usa o Looper da thread principal
    private var updateRunnable: Runnable? = null // Alterei para permitir que seja nulo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o Firebase
        FirebaseApp.initializeApp(this)

        // Verifica se o usuário está autenticado
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // Se não estiver autenticado, redireciona para o LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finaliza MainActivity para que não fique na pilha
            return
        }

        setContentView(R.layout.activity_main)

        initializeUI()  // Configura o RecyclerView e o adapter
        startMonitoring() // Inicia o monitoramento contínuo
    }

    private fun initializeUI() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val icon = ContextCompat.getDrawable(this, R.drawable.ic_window_closed)
            ?: throw IllegalArgumentException("Ícone não encontrado!")

        // Inicializa o adapter com hospedagens fixas
        val hospedagensIniciais = mutableListOf(
            Hospedagem("Hospedagem TESTE Nº 01", 0.0f, icon),
            Hospedagem("Hospedagem MAQUETE Nº 02", 0.0f, icon)
        )

        adapter = HospedagemAdapter(this, hospedagensIniciais)
        recyclerView.adapter = adapter
    }

    private fun startMonitoring() {
        // Certificando-se que o updateRunnable será inicializado corretamente
        updateRunnable = object : Runnable {
            override fun run() {
                updateHospedagens() // Atualiza os dados de CO periodicamente
                handler.postDelayed(this, updateInterval) // Reagenda o Runnable
            }
        }
        updateRunnable?.let { handler.post(it) } // Garante que o handler use updateRunnable
    }

    override fun onDestroy() {
        super.onDestroy()
        // Verifique se o updateRunnable está inicializado antes de removê-lo
        updateRunnable?.let {
            handler.removeCallbacks(it) // Para o monitoramento
        }
        stopAlert() // Para o alerta
        mediaPlayer?.release() // Libera o MediaPlayer
    }

    private fun updateHospedagens() {
        CoroutineScope(Dispatchers.Main).launch {
            val urlHardware_1 = "https://api.thingspeak.com/channels/2704097/feeds.json?results=1"
            val resultcCO_1 = withContext(Dispatchers.IO) { makeApiRequest(urlHardware_1) }

            val urlHardware_2 = "https://api.thingspeak.com/channels/2720761/feeds.json?results=1"
            val resultcCO_2 = withContext(Dispatchers.IO) { makeApiRequest(urlHardware_2) }

            // Atualiza os valores de CO das hospedagens no adapter
            adapter.updateHospedagem(0, resultcCO_1)
            adapter.updateHospedagem(1, resultcCO_2)

            // Verifica os níveis de CO e aciona/para o alarme conforme necessário
            checkCOLevels(resultcCO_1, "Hospedagem 1")
            checkCOLevels(resultcCO_2, "Hospedagem 2")
        }
    }

    private fun checkCOLevels(value: Float, label: String) {
        if (value > 5) { // Aciona o alarme se o valor de CO for maior que 5
            if (!isAlertActive) {
                Toast.makeText(this, "Alerta! Alto nível de CO detectado em $label", Toast.LENGTH_SHORT).show()
                startAlert()
            }
        } else {
            if (isAlertActive) {
                stopAlert()
            }
        }
    }

    private fun startAlert() {
        if (isAlertActive) return // Verifica se o alerta já está ativo

        isAlertActive = true
        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound)
            ?: run {
                Toast.makeText(this, "Erro ao carregar o som de alerta", Toast.LENGTH_SHORT).show()
                return
            }

        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        adapter.notifyDataSetChanged() // Atualiza o adapter para exibir o botão de alerta
    }

    fun stopAlert() {
        if (isAlertActive) {
            isAlertActive = false
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            adapter.notifyDataSetChanged() // Atualiza o adapter para ocultar o botão de alerta
        }
    }

    private suspend fun makeApiRequest(url: String): Float {
        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && responseData != null) {
                val field1Value = extractField1FromJson(responseData)
                field1Value.toFloatOrNull() ?: 0.0f
            } else {
                0.0f
            }
        } catch (e: Exception) {
            e.printStackTrace()  // Loga a exceção
            0.0f // Retorna valor padrão em caso de erro
        }
    }

    private fun extractField1FromJson(jsonData: String): String {
        return jsonData
            .substringAfter("\"feeds\":[{")
            .substringAfter("\"field1\":\"")
            .substringBefore("\"")
    }
}
