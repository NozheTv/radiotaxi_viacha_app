package com.example.a31deenero

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailField = findViewById<EditText>(R.id.etEmail)
        val passwordField = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val registerButton = findViewById<Button>(R.id.btnRegister)

        registerButton.setOnClickListener {
            val intent = Intent(this, Registro_cliente::class.java)
            startActivity(intent)
        }


        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty()) {
                emailField.error = "Por favor ingresa tu correo"
                emailField.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordField.error = "Por favor ingresa tu contraseña"
                passwordField.requestFocus()
                return@setOnClickListener
            }

            loginCliente(email, password)
        }


    }

    private fun loginCliente(email: String, password: String) {
        val url = "http://192.168.100.45/radiotaxi_viacha_mvc/public/api/login_cliente.php"
        val queue: RequestQueue = Volley.newRequestQueue(this)
        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)

        val request = JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                try {
                    val message = response.getString("message")
                    val user = response.getJSONObject("user")
                    val clienteId = user.getString("id")

                    Toast.makeText(this, "Logueado: $message", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, Selector::class.java)
                    intent.putExtra("ID_CLIENTE", clienteId)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al procesar respuesta JSON", Toast.LENGTH_LONG).show()
                }

            },
            { error ->
                // Mostrar mensaje básico
                var message = "Error desconocido"
                error.networkResponse?.let {
                    val statusCode = it.statusCode
                    val data = String(it.data)
                    message = "Error $statusCode: $data"
                    // Puedes loggear más info en Logcat para debug
                    android.util.Log.e("VolleyError", "Status $statusCode, Body $data")
                } ?: run {
                    // Si no hay respuesta del servidor
                    message = error.message ?: "Error en la conexión"
                    android.util.Log.e("VolleyError", "Mensaje: $message")
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )

        queue.add(request)
    }

}