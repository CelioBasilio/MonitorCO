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
import com.example.monitorco.model.Usuario
import com.example.monitorco.utils.Utils.isCNPJValido
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CadastroActivity : ComponentActivity() {

    private lateinit var nomeEditText: EditText
    private lateinit var enderecoEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var confirmarSenhaEditText: EditText
    private lateinit var cnpjEditText: EditText
    private lateinit var cadastrarButton: Button
    private lateinit var loginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        nomeEditText = findViewById(R.id.nomeEditText)
        enderecoEditText = findViewById(R.id.enderecoEditText)
        emailEditText = findViewById(R.id.emailEditText)
        senhaEditText = findViewById(R.id.senhaEditText)
        confirmarSenhaEditText = findViewById(R.id.confirmarSenhaEditText)
        cnpjEditText = findViewById(R.id.cnpjEditText)
        cadastrarButton = findViewById(R.id.cadastrarButton)
        loginLink = findViewById(R.id.loginLink)

        cadastrarButton.setOnClickListener {
            val senha = senhaEditText.text.toString()
            val confirmarSenha = confirmarSenhaEditText.text.toString()
            val cnpj = cnpjEditText.text.toString()

            // Verifique se o CNPJ é válido
            if (!isCNPJValido(cnpj)) {
                Toast.makeText(this, "CNPJ inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha == confirmarSenha) {
                // Chame a função para cadastrar o usuário
                cadastrarUsuario(nomeEditText.text.toString(), enderecoEditText.text.toString(), emailEditText.text.toString(), senha, cnpj)
            } else {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            }
        }

        loginLink.setOnClickListener {
            // Navega para a LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Opcional: fecha a CadastroActivity
        }
    }

    private fun cadastrarUsuario(nome: String, endereco: String, email: String, senha: String, cnpj: String) {
        // Validação simples
        if (nome.isEmpty() || endereco.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos corretamente!", Toast.LENGTH_SHORT).show()
            return
        }

        val usuario = Usuario(nome, endereco, email, senha, cnpj)

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.cadastrarUsuario(usuario)
                if (response.isSuccessful) {
                    Toast.makeText(this@CadastroActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish() // Finaliza a atividade após o cadastro
                } else {
                    Toast.makeText(this@CadastroActivity, "Erro ao cadastrar. Tente novamente.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                Toast.makeText(this@CadastroActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@CadastroActivity, "Erro inesperado: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
