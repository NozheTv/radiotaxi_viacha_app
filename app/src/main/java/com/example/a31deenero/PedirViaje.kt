package com.example.a31deenero

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import org.json.JSONObject

class PedirViaje : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var origenLatLng: Point? = null
    private var destinoLatLng: Point? = null
    private lateinit var btnConfirmarDestino: Button

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val ok = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (ok) {
            enableLocationComponent()
        } else {
            Toast.makeText(
                this,
                "Se requieren permisos de ubicación para funcionar correctamente",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedir_viaje)

        mapView = findViewById(R.id.mapView)
        btnConfirmarDestino = findViewById(R.id.btnConfirmarDestino)

        // Inicialización Mapbox
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            // Configura componente de ubicación si permisos otorgados
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                enableLocationComponent()
            } else {
                pedirPermisosUbicacion()
            }
        }

        // Lógica para seleccionar destino al tocar el mapa
        mapView.getMapboxMap().addOnMapClickListener { point ->
            destinoLatLng = Point.fromLngLat(point.longitude(), point.latitude())
            // Opcional: colocar un marcador de destino o ajustar la cámara
            mapView.getMapboxMap().easeTo(
                CameraOptions.Builder()
                    .center(destinoLatLng)
                    .zoom(14.0)
                    .build()
            )
            Toast.makeText(this, "Destino seleccionado", Toast.LENGTH_SHORT).show()
            true
        }

        // Botón confirmar destino
        btnConfirmarDestino.setOnClickListener {
            if (origenLatLng != null && destinoLatLng != null) {
                realizarPedido()
            } else {
                Toast.makeText(this, "Debe seleccionar origen y destino", Toast.LENGTH_SHORT).show()
            }
        }

        // Intent local para obtener la ubicación actual si ya tienes un helper
        obtenerUbicacionActual()
    }

    private fun pedirPermisosUbicacion() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Habilita el componente de ubicación (asegúrate de que tus permisos ya están concedidos)
    private fun enableLocationComponent() {
        // Este método debería activar el layout de la ubicación si usas el plugin de Mapbox
        // La implementación exacta depende de la versión de Mapbox Maps SDK que uses.
        // A continuación se ilustra un enfoque simplificado:
        // mapView.location.nextLocation() // ejemplo ficticio si usas un helper real
        // Aquí agregas un marcador o simplemente centras la cámara en origen una vez obtenido
        // Si ya tienes otro helper, módalo para que establezca origenLatLng y mueva la cámara.
    }

    private fun obtenerUbicacionActual() {
        // Implementa tu obtención real de la ubicación (FusedLocationProviderClient, etc.)
        // Este ejemplo usa una ubicación simulada para evitar dependencias adicionales.
        origenLatLng =
            Point.fromLngLat(-68.148, -16.496) // Reemplaza con la ubicación real al obtenerla

        // Centrar mapa en origen si se obtuvo
        origenLatLng?.let {
            mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .center(it)
                    .zoom(14.0)
                    .build()
            )
            Toast.makeText(this, "Ubicación de origen establecida", Toast.LENGTH_SHORT).show()
        }
    }

    private fun realizarPedido() {
        // Construir el JSON para tu API PHP
        val jsonBody = JSONObject().apply {
            put("id_cliente", 1) // Reemplaza con el ID del usuario actual
            put("origen_latitud", origenLatLng?.latitude())
            put("origen_longitud", origenLatLng?.longitude())
            put("destino_latitud", destinoLatLng?.latitude())
            put("destino_longitud", destinoLatLng?.longitude())
            put("prioridad", false)
        }

        val url = "http://172.16.8.142/radiotaxi_viacha_mvc/public/api/pedido.php" // Reemplaza con tu endpoint real

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                val mensaje = response.optString("message", "Pedido creado")
                val tarifa = response.optDouble("tarifa", Double.NaN)
                val tarifaStr = if (tarifa.isNaN()) "" else "\nTarifa: $tarifa"
                Toast.makeText(this, "Pedido creado: $mensaje$tarifaStr", Toast.LENGTH_LONG).show()
                // Opcional: navegar a otra Activity o limpiar UI
                finish()
            },
            { error ->
                val message = error.networkResponse?.let {
                    "Error ${it.statusCode}: ${String(it.data)}"
                } ?: error.message ?: "Error desconocido"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            })

        Volley.newRequestQueue(this).add(request)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}