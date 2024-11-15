package com.example.monitorco

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.monitorco.models.Hospedagem

class HospedagemAdapter(
    private val context: Context,
    var hospedagens: MutableList<Hospedagem>, // Mantemos MutableList
    private val alertManager: AlertManager
) : RecyclerView.Adapter<HospedagemAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.labelManometro)
        val manometer: ManometerView = view.findViewById(R.id.manometer_view)
        val janelaIcon: ImageView = view.findViewById(R.id.janelaIcon)
        val alertButton: Button = view.findViewById(R.id.alertButton)
        private val handler = Handler(Looper.getMainLooper())  // Para executar ações na thread principal

        init {
            alertButton.setOnClickListener {
                alertManager.stopAlert()
            }
        }

        fun startUpdatingLabel(hospedagem: Hospedagem) {
            handler.post(object : Runnable {
                override fun run() {
                    label.text = hospedagem.label
                    manometer.updateValue(hospedagem.value)

                    handler.postDelayed(this, 100)
                }
            })
        }

        fun stopUpdatingLabel() {
            handler.removeCallbacksAndMessages(null)  // Para de atualizar quando o item for reciclado
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_hospedagem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hospedagem = hospedagens[position]

        holder.startUpdatingLabel(hospedagem)

        if (hospedagem.value > 10) {
            holder.janelaIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_window_open))
            startColorAnimation(holder.itemView)
            holder.alertButton.visibility = View.VISIBLE
            alertManager.startAlert()
        } else {
            holder.janelaIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_window_closed))
            stopColorAnimation(holder.itemView)
            holder.alertButton.visibility = View.GONE
            alertManager.stopAlert()
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.stopUpdatingLabel()
        super.onViewRecycled(holder)
    }

    override fun getItemCount() = hospedagens.size

    private fun startColorAnimation(view: View) {
        val existingAnimator = view.tag as? ObjectAnimator
        if (existingAnimator != null && existingAnimator.isRunning) return

        val colorFrom = Color.argb(255, 255, 0, 0)
        val colorTo = Color.argb(0, 255, 0, 0)

        val animator = ObjectAnimator.ofArgb(view, "backgroundColor", colorFrom, colorTo).apply {
            duration = 3000
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
}
