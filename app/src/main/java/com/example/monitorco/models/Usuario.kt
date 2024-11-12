package com.example.monitorco.models

// Classe de modelo que representa um usuário do sistema.
data class Usuario(
    val nome: String,    // Nome do usuário.
    val endereco: String, // Endereço do usuário.
    val email: String,    // Endereço de email do usuário.
    val senha: String,    // Senha do usuário.
    val cnpj: String      // CNPJ do usuário, caso seja uma pessoa jurídica.
)
