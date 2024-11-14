package com.example.monitorco.models

import android.graphics.drawable.Drawable

// Classe de modelo que representa uma hospedagem.
data class Hospedagem(
    val label: String, // Nome ou título da hospedagem.
    var value: Float, // Valor associado à hospedagem (exemplo: quantidade de CO ou algum outro valor).
    val icon: Drawable, // Ícone que representa visualmente a hospedagem.
    var isAlertActive: Boolean = false // Novo campo que indica se o alerta está ativado para esta hospedagem.
)
