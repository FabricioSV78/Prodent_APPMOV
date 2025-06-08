package com.example.prodent.view

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.prodent.R
import com.example.prodent.model.Cita
import com.example.prodent.viewmodel.CitaViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import android.graphics.Color
import android.view.Gravity
import com.example.prodent.databinding.ActivityHomedoctorBinding


class HomeDoctorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomedoctorBinding
    private val citaViewModel: CitaViewModel by viewModels()
    private var doctorId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomedoctorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        doctorId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        binding.bottomNavigationView.selectedItemId = R.id.nav_home

        // Observa el LiveData del ViewModel
        citaViewModel.citasPendientes.observe(this, Observer { citas ->
            binding.contenedorCitas.removeAllViews()
            if (citas.isEmpty()) {
                val mensaje = TextView(this).apply {
                    text = "No hay citas pendientes"
                    textSize = 16f
                    gravity = Gravity.CENTER
                    setTextColor(Color.GRAY)
                }
                binding.contenedorCitas.addView(mensaje)
            } else {
                citas.forEach { agregarCitaUI(it) }
            }
        })

        // Fecha actual al abrir la pantalla
        val fechaActual = obtenerFechaActual()
        citaViewModel.cargarCitasPorFecha(doctorId, fechaActual)

        // Selección de fecha
        binding.calendarIcon.setOnClickListener {
            mostrarDatePicker()
        }

        // Navegación inferior
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeDoctorActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_calendar -> {
                    val intent = Intent(this, HorarioActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_configuracion -> {
                    startActivity(Intent(this, ConfiguracionActivity::class.java))
                    true
                }
                else -> false


            }
        }

    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, y, m, d ->
                val fecha = "$d/${m + 1}/$y"
                citaViewModel.cargarCitasPorFecha(doctorId, fecha)
            },
            anio, mes, dia
        )
        datePicker.show()
    }

    private fun obtenerFechaActual(): String {
        val calendario = Calendar.getInstance()
        val dia = calendario.get(Calendar.DAY_OF_MONTH)
        val mes = calendario.get(Calendar.MONTH) + 1
        val anio = calendario.get(Calendar.YEAR)
        return "$dia/$mes/$anio"
    }

    private fun agregarCitaUI(cita: Cita) {
        val tarjeta = layoutInflater.inflate(R.layout.item_cita, binding.contenedorCitas, false)
        tarjeta.findViewById<TextView>(R.id.tvFechaHora).text =
            "Tu próxima cita es el ${cita.fecha} a las ${cita.hora}"
        tarjeta.findViewById<TextView>(R.id.tvPaciente).text = cita.paciente
        binding.contenedorCitas.addView(tarjeta)
    }
}
