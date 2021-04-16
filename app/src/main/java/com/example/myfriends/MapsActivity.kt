package com.example.myfriends

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "xyz"

    lateinit var friendLocation: LatLng
    lateinit var currentLocation: LatLng

    private lateinit var mMap: GoogleMap
    private var friendName: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        var extras: Bundle = intent.extras!!
        friendLocation = LatLng(extras.getDouble("homeLocationLat"), extras.getDouble("homeLocationLon"))
        currentLocation = LatLng(extras.getDouble("currentLocationLat"), extras.getDouble("currentLocationLon"))
        friendName = extras.getString("friendName")
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //We should create markers, which are things that you can add to your map.
        //In the FriendsApp, if you go to the map view of all your friends, you just need to create a marker for each of your friends and add it to the map.

        googleMap?.addMarker(MarkerOptions()
                .position(friendLocation)
                .title(friendName))
        googleMap?.addMarker(MarkerOptions()
                .position(currentLocation)
                .title("Current Friend Location"))
        /*val easvMarker = MarkerOptions().position(EASV).title("EASV").icon(
                BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_add_location_24)
        )
        mMap.addMarker(easvMarker)*/
        mMap.moveCamera(CameraUpdateFactory.newLatLng(friendLocation))
        val viewPoint = CameraUpdateFactory.newLatLngZoom(friendLocation, 13F)
        mMap.animateCamera(viewPoint)

        // setupZoomLevels()
    }

    /*private fun setupZoomLevels() {
        spinnerZoomLevel.adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.zoomlevels,
                        android.R.layout.simple_spinner_dropdown_item
                )
    }*/
}

