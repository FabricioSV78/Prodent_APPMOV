package com.example.prodent.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.prodent.model.Cita
import com.example.prodent.model.Doctor
import com.example.prodent.model.Horario
import com.google.firebase.firestore.FirebaseFirestore

class CitaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // LiveData que expone la lista de citas pendientes
    private val _citasPendientes = MutableLiveData<List<Cita>>()
    val citasPendientes: LiveData<List<Cita>> get() = _citasPendientes

    // LiveData para saber si la cita fue guardada con éxito
    private val _citaGuardada = MutableLiveData<Boolean>()
    val citaGuardada: LiveData<Boolean> get() = _citaGuardada

    // LiveData para los doctores
    private val _doctores = MutableLiveData<List<Doctor>>()
    val doctores: LiveData<List<Doctor>> get() = _doctores

    private val _horarios = MutableLiveData<List<Horario>>()
    val horarios: LiveData<List<Horario>> get() = _horarios


    // cargar citas por fecha
    fun cargarCitasPorFecha(doctorId: String, fecha: String) {
        db.collection("citas")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("estado", "pendiente")
            .whereEqualTo("fecha", fecha)
            .get()
            .addOnSuccessListener { result ->
                val citas = result.map { it.toObject(Cita::class.java) }
                _citasPendientes.value = citas
            }
            .addOnFailureListener {
                _citasPendientes.value = emptyList()
            }
    }

    val _citasPendientesPacientes = MutableLiveData<List<Cita>>()
    val citasPendientesPacientes: LiveData<List<Cita>> get() = _citasPendientesPacientes

    fun obtenerCitasPaciente(uid: String) {
        db.collection("citas")
            .whereEqualTo("pacienteId", uid)
            .whereEqualTo("estado", "pendiente")
            .get()
            .addOnSuccessListener { result ->
                val citas = result.map { it.toObject(Cita::class.java) }
                _citasPendientesPacientes.value = citas
            }
            .addOnFailureListener {
                _citasPendientes.value = emptyList()
            }
    }
    fun cargarDoctores() {
        db.collection("usuarios")
            .whereEqualTo("rol", "Doctor")
            .get()
            .addOnSuccessListener { result ->
                val doctoresList = result.map { document ->
                    val doctor = document.toObject(Doctor::class.java)
                    doctor.copy(id = document.id)
                }
                _doctores.value = doctoresList
            }
            .addOnFailureListener {
                _doctores.value = emptyList()
            }
    }

    // Guardar una nueva cita
    fun guardarCita(cita: Cita) {
        db.collection("citas")
            .add(cita)
            .addOnSuccessListener {
                _citaGuardada.value = true
            }
            .addOnFailureListener {
                _citaGuardada.value = false
            }
    }
    // Cargar horarios disponibles para un doctor y una fecha
    fun cargarHorarios(doctorId: String, fecha: String) {
        val horariosRef = db.collection("horarios")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("fecha", fecha)

        val citasRef = db.collection("citas")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("fecha", fecha)

        horariosRef.get().addOnSuccessListener { horariosDocs ->
            val horariosDisponibles = horariosDocs.map { it.toObject(Horario::class.java) }.toMutableList()

            citasRef.get().addOnSuccessListener { citasDocs ->
                val horasReservadas = citasDocs.mapNotNull { it.getString("hora") }

                // Filtrar horarios que no están reservados
                val filtrados = horariosDisponibles.filter { horario ->
                    val horaCompleta = "${horario.horaInicio} - ${horario.horaFin}"
                    !horasReservadas.contains(horaCompleta)
                }

                _horarios.value = filtrados
            }.addOnFailureListener {
                _horarios.value = horariosDisponibles // Si no se pudo obtener citas, mostrar todos
            }
        }.addOnFailureListener {
            _horarios.value = emptyList()
        }
    }



}