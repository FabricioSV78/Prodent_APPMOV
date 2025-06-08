package com.example.prodent.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

import com.example.prodent.R
import com.example.prodent.databinding.ActivityHomepacienteBinding
import com.example.prodent.databinding.ActivityRegistrarCitaBinding
import com.example.prodent.model.Cita
import com.example.prodent.model.Doctor
import com.example.prodent.viewmodel.CitaViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class RegistrarCitaActivity : AppCompatActivity() {

    private lateinit var spinnerDoctor: Spinner
    private lateinit var calendarView: CalendarView
    private lateinit var btnReservar: Button
    private lateinit var layoutChips: LinearLayout

    private lateinit var selectedDoctor: String
    private lateinit var selectedDate: String
    private lateinit var selectedTime: String

    private val citaViewModel: CitaViewModel by viewModels()
    private var listaDoctores: List<Doctor> = listOf()
    private lateinit var binding: ActivityRegistrarCitaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarCitaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNavigationView.selectedItemId = R.id.nav_calendar

        spinnerDoctor = findViewById(R.id.spinnerDoctor)
        calendarView = findViewById(R.id.calendarView)
        btnReservar = findViewById(R.id.btnReservar)
        layoutChips = findViewById(R.id.layoutChips)

        // Cargar doctores desde Firestore
        citaViewModel.cargarDoctores()

        // Observar los doctores
        citaViewModel.doctores.observe(this) { doctores ->
            listaDoctores = doctores
            val nombres = doctores.map { "${it.nombre} ${it.apellido}" }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDoctor.adapter = adapter
        }

        // Navegación inferior
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomePacienteActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_calendar -> {
                    val intent = Intent(this, RegistrarCitaActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }


        // Obtener fecha seleccionada desde el calendario
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"

            Log.d("DEBUG_FECHA", "Fecha seleccionada: $selectedDate")
            Log.d("DEBUG_FECHA", "Doctor seleccionado (UID): $selectedDoctor")

            if (::selectedDoctor.isInitialized && selectedDoctor.isNotBlank()) {
                citaViewModel.cargarHorarios(selectedDoctor, selectedDate)
            }
        }



        // Selección del doctor desde el spinner
        spinnerDoctor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val doctor = listaDoctores[position]
                selectedDoctor = doctor.id
                if (::selectedDate.isInitialized) {
                    citaViewModel.cargarHorarios(selectedDoctor, selectedDate)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }



        // Observar los horarios disponibles
        citaViewModel.horarios.observe(this) { horarios ->
            layoutChips.removeAllViews()

            if (horarios.isEmpty()) {
                Toast.makeText(this, "No hay horarios disponibles para esta fecha", Toast.LENGTH_SHORT).show()
            }

            horarios.forEach { horario ->
                val chip = layoutInflater.inflate(R.layout.chip_horario, layoutChips, false)
                val text = "${horario.horaInicio} - ${horario.horaFin}"
                chip.findViewById<TextView>(R.id.textHorario).text = text

                chip.setOnClickListener {
                    selectedTime = text
                    Toast.makeText(this, "Horario seleccionado: $selectedTime", Toast.LENGTH_SHORT).show()
                }

                layoutChips.addView(chip)
            }
        }

        // Acción de reservar cita
        btnReservar.setOnClickListener {
            if (::selectedDoctor.isInitialized && ::selectedDate.isInitialized && ::selectedTime.isInitialized) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                val db = FirebaseFirestore.getInstance()

                // Obtener nombre del paciente
                db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val nombre = doc.getString("nombre") ?: ""
                        val apellido = doc.getString("apellido") ?: ""
                        val nombrePaciente = "$nombre $apellido"

                        // Crear cita
                        val cita = Cita(
                            paciente = uid,
                            fecha = selectedDate,
                            hora = selectedTime,
                            estado = "pendiente",
                            doctorId = selectedDoctor
                        )

                        // Guardar cita
                        citaViewModel.guardarCita(cita)
                        Toast.makeText(this, "Cita reservada para el $selectedDate a las $selectedTime", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "No se pudo obtener el nombre del paciente", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Selecciona una cita", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
