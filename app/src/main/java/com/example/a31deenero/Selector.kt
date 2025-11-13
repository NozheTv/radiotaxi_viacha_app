package com.example.a31deenero

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Selector : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_selector)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnPedirViaje = findViewById<Button>(R.id.btnPedirViaje)
        val btnHistorial = findViewById<Button>(R.id.btnHistorial)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarCesion)
        val clienteId = intent.getStringExtra("ID_CLIENTE") ?: ""



        // Navegar a la pantalla para pedir viaje
        btnPedirViaje.setOnClickListener {
            val intent = Intent(this, PedirViaje::class.java)
            intent.putExtra("ID_CLIENTE", clienteId)  // clienteId desde el login o almacenado
            startActivity(intent)

        }

        // Navegar al historial de viajes
        btnHistorial.setOnClickListener {
            val intent = Intent(this, Historial::class.java)
            startActivity(intent)
        }

        // Cerrar sesión y volver al login
        btnCerrarSesion.setOnClickListener {
            // Aquí puedes limpiar datos de sesión o preferencias
            // Por ejemplo:
            // val prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
            // prefs.edit().clear().apply()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}