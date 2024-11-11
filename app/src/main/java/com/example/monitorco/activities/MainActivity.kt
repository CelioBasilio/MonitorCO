package com.example.monitorco.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monitorco.R
import com.example.monitorco.adapters.CardAdapter
import com.example.monitorco.databinding.ActivityMainBinding
import com.example.monitorco.managers.BrokerManager
import com.example.monitorco.managers.CardManager
import com.example.monitorco.models.Card
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cardManager: CardManager
    private lateinit var brokerManager: BrokerManager
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var cardAdapter: CardAdapter
    private var userId: String? = null
    private var empresaNome: String? = null // Nome da empresa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        // Recuperar o userId passado pelo Intent
        userId = intent.getStringExtra("USER_ID")

        // Verificar se o usuário está logado, se não redirecionar para a tela de login
        if (userId == null) {
            redirectToLogin()
            return
        }

        // Recuperar o nome da empresa do usuário
        empresaNome = getEmpresaNome()

        if (empresaNome.isNullOrBlank()) {
            empresaNome = "pousada"
        }

        // Inicializar o CardManager e o BrokerManager
        cardManager = CardManager()

        // Inicializar o BrokerManager
        brokerManager = BrokerManager(
            context = this,
            onMessageReceived = { topic, message -> onMessageReceived(topic, message) },
            username = empresaNome!!,
            password = "Pousada123"
        )

        // Configurar RecyclerView
        setupRecyclerView()

        // Carregar os cards salvos
        loadCards()

        // Configurar botões
        setupButtons()

        // Conectar ao broker MQTT com os tópicos dos cards
        connectToBroker()
    }

    private fun setupRecyclerView() {
        cardAdapter = CardAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = cardAdapter
        }
    }

    private fun setupButtons() {
        // Botão para abrir a tela de gerenciamento de cards
        binding.btnAddCard.setOnClickListener {
            openManageCardsActivity()
        }

        // Botão para exibir os tópicos MQTT dos cards
        binding.btnShowTopic.setOnClickListener {
            showCardTopics()
        }
    }

    private fun loadCards() {
        cardManager.getCardsByUserId(userId ?: "") { cards ->
            if (cards != null) {
                cardAdapter.setCards(cards)
            } else {
                Toast.makeText(this, "Erro ao carregar os cards.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun connectToBroker() {
        cardManager.getCardsByUserId(userId ?: "") { cards ->
            if (cards != null) {
                val topics = cards.map { "pousada/${it.cardId}/status" }
                brokerManager.connect(topics)
            } else {
                Log.e("MainActivity", "Erro ao carregar os cards para conexão MQTT.")
            }
        }
    }

    private fun showCardTopics() {
        cardManager.getCardsByUserId(userId ?: "") { cards ->
            if (cards != null) {
                val topics = cards.joinToString("\n") { "pousada/${it.cardId}/status" }
                Toast.makeText(this, "Tópicos:\n$topics", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Nenhum tópico disponível.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openManageCardsActivity() {
        try {
            val intent = Intent(this, ManageCardsActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro ao abrir ManageCardsActivity", e)
            Toast.makeText(this, "Erro ao abrir a tela de gerenciamento de cards.", Toast.LENGTH_SHORT).show()
        }
    }



    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getEmpresaNome(): String? {
        return firebaseAuth.currentUser?.displayName
    }

    private fun onMessageReceived(topic: String, message: String) {
        Log.d("MainActivity", "Mensagem recebida no tópico $topic: $message")

        val cardId = topic.split("/")[1]
        cardManager.getCardsByUserId(userId ?: "") { cards ->
            val card = cards?.firstOrNull { it.cardId == cardId }
            card?.let {
                it.sensorCO = message.toIntOrNull() ?: 0
                cardManager.updateCard(it) { success ->
                    if (success) {
                        loadCards()
                    } else {
                        Toast.makeText(this, "Erro ao atualizar o card.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
