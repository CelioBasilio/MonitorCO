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

        // Inicializa o Firebase
        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_login)

        // Inicializa Firebase Authentication
        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        senhaEditText = findViewById(R.id.senhaEditText)
        loginButton = findViewById(R.id.loginButton)
        cadastroLink = findViewById(R.id.cadastroLink)

        // Verifica se o usuário já está autenticado ao abrir o LoginActivity
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Usuário já está logado, redireciona para a MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Ação do botão de login
        loginButton.setOnClickListener {
            fazerLogin()
        }

        // Ação do link para cadastro
        cadastroLink.setOnClickListener {
            // Navega para a tela de cadastro
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
            Toast.makeText(this, "Sem conexão com a Internet. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se os campos de email e senha estão vazios
        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        // Faz o login utilizando Firebase Authentication
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login bem-sucedido, salva o estado de autenticação
                    val sharedPrefs = getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
                    sharedPrefs.edit().apply {
                        putBoolean("isLoggedIn", true)
                        apply()
                    }
                    Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()

                    // Navega para a MainActivity após o login
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Caso o login falhe, exibe a mensagem de erro
                    val errorMessage = task.exception?.message ?: "Erro desconhecido"
                    Toast.makeText(this, "Falha no login: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Caso ocorra um erro ao tentar autenticar, exibe a mensagem
                Toast.makeText(this, "Erro ao autenticar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
