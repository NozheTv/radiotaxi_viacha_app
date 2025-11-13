package com.example.a31deenero

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Registro_cliente : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegistrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_cliente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etTelefono = findViewById(R.id.editTextNumber)
        etDireccion = findViewById(R.id.etDireccion)
        etPassword = findViewById(R.id.etPassword)
        btnRegistrar = findViewById(R.id.btnLogin)

        btnRegistrar.setOnClickListener {
            val nombre = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val direccion = etDireccion.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nombre.isEmpty()) {
                etName.error = "Por favor ingresa tu nombre"
                etName.requestFocus()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                etEmail.error = "Por favor ingresa tu correo"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            if (telefono.isEmpty()) {
                etTelefono.error = "Por favor ingresa tu teléfono"
                etTelefono.requestFocus()
                return@setOnClickListener
            }
            if (direccion.isEmpty()) {
                etDireccion.error = "Por favor ingresa tu dirección"
                etDireccion.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Por favor ingresa tu contraseña"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // Llamar función para registrar
            registerCliente(nombre, email, telefono, direccion, password)
        }
    }
    private fun registerCliente(
        nombre: String,
        email: String,
        telefono: String,
        direccion: String,
        password: String
    ) {
        val url = "http://192.168.100.45/radiotaxi_viacha_mvc/public/api/register.php" // Ajusta según tu URL
        val queue: RequestQueue = Volley.newRequestQueue(this)
        val jsonBody = JSONObject().apply {
            put("nombre", nombre)
            put("email", email)
            put("telefono", telefono)
            put("direccion", direccion)
            put("password", password)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                try {
                    val message = response.getString("message")
                    Toast.makeText(this, "Registro: $message", Toast.LENGTH_LONG).show()
                    // Opcional: cerrar actividad o iniciar otra
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error procesando respuesta JSON", Toast.LENGTH_LONG)
                        .show()
                }
            },
            { error ->
                var message = "Error desconocido"
                error.networkResponse?.let {
                    val statusCode = it.statusCode
                    val data = String(it.data)
                    message = "Error $statusCode: $data"
                } ?: run {
                    message = error.message ?: "Error en la conexión"
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )

        queue.add(request)
    }
}