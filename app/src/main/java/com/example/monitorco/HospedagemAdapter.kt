package com.example.monitorco

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.animation.ObjectAnimator
import android.graphics.Color
import android.view.animation.AccelerateDecelerateInterpolator

class HospedagemAdapter(
        private val context: Context,
        private var hospedagens: MutableList<Hospedagem>
) : RecyclerView.Adapter<HospedagemAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val label: TextView = view.findViewById(R.id.labelManometro)
                val manometer: ManometerView = view.findViewById(R.id.manometer_view)
                val janelaIcon: ImageView = view.findViewById(R.id.janelaIcon)
                val alertButton: Button = view.findViewById(R.id.alertButton)

                init {
                        alertButton.setOnClickListener {
                                // Chama o método stopAlert na MainActivity ao pressionar o botão
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
                holder.janelaIcon.setImageDrawable(hospedagem.icon)

                // Verifica se o valor é maior ou igual a 20 para animar a cor de fundo
                if (hospedagem.value >= 5) {
                        startColorAnimation(holder.itemView)
                } else {
                        stopColorAnimation(holder.itemView)
                }

                // Controlar a visibilidade do botão com base no estado do alerta
                holder.alertButton.visibility =
                        if ((context as MainActivity).isAlertActive) View.VISIBLE else View.GONE
        }

        private fun startColorAnimation(view: View) {
                val colorFrom = Color.argb(255, 255, 0, 0) // Vermelho
                val colorTo = Color.argb(0, 255, 0, 0) // Transparente

                val animator = ObjectAnimator.ofArgb(view, "backgroundColor", colorFrom, colorTo)
                animator.duration = 1000 // Duração da animação
                animator.repeatMode = ObjectAnimator.REVERSE // Reverte a animação
                animator.repeatCount = ObjectAnimator.INFINITE // Repetir infinitamente
                animator.interpolator = AccelerateDecelerateInterpolator()
                animator.start()
        }

        private fun stopColorAnimation(view: View) {
                view.setBackgroundColor(Color.TRANSPARENT) // Reseta a cor para transparente
        }

        override fun getItemCount() = hospedagens.size

        // Função para atualizar o valor de uma hospedagem existente
        fun updateHospedagem(index: Int, newValue: Float) {
                if (index in hospedagens.indices) {
                        hospedagens[index].value = newValue
                        notifyItemChanged(index) // Notifica o RecyclerView que o item foi atualizado
                }
        }
}
