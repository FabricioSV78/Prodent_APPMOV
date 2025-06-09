package com.example.prodent.view

import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.prodent.databinding.ActivityMapaBinding

class MapaActivity: AppCompatActivity() {
    private lateinit var binding: ActivityMapaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val textView = binding.verhorariosTv
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }
}