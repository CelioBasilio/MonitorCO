package com.example.monitorco.models

import com.google.firebase.firestore.DocumentId

data class Card(
    @DocumentId val id: String = "", // ID único no Firestore
    val cardId: String,              // ID do card (sensor)
    val userId: String,              // ID do usuário associado
    val hospedagemNumero: String,    // Número do quarto/hospedagem
    var sensorCO: Int = 0,           // Nível de CO (0-100)
    var janela: Boolean = false,     // Estado da janela (true = aberta, false = fechada)
    var alertaAtivo: Boolean = false // Alerta ativo ou não
)
