package com.example.monitorco

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.monitorco.models.Hospedagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import com.google.android.material.snackbar.Snackbar

class MainActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HospedagemAdapter
    private lateinit var mqttClientService: MqttClientService
    private lateinit var alertManager: AlertManager
    private var isAlertActive = false

    private val channelId = "CO_ALERT_CHANNEL"

    // ViewModel para gerenciar os dados de CO
    private val viewModel: MqttViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        // Verificar se o usuário está logado
        if (isUserLoggedIn()) {
            setupUI()
            setupMqtt()
            observeCOValue()
        } else {
            navigateToLogin()
        }
    }

    // Verifica se o usuário está logado
    private fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    // Navega para a tela de login se o usuário não estiver logado
    private fun navigateToLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

    // Configura a UI, RecyclerView, Adapter e outros componentes
    private fun setupUI() {
        setContentView(R.layout.activity_main)

        alertManager = AlertManager(this)
        mqttClientService = MqttClientService(this, viewModel)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Usando mutableListOf() para garantir que a lista seja mutável
        val hospedagensIniciais = mutableListOf(
            Hospedagem("Hospedagem Nº 01", 0, null) // Aqui você cria uma lista mutável
        )

        // Passa a lista mutável para o Adapter
        adapter = HospedagemAdapter(this, hospedagensIniciais, alertManager)
        recyclerView.adapter = adapter

        createNotificationChannel()  // Criar canal de notificação
    }

    // Configura o cliente MQTT
    private fun setupMqtt() {
        mqttClientService.connect {
            if (mqttClientService.isConnected()) {
                mqttClientService.subscribe("pousada/0001/status")
            } else {
                showNotification("Falha na conexão MQTT. Tente novamente.")
            }
        }
    }

    // Observa as atualizações do valor de CO no ViewModel
    private fun observeCOValue() {
        viewModel.coValue.observe(this, Observer { coValue ->
            updateCOValue(coValue)
        })
    }

    // Atualiza o valor de CO na lista de hospedagens
    private fun updateCOValue(coValue: Int) {
        val hospedagem = adapter.hospedagens[0]  // Alterar para a primeira hospedagem
        hospedagem.value = coValue  // Atualiza o valor de CO
        adapter.notifyItemChanged(0)  // Notifica o adapter sobre a mudança

        showNotification("Novo valor de CO: $coValue")  // Exibe notificação
        checkCOLevels(coValue)  // Verifica níveis de CO
    }

    // Verifica se o nível de CO ultrapassou o limite e ativa/desativa o alerta
    private fun checkCOLevels(value: Int) {
        if (value > 10 && !isAlertActive) {
            isAlertActive = true
            alertManager.startAlert()
        } else if (value <= 10 && isAlertActive) {
            isAlertActive = false
            alertManager.stopAlert()
        }
    }

    // Criação do canal de notificação para Android O e versões superiores
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "CO Alert Channel"
            val descriptionText = "Canal para alertas de CO"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Método para exibir a notificação
    @SuppressLint("NotificationPermission")
    private fun showNotification(message: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Novo valor de CO")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    // Exibe uma mensagem de erro em forma de Toast
    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Exibe uma mensagem de erro em forma de Snackbar
    private fun showSnackbar(message: String) {
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
    }
}
