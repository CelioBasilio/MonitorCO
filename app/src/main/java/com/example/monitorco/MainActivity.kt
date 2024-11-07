package com.example.monitorco

import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.monitorco.utils.Utils

import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.launch


import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


data class Feed(
    val created_at: String,
    val entry_id: Int,
    val field1: String?
)

data class Channel(
    val feeds: List<Feed>
)


class MainActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HospedagemAdapter
    private var mediaPlayer: MediaPlayer? = null
    var isAlertActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verifica a conexão com a internet
        if (!Utils.isNetworkAvailable(this)) {
            showNoConnectionAlert()
            return
        }

        // Verifica se o usuário está logado, caso contrário redireciona para LoginActivity
        if (!isUserLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Encerra a MainActivity se não estiver logado
            return
        }

        setContentView(R.layout.activity_main)
        initializeUI()
        loadHospedagens()
    }


    private fun initializeUI() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HospedagemAdapter(this, mutableListOf())
        recyclerView.adapter = adapter
    }

    private fun showNoConnectionAlert() {
        Toast.makeText(this, "Por favor, conecte-se à internet.", Toast.LENGTH_LONG).show()
        finish() // Encerra o aplicativo
    }

    fun loadHospedagens() {
        CoroutineScope(Dispatchers.Main).launch {
            // Chamando makeApiRequest de forma assíncrona
            val urlHardware_1 = "https://api.thingspeak.com/channels/2704097/feeds.json?results=1"
            val resultcCO_1 = withContext(Dispatchers.IO) { makeApiRequest(urlHardware_1) }

            val urlHardware_2 = "https://api.thingspeak.com/channels/2720761/feeds.json?results=1"
            val resultcCO_2 = withContext(Dispatchers.IO) { makeApiRequest(urlHardware_2) }

            val icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_window_closed)
            if (icon != null) {
                addNewHospedagem("Hospedagem TESTE Nº 01", resultcCO_1, icon)
                addNewHospedagem("Hospedagem MAQUETE Nº 02", resultcCO_2, icon)

            } else {
                Toast.makeText(this@MainActivity, "Ícone não encontrado", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addNewHospedagem(label: String, value: Float, icon: Drawable) {
        val hospedagem = Hospedagem(label, value, icon)
        adapter.addHospedagem(hospedagem)
        checkCOLevels(value)
    }

    private fun checkCOLevels(value: Float) {
        if (value >= 20) {
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
                    handler.postDelayed(this, 3000) // Ajuste o intervalo de repetição
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


    private fun isUserLoggedIn(): Boolean {
        val sharedPrefs = getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
        return sharedPrefs.getBoolean("isLoggedIn", false)
    }

    ////// AS FUNÇÕES ABAIXO SÃO DE COMUNICAÇÃO E RESULTADO DO BROKER

    private fun fetchJson(url: String) {
        lifecycleScope.launch {
            try {
                val jsonData = makeNetworkRequest(url)
                jsonData?.let {
                    println("JSON recebido: $it")
                    val channel = parseJson(it)
                    // Se precisar, continue a processar os dados
                } ?: println("Erro ao buscar dados.")
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Erro ao acessar os dados: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun makeNetworkRequest(url: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        return client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        }
    }

    private fun parseJson(jsonData: String): Channel {
        val gson = Gson()
        return gson.fromJson(jsonData, Channel::class.java)
    }

    private suspend fun makeApiRequest(urlbroker: String): Float {
        val client = OkHttpClient()
        val request = Request.Builder()
//            .url("https://api.thingspeak.com/channels/2704097/feeds.json?results=2")
            .url(urlbroker)
            .build()

        val response = client.newCall(request).execute()
        val responseData = response.body?.string()

        return if (response.isSuccessful && responseData != null) {
            // Extraia o valor do CO
            val field1Value = extractField1FromJson(responseData)
            field1Value.toFloatOrNull() ?: 0.0f  // Converta para Float, ou use 0.0f se não puder converter
        } else {
            0.0f  // Valor padrão em caso de falha
        }
    }

    fun extractField1FromJson(jsonData: String): String {
        return jsonData
            .substringAfter("\"feeds\":[{")
            .substringAfter("\"field1\":\"")
            .substringBefore("\"")
    }

}
