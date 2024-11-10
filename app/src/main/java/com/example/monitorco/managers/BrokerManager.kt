package com.example.monitorco.managers

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import java.util.concurrent.ConcurrentHashMap

class BrokerManager(
    private val context: Context,
    private val onMessageReceived: (String, String) -> Unit, // Função callback para mensagens recebidas
    private val username: String,
    private val password: String
) {
    private val brokerUrl = "ssl://fa4780e14bb745dc86e03dec0ae2aacd.s1.eu.hivemq.cloud:8883"
    private val clientId = MqttClient.generateClientId()
    private var mqttClient: MqttClient? = null
    private var isConnected = false

    private var topics: List<String> = emptyList() // Lista de tópicos a assinar
    private val subscribedTopics = ConcurrentHashMap<String, Boolean>() // Tópicos assinados

    // Função para conectar ao broker e se inscrever nos tópicos
    fun connect(topics: List<String>) {
        this.topics = topics

        try {
            mqttClient = MqttClient(brokerUrl, clientId, null)
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                userName = username
                password = password.toString().toCharArray()
                connectionTimeout = 10
                keepAliveInterval = 30
                isAutomaticReconnect = true // Habilitar reconexão automática
            }

            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    Log.d("BrokerManager", "Conectado ao broker MQTT: $serverURI")
                    isConnected = true
                    if (reconnect) {
                        Log.d("BrokerManager", "Reconectado ao broker MQTT")
                    }
                    subscribeToTopics() // Reassinar aos tópicos após reconexão
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.e("BrokerManager", "Conexão perdida: ${cause?.message}")
                    isConnected = false
                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    val msg = message.toString()
                    Log.d("BrokerManager", "Mensagem recebida: Tópico: $topic, Mensagem: $msg")
                    onMessageReceived(topic, msg)
                }

                override fun deliveryComplete(token: IMqttDeliveryToken) {
                    Log.d("BrokerManager", "Mensagem entregue")
                }
            })

            // Conectando ao broker
            mqttClient?.connect(options)
        } catch (e: Exception) {
            Log.e("BrokerManager", "Erro ao conectar: ${e.message}")
        }
    }

    // Função para se inscrever em múltiplos tópicos
    private fun subscribeToTopics() {
        if (isConnected) {
            // Verifique os tópicos antes de inscrever
            topics.forEach { topic ->
                if (!subscribedTopics.containsKey(topic)) {
                    subscribe(topic)
                }
            }
        } else {
            Log.e("BrokerManager", "Não conectado, não foi possível se inscrever nos tópicos.")
        }
    }

    // Função para se inscrever em um tópico específico
    fun subscribe(topic: String) {
        if (mqttClient?.isConnected == true && !subscribedTopics.containsKey(topic)) {
            try {
                mqttClient?.subscribe(topic)
                subscribedTopics[topic] = true
                Log.d("BrokerManager", "Inscrito no tópico: $topic")
            } catch (e: Exception) {
                Log.e("BrokerManager", "Erro ao se inscrever no tópico: $topic. Erro: ${e.message}")
            }
        }
    }

    // Função para se desinscrever de um tópico
    fun unsubscribe(topic: String) {
        if (mqttClient?.isConnected == true && subscribedTopics.containsKey(topic)) {
            try {
                mqttClient?.unsubscribe(topic)
                subscribedTopics.remove(topic)
                Log.d("BrokerManager", "Desinscrito do tópico: $topic")
            } catch (e: Exception) {
                Log.e("BrokerManager", "Erro ao desinscrever do tópico: $topic. Erro: ${e.message}")
            }
        }
    }

    // Função para publicar uma mensagem em um tópico
    fun publish(topic: String, message: String) {
        if (mqttClient?.isConnected == true) {
            try {
                val mqttMessage = MqttMessage()
                mqttMessage.payload = message.toByteArray()
                mqttClient?.publish(topic, mqttMessage)
                Log.d("BrokerManager", "Mensagem publicada no tópico $topic: $message")
            } catch (e: Exception) {
                Log.e("BrokerManager", "Erro ao publicar mensagem: ${e.message}")
            }
        } else {
            Log.e("BrokerManager", "Não conectado, não foi possível publicar a mensagem.")
        }
    }

    // Função para desconectar do broker
    fun disconnect() {
        try {
            mqttClient?.disconnect()
            mqttClient?.close()
            isConnected = false
            Log.d("BrokerManager", "Desconectado do broker MQTT")
        } catch (e: Exception) {
            Log.e("BrokerManager", "Erro ao desconectar: ${e.message}")
        }
    }

    // Função para verificar se está conectado
    fun isConnected(): Boolean {
        return mqttClient?.isConnected == true
    }
}
