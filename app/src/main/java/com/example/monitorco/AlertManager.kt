package com.example.monitorco


import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast

class AlertManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun startAlert() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, R.raw.alert_sound)?.apply {
                isLooping = true
                start()
            }
            Toast.makeText(context, "Alerta de CO ativado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAlert() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        Toast.makeText(context, "Alerta de CO desativado", Toast.LENGTH_SHORT).show()
    }
}
