package com.example.monitorco

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.monitorco.utils.Utils.isValidCNPJ
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : ComponentActivity() {

    private lateinit var nomeEditText: EditText
    private lateinit var enderecoEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var confirmarSenhaEditText: EditText
    private lateinit var cnpjEditText: EditText
    private lateinit var cadastrarButton: Button
    private lateinit var loginLink: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    val imgVoltar : ImageView = findViewById(R.id.imgVoltarLogin)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cadastro_activity)

        botaoVoltar()

        // Inicializa Firebase Authentication e Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inicializa os campos da interface de usuário
        nomeEditText = findViewById(R.id.nomeEditText)
        enderecoEditText = findViewById(R.id.enderecoEditText)
        emailEditText = findViewById(R.id.emailEditText)
        senhaEditText = findViewById(R.id.senhaEditText)
        confirmarSenhaEditText = findViewById(R.id.confirmarSenhaEditText)
        cnpjEditText = findViewById(R.id.cnpjEditText)
        cadastrarButton = findViewById(R.id.cadastrarButton)
        loginLink = findViewById(R.id.loginLink)

        // Ação do botão de cadastro
        cadastrarButton.setOnClickListener {
            // Obtém os valores inseridos pelo usuário
            val nome = nomeEditText.text.toString()
            val endereco = enderecoEditText.text.toString()
            val email = emailEditText.text.toString()
            val senha = senhaEditText.text.toString()
            val confirmarSenha = confirmarSenhaEditText.text.toString()
            val cnpj = cnpjEditText.text.toString().replace("[^0-9]".toRegex(), "")

            // Valida se os campos estão preenchidos
            if (nome.isEmpty() || endereco.isEmpty() || email.isEmpty() || senha.isEmpty() || cnpj.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos corretamente!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Valida o formato do CNPJ
            if (!isValidCNPJ(cnpj)) {
                Toast.makeText(this, "CNPJ inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verifica se as senhas coincidem
            if (senha != confirmarSenha) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Chama a função para cadastrar o usuário
            cadastrarUsuario(nome, endereco, email, senha, cnpj)
        }

        // Ação do link para login
        loginLink.setOnClickListener {
            // Navega para a tela de login
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Finaliza a tela de cadastro
        }
    }

    // Função para cadastrar o usuário no Firebase Authentication
    private fun cadastrarUsuario(nome: String, endereco: String, email: String, senha: String, cnpj: String) {
        auth.createUserWithEmailAndPassword(email, senha) // Cria o usuário com o email e senha
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Caso o cadastro seja bem-sucedido, salva os dados do usuário no Firestore
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    salvarDadosNoFirestore(userId, nome, endereco, email, cnpj)
                } else {
                    // Caso ocorra um erro, exibe uma mensagem de erro
                    val exceptionMessage = task.exception?.message ?: "Erro desconhecido"
                    Toast.makeText(this, "Erro ao cadastrar: $exceptionMessage", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Exibe uma mensagem de erro caso a criação do usuário falhe
                Toast.makeText(this, "Erro ao cadastrar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Função para salvar os dados do usuário no Firestore
    private fun salvarDadosNoFirestore(userId: String, nome: String, endereco: String, email: String, cnpj: String) {
        val usuario = hashMapOf(
            "nome" to nome,
            "endereco" to endereco,
            "email" to email,
            "cnpj" to cnpj
        )

        // Salva os dados do usuário no Firestore
        firestore.collection("usuarios").document(userId)
            .set(usuario)
            .addOnSuccessListener {
                // Exibe uma mensagem de sucesso e navega para a tela de login
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish() // Finaliza a tela de cadastro
            }
            .addOnFailureListener { e ->
                // Exibe uma mensagem de erro caso a gravação no Firestore falhe
                Toast.makeText(this, "Erro ao salvar dados: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    fun botaoVoltar(){
        imgVoltar.setOnClickListener {
            val intent = Intent(this@CadastroActivity, WelcomeActivity::class.java)
            startActivity(intent)
        }
    }

}
