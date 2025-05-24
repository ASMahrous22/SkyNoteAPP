package com.example.skynote.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.skynote.databinding.ActivityMapSearchBinding
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay

class MapSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapSearchBinding
    private lateinit var mapMarker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Required for OSMDroid to avoid 403 errors
        Configuration.getInstance().load(
            applicationContext,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        binding = ActivityMapSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val map = binding.map
        map.setMultiTouchControls(true)

        val cairo = GeoPoint(30.0444, 31.2357)
        map.controller.setZoom(10.0)
        map.controller.setCenter(cairo)

        // Initialize the marker
        mapMarker = Marker(map)
        mapMarker.position = cairo
        mapMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapMarker.title = "Selected Location"
        map.overlays.add(mapMarker)

        // Set up tap listener using MapEventsOverlay
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (p != null) {
                    mapMarker.position = p
                    map.controller.setCenter(p)
                    map.invalidate()

                    val resultIntent = Intent().apply {
                        putExtra("lat", p.latitude)
                        putExtra("lon", p.longitude)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean = false
        })

        map.overlays.add(mapEventsOverlay)
    }
}
