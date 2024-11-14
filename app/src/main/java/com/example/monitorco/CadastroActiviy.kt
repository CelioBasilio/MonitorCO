package com.example.monitorco

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.monitorco.utils.Utils.isValidCNPJ
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

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

        // Inicializa os campos da interface de usuário
        nomeEditText = findViewById(R.id.nomeEditText)
        enderecoEditText = findViewById(R.id.enderecoEditText)
        emailEditText = findViewById(R.id.emailEditText)
        senhaEditText = findViewById(R.id.senhaEditText)
        confirmarSenhaEditText = findViewById(R.id.confirmarSenhaEditText)
        cnpjEditText = findViewById(R.id.cnpjEditText)
        cadastrarButton = findViewById(R.id.cadastrarButton)
        loginLink = findViewById(R.id.loginLink)

        // Configura o clique no botão de cadastro
        cadastrarButton.setOnClickListener {
            val nome = nomeEditText.text.toString().trim()
            val endereco = enderecoEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val senha = senhaEditText.text.toString().trim()
            val confirmarSenha = confirmarSenhaEditText.text.toString().trim()
            val cnpj = cnpjEditText.text.toString().replace("[^0-9]".toRegex(), "")

            // Validação dos campos
            if (nome.isEmpty() || endereco.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty() || cnpj.isEmpty()) {
                showToast("Preencha todos os campos!")
                return@setOnClickListener
            }

            // Valida o formato do e-mail
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Formato de e-mail incorreto")
                return@setOnClickListener
            }

            // Valida o CNPJ
            if (!isValidCNPJ(cnpj)) {
                showToast("CNPJ inválido")
                return@setOnClickListener
            }

            // Verifica se as senhas coincidem
            if (senha != confirmarSenha) {
                showToast("As senhas não coincidem")
                return@setOnClickListener
            }

            // Verifica se o CNPJ já está cadastrado no Firestore
            verificarCNPJExistente(cnpj) { cnpjExiste ->
                if (cnpjExiste) {
                    showToast("CNPJ já cadastrado")
                } else {
                    // Inicia o cadastro do usuário
                    cadastrarUsuario(nome, endereco, email, senha, cnpj)
                }
            }
        }

        // Configura o clique no link de login
        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Função para verificar se o CNPJ já existe no Firestore
    private fun verificarCNPJExistente(cnpj: String, callback: (Boolean) -> Unit) {
        firestore.collection("usuarios")
            .whereEqualTo("cnpj", cnpj)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result: QuerySnapshot? = task.result
                    callback(!result?.isEmpty!!)
                } else {
                    showToast("Erro ao verificar CNPJ")
                    callback(false)
                }
            }
            .addOnFailureListener {
                showToast("Erro ao acessar o banco de dados")
                callback(false)
            }
    }

    // Função para cadastrar o usuário no Firebase Authentication
    private fun cadastrarUsuario(nome: String, endereco: String, email: String, senha: String, cnpj: String) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Cadastro bem-sucedido, salva os dados no Firestore
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    salvarDadosNoFirestore(userId, nome, endereco, email, cnpj)
                } else {
                    // Tratamento para e-mail já cadastrado
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        showToast("E-mail já cadastrado")
                    } else {
                        // Erro genérico (oculta mensagem em inglês)
                        showToast("Erro ao criar conta")
                    }
                }
            }
            .addOnFailureListener {
                showToast("Erro ao criar usuário")
            }
    }

    // Função para salvar dados no Firestore
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
                showToast("Cadastro realizado com sucesso!")
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                showToast("Erro ao salvar dados")
                auth.currentUser?.delete() // Exclui o usuário caso haja falha ao salvar
            }
    }

    // Função utilitária para exibir mensagens
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
