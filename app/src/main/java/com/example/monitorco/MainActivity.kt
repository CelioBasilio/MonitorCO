package com.example.monitorco

import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HospedagemAdapter
    private var mediaPlayer: MediaPlayer? = null
    var isAlertActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Passa a referência da MainActivity ao adapter
        adapter = HospedagemAdapter(this, mutableListOf())
        recyclerView.adapter = adapter

        // Exemplo inicial de hospedagem
        addNewHospedagem("Hospedagem Nº 01", 20f, ContextCompat.getDrawable(this, R.drawable.ic_window_closed)!!)
    }

    private fun addNewHospedagem(label: String, value: Float, icon: Drawable) {
        val hospedagem = Hospedagem(label, value, icon)
        adapter.addHospedagem(hospedagem)
        checkCOLevels(value)
    }

    private fun checkCOLevels(value: Float) {
        if (value >= 20) {
            startAlert()
        }
    }

    private fun startAlert() {
        if (!isAlertActive) {
            isAlertActive = true
            mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound)
            mediaPlayer?.start()

            // Repetir o som do alerta
            val handler = android.os.Handler()
            val runnable = object : Runnable {
                override fun run() {
                    if (isAlertActive) {
                        mediaPlayer?.start() // Reproduz o som novamente
                        handler.postDelayed(this, 10) // Repete a cada 0,010 segundos (ajuste se necessário)
                    }
                }
            }
            handler.postDelayed(runnable, 3000) // Começa a repetição após 3 segundos

            // Ajusta a altura do RecyclerView se necessário
            //recyclerView.layoutParams.height = 300 // altura quando o alerta está ativo
            //recyclerView.requestLayout()
        }
    }

    fun stopAlert() { // Método acessível a partir do Adapter
        if (isAlertActive) {
            isAlertActive = false
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            // Retorna à altura original do RecyclerView
            //recyclerView.layoutParams.height = 400 // altura original do CardView
            //recyclerView.requestLayout()

            // Notifica o Adapter para atualizar a visibilidade do botão
            adapter.notifyDataSetChanged()
        }
    }


    private fun playAlertSound() {
        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound)
        mediaPlayer?.start()
    }
}
