package com.example.a31deenero

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import org.json.JSONObject

import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager


class PedirViaje : AppCompatActivity() {

    private lateinit var pointAnnotationManager: PointAnnotationManager

    private lateinit var mapView: MapView
    private var origenLatLng: Point? = null
    private var destinoLatLng: Point? = null
    private lateinit var btnConfirmarDestino: Button
    private lateinit var switchPrioridad: Switch

    private var hasCenteredCamera = false

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
        switchPrioridad = findViewById(R.id.switchPrioridad)


        // Inicialización Mapbox
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {

            pointAnnotationManager = mapView.annotations.createPointAnnotationManager()

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

            // Limpiar anotaciones anteriores para mantener solo una
            pointAnnotationManager.deleteAll()

            // Carga el bitmap desde drawable (reemplaza 'tu_icono' por tu recurso)
            val bitmap = android.graphics.BitmapFactory.decodeResource(resources, R.drawable.ubi)

            // Crea la anotación con el punto y el icono
            val pointAnnotationOptions = PointAnnotationOptions()
                .withPoint(destinoLatLng!!)
                .withIconImage(bitmap)

            // Añade la anotación al mapa
            pointAnnotationManager.create(pointAnnotationOptions)

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
    }

    private fun pedirPermisosUbicacion() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun enableLocationComponent() {
        // Configura y muestra el puck de ubicación con orientación
        mapView.location.locationPuck = createDefault2DPuck(withBearing = true)
        mapView.location.enabled = true
        mapView.location.puckBearingEnabled = true

        // Listener para centrar cámara solo la primera vez que se recibe ubicación
        mapView.location.addOnIndicatorPositionChangedListener(object : OnIndicatorPositionChangedListener {
            override fun onIndicatorPositionChanged(point: Point) {
                if (!hasCenteredCamera) {
                    origenLatLng = Point.fromLngLat(point.longitude(), point.latitude())
                    mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(origenLatLng)
                            .zoom(14.0)
                            .build()
                    )
                    hasCenteredCamera = true
                    Toast.makeText(this@PedirViaje, "Ubicación de origen establecida", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun realizarPedido() {
        val clienteId = intent.getStringExtra("ID_CLIENTE")
        if(clienteId == null || clienteId.isEmpty()){
            Toast.makeText(this, "ID cliente no disponible", Toast.LENGTH_LONG).show()
            finish()  // o manejar la falta de id según convenga
            return
        }
        val jsonBody = JSONObject().apply {
            put("id_cliente", clienteId.toInt())
            put("origen_latitud", origenLatLng?.latitude())
            put("origen_longitud", origenLatLng?.longitude())
            put("destino_latitud", destinoLatLng?.latitude())
            put("destino_longitud", destinoLatLng?.longitude())
            put("prioridad", switchPrioridad.isChecked)
        }

        val url = "http://192.168.100.45/radiotaxi_viacha_mvc/public/api/pedido.php"

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                val mensaje = response.optString("message", "Pedido creado")
                val tarifa = response.optDouble("tarifa", Double.NaN)
                val tarifaStr = if (tarifa.isNaN()) "" else "\nTarifa: $tarifa"
                Toast.makeText(this, "Pedido creado: $mensaje$tarifaStr", Toast.LENGTH_LONG).show()
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
