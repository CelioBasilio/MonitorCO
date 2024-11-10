package com.example.monitorco.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

object Utils {

    // Verifica se há conexão de rede disponível
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    // Inicializa o Firebase (para garantir que ele esteja pronto antes do uso)
    fun initializeFirebase(context: Context) {
        try {
            FirebaseApp.initializeApp(context)
            FirebaseFirestore.getInstance() // Para garantir que o Firestore esteja pronto
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Validação de CNPJ
    fun isCNPJValido(cnpj: String): Boolean {
        val cnpjLimpo = cnpj.replace("[^0-9]".toRegex(), "")
        if (cnpjLimpo.length != 14) return false
        if (cnpjLimpo.all { it == cnpjLimpo[0] }) return false

        val digitos = cnpjLimpo.substring(0, 12).map { it.toString().toInt() }
        val primeiroDV = calcularDigitoVerificador(digitos, 5)
        val segundoDV = calcularDigitoVerificador(digitos + primeiroDV, 6)

        return cnpjLimpo[12].toString() == primeiroDV.toString() && cnpjLimpo[13].toString() == segundoDV.toString()
    }

    private fun calcularDigitoVerificador(digitos: List<Int>, pesoInicial: Int): Int {
        val soma = digitos.mapIndexed { index, d -> d * (pesoInicial - index) }.sum()
        val resto = soma % 11
        return if (resto < 2) 0 else 11 - resto
    }
}
