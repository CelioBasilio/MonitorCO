package com.example.monitorco

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp

class Splash : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o Firebase
        FirebaseApp.initializeApp(this)

        // Verifica se o usuário está autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Redireciona para a tela apropriada com base no estado de autenticação
        if (currentUser != null) {
            // Se o usuário já estiver autenticado, redireciona para a MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Caso contrário, redireciona para a LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Finaliza a SplashActivity para que não fique na pilha
        finish()
    }
}
