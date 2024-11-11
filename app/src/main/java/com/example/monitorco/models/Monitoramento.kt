package com.example.monitorco.models

import com.google.firebase.firestore.DocumentId

data class Monitoramento(
    @DocumentId val id: String = "",             // ID único do monitoramento no Firestore
    val cardId: String,                          // ID do card que disparou o alerta
    val nivelCO: Float,                          // Nível de CO no monitoramento
    val janela: Boolean,                         // Estado da janela
    val timestamp: String,                       // Timestamp do monitoramento
    val alertaAtivo: Boolean                     // Se o alerta foi disparado
)
