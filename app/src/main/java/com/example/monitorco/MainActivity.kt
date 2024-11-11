package com.example.monitorco

import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HospedagemAdapter
    private var mediaPlayer: MediaPlayer? = null
    var isAlertActive: Boolean = false

    private val updateInterval = 5000L // Intervalo de atualização em milissegundos (5 segundos)
    private val handler = android.os.Handler() // Handler para controle do Runnable
    private lateinit var updateRunnable: Runnable // Runnable para atualização periódica

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeUI()  // Inicializa o RecyclerView com hospedagens fixas
        startMonitoring() // Inicia o monitoramento contínuo
    }

    private fun initializeUI() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val icon = ContextCompat.getDrawable(this, R.drawable.ic_window_closed)
        // Inicialize o adapter com as hospedagens fixas
        val hospedagensIniciais = mutableListOf(
            Hospedagem("Hospedagem TESTE Nº 01", 0.0f, icon!!),
            Hospedagem("Hospedagem MAQUETE Nº 02", 0.0f, icon)
        )

        adapter = HospedagemAdapter(this, hospedagensIniciais)
        recyclerView.adapter = adapter
    }

    private fun startMonitoring() {
        updateRunnable = object : Runnable {
            override fun run() {
                updateHospedagens() // Atualiza os dados de CO periodicamente
                handler.postDelayed(this, updateInterval) // Reagenda o Runnable
            }
        }
        handler.post(updateRunnable) // Inicia o ciclo de monitoramento
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable) // Remove o Runnable ao destruir a atividade
    }

    private fun updateHospedagens() {
        CoroutineScope(Dispatchers.Main).launch {
            val urlHardware_1 = "https://api.thingspeak.com/channels/2704097/feeds.json?results=1"
            val resultcCO_1 = withContext(Dispatchers.IO) { makeApiRequest(urlHardware_1) }

            val urlHardware_2 = "https://api.thingspeak.com/channels/2720761/feeds.json?results=1"
            val resultcCO_2 = withContext(Dispatchers.IO) { makeApiRequest(urlHardware_2) }

            // Atualiza os valores de CO das hospedagens existentes no adapter
            adapter.updateHospedagem(0, resultcCO_1) // Atualiza a primeira hospedagem
            adapter.updateHospedagem(1, resultcCO_2) // Atualiza a segunda hospedagem

            checkCOLevels(resultcCO_1)
            checkCOLevels(resultcCO_2)
        }
    }

//    private fun checkCOLevels(value: Float) {
//        val coLimit = 2f // Limite padrão de CO
//        if (value >= coLimit) {
//            startAlert()
//        }
//    }

    private fun checkCOLevels(value: Float) {
        if (value >= 5) {  // Agora o alerta será acionado quando o valor atingir 6
            startAlert()
        }
    }

    private fun startAlert() {
        if (isAlertActive) return

        isAlertActive = true
        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound)
        mediaPlayer?.start()

        val handler = android.os.Handler()
        val runnable = object : Runnable {
            override fun run() {
                if (isAlertActive) {
                    mediaPlayer?.start()
                    handler.postDelayed(this, 3000) // Ajuste o intervalo de repetição do som
                }
            }
        }
        handler.postDelayed(runnable, 3000)
    }

    fun stopAlert() {
        if (isAlertActive) {
            isAlertActive = false
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            adapter.notifyDataSetChanged()
        }
    }

    private suspend fun makeApiRequest(urlbroker: String): Float {
        val client = OkHttpClient()
        val request = Request.Builder().url(urlbroker).build()

        val response = client.newCall(request).execute()
        val responseData = response.body?.string()

        return if (response.isSuccessful && responseData != null) {
            val field1Value = extractField1FromJson(responseData)
            field1Value.toFloatOrNull() ?: 0.0f
        } else {
            0.0f
        }
    }

    private fun extractField1FromJson(jsonData: String): String {
        return jsonData
            .substringAfter("\"feeds\":[{")
            .substringAfter("\"field1\":\"")
            .substringBefore("\"")
    }
}
