package com.example.monitorco

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.monitorco.R
import com.example.monitorco.models.Hospedagem

class HospedagemAdapter(
    private val context: Context,
    var hospedagens: MutableList<Hospedagem>
) : RecyclerView.Adapter<HospedagemAdapter.ViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()
    private var listeners: MutableList<ListenerRegistration> = mutableListOf()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.labelManometro)
        val manometer: ManometerView = view.findViewById(R.id.manometer_view)
        val janelaIcon: ImageView = view.findViewById(R.id.janelaIcon)
        val alertButton: Button = view.findViewById(R.id.alertButton)

        init {
            alertButton.setOnClickListener {
                (context as MainActivity).stopAlert()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_hospedagem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hospedagem = hospedagens[position]
        holder.label.text = hospedagem.label
        holder.manometer.updateValue(hospedagem.value)

        // Atualizar o ícone e a animação da janela
        if (hospedagem.value > 5) {
            holder.janelaIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_window_open))
            startColorAnimation(holder.itemView)
        } else {
            holder.janelaIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_window_closed))
            stopColorAnimation(holder.itemView)
        }

        // Controlar a visibilidade do botão de alerta com base no estado do alerta individual
        holder.alertButton.visibility = if (hospedagem.isAlertActive) View.VISIBLE else View.GONE

        holder.alertButton.setOnClickListener {
            // Desativa o alerta tanto no nível da hospedagem quanto global
            (context as MainActivity).stopAlert(position)
        }
    }




    private fun startColorAnimation(view: View) {
        val existingAnimator = view.tag as? ObjectAnimator
        if (existingAnimator != null && existingAnimator.isRunning) return

        val colorFrom = Color.argb(255, 255, 0, 0) // Vermelho
        val colorTo = Color.argb(0, 255, 0, 0) // Transparente

        val animator = ObjectAnimator.ofArgb(view, "backgroundColor", colorFrom, colorTo).apply {
            duration = 1000
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        view.tag = animator
    }

    private fun stopColorAnimation(view: View) {
        (view.tag as? ObjectAnimator)?.cancel()
        view.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun getItemCount() = hospedagens.size

    // Função para atualizar o valor de uma hospedagem existente
    fun updateHospedagem(index: Int, newValue: Float) {
        if (index in hospedagens.indices) {
            hospedagens[index].value = newValue
            notifyItemChanged(index)
        }
    }

    // Função para escutar mudanças em tempo real no Firestore


    // Função para remover todos os listeners
    fun stopListening() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }
}
