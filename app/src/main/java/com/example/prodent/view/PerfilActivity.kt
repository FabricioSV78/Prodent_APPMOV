package com.example.prodent.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prodent.databinding.ActivityPerfilBinding
import com.example.prodent.viewmodel.PerfilViewModel
import com.example.prodent.R

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private val viewModel = PerfilViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.obtenerDatosUsuario { usuario ->
            binding.etNombre.setText(usuario.nombre)
            binding.etCorreo.setText(usuario.correo)

            if (usuario.rol == "Paciente") {
                binding.etTelefono.setText(usuario.telefono)
                binding.etTelefono.visibility = View.VISIBLE
                binding.tvTelefonoLabel.visibility = View.VISIBLE
            } else {
                binding.etTelefono.visibility = View.GONE
                binding.tvTelefonoLabel.visibility = View.GONE
            }
        }

        binding.btnGuardar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val correo = binding.etCorreo.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()
            val nuevaContrasena = binding.etContrasena.text.toString().trim()

            viewModel.actualizarDatosPerfil(
                nombre = nombre,
                correo = correo,
                telefono = telefono,
                nuevaContrasena = nuevaContrasena
            ) { mensaje ->
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivFotoPerfil.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, 1001)
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val destino = if (viewModel.rolUsuario == "Doctor") {
                        HomeDoctorActivity::class.java
                    } else {
                        HomePacienteActivity::class.java
                    }
                    startActivity(Intent(this, destino))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val uri = data?.data
            uri?.let {
                binding.ivFotoPerfil.setImageURI(it)
            }
        }
    }
}
