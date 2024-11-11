package com.example.monitorco.managers

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class FirestoreManager {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val TAG = "FirestoreManager"

    // Função para salvar dados no Firestore
    fun saveData(
        collection: String,
        documentId: String,
        data: Map<String, Any>,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection(collection).document(documentId)
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Dados salvos com sucesso na coleção '$collection'.")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao salvar dados na coleção '$collection': ${e.message}")
                onComplete(false)
            }
    }

    // Função para recuperar dados de um documento específico
    fun getData(
        collection: String,
        documentId: String,
        onComplete: (DocumentSnapshot?) -> Unit
    ) {
        db.collection(collection).document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "Documento '$documentId' encontrado na coleção '$collection'.")
                    onComplete(document)
                } else {
                    Log.d(TAG, "Documento '$documentId' não encontrado.")
                    onComplete(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao recuperar dados do documento '$documentId': ${e.message}")
                onComplete(null)
            }
    }

    // Função para recuperar o nome da empresa associado ao usuário
    fun getEmpresaNome(
        userId: String,
        onComplete: (String?) -> Unit
    ) {
        db.collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val empresaNome = document.getString("empresaNome")
                    if (empresaNome != null) {
                        Log.d(TAG, "Nome da empresa '$empresaNome' recuperado para o usuário '$userId'.")
                        onComplete(empresaNome)
                    } else {
                        Log.d(TAG, "Campo 'empresaNome' não encontrado para o usuário '$userId'.")
                        onComplete(null)
                    }
                } else {
                    Log.d(TAG, "Documento do usuário '$userId' não encontrado.")
                    onComplete(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao recuperar nome da empresa para o usuário '$userId': ${e.message}")
                onComplete(null)
            }
    }

    // Função para recuperar todos os documentos de uma coleção
    fun getCollectionData(
        collection: String,
        onComplete: (List<DocumentSnapshot>) -> Unit
    ) {
        db.collection(collection)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d(TAG, "Dados da coleção '$collection' recuperados com sucesso.")
                onComplete(querySnapshot.documents)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao recuperar dados da coleção '$collection': ${e.message}")
                onComplete(emptyList())
            }
    }

    // Função para realizar consultas personalizadas
    fun queryData(
        collection: String,
        field: String,
        value: Any,
        onComplete: (List<DocumentSnapshot>) -> Unit // Retorna uma lista de DocumentSnapshot
    ) {
        db.collection(collection)
            .whereEqualTo(field, value)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d(TAG, "Consulta na coleção '$collection' com campo '$field' = '$value' realizada com sucesso.")

                // Retorna a lista de documentos ou uma lista vazia se não houver documentos
                onComplete(querySnapshot?.documents ?: emptyList()) // Se for nulo, retorna lista vazia
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao realizar consulta: ${e.message}")
                onComplete(emptyList()) // Passa uma lista vazia em caso de erro
            }
    }

    // Função para deletar um documento
    fun deleteDocument(
        collection: String,
        documentId: String,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection(collection).document(documentId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Documento '$documentId' deletado da coleção '$collection'.")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao deletar documento '$documentId': ${e.message}")
                onComplete(false)
            }
    }

    // Função para atualizar campos específicos de um documento
    fun updateFields(
        collection: String,
        documentId: String,
        updates: Map<String, Any>,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection(collection).document(documentId)
            .update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "Campos atualizados no documento '$documentId' da coleção '$collection'.")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao atualizar campos no documento '$documentId': ${e.message}")
                onComplete(false)
            }
    }
}
