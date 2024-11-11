package com.example.monitorco

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        // Inicializa Firebase Authentication e Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        nomeEditText = findViewById(R.id.nomeEditText)
        enderecoEditText = findViewById(R.id.enderecoEditText)
        emailEditText = findViewById(R.id.emailEditText)
        senhaEditText = findViewById(R.id.senhaEditText)
        confirmarSenhaEditText = findViewById(R.id.confirmarSenhaEditText)
        cnpjEditText = findViewById(R.id.cnpjEditText)
        cadastrarButton = findViewById(R.id.cadastrarButton)
        loginLink = findViewById(R.id.loginLink)

        cadastrarButton.setOnClickListener {
            val nome = nomeEditText.text.toString()
            val endereco = enderecoEditText.text.toString()
            val email = emailEditText.text.toString()
            val senha = senhaEditText.text.toString()
            val confirmarSenha = confirmarSenhaEditText.text.toString()
            val cnpj = cnpjEditText.text.toString().replace("[^0-9]".toRegex(), "")

            // Validação de campos vazios
            if (nome.isEmpty() || endereco.isEmpty() || email.isEmpty() || senha.isEmpty() || cnpj.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos corretamente!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validação CNPJ
            if (!isValidCNPJ(cnpj)) {
                Toast.makeText(this, "CNPJ inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // Verificar se as senhas coincidem
            if (senha != confirmarSenha) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Criar o usuário
            cadastrarUsuario(nome, endereco, email, senha, cnpj)
        }

        loginLink.setOnClickListener {
            // Navega para a tela de login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun cadastrarUsuario(nome: String, endereco: String, email: String, senha: String, cnpj: String) {
        // Cria o usuário com Firebase Authentication
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Usuário criado com sucesso, agora salva no Firestore
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    salvarDadosNoFirestore(userId, nome, endereco, email, cnpj)
                } else {
                    val exceptionMessage = task.exception?.message ?: "Erro desconhecido"
                    Toast.makeText(this, "Erro ao cadastrar: $exceptionMessage", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao cadastrar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun salvarDadosNoFirestore(userId: String, nome: String, endereco: String, email: String, cnpj: String) {
        val usuario = hashMapOf(
            "nome" to nome,
            "endereco" to endereco,
            "email" to email,
            "cnpj" to cnpj
        )

        firestore.collection("usuarios").document(userId)
            .set(usuario)
            .addOnSuccessListener {
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                // Ao cadastrar, já navega para o LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar dados: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
