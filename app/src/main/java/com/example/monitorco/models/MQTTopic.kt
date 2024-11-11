package com.example.monitorco.models

data class MQTTopic(
    val userId: String,           // ID do usuário associado
    val hospedagemNumero: String, // Número da hospedagem
    val cardId: String,           // ID do card
    val tipo: String              // Tipo de informação (ex: "status", "alerta", "sensorCO")
) {
    // Retorna o tópico completo como string, baseado nos dados do card
    fun getTopic(): String {
        return "hospedagem/$userId/$hospedagemNumero/$cardId/$tipo"
    }
}
