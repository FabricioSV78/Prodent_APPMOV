package com.example.prodent.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.prodent.R
import com.example.prodent.databinding.ActivityCitaspacienteBinding
import com.example.prodent.model.Cita
import com.example.prodent.viewmodel.CitasPacienteViewModel
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore

class CitasPacienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCitaspacienteBinding
    private val viewModel: CitasPacienteViewModel by viewModels()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCitaspacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabs()
        setupReservaCitaButton()
        observeViewModel()
        viewModel.cargarCitas() // cargar desde Firebase

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomePacienteActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_calendar -> {
                    startActivity(Intent(this, CitasPacienteActivity::class.java))
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

    private fun setupTabs() {
        showCitasPendientes()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showCitasPendientes()
                    1 -> showCitasPasadas()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showCitasPendientes() {
        viewModel.citasPendientes.value?.let { renderCitasPendientes(it) }
    }

    private fun showCitasPasadas() {
        viewModel.citasPasadas.value?.let { renderCitasPasadas(it) }
    }

    private fun observeViewModel() {
        viewModel.citasPendientes.observe(this) {
            if (binding.tabLayout.selectedTabPosition == 0) {
                renderCitasPendientes(it)
            }
        }
        viewModel.citasPasadas.observe(this) {
            if (binding.tabLayout.selectedTabPosition == 1) {
                renderCitasPasadas(it)
            }
        }
    }

    private fun renderCitasPendientes(citas: List<Cita>) {
        binding.containerCitas.removeAllViews()

        if (citas.isEmpty()) {
            agregarMensaje("No tienes citas pendientes")
            return
        }

        for (cita in citas) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_cita_paciente_pendiente, binding.containerCitas, false)
            view.findViewById<TextView>(R.id.tvFechaCita).text = cita.fecha
            view.findViewById<TextView>(R.id.tvHoraCita).text = cita.hora

            firestore.collection("usuarios").document(cita.doctorId)
                .get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre") ?: ""
                    val nombreCompleto = "Dr. $nombre"
                    view.findViewById<TextView>(R.id.tvDoctorCita).text = nombreCompleto
                }
                .addOnFailureListener {
                    view.findViewById<TextView>(R.id.tvDoctorCita).text = "Doctor"
                }


            view.findViewById<TextView>(R.id.btnCancelar).setOnClickListener {
                Toast.makeText(this, "Cancelar no implementado", Toast.LENGTH_SHORT).show()
            }


            binding.containerCitas.addView(view)
        }
    }

    private fun renderCitasPasadas(citas: List<Cita>) {
        binding.containerCitas.removeAllViews()

        if (citas.isEmpty()) {
            agregarMensaje("No tienes citas pasadas")
            return
        }

        for (cita in citas) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_cita_paciente_pasada, binding.containerCitas, false)
            view.findViewById<TextView>(R.id.tvFechaCita).text = cita.fecha
            view.findViewById<TextView>(R.id.tvHoraCita).text = cita.hora

            firestore.collection("usuarios").document(cita.doctorId)
                .get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre") ?: ""
                    val nombreCompleto = "Dr. $nombre"
                    view.findViewById<TextView>(R.id.tvDoctorCita).text = nombreCompleto
                }
                .addOnFailureListener {
                    view.findViewById<TextView>(R.id.tvDoctorCita).text = "Doctor"
                }

            view.findViewById<TextView>(R.id.btnCalificar).setOnClickListener {
                Toast.makeText(this, "Calificación no implementada", Toast.LENGTH_SHORT).show()
            }

            binding.containerCitas.addView(view)
        }
    }

    private fun agregarMensaje(mensaje: String) {
        val textView = TextView(this).apply {
            text = mensaje
            textSize = 16f
            setPadding(16, 32, 16, 0)
        }
        binding.containerCitas.addView(textView)
    }

    private fun setupReservaCitaButton() {
        binding.btnReservarCita.setOnClickListener {
            startActivity(Intent(this, RegistrarCitaActivity::class.java))
        }
    }
}