package com.example.monitorco

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.core.content.edit
import android.widget.Toast

// A SettingsActivity é responsável por gerenciar as configurações do limite de CO na aplicação.
class SettingsActivity : ComponentActivity() {

    // Declaração do campo de entrada onde o usuário pode inserir o novo limite de CO
    private lateinit var coLimitInput: EditText

//    val imgVoltar : ImageView = findViewById(R.id.imgVoltarLogin)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Inicializa a referência ao campo de entrada (EditText) onde o usuário digita o limite
        coLimitInput = findViewById(R.id.coLimitInput)

        // Inicializa a referência ao botão de salvar as configurações
        val saveButton: Button = findViewById(R.id.saveButton)

        // Recupera os SharedPreferences para acessar as configurações salvas
        val sharedPreferences = getSharedPreferences("MonitorCOSettings", Context.MODE_PRIVATE)

        // Recupera o limite de CO atual, com valor padrão de 7 se não encontrado
        val currentLimit = sharedPreferences.getInt("coLimit", 7)

        // Define o valor atual do limite de CO no campo de entrada (EditText)
        coLimitInput.setText(currentLimit.toString())

        // Configura o comportamento do botão de salvar
        saveButton.setOnClickListener {
            // Tenta converter o texto inserido pelo usuário em um número inteiro
            val newLimit = coLimitInput.text.toString().toIntOrNull()

            // Verifica se o valor inserido é válido e maior que 0
            if (newLimit != null && newLimit > 0) {
                // Se for válido, salva o novo limite nos SharedPreferences
                sharedPreferences.edit {
                    putInt("coLimit", newLimit) // Atualiza o limite de CO
                }

                // Exibe uma mensagem de sucesso
                Toast.makeText(this, "Limite de CO atualizado para $newLimit", Toast.LENGTH_SHORT).show()

                // Fecha a activity e retorna para a tela anterior
                finish()
            } else {
                // Se o valor não for válido, exibe uma mensagem de erro
                Toast.makeText(this, "Por favor, insira um valor válido", Toast.LENGTH_SHORT).show()
            }
        }
//        botaoVoltar()
    }

    // Método estático para iniciar a SettingsActivity a partir de outra activity
    companion object {
        fun start(context: Context) {
            // Cria uma nova Intent para iniciar a SettingsActivity
            val intent = Intent(context, SettingsActivity::class.java)

            // Inicia a activity
            context.startActivity(intent)
        }
    }

//    fun botaoVoltar(){
//        imgVoltar.setOnClickListener {
//            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
//            startActivity(intent)
//        }
//    }
}
