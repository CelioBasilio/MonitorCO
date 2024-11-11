package com.example.monitorco.models

import android.graphics.drawable.Drawable

data class Hospedagem(
    val label: String,
    var value: Float,
    val icon: Drawable,
    var isAlertActive: Boolean = false) // Novo campo para controlar o alerta individualmente
