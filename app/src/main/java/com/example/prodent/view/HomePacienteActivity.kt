package com.example.prodent.view

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.prodent.R
import com.example.prodent.databinding.ActivityHomedoctorBinding
import com.example.prodent.databinding.ActivityHomepacienteBinding
import com.example.prodent.model.Cita
import com.example.prodent.viewmodel.CitaViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.getValue

class HomePacienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomepacienteBinding
    private val viewModel: CitaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomepacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.selectedItemId = R.id.nav_home

        binding.mapaLayout.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            startActivity(intent)
            true
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

        val pacienteId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModel.obtenerCitasPaciente(pacienteId)

        viewModel.citasPendientesPacientes.observe(this) { citas ->
            binding.contenedorCitasPaciente.removeAllViews()
            if (citas.isEmpty()) {
                val mensaje = TextView(this).apply {
                    text = "No tienes citas"
                    textSize = 16f
                    gravity = Gravity.CENTER
                    setTextColor(Color.GRAY)
                }
                binding.contenedorCitasPaciente.addView(mensaje)
            } else {
                citas.forEach { cita ->
                    val tarjeta = layoutInflater.inflate(R.layout.item_cita_paciente, binding.contenedorCitasPaciente, false)
                    tarjeta.findViewById<TextView>(R.id.tvInfoCita).text = "Tu próxima cita es el ${cita.fecha} a las ${cita.hora}"
                    FirebaseFirestore.getInstance()
                        .collection("doctores")
                        .document(cita.doctorId)
                        .get()
                        .addOnSuccessListener { doc ->
                            val nombreDoctor = doc.getString("nombre") ?: "Doctor"
                            tarjeta.findViewById<TextView>(R.id.tvDoctorCita).text = nombreDoctor
                        }
                        .addOnFailureListener {
                            tarjeta.findViewById<TextView>(R.id.tvDoctorCita).text = "Nombre no disponible"
                        }

                    // 3. Agrega la tarjeta al contenedor
                    binding.contenedorCitasPaciente.addView(tarjeta)
                } }
            }


        }
    }

