package com.example.prodent.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.prodent.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.collections.random

class ConsejoSaludActivity : BottomSheetDialogFragment(){
    private val consejosSalud = listOf(
        "Cepíllate los dientes al menos dos veces al día durante dos minutos cada vez.",
        "Usa hilo dental diariamente para eliminar la placa entre los dientes.",
        "Limita el consumo de alimentos azucarados y bebidas ácidas.",
        "Visita a tu dentista cada 6 meses para revisiones regulares.",
        "Cambia tu cepillo de dientes cada 3-4 meses o cuando las cerdas se desgasten.",
        "Considera usar un enjuague bucal con flúor para fortalecer el esmalte dental.",
        "Mantén una dieta equilibrada rica en frutas y verduras para una buena salud bucal."
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_consejo_salud, container, false)

        val randomConsejo = consejosSalud.random()
        view.findViewById<TextView>(R.id.tvConsejo).text = randomConsejo

        view.findViewById<Button>(R.id.btnCerrar).setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // Para hacer el modal más grande
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return dialog
    }
}