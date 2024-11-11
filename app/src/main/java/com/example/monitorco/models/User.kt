package com.example.monitorco.models

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val id: String = "",             // ID único do usuário no Firestore
    val nome: String,                            // Nome da empresa
    val endereco: String,                        // Endereço da empresa
    val email: String,                           // Email da empresa
    val cnpj: String                             // CNPJ da empresa
)
