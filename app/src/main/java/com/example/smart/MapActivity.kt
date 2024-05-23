package com.example.smart

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.smart.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class MapActivity : FragmentActivity(), LocationListener, OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var uid: String
    private var mLatitude = 0.0
    private var mLongitude = 0.0

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("parking_locations")

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val TAG = "MapActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uid = intent.getStringExtra("uid") ?: ""

        if (checkLocationPermission()) {
            initializeMap()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap.uiSettings.isZoomControlsEnabled = true

        if (checkLocationPermission()) {
            mGoogleMap.isMyLocationEnabled = true
            setupLocationUpdates()
        } else {
            Log.d(TAG, "Location permission not granted")
        }

        mGoogleMap.setOnMapClickListener { latLng ->
            addMarkerAndShowDialog(latLng)
        }

        mGoogleMap.setOnInfoWindowClickListener { marker ->
            val parkingId = marker.tag as? String ?: return@setOnInfoWindowClickListener
            val snippetParts = marker.snippet?.split(", ") ?: listOf("", "", "")
            val totalSlots = snippetParts.getOrNull(0)?.split(":")?.get(1)?.trim()?.toInt() ?: 0
            val emptySlots = snippetParts.getOrNull(1)?.split(":")?.get(1)?.trim()?.toInt() ?: 0
            val price = snippetParts.getOrNull(2)?.split(":")?.get(1)?.trim()?.toDouble() ?: 0.0

            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("parkingId", parkingId)
            intent.putExtra("location", marker.title)
            intent.putExtra("totalSlots", totalSlots)
            intent.putExtra("emptySlots", emptySlots)
            intent.putExtra("price", price)
            startActivity(intent)
        }

        loadMarkersFromFirebase()
    }

    private fun addMarkerAndShowDialog(latLng: LatLng) {
        val markerOptions = MarkerOptions().position(latLng).title("New Parking Location")
        val marker = mGoogleMap.addMarker(markerOptions)
        marker?.showInfoWindow()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Parking Location")
        val view = layoutInflater.inflate(R.layout.dialog_update_slot, null)
        builder.setView(view)

        val locationInput = view.findViewById<EditText>(R.id.location_input)
        val totalSlotsInput = view.findViewById<EditText>(R.id.total_slots_input)
        val emptySlotsInput = view.findViewById<EditText>(R.id.empty_slots_input)
        val priceSlotInput = view.findViewById<EditText>(R.id.price_slot)

        builder.setPositiveButton("Add") { dialog, which ->
            val location = locationInput.text.toString()
            val totalSlots = totalSlotsInput.text.toString().toIntOrNull()
            val emptySlots = emptySlotsInput.text.toString().toIntOrNull()
            val price = priceSlotInput.text.toString().toDoubleOrNull()

            if (location.isNotEmpty() && totalSlots != null && emptySlots != null && price != null) {
                val parkingLocation = ParkingLocation(uid, location, totalSlots, emptySlots, latLng.longitude, latLng.latitude, price)
                database.push().setValue(parkingLocation).addOnSuccessListener {
                    Toast.makeText(this, "Parking location added", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to add parking location", Toast.LENGTH_SHORT).show()
                    marker?.remove()
                }
            } else {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show()
                marker?.remove()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            marker?.remove()
        }

        builder.show()
    }

    private fun loadMarkersFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mGoogleMap.clear()
                for (parkingSnapshot in snapshot.children) {
                    val parkingLocation = parkingSnapshot.getValue(ParkingLocation::class.java)
                    if (parkingLocation != null) {
                        val latLng = LatLng(parkingLocation.latitude, parkingLocation.longitude)
                        val markerOptions = MarkerOptions().position(latLng)
                            .title(parkingLocation.location)
                            .snippet("Total Slots: ${parkingLocation.total_slots}, Empty Slots: ${parkingLocation.empty_slots}, Price: ${parkingLocation.price}")
                        val marker = mGoogleMap.addMarker(markerOptions)
                        marker?.tag = parkingSnapshot.key
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MapActivity, "Failed to load parking locations", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupLocationUpdates() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val provider = locationManager.getBestProvider(android.location.Criteria(), true)
        if (provider != null) {
            if (checkLocationPermission()) {
                locationManager.requestLocationUpdates(provider, 20000, 0f, this)
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    onLocationChanged(location)
                } else {
                    Log.d(TAG, "Last known location is null")
                }
            } else {
                Log.d(TAG, "Location permission not granted")
            }
        } else {
            Log.d(TAG, "No location provider available")
        }
    }

    override fun onLocationChanged(location: Location) {
        mLatitude = location.latitude
        mLongitude = location.longitude
        val latLng = LatLng(mLatitude, mLongitude)

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12f))
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

data class ParkingLocation(
    val uid: String,
    val location: String = "",
    val total_slots: Int = 0,
    val empty_slots: Int = 0,
    val longitude: Double = 0.0,
    val latitude: Double = 0.0,
    val price: Double = 0.0
)
{
    constructor() : this("", "", 0, 0, 0.0, 0.0, 0.0)
}
