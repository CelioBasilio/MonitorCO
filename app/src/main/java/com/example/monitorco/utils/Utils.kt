package com.example.monitorco.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.example.monitorco.api.RetrofitInstance
import com.example.monitorco.model.Login
import retrofit2.HttpException
import java.io.IOException

object Utils {

    /**
     * Verifica se há conexão com a internet disponível.
     */
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

    /**
     * Valida se o CNPJ informado é válido.
     */
    fun isValidCNPJ(cnpj: String): Boolean {
        // Remove caracteres não numéricos
        val cleanCNPJ = cnpj.replace("[^0-9]".toRegex(), "")

        // Verifica se o CNPJ tem 14 dígitos
        if (cleanCNPJ.length != 14) return false

        // Verifica se o CNPJ é uma sequência de números iguais (exemplo: 11111111111111)
        if (cleanCNPJ.all { it == cleanCNPJ[0] }) return false

        // Validação do primeiro dígito verificador
        val firstMultiplier = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
        val firstDigit = calculateDigit(cleanCNPJ.substring(0, 12), firstMultiplier)

        // Validação do segundo dígito verificador
        val secondMultiplier = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
        val secondDigit = calculateDigit(cleanCNPJ.substring(0, 13), secondMultiplier)

        // Compara os dígitos verificadores calculados com os do CNPJ
        return cleanCNPJ[12] == firstDigit && cleanCNPJ[13] == secondDigit
    }

    // Função para calcular o dígito verificador
    fun calculateDigit(cnpjBase: String, multipliers: IntArray): Char {
        val sum = cnpjBase.mapIndexed { index, char ->
            char.toString().toInt() * multipliers[index]
        }.sum()

        val remainder = sum % 11
        return if (remainder < 2) {
            '0'
        } else {
            (11 - remainder).toString()[0]
        }
    }


    /**
     * Verifica se o servidor está disponível para autenticar o usuário.
     * @param email Email do usuário.
     * @param senha Senha do usuário.
     * @return true se o login for bem-sucedido, false caso contrário.
     */
    suspend fun isServerAvailable(email: String, senha: String): Boolean {
        val login = Login(email, senha)
        return try {
            val response = RetrofitInstance.api.login(login)
            response.isSuccessful
        } catch (e: HttpException) {
            // Erro relacionado ao servidor (ex: código 500, 404)
            false
        } catch (e: IOException) {
            // Erro de rede (ex: falta de conexão)
            false
        } catch (e: Exception) {
            // Outros erros inesperados
            false
        }
    }

    // Outras funções de validação podem ser adicionadas aqui
}
