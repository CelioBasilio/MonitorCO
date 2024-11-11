package com.example.monitorco.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.monitorco.R
import com.example.monitorco.models.Card

class CardAdapter : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    private val cardList = mutableListOf<Card>()

    fun setCards(cards: List<Card>) {
        cardList.clear()
        cardList.addAll(cards)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cardList[position]
        holder.bind(card)
    }

    override fun getItemCount() = cardList.size

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val labelManometro: TextView = itemView.findViewById(R.id.labelManometro)
        private val janelaIcon: ImageView = itemView.findViewById(R.id.janelaIcon)
        private val alertButton: View = itemView.findViewById(R.id.alertButton)

        fun bind(card: Card) {
            // Exibe o nível de CO no manômetro
            labelManometro.text = "Nível de CO: ${card.sensorCO} ppm"

            // Muda o ícone de janela dependendo se está aberta ou fechada
            janelaIcon.setImageResource(
                if (card.janela) R.drawable.ic_window_open else R.drawable.ic_window_closed
            )

            // Lógica para o botão de alerta
            alertButton.visibility = if (card.sensorCO > 100) View.VISIBLE else View.GONE
            alertButton.setOnClickListener {
                // Aqui você pode adicionar a lógica para parar o alerta, caso necessário
                Toast.makeText(itemView.context, "Alerta! Nível de CO alto", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
