package com.example.monitorco

import android.graphics.Rect
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
import android.view.View
import com.example.monitorco.models.Hospedagem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : ComponentActivity() {

    // Variáveis relacionadas ao RecyclerView e ao adapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HospedagemAdapter
    private var mediaPlayer: MediaPlayer? = null
    var isAlertActive: Boolean = false // Indica se o alerta está ativo ou não

    // Configurações de intervalo e controle de atualização
    private val updateInterval = 5000L // Intervalo de atualização (5 segundos)
    private val handler = Handler(Looper.getMainLooper()) // Usa o Looper da thread principal
    private var updateRunnable: Runnable? = null // Runnable para atualização periódica
    private var alertCooldownTime: Long = 0 // Armazena o tempo em que o alerta foi desativado
    private val cooldownDuration = 5 * 60 * 1000L // Duração do cooldown em milissegundos (5 minutos)

    // Variáveis relacionadas ao salvamento no banco de dados
    private var isSavingToDatabase: Boolean = false
    private val saveInterval = 2 * 60 * 1000L // Intervalo de 2 minutos em milissegundos
    private var saveHandler: Handler? = null
    private var saveRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o Firebase
        FirebaseApp.initializeApp(this)

        // Verifica se o usuário está autenticado no Firebase
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // Se não estiver autenticado, redireciona para a tela de login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finaliza MainActivity para que não fique na pilha
            return
        }

        setContentView(R.layout.activity_main)

        initializeUI()  // Configura o RecyclerView e o adapter
        startMonitoring() // Inicia o monitoramento contínuo dos sensores
    }

    private fun initializeUI() {
        // Inicializa o RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Adiciona espaçamento entre os itens
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.item_spacing)
        recyclerView.addItemDecoration(SpacingItemDecoration(spacingInPixels))

        // Carrega o ícone de janela fechada
        val icon = ContextCompat.getDrawable(this, R.drawable.ic_window_closed)
            ?: throw IllegalArgumentException("Ícone não encontrado!")

        // Inicializa a lista de hospedagens com dados fictícios
        val hospedagensIniciais = mutableListOf(
            Hospedagem("Hospedagem TESTE Nº 01", 0.0f, icon),
            Hospedagem("Hospedagem MAQUETE Nº 02", 0.0f, icon)
        )

        // Configura o adapter com as hospedagens iniciais
        adapter = HospedagemAdapter(this, hospedagensIniciais)
        recyclerView.adapter = adapter
    }

    private fun startMonitoring() {
        updateRunnable = object : Runnable {
            override fun run() {
                updateHospedagens() // Atualiza os dados de CO periodicamente
                handler.postDelayed(this, updateInterval) // Reagenda o Runnable para execução periódica
            }
        }
        updateRunnable?.let { handler.post(it) } // Inicia o monitoramento
    }

    override fun onDestroy() {
        super.onDestroy()
        updateRunnable?.let { handler.removeCallbacks(it) } // Para o monitoramento
        stopAlert() // Para o alerta
        mediaPlayer?.release() // Libera o MediaPlayer
    }

    private fun updateHospedagens() {
        CoroutineScope(Dispatchers.Main).launch {
            val urlHardware_1 = "https://api.thingspeak.com/channels/2704097/feeds.json?results=1"
            val resultcCO_1 = withContext(Dispatchers.IO) { makeApiRequest(urlHardware_1) }

            val urlHardware_2 = "https://api.thingspeak.com/channels/2720761/feeds.json?results=1"
            val resultcCO_2 = withContext(Dispatchers.IO) { makeApiRequest(urlHardware_2) }

            adapter.updateHospedagem(0, resultcCO_1)
            adapter.updateHospedagem(1, resultcCO_2)

            checkCOLevels(resultcCO_1, "Hospedagem 1", 0)
            checkCOLevels(resultcCO_2, "Hospedagem 2", 1)
        }
    }

    private fun saveEventToDatabase(cardLabel: String, coLevel: Float, alertState: Boolean) {
        val firestore = FirebaseFirestore.getInstance()
        val eventData = hashMapOf(
            "timestamp" to System.currentTimeMillis(),
            "date" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(System.currentTimeMillis()),
            "cardLabel" to cardLabel,
            "coLevel" to coLevel,
            "alertState" to alertState
        )

        firestore.collection("alerts")
            .add(eventData)
            .addOnSuccessListener { println("Evento salvo com sucesso!") }
            .addOnFailureListener { e -> println("Erro ao salvar evento: ${e.message}") }
    }

    private fun startSavingEvents(cardLabel: String, coLevel: Float) {
        if (isSavingToDatabase) return

        isSavingToDatabase = true
        saveHandler = Handler(Looper.getMainLooper())

        saveRunnable = object : Runnable {
            override fun run() {
                saveEventToDatabase(cardLabel, coLevel, isAlertActive)
                saveHandler?.postDelayed(this, saveInterval)
            }
        }
        saveRunnable?.let { saveHandler?.post(it) }
    }

    private fun stopSavingEvents() {
        if (!isSavingToDatabase) return

        isSavingToDatabase = false
        saveRunnable?.let { saveHandler?.removeCallbacks(it) }
        saveHandler = null
    }

    private fun startAlert() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound)?.apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao iniciar o alerta", Toast.LENGTH_SHORT).show()
        }
        adapter.notifyDataSetChanged()
    }

    fun stopAlert(hospedagemIndex: Int? = null) {
        if (!isAlertActive) return
        isAlertActive = false
        alertCooldownTime = System.currentTimeMillis() + cooldownDuration

        if (hospedagemIndex != null && hospedagemIndex in adapter.hospedagens.indices) {
            adapter.hospedagens[hospedagemIndex].isAlertActive = false
            adapter.notifyItemChanged(hospedagemIndex)
        }

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun checkCOLevels(value: Float, label: String, index: Int) {
        val currentTime = System.currentTimeMillis()
        val hospedagem = adapter.hospedagens[index]

        if (value > 10) {
            if (!hospedagem.isAlertActive && currentTime > alertCooldownTime) {
                hospedagem.isAlertActive = true
                isAlertActive = true
                Toast.makeText(this, "Alerta! Alto nível de CO detectado em $label", Toast.LENGTH_SHORT).show()
                startAlert()
                startSavingEvents(label, value)
            }
        } else {
            if (hospedagem.isAlertActive) {
                hospedagem.isAlertActive = false
                stopAlert(index)
                stopSavingEvents()
            }
        }
        adapter.notifyItemChanged(index)
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
            e.printStackTrace()
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

// Classe para espaçamento entre os itens do RecyclerView
class SpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = spacing
        outRect.right = spacing
        outRect.bottom = spacing
    }
}
