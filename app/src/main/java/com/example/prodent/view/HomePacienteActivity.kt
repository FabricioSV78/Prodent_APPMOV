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

            }


        }


