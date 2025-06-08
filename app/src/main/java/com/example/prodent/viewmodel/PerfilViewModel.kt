package com.example.prodent.viewmodel

import androidx.lifecycle.ViewModel
import com.example.prodent.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var rolUsuario: String = ""

    fun obtenerDatosUsuario(callback: (Usuario) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val rol = doc.getString("rol") ?: ""
                rolUsuario = rol

                val usuarioBase = Usuario(
                    id = uid,
                    nombre = doc.getString("nombre") ?: "",
                    apellido = doc.getString("apellido") ?: "",
                    correo = doc.getString("correo") ?: "",
                    rol = rol
                )

                if (rol == "Paciente") {
                    db.collection("pacientes")
                        .whereEqualTo("usuarioId", uid)
                        .get()
                        .addOnSuccessListener { result ->
                            val telefono = result.documents.firstOrNull()?.getString("telefono") ?: ""
                            val usuarioFinal = usuarioBase.copy(telefono = telefono)
                            callback(usuarioFinal)
                        }
                } else {
                    callback(usuarioBase)
                }
            }
    }

    fun actualizarDatosPerfil(
        nombre: String,
        correo: String,
        telefono: String,
        nuevaContrasena: String,
        callback: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return

        val datosActualizados = mutableMapOf<String, Any>(
            "nombre" to nombre,
            "correo" to correo
        )

        db.collection("usuarios").document(uid).update(datosActualizados)
            .addOnSuccessListener {
                // Actualizar teléfono solo si es paciente
                if (rolUsuario == "Paciente") {
                    db.collection("pacientes")
                        .whereEqualTo("usuarioId", uid)
                        .get()
                        .addOnSuccessListener { result ->
                            val docId = result.documents.firstOrNull()?.id
                            if (docId != null) {
                                db.collection("pacientes").document(docId)
                                    .update("telefono", telefono)
                            }
                        }
                }

                val user = auth.currentUser
                user?.updateEmail(correo)
                if (nuevaContrasena.isNotBlank()) {
                    user?.updatePassword(nuevaContrasena)
                }

                callback("Perfil actualizado correctamente")
            }
            .addOnFailureListener {
                callback("Error al actualizar perfil: ${it.message}")
            }
    }
}
