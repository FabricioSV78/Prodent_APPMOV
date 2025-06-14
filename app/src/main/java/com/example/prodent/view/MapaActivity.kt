package com.example.prodent.view

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.prodent.R
import com.example.prodent.databinding.ActivityMapaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MapaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapaBinding
    private lateinit var rolUsuario: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Subrayar texto
        binding.verhorariosTv.paintFlags = binding.verhorariosTv.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // Obtener rol del usuario
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                rolUsuario = document.getString("rol") ?: ""

                // Configurar navegación inferior después de conocer el rol
                binding.bottomNavigationView.setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.nav_home -> {
                            val intent = if (rolUsuario == "Doctor") {
                                Intent(this, HomeDoctorActivity::class.java)
                            } else {
                                Intent(this, HomePacienteActivity::class.java)
                            }
                            startActivity(intent)
                            true
                        }

                        R.id.nav_calendar -> {
                            val intent = if (rolUsuario == "Doctor") {
                                Intent(this, HorarioActivity::class.java)
                            } else {
                                Intent(this, CitasPacienteActivity::class.java)
                            }
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
    }
}
