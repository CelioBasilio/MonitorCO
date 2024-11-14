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
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        emailEditText = findViewById(R.id.emailEditText)
        senhaEditText = findViewById(R.id.senhaEditText)
        loginButton = findViewById(R.id.loginButton)
        cadastroLink = findViewById(R.id.cadastroLink)

        loginButton.setOnClickListener {
            fazerLogin()
        }

        cadastroLink.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }

    private fun fazerLogin() {
        val email = emailEditText.text.toString().trim()
        val senha = senhaEditText.text.toString().trim()

        if (!isNetworkAvailable(this)) {
            showToast("Sem conexão com a Internet.")
            return
        }

        if (email.isEmpty() || senha.isEmpty()) {
            showToast("Preencha todos os campos.")
            return
        }

        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
