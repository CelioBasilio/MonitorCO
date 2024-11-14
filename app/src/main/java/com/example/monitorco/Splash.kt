package com.example.monitorco

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp

class Splash : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Define o layout da Splash Screen
        setContentView(R.layout.splash)

        // Inicializa o Firebase
        FirebaseApp.initializeApp(this)

        // Obtém a referência ao ImageView e aplica a animação de zoom-in
        val imgLogo = findViewById<ImageView>(R.id.imgLogoSplash)
        imgLogo.scaleX = 0f
        imgLogo.scaleY = 0f
        imgLogo.alpha = 0f

        // Anima a imagem para crescer e desvanecer ao mesmo tempo
        imgLogo.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(1000) // Duração da animação (1 segundo)
            .start()

        // Adiciona um delay antes de redirecionar para a próxima tela
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000) // 2000ms = 2 segundos
    }
}
