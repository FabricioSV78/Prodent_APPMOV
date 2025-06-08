package com.example.prodent.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.prodent.model.Cita
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CitasPacienteViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _citasPendientes = MutableLiveData<List<Cita>>()
    val citasPendientes: LiveData<List<Cita>> = _citasPendientes

    private val _citasPasadas = MutableLiveData<List<Cita>>()
    val citasPasadas: LiveData<List<Cita>> = _citasPasadas

    fun cargarCitas() {
        val pacienteId = auth.currentUser?.uid ?: return
        db.collection("citas")
            .whereEqualTo("paciente", pacienteId)
            .get()
            .addOnSuccessListener { result ->
                val hoy = Calendar.getInstance()
                val formato = SimpleDateFormat("d/M/yyyy", Locale.getDefault())

                val pendientes = mutableListOf<Cita>()
                val pasadas = mutableListOf<Cita>()

                for (doc in result) {
                    val cita = doc.toObject(Cita::class.java)
                    val fechaCita = formato.parse(cita.fecha)
                    if (fechaCita != null && fechaCita.before(hoy.time)) {
                        pasadas.add(cita)
                    } else {
                        pendientes.add(cita)
                    }
                }

                _citasPendientes.value = pendientes
                _citasPasadas.value = pasadas
            }
    }

}