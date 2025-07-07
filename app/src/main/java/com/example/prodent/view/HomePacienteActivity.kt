package com.example.prodent.view

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.prodent.R
import com.example.prodent.databinding.ActivityHomedoctorBinding
import com.example.prodent.databinding.ActivityHomepacienteBinding
import com.example.prodent.model.Cita
import com.example.prodent.viewmodel.CitaViewModel
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.getValue

class HomePacienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomepacienteBinding
    private lateinit var sharedPref: SharedPreferences
    private val viewModel: CitaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomepacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTime", true)
        if (isFirstTime) {
            mostrarConsejoSalud()
            sharedPref.edit().putBoolean("isFirstTime", false).apply()
        }

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
                    true
                }

                R.id.nav_calendar -> {
                    startActivity(Intent(this, CitasPacienteActivity::class.java))
                    finish()
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

        val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val isFirstLogin = sharedPref.getBoolean("first_login_${uid}", true)

        if (isFirstLogin) {
            mostrarGuia()

            editor.putBoolean("first_login_${uid}", false)
            editor.apply()
        }

    }
    private fun mostrarConsejoSalud() {
        val consejoDialog = ConsejoSaludActivity()
        consejoDialog.show(supportFragmentManager, "ConsejoSalud")
    }

    // Resetear el flag cuando cierran sesión (opcional)
    companion object {
        fun resetFirstTimeFlag(context: Context) {
            val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("isFirstTime", true).apply()
        }
    }

    private var secuenciaGuia: TapTargetSequence? = null
    private fun mostrarGuia() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val homeItem = bottomNav.findViewById<View>(R.id.nav_home)
        val calendarioItem = bottomNav.findViewById<View>(R.id.nav_calendar)
        val notificacionItem = bottomNav.findViewById<View>(R.id.nav_notifications)
        val configuracionItem = bottomNav.findViewById<View>(R.id.nav_configuracion)

        secuenciaGuia = TapTargetSequence(this)
            .targets(
                TapTarget.forView(homeItem, "Inicio", "Vista principal y proximos recordatorios")
                    .cancelable(true).dimColor(android.R.color.black) ,
                TapTarget.forView(calendarioItem, "Citas", "Consulta tus citas pasadas y futuras")
                    .cancelable(true).dimColor(android.R.color.black) ,
                TapTarget.forView(notificacionItem, "Notificaciones", "Revisa alertas importantes")
                    .cancelable(true).dimColor(android.R.color.black) ,
                TapTarget.forView(configuracionItem, "Ajustes", "Configura tu perfil y preferencias")
                    .cancelable(true).dimColor(android.R.color.black)
            )
            .listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() {
                }

                override fun onSequenceStep(tapTarget: TapTarget, targetClicked: Boolean) {}

                override fun onSequenceCanceled(tapTarget: TapTarget) {
                }
            })

        secuenciaGuia?.start()

    }
}


