package com.example.monitorco

import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.monitorco.utils.Utils

class MainActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HospedagemAdapter
    private var mediaPlayer: MediaPlayer? = null
    var isAlertActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verifica a conexão com a internet
        if (!Utils.isNetworkAvailable(this)) {
            showNoConnectionAlert()
            return
        }

        // Verifica se o usuário está logado, caso contrário redireciona para LoginActivity
        if (!isUserLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Encerra a MainActivity se não estiver logado
            return
        }

        setContentView(R.layout.activity_main)
        initializeUI()
        loadHospedagens()
    }

    private fun initializeUI() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HospedagemAdapter(this, mutableListOf())
        recyclerView.adapter = adapter
    }

    private fun showNoConnectionAlert() {
        Toast.makeText(this, "Por favor, conecte-se à internet.", Toast.LENGTH_LONG).show()
        finish() // Encerra o aplicativo
    }

    private fun loadHospedagens() {
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
        if (isAlertActive) return

        isAlertActive = true
        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound)
        mediaPlayer?.start()

        val handler = android.os.Handler()
        val runnable = object : Runnable {
            override fun run() {
                if (isAlertActive) {
                    mediaPlayer?.start()
                    handler.postDelayed(this, 3000) // Ajuste o intervalo de repetição
                }
            }
        }
        handler.postDelayed(runnable, 3000)
    }

    fun stopAlert() {
        if (isAlertActive) {
            isAlertActive = false
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            adapter.notifyDataSetChanged()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPrefs = getSharedPreferences("MeuAppPrefs", MODE_PRIVATE)
        return sharedPrefs.getBoolean("isLoggedIn", false)
    }
}
