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
        // Inicia o Runnable que irá atualizar as hospedagens periodicamente
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
        // Verifique se o updateRunnable está inicializado antes de removê-lo
        updateRunnable?.let {
            handler.removeCallbacks(it) // Para o monitoramento
        }
        stopAlert() // Para o alerta
        mediaPlayer?.release() // Libera o MediaPlayer
    }

    private fun updateHospedagens() {
        // Função para atualizar as informações de CO das hospedagens
        CoroutineScope(Dispatchers.Main).launch {
            // Faz chamadas de API para pegar os dados de CO de dois sensores diferentes
            val urlHardware_1 = "https://api.thingspeak.com/channels/2704097/feeds.json?results=1"
            val resultcCO_1 = withContext(Dispatchers.IO) { makeApiRequest(urlHardware_1) }

            val urlHardware_2 = "https://api.thingspeak.com/channels/2720761/feeds.json?results=1"
            val resultcCO_2 = withContext(Dispatchers.IO) { makeApiRequest(urlHardware_2) }

            // Atualiza os valores de CO no adapter para refletir nas views
            adapter.updateHospedagem(0, resultcCO_1)
            adapter.updateHospedagem(1, resultcCO_2)

            // Verifica os níveis de CO e aciona/desativa o alerta conforme necessário
            checkCOLevels(resultcCO_1, "Hospedagem 1", 0)
            checkCOLevels(resultcCO_2, "Hospedagem 2", 1)
        }
    }

    private fun saveEventToDatabase(cardLabel: String, coLevel: Float, alertState: Boolean) {
        // Função para salvar o evento no Firestore
        val firestore = FirebaseFirestore.getInstance()
        val eventData = hashMapOf(
            "timestamp" to System.currentTimeMillis(),
            "date" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()),
            "cardLabel" to cardLabel,
            "coLevel" to coLevel,
            "alertState" to alertState
        )

        firestore.collection("alerts") // Salva na coleção 'alerts' no Firestore
            .add(eventData)
            .addOnSuccessListener {
                println("Evento salvo com sucesso!")
            }
            .addOnFailureListener { e ->
                e.printStackTrace() // Caso haja erro ao salvar
                println("Erro ao salvar evento: ${e.message}")
            }
    }

    private fun startSavingEvents(cardLabel: String, coLevel: Float) {
        // Inicia o salvamento periódico dos eventos no banco
        if (isSavingToDatabase) return

        isSavingToDatabase = true
        saveHandler = Handler(Looper.getMainLooper())

        saveRunnable = object : Runnable {
            override fun run() {
                saveEventToDatabase(cardLabel, coLevel, isAlertActive) // Salva o evento no banco
                saveHandler?.postDelayed(this, saveInterval) // Reagenda o salvamento
            }
        }
        saveRunnable?.let { saveHandler?.post(it) } // Inicia o handler para salvar eventos
    }

    private fun stopSavingEvents() {
        // Para o salvamento periódico dos eventos
        if (!isSavingToDatabase) return

        isSavingToDatabase = false
        saveRunnable?.let { saveHandler?.removeCallbacks(it) } // Remove o Runnable do handler
        saveHandler = null
    }


    private fun startAlert() {
        // Inicia o som de alerta usando o MediaPlayer
        try {
            mediaPlayer?.release() // Libera o MediaPlayer, se já estiver inicializado
            mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound)?.apply {
                isLooping = true
                setOnPreparedListener { start() }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@MainActivity, "Erro ao carregar o som de alerta", Toast.LENGTH_SHORT).show()
                    release()
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Loga a exceção caso ocorra erro ao iniciar o som
            Toast.makeText(this, "Erro ao iniciar o alerta", Toast.LENGTH_SHORT).show()
        }
        adapter.notifyDataSetChanged() // Atualiza a interface de usuário
    }

    fun stopAlert(hospedagemIndex: Int? = null) {
        // Função para parar o alerta
        if (!isAlertActive) return

        isAlertActive = false
        alertCooldownTime = System.currentTimeMillis() + cooldownDuration // Define o cooldown de 5 minutos

        // Se um índice for passado, desative o alerta dessa hospedagem
        if (hospedagemIndex != null && hospedagemIndex in adapter.hospedagens.indices) {
            adapter.hospedagens[hospedagemIndex].isAlertActive = false
            adapter.notifyItemChanged(hospedagemIndex)
        }

        // Para o som de alerta
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop() // Para o som se estiver tocando
            }
            it.release() // Libera o MediaPlayer
        }
        mediaPlayer = null
    }

    private fun checkCOLevels(value: Float, label: String, index: Int) {
        // Função para verificar os níveis de CO e ativar/desativar o alerta
        val currentTime = System.currentTimeMillis()
        val hospedagem = adapter.hospedagens[index]

        if (value > 5) { // Se o nível de CO estiver acima de 5
            if (!hospedagem.isAlertActive && currentTime > alertCooldownTime) {
                hospedagem.isAlertActive = true
                isAlertActive = true
                Toast.makeText(this, "Alerta! Alto nível de CO detectado em $label", Toast.LENGTH_SHORT).show()
                startAlert() // Inicia o alerta sonoro
                startSavingEvents(label, value) // Inicia o salvamento dos eventos
            }
        } else { // Se o nível de CO estiver seguro
            if (hospedagem.isAlertActive) {
                hospedagem.isAlertActive = false
                stopAlert(index) // Para o alerta sonoro
                stopSavingEvents() // Para o salvamento dos eventos
            }
        }
        adapter.notifyItemChanged(index) // Atualiza a interface
    }

    private suspend fun makeApiRequest(url: String): Float {
        // Função para fazer uma requisição HTTP e obter o nível de CO
        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()

            if (response.isSuccessful && responseData != null) {
                val field1Value = extractField1FromJson(responseData) // Extrai o valor do CO
                field1Value.toFloatOrNull() ?: 0.0f // Converte o valor ou retorna 0.0f
            } else {
                0.0f
            }
        } catch (e: Exception) {
            e.printStackTrace()  // Loga a exceção
            0.0f // Retorna valor padrão em caso de erro
        }
    }

    private fun extractField1FromJson(jsonData: String): String {
        // Função para extrair o valor do CO do JSON da resposta da API
        return jsonData
            .substringAfter("\"feeds\":[{")
            .substringAfter("\"field1\":\"")
            .substringBefore("\"")
    }
}
