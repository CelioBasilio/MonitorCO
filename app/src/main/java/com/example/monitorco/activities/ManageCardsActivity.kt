package com.example.monitorco.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.monitorco.R
import com.example.monitorco.adapters.CardAdapter
import com.example.monitorco.databinding.ActivityManageCardsBinding
import com.example.monitorco.managers.CardManager
import com.example.monitorco.models.Card

class ManageCardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageCardsBinding
    private lateinit var cardManager: CardManager
    private lateinit var cardAdapter: CardAdapter
    private var userId: String = "" // ID do usuário logado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageCardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cardManager = CardManager()
        userId = intent.getStringExtra("USER_ID") ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Usuário não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Botão para salvar um novo card
        binding.btnSaveCard.setOnClickListener {
            val cardName = binding.edtCardName.text.toString()
            val hospedagemNumero = binding.edtHospedagemNumero.text.toString()
            if (cardName.isNotEmpty() && hospedagemNumero.isNotEmpty()) {
                saveNewCard(cardName, hospedagemNumero)
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Carregar cards existentes
        loadCards()
    }

    private fun loadCards() {
        cardManager.getCardsByUserId(userId) { cards ->
            if (cards != null) {
                cardAdapter.setCards(cards)
            } else {
                Toast.makeText(this, "Erro ao carregar cards.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveNewCard(cardName: String, hospedagemNumero: String) {
        val newCardId = generateNewCardId()

        val newCard = Card(
            id = "",
            cardId = newCardId,
            userId = userId,
            hospedagemNumero = hospedagemNumero,
            sensorCO = 0,
            janela = false,
            alertaAtivo = false
        )

        cardManager.addCard(newCard) { success ->
            if (success) {
                Toast.makeText(this, "Card salvo com sucesso!", Toast.LENGTH_SHORT).show()
                val topic = "pousada/$newCardId/status"
                binding.txtCardInfo.text = "Tópico MQTT: $topic"
                loadCards()
            } else {
                Toast.makeText(this, "Erro ao salvar card", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateNewCardId(): String {
        return "card-${System.currentTimeMillis()}"
    }
}
