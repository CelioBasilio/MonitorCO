package com.example.monitorco

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.monitorco.api.RetrofitInstance
import com.example.monitorco.model.Login
import com.example.monitorco.utils.Utils.isNetworkAvailable
import com.example.monitorco.utils.Utils.isServerAvailable
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginActivity : ComponentActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var cadastroLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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

        // Verifica a conexão com a internet
        if (!isNetworkAvailable(this@LoginActivity)) {
            Toast.makeText(this, "Sem conexão com a Internet. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica se os campos estão vazios
        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica a conexão com o servidor
        lifecycleScope.launch {
            if (!isServerAvailable(email, senha)) {
                Toast.makeText(
                    this@LoginActivity,
                    "Não há conexão com o servidor. Tente novamente mais tarde.",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            val login = Login(email, senha) // Criação da instância do Login

            // Continue com o processo de login
            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.login(login)
                    if (response.isSuccessful) {
                        val sharedPrefs = getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
                        sharedPrefs.edit().apply {
                            putBoolean("isLoggedIn", true)
                            apply()
                        }
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Login falhou. Tente novamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: HttpException) {
                    Toast.makeText(this@LoginActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Erro inesperado: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

}
