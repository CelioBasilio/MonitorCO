package com.example.monitorco.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build


object Utils {

    // Verifica se há conexão com a internet disponível.
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Verifica se está rodando em uma versão igual ou superior ao Android M (API 23)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            // Verifica se há uma conexão com a internet disponível (Wi-Fi, dados móveis ou Ethernet)
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } else {
            // Para versões mais antigas (abaixo do Android M)
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo?.isConnected == true // Verifica se a rede está conectada
        }
    }

    // Valida se o CNPJ informado é válido.
    fun isValidCNPJ(cnpj: String): Boolean {
        val cleanCNPJ = cnpj.replace("[^0-9]".toRegex(), "") // Remove caracteres não numéricos
        if (cleanCNPJ.length != 14) return false // Verifica se o CNPJ tem 14 dígitos
        if (cleanCNPJ.all { it == cleanCNPJ.first() }) return false // Verifica se o CNPJ é uma sequência de números iguais

        // Validação do primeiro dígito verificador
        val firstMultiplier = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
        val firstDigit = calculateDigit(cleanCNPJ.substring(0, 12), firstMultiplier)

        // Validação do segundo dígito verificador
        val secondMultiplier = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
        val secondDigit = calculateDigit(cleanCNPJ.substring(0, 13), secondMultiplier)

        return cleanCNPJ[12] == firstDigit && cleanCNPJ[13] == secondDigit // Compara os dígitos verificadores
    }


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
