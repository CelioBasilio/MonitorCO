package com.example.monitorco

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.core.content.edit
import android.widget.Toast

class SettingsActivity : ComponentActivity() {

    private lateinit var coLimitInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        coLimitInput = findViewById(R.id.coLimitInput)
        val saveButton: Button = findViewById(R.id.saveButton)

        // Carrega o limite atual de CO dos SharedPreferences
        val sharedPreferences = getSharedPreferences("MonitorCOSettings", Context.MODE_PRIVATE)
        val currentLimit = sharedPreferences.getInt("coLimit", 7)
        coLimitInput.setText(currentLimit.toString())

        saveButton.setOnClickListener {
            val newLimit = coLimitInput.text.toString().toIntOrNull()
            if (newLimit != null && newLimit > 0) {
                // Salva o novo limite nos SharedPreferences
                sharedPreferences.edit {
                    putInt("coLimit", newLimit)
                }
                Toast.makeText(this, "Limite de CO atualizado para $newLimit", Toast.LENGTH_SHORT).show()
                finish() // Fecha a activity de configurações
            } else {
                Toast.makeText(this, "Por favor, insira um valor válido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}