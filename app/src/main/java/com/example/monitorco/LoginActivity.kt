package com.example.monitorco

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.monitorco.utils.Utils.isNetworkAvailable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp

class LoginActivity : ComponentActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var cadastroLink: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Certifique-se de que o Firebase está inicializado
        FirebaseApp.initializeApp(this)

        // Define o layout da Activity
        setContentView(R.layout.activity_login)

        // Inicializa Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Inicializa os elementos da interface
        emailEditText = findViewById(R.id.emailEditText)
        senhaEditText = findViewById(R.id.senhaEditText)
        loginButton = findViewById(R.id.loginButton)
        cadastroLink = findViewById(R.id.cadastroLink)

        // Verifica se o usuário já está autenticado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Configura o clique do botão de login
        loginButton.setOnClickListener {
            fazerLogin()
        }

        // Configura o clique no link de cadastro
        cadastroLink.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
            finish()
        }
    }

    // Função para realizar o login
    private fun fazerLogin() {
        val email = emailEditText.text.toString().trim()
        val senha = senhaEditText.text.toString().trim()

        // Verifica a conexão com a internet
        if (!isNetworkAvailable(this)) {
            showToast("Sem conexão com a Internet. Tente novamente mais tarde.")
            return
        }

        // Verifica se os campos estão preenchidos
        if (email.isEmpty() || senha.isEmpty()) {
            showToast("Por favor, preencha todos os campos.")
            return
        }

        // Verifica o formato do email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email em formato incorreto.")
            return
        }

        // Faz o login no Firebase Authentication
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login bem-sucedido
                    showToast("Login realizado com sucesso!")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    showToast("Falha no login: ${task.exception?.message}")
                }
            }
            .addOnFailureListener { e ->
                showToast("Erro ao autenticar: ${e.message}")
            }
    }

    // Função auxiliar para exibir mensagens Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
