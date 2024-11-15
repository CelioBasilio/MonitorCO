package com.example.monitorco

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// ViewModel para gerenciar os dados de CO
class MqttViewModel : ViewModel() {
    private val _coValue = MutableLiveData<Int>()
    val coValue: LiveData<Int> get() = _coValue

    fun updateCOValue(value: Int) {
        _coValue.value = value
    }
}

class MqttClientService(private val context: Context, private val viewModel: MqttViewModel) {

    private var mqttClient: MqttAndroidClient? = null
    private val mqttServerUri = "ssl://fa4780e14bb745dc86e03dec0ae2aacd.s1.eu.hivemq.cloud:8883"  // Broker MQTT com porta 8883

    // Conecta ao servidor MQTT
    fun connect(onSuccess: () -> Unit) {
        if (mqttClient != null && mqttClient!!.isConnected) {
            Log.d("MqttClientService", "Já está conectado")
            onSuccess() // Chama a função de sucesso diretamente
            return
        }

        mqttClient = MqttAndroidClient(context, mqttServerUri, MqttClient.generateClientId()).apply {
            setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    cause?.printStackTrace()
                    Log.e("MqttClientService", "Conexão perdida: ${cause?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    message?.let {
                        // Processa a mensagem recebida
                        val sensorData = it.payload.decodeToString()
                        val value = parseCOValue(sensorData)
                        Log.d("MqttClientService", "Mensagem recebida: $sensorData")
                        onCODataReceived(value)  // Passa o valor para quem chamou
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d("MqttClientService", "Entrega completa: ${token?.message}")
                }
            })
        }

        val options = MqttConnectOptions().apply {
            userName = "pousada"  // Seu nome de usuário
            password = "Pousada123".toCharArray()  // Sua senha
            isCleanSession = true
            connectionTimeout = 10
            keepAliveInterval = 20
            // Não há necessidade de SSL customizado, vamos deixar o Paho Android usar o SSL padrão
        }

        mqttClient?.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MqttClientService", "Conexão bem-sucedida")
                onSuccess()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                exception?.printStackTrace()
                Log.e("MqttClientService", "Falha na conexão: ${exception?.message}")
                // Aqui, você pode exibir a mensagem de erro na Activity
                (context as? MainActivity)?.showToast("Falha na conexão MQTT. Tente novamente.")
            }
        })
    }

    private fun parseCOValue(sensorData: String): Int {
        // Exemplo de como você pode extrair o valor de CO de uma string JSON
        return sensorData.toIntOrNull() ?: 0
    }

    // Método para processar os dados de CO
    private fun onCODataReceived(coValue: Int) {
        // Passar o valor de CO para o ViewModel
        viewModel.updateCOValue(coValue)
    }

    fun isConnected(): Boolean = mqttClient?.isConnected == true

    fun subscribe(topic: String) {
        try {
            mqttClient?.subscribe(topic, 1)
            Log.d("MqttClientService", "Inscrito no tópico: $topic")
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}
