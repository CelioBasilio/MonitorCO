package com.example.monitorco.activities

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.monitorco.managers.CardManager
import com.example.monitorco.managers.FirestoreManager
import com.example.monitorco.R
import com.example.monitorco.adapters.CardAdapter
import com.example.monitorco.models.Card
import com.google.firebase.auth.FirebaseAuth

class CardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var noCardsTextView: TextView
    private lateinit var firestoreManager: FirestoreManager
    private lateinit var cardAdapter: CardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cards)

        // Inicializar views
        recyclerView = findViewById(R.id.recyclerView)
        noCardsTextView = findViewById(R.id.cardsTextView)

        // Inicializar FirestoreManager
        firestoreManager = FirestoreManager()

        // Inicializar o CardAdapter
        cardAdapter = CardAdapter()

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = cardAdapter

        // Carregar os cards
        loadCards()
    }

    private fun loadCards() {
        // Recuperar o userId do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            // Se não houver um usuário logado, exibe uma mensagem de erro ou redireciona para a tela de login
            Toast.makeText(this, "Usuário não logado", Toast.LENGTH_SHORT).show()
            return
        }

        // Recupera os cards do Firebase
        firestoreManager.queryData("cards", "userId", userId) { documents ->
            // Certifique-se de que `documents` é uma lista de documentos do Firestore.
            val cardList = documents.mapNotNull { document ->
                // Verifica se o documento contém os campos necessários
                val sensorCO = document.getLong("sensorCO")?.toInt() ?: 0
                val janela = document.getBoolean("janela") ?: false
                val cardId = document.getString("cardId") ?: ""
                val hospedagemNumero = document.getString("hospedagemNumero") ?: ""

                // Se os campos obrigatórios estiverem presentes, cria o Card
                if (cardId.isNotEmpty() && hospedagemNumero.isNotEmpty()) {
                    Card(
                        id = document.id, // O ID do documento
                        cardId = cardId,
                        userId = userId,  // O ID do usuário logado
                        hospedagemNumero = hospedagemNumero,
                        sensorCO = sensorCO,
                        janela = janela
                    )
                } else {
                    null // Se faltar algum campo obrigatório, retorna null
                }
            }

            // Verifica se a lista de cards não está vazia
            if (cardList.isNotEmpty()) {
                noCardsTextView.visibility = TextView.GONE
                cardAdapter.setCards(cardList) // Atualiza o adapter com os cards
            } else {
                noCardsTextView.visibility = TextView.VISIBLE
                cardAdapter.setCards(emptyList()) // Não exibe nada se não houver cards
            }
        }
    }


}
