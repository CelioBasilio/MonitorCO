package com.example.monitorco.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build


object Utils {

    // Verifica se há conexão com a internet disponível.
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

    // Valida se o CNPJ informado é válido.
    fun isValidCNPJ(cnpj: String): Boolean {
        val cleanCNPJ = cnpj.replace("[^0-9]".toRegex(), "") // Remove caracteres não numéricos
        if (cleanCNPJ.length != 14) return false // Verifica se o CNPJ tem 14 dígitos
        if (cleanCNPJ.all { it == cleanCNPJ[0] }) return false // Verifica se o CNPJ é uma sequência de números iguais

        // Validação do primeiro dígito verificador
        val firstMultiplier = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
        val firstDigit = calculateDigit(cleanCNPJ.substring(0, 12), firstMultiplier)

        // Validação do segundo dígito verificador
        val secondMultiplier = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
        val secondDigit = calculateDigit(cleanCNPJ.substring(0, 13), secondMultiplier)

        return cleanCNPJ[12] == firstDigit && cleanCNPJ[13] == secondDigit // Compara os dígitos verificadores
    }

    // Função para calcular o dígito verificador
    fun calculateDigit(cnpjBase: String, multipliers: IntArray): Char {
        val sum = cnpjBase.mapIndexed { index, char -> char.toString().toInt() * multipliers[index] }.sum() // Calcula a soma ponderada
        val remainder = sum % 11 // Calcula o resto da divisão por 11
        return if (remainder < 2) {
            '0' // Se o resto for menor que 2, o dígito é 0
        } else {
            (11 - remainder).toString()[0] // Caso contrário, retorna o dígito calculado
        }
    }

}
