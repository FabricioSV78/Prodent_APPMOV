package com.example.prodent.view

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.prodent.R
import com.example.prodent.databinding.ActivityMapaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.net.toUri

class MapaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapaBinding
    private lateinit var rolUsuario: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.verhorariosTv.paintFlags = binding.verhorariosTv.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                rolUsuario = document.getString("rol") ?: ""

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
                binding.verhorariosTv.setOnClickListener {
                    val dialogView = layoutInflater.inflate(R.layout.dialog_horarios, null)
                    AlertDialog.Builder(this)
                        .setTitle("Horarios de Atención")
                        .setView(dialogView)
                        .setPositiveButton("Cerrar") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }

                binding.apply {
                    whatsIv.setOnClickListener {
                        val url = "https://web.whatsapp.com/send?phone=+51987529718&text=Hola"
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        startActivity(intent)
                    }
                    instaIv.setOnClickListener {
                        val url = "https://www.instagram.com/prodentperu/"
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        startActivity(intent)
                    }
                    faceIv.setOnClickListener {
                        val url = "https://www.facebook.com/prodent.pe/?locale=es_LA"
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        startActivity(intent)
                    }
                }
            }
    }
}
