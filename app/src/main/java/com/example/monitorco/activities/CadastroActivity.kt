package com.example.monitorco.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.monitorco.R
import com.example.monitorco.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var nomeEditText: EditText
    private lateinit var enderecoEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var confirmarSenhaEditText: EditText
    private lateinit var cnpjEditText: EditText
    private lateinit var cadastrarButton: Button
    private lateinit var loginLink: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        // Inicializando os componentes
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        nomeEditText = findViewById(R.id.nomeEditText)
        enderecoEditText = findViewById(R.id.enderecoEditText)
        emailEditText = findViewById(R.id.emailEditText)
        senhaEditText = findViewById(R.id.senhaEditText)
        confirmarSenhaEditText = findViewById(R.id.confirmarSenhaEditText)
        cnpjEditText = findViewById(R.id.cnpjEditText)
        cadastrarButton = findViewById(R.id.cadastrarButton)
        loginLink = findViewById(R.id.loginLink)
        progressBar = findViewById(R.id.progressBar)

        // Esconder a progressBar inicialmente
        progressBar.visibility = View.GONE

        // Ação de cadastro
        cadastrarButton.setOnClickListener {
            val nome = nomeEditText.text.toString().trim()
            val endereco = enderecoEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val senha = senhaEditText.text.toString().trim()
            val confirmarSenha = confirmarSenhaEditText.text.toString().trim()
            val cnpj = cnpjEditText.text.toString().trim()

            // Verificar se todos os campos estão preenchidos e se as senhas são iguais
            if (nome.isEmpty() || endereco.isEmpty() || email.isEmpty() || senha.isEmpty() || cnpj.isEmpty()) {
                Toast.makeText(this, "Todos os campos devem ser preenchidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha != confirmarSenha) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mostrar progressBar
            progressBar.visibility = View.VISIBLE

            // Criar o usuário no Firebase Authentication
            firebaseAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Se o cadastro for bem-sucedido, obtém o ID do usuário
                        val userId = firebaseAuth.currentUser?.uid

                        if (userId != null) {
                            // Criação do usuário para o Firestore
                            val user = User(id = userId, nome = nome, endereco = endereco, email = email, cnpj = cnpj)

                            // Salvar dados no Firestore
                            firestore.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()

                                    // Redireciona para a tela principal após o cadastro
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Erro ao salvar dados no Firestore", Toast.LENGTH_SHORT).show()
                                    progressBar.visibility = View.GONE
                                }
                        }
                    } else {
                        // Caso falhe no cadastro no Firebase Authentication
                        Toast.makeText(this, "Erro ao cadastrar usuário: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                    }
                }
        }

        // Link para a tela de login
        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
