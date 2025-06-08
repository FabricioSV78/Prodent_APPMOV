package com.example.prodent.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.prodent.R
import com.example.prodent.databinding.ActivityConfiguracionBinding
import com.example.prodent.viewmodel.ConfiguracionViewModel

class ConfiguracionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracionBinding
    private val viewModel = ConfiguracionViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvEditarPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        binding.tvCerrarSesion.setOnClickListener {
            viewModel.cerrarSesion {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomePacienteActivity::class.java)) // o HomeDoctorActivity si detectas rol
                    true
                }

                R.id.nav_calendar -> {
                    startActivity(Intent(this, RegistrarCitaActivity::class.java))
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
