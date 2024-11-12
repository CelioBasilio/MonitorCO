package com.example.monitorco

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_activity)

        val btRegister: Button = findViewById(R.id.btRegisterWelcome)
        val btLogin: Button = findViewById(R.id.btLoginWelcome)

        btRegister.setOnClickListener {
            val intent = Intent(this@WelcomeActivity, CadastroActivity::class.java)
            startActivity(intent)
        }

        btLogin.setOnClickListener {
            val intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}