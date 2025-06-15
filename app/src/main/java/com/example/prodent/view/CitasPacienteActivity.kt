package com.example.prodent.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.prodent.R
import com.example.prodent.databinding.ActivityCitaspacienteBinding
import com.example.prodent.model.CalificacionDoctor
import com.example.prodent.model.Cita
import com.example.prodent.model.NotificacionCita
import com.example.prodent.viewmodel.CitasPacienteViewModel
import com.example.prodent.viewmodel.NotificacionesViewModel
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CitasPacienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCitaspacienteBinding
    private val viewModel: CitasPacienteViewModel by viewModels()
    private val notificacionesViewModel: NotificacionesViewModel by viewModels()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCitaspacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNavigationView.selectedItemId = R.id.nav_calendar

        setupTabs()
        setupReservaCitaButton()
        observeViewModel()
        viewModel.cargarCitas() // cargar desde Firebase

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomePacienteActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_calendar -> {
                    true
                }

                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificacionesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_configuracion -> {
                    startActivity(Intent(this, ConfiguracionActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("notificaciones")
            .whereEqualTo("uid", uid)
            .whereEqualTo("visto", false)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val badge = binding.bottomNavigationView.getOrCreateBadge(R.id.nav_notifications)
                    badge.isVisible = true
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
        citas.forEach { cita ->
            val view = layoutInflater.inflate(R.layout.item_cita_paciente_pendiente, binding.containerCitas, false)

            val tvFechaCita = view.findViewById<TextView>(R.id.tvFechaCita)
            val tvHoraCita = view.findViewById<TextView>(R.id.tvHoraCita)
            val tvDoctorCita = view.findViewById<TextView>(R.id.tvDoctorCita)
            val btnCancelar = view.findViewById<Button>(R.id.btnCancelar)

            tvFechaCita.text = cita.fecha
            tvHoraCita.text = cita.hora

            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(cita.doctorId)
                .get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre") ?: ""
                    tvDoctorCita.text = "$nombre"
                }
                .addOnFailureListener {
                    tvDoctorCita.text = "Nombre no disponible"
                }

            btnCancelar.setOnClickListener {
                FirebaseFirestore.getInstance().collection("citas")
                    .whereEqualTo("paciente", cita.paciente)
                    .whereEqualTo("fecha", cita.fecha)
                    .whereEqualTo("hora", cita.hora)
                    .whereEqualTo("doctorId", cita.doctorId)
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            FirebaseFirestore.getInstance().collection("citas").document(document.id).delete()
                        }

                        // Crear notificación para el paciente
                        val horaSplit = cita.hora.split(" - ")
                        val notificacion = NotificacionCita(
                            mensaje = "Tu cita del ${cita.fecha} ha sido cancelada",
                            horaInicio = horaSplit[0],
                            horaFin = horaSplit[1]
                        )
                        notificacionesViewModel.agregarNotificacion(notificacion)

                        // Crear notificación para el doctor
                        val notificacionDoctor = mapOf(
                            "uid" to cita.doctorId,
                            "mensaje" to "Una cita del ${cita.fecha} ha sido cancelada por el paciente",
                            "horaInicio" to horaSplit[0],
                            "horaFin" to horaSplit[1],
                            "timestamp" to System.currentTimeMillis()
                        )
                        FirebaseFirestore.getInstance()
                            .collection("notificaciones")
                            .add(notificacionDoctor)

                        Toast.makeText(this, "Cita cancelada", Toast.LENGTH_SHORT).show()
                        viewModel.cargarCitas()
                    }

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

            // Aquí usamos el LiveData para observar las calificaciones
            viewModel.calificaciones.observe(this, Observer { calificaciones ->
                val calificacion = calificaciones[cita.doctorId] ?: 0.0
                if (calificacion > 0) {
                    view.findViewById<TextView>(R.id.btnCalificar).visibility = View.GONE
                    view.findViewById<LinearLayout>(R.id.llCalificacion).visibility = View.VISIBLE
                    view.findViewById<TextView>(R.id.tvCalificacion).text = "$calificacion ★"
                } else {
                    view.findViewById<TextView>(R.id.btnCalificar).visibility = View.VISIBLE
                    view.findViewById<LinearLayout>(R.id.llCalificacion).visibility = View.GONE
                }
            })

            view.findViewById<TextView>(R.id.btnCalificar).setOnClickListener {
                showCalificarModal(view, cita.doctorId)
            }

            binding.containerCitas.addView(view)
        }
    }


    private fun showCalificarModal(view: View, doctorId: String) {
        // Crear el AlertDialog para calificar al doctor
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Calificar al Doctor")

        // Inflar el layout del modal
        val modalView = layoutInflater.inflate(R.layout.modal_calificar, null)
        builder.setView(modalView)

        val ratingBar = modalView.findViewById<RatingBar>(R.id.ratingBar)
        val comentarioEditText = modalView.findViewById<EditText>(R.id.etComentario)
        val btnEnviarCalificacion = modalView.findViewById<Button>(R.id.btnEnviarCalificacion)

        // Mostrar el AlertDialog
        val dialog = builder.create()
        dialog.show()

        // Acción al presionar el botón de enviar calificación en el modal
        btnEnviarCalificacion.setOnClickListener {
            val calificacion = ratingBar.rating.toDouble()
            val comentario = comentarioEditText.text.toString()
            val pacienteId = FirebaseAuth.getInstance().currentUser?.uid
            val fechaActual = SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(Date())

            if (doctorId.isNotEmpty() && pacienteId != null) {
                val calificacionDoctor = CalificacionDoctor(
                    doctorId = doctorId,
                    pacienteId = pacienteId,
                    calificacion = calificacion,
                    comentario = comentario,
                    fecha = fechaActual
                )

                firestore.collection("calificaciones_doctor")
                    .add(calificacionDoctor)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Calificación guardada con éxito", Toast.LENGTH_SHORT).show()
                        // Reemplazar el botón de calificación por la calificación y mostrar las estrellas
                        view.findViewById<TextView>(R.id.btnCalificar).visibility = View.GONE
                        view.findViewById<LinearLayout>(R.id.llCalificacion).visibility = View.VISIBLE

                        view.findViewById<TextView>(R.id.tvCalificacion).text = "$calificacion ★"
                        dialog.dismiss()  // Cerrar el modal después de guardar
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar la calificación", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Error al obtener los datos del doctor", Toast.LENGTH_SHORT).show()
            }
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