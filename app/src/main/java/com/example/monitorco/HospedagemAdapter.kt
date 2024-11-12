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
    private val context: Context,  // Contexto da atividade que está usando este adapter
    var hospedagens: MutableList<Hospedagem> // Lista de hospedagens que será exibida no RecyclerView
) : RecyclerView.Adapter<HospedagemAdapter.ViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance() // Instância do Firestore
    private var listeners: MutableList<ListenerRegistration> = mutableListOf() // Lista de listeners para escutar dados em tempo real

    // ViewHolder que representa cada item na lista de hospedagens
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.labelManometro) // Rótulo de cada item
        val manometer: ManometerView = view.findViewById(R.id.manometer_view) // Exibe o valor de CO em formato gráfico
        val janelaIcon: ImageView = view.findViewById(R.id.janelaIcon) // Ícone da janela (aberta ou fechada)
        val alertButton: Button = view.findViewById(R.id.alertButton) // Botão de alerta para desativar o alerta

        init {
            // Ao clicar no botão de alerta, o alerta é desativado
            alertButton.setOnClickListener {
                (context as MainActivity).stopAlert()
            }
        }
    }

    // Cria a ViewHolder para cada item da lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_hospedagem, parent, false)
        return ViewHolder(view)
    }

    // Configura os dados para cada item na lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hospedagem = hospedagens[position] // Obtemos a hospedagem para esse índice

        // Define o rótulo e o valor do manômetro (representando o nível de CO)
        holder.label.text = hospedagem.label
        holder.manometer.updateValue(hospedagem.value)

        // Atualiza o ícone da janela com base no valor do CO
        if (hospedagem.value > 5) {
            holder.janelaIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_window_open))
            startColorAnimation(holder.itemView) // Inicia animação de alerta se CO estiver alto
        } else {
            holder.janelaIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_window_closed))
            stopColorAnimation(holder.itemView) // Para animação se CO estiver seguro
        }

        // Controla a visibilidade do botão de alerta com base no estado do alerta individual
        holder.alertButton.visibility = if (hospedagem.isAlertActive) View.VISIBLE else View.GONE

        // Clica no botão de alerta para desativar o alerta
        holder.alertButton.setOnClickListener {
            (context as MainActivity).stopAlert(position) // Desativa o alerta para esse item
        }
    }

    // Inicia a animação de cor (alerta visual) caso o nível de CO seja alto
    private fun startColorAnimation(view: View) {
        val existingAnimator = view.tag as? ObjectAnimator
        if (existingAnimator != null && existingAnimator.isRunning) return

        val colorFrom = Color.argb(255, 255, 0, 0) // Cor vermelha
        val colorTo = Color.argb(0, 255, 0, 0) // Cor transparente

        // Cria e inicia a animação de cor
        val animator = ObjectAnimator.ofArgb(view, "backgroundColor", colorFrom, colorTo).apply {
            duration = 1000 // Duração da animação (1 segundo)
            repeatMode = ObjectAnimator.REVERSE // Faz a animação reverter
            repeatCount = ObjectAnimator.INFINITE // Repete a animação infinitamente
            interpolator = AccelerateDecelerateInterpolator() // Interpolação suave
            start()
        }
        view.tag = animator // Armazena o animador na tag do view para controle futuro
    }

    // Para a animação de cor e reseta o fundo para transparente
    private fun stopColorAnimation(view: View) {
        (view.tag as? ObjectAnimator)?.cancel() // Cancela a animação existente
        view.setBackgroundColor(Color.TRANSPARENT) // Reseta a cor de fundo para transparente
    }

    override fun getItemCount() = hospedagens.size // Retorna o número de itens na lista

    // Função para atualizar o valor de uma hospedagem existente
    fun updateHospedagem(index: Int, newValue: Float) {
        if (index in hospedagens.indices) {
            hospedagens[index].value = newValue // Atualiza o valor do CO
            notifyItemChanged(index) // Notifica o RecyclerView sobre a mudança
        }
    }

    // Função para remover todos os listeners do Firestore
    fun stopListening() {
        listeners.forEach { it.remove() } // Remove todos os listeners
        listeners.clear() // Limpa a lista de listeners
    }
}
