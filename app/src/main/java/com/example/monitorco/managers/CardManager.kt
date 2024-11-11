package com.example.monitorco.managers

import android.util.Log
import com.example.monitorco.models.Card
import com.example.monitorco.models.Monitoramento
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CardManager {

    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "CardManager"

    // Método para obter um Card por User ID
    fun getCardByUserId(userId: String, callback: (Card?) -> Unit) {
        firestore.collection("cards")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(TAG, "Nenhum card encontrado para o usuário $userId")
                    callback(null)
                } else {
                    val card = documents.first().toObject<Card>()
                    callback(card)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao buscar card: ${exception.message}")
                callback(null)
            }
    }

    // Método para obter todos os Cards de um usuário
    fun getCardsByUserId(userId: String, callback: (List<Card>?) -> Unit) {
        firestore.collection("cards")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val cards = documents.mapNotNull { it.toObject<Card>() }
                callback(cards)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao buscar cards: ${exception.message}")
                callback(null)
            }
    }

    // Método para obter o tópico do Card com base no User ID
    fun getCardTopicByUserId(userId: String, callback: (String?) -> Unit) {
        firestore.collection("cards")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d(TAG, "Nenhum card encontrado para o usuário $userId")
                    callback(null)
                } else {
                    // Certificando-se de que o card não é nulo
                    val card = documents.first().toObject<Card>()
                    if (card != null && card.id != null) {
                        val topic = "pousada/${card.id}/status"
                        callback(topic)
                    } else {
                        Log.d(TAG, "Card ou ID não encontrados para o usuário $userId")
                        callback(null)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao gerar tópico: ${exception.message}")
                callback(null)
            }
    }

    // Método para adicionar um novo Card
    fun addCard(card: Card, callback: (Boolean) -> Unit) {
        firestore.collection("cards")
            .add(card)
            .addOnSuccessListener {
                Log.d(TAG, "Card adicionado com sucesso")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao adicionar card: ${exception.message}")
                callback(false)
            }
    }

    // Método para atualizar um Card no Firestore
    fun updateCard(card: Card, callback: (Boolean) -> Unit) {
        firestore.collection("cards").document(card.id)
            .set(card)
            .addOnSuccessListener {
                Log.d(TAG, "Card atualizado com sucesso")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao atualizar card: ${exception.message}")
                callback(false)
            }
    }

    // Método para verificar o nível de CO e o estado da janela
    fun checkCardStatus(card: Card): String {
        val coLevel = card.sensorCO
        val windowStatus = if (card.janela) "Aberta" else "Fechada"
        return "Nível de CO: $coLevel | Estado da janela: $windowStatus"
    }

    // Método para verificar se o alerta está ativo
    fun isAlertActive(card: Card): Boolean {
        return card.alertaAtivo
    }

    // Método para obter todos os Cards no Firestore
    fun getAllCards(callback: (List<Card>?) -> Unit) {
        firestore.collection("cards")
            .get()
            .addOnSuccessListener { documents ->
                val cards = documents.mapNotNull { it.toObject<Card>() }
                callback(cards)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao buscar todos os cards: ${exception.message}")
                callback(null)
            }
    }

    // Método para registrar um novo evento de monitoramento (alerta)
    fun registerMonitoramento(
        cardId: String,
        nivelCO: Float,
        janela: Boolean,
        alertaAtivo: Boolean,
        callback: (Boolean) -> Unit
    ) {
        val timestamp = getCurrentTimestamp()

        val monitoramento = Monitoramento(
            nivelCO = nivelCO,
            janela = janela,
            alertaAtivo = alertaAtivo,
            cardId = cardId,
            timestamp = timestamp
        )

        firestore.collection("monitoramentos")
            .add(monitoramento)
            .addOnSuccessListener {
                Log.d(TAG, "Monitoramento registrado com sucesso")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao registrar monitoramento: ${exception.message}")
                callback(false)
            }
    }

    // Função auxiliar para obter o timestamp atual no formato ISO 8601
    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
