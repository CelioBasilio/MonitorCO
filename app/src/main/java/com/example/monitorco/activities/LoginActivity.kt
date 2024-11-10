package com.example.monitorco.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log  // Importe a classe Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.monitorco.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var cadastroLink: TextView
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializando os componentes
        firebaseAuth = FirebaseAuth.getInstance()
        emailEditText = findViewById(R.id.emailEditText)
        senhaEditText = findViewById(R.id.senhaEditText)
        loginButton = findViewById(R.id.loginButton)
        cadastroLink = findViewById(R.id.cadastroLink)

        // Ação de login
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val senha = senhaEditText.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Email e senha são obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Realizar login com Firebase Authentication
            firebaseAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Login bem-sucedido
                        Toast.makeText(this, "Login bem-sucedido", Toast.LENGTH_SHORT).show()

                        // Chama a função para verificar e atualizar os dados do usuário
                        checkAndUpdateUserData()

                        // Passa o userId para a MainActivity
                        val userId = firebaseAuth.currentUser?.uid
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("USER_ID", userId) // Passando o userId para a MainActivity
                        startActivity(intent)
                        finish()
                    } else {
                        // Erro no login
                        Toast.makeText(this, "Erro ao fazer login: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Link para a tela de cadastro
        cadastroLink.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
            finish() // Finaliza a activity atual
        }
    }

    // Função para verificar e atualizar os dados do usuário
    fun checkAndUpdateUserData() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            // Referência ao documento do usuário no Firestore
            val userDocRef = firestore.collection("users").document(userId)

            // Recupera os dados do usuário
            userDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val nome = document.getString("nome")
                    val endereco = document.getString("endereco")
                    val cnpj = document.getString("cnpj")

                    // Verifique se os dados necessários estão completos
                    if (nome.isNullOrBlank() || endereco.isNullOrBlank() || cnpj.isNullOrBlank()) {
                        // Se algum dado necessário estiver faltando, atualize com informações padrões
                        val updatedUser: Map<String, Any> = hashMapOf(
                            "nome" to (nome ?: firebaseAuth.currentUser?.displayName ?: "Nome padrão"),
                            "endereco" to (endereco ?: "Endereço padrão"),
                            "cnpj" to (cnpj ?: "CNPJ padrão")
                        )

                        // Atualiza o Firestore com os dados faltantes
                        userDocRef.update(updatedUser).addOnSuccessListener {
                            Log.d("LoginActivity", "Dados do usuário atualizados com sucesso.")
                        }.addOnFailureListener { e ->
                            Log.e("LoginActivity", "Erro ao atualizar dados do usuário: ${e.message}")
                        }
                    }
                } else {
                    // Se o documento não existir, significa que o usuário não foi criado corretamente no Firestore
                    // Podemos criar um documento com dados padrão ou enviar o usuário para a tela de cadastro
                    Log.e("LoginActivity", "Usuário não encontrado no Firestore.")
                }
            }.addOnFailureListener { e ->
                Log.e("LoginActivity", "Erro ao buscar dados do usuário: ${e.message}")
            }
        }
    }
}
