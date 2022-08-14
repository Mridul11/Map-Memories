package com.supercoolapps.mapmemories

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.supercoolapps.mapmemories.MainActivity.Companion.EXTRA_MAP_TITLE
import com.supercoolapps.mapmemories.MainActivity.Companion.EXTRA_USER_MAP
import com.supercoolapps.mapmemories.databinding.ActivityCreateMapBinding
import com.supercoolapps.models.Place
import com.supercoolapps.models.UserMap

class CreateMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCreateMapBinding
    private var markers: MutableList<Marker> = mutableListOf()

    companion object{
        const val TAG = "CreateMapActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = intent.getStringExtra(EXTRA_MAP_TITLE)
        binding = ActivityCreateMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.view?.let {
            Snackbar.make(it, "Long press to add a marker!", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", {})
                .setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create_map, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       // Check that item is save menu option
        if(item.itemId == R.id.miSave){
            Log.i(TAG , "tapped on save!")
            if(markers.isEmpty()){
                Toast.makeText(this, "Atleadt one marker is required!", Toast.LENGTH_SHORT).show()
                return true
            }
            val places= markers.map { marker -> marker.title?.let { marker.snippet?.let { it1 ->
                Place(it,
                    it1, marker.position.latitude, marker.position.longitude)
            } } }
            val userMap = intent.getStringExtra(EXTRA_MAP_TITLE)?.let { UserMap(it,
                places as List<Place>
            ) }
            val data = Intent()
            data.putExtra(EXTRA_USER_MAP, userMap)
            setResult(Activity.RESULT_OK, data)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
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

        mMap.setOnInfoWindowClickListener {markerToDelete ->
            Log.i(TAG, "onWindowClickListener - delete this marker")
            markers.remove(markerToDelete)
            markerToDelete.remove()
        }
        mMap.setOnMapLongClickListener {latLong ->
            Log.i(TAG,  "onMapClickListener!")
            showAlertDialog(latLong)
        }
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun showAlertDialog(latLong: LatLng) {
        val placeFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_place, null)
       val alertDialog = AlertDialog.Builder(this)
           .setTitle("create a marker")
           .setView(placeFormView)
           .setNegativeButton("Cancel", null)
           .setPositiveButton("OK", null)
           .show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = placeFormView.findViewById<EditText>(R.id.etTitle).text.toString()
            val description = placeFormView.findViewById<EditText>(R.id.etDescription).text.toString()
            if(title.trim().isEmpty() && description.trim().isEmpty() ){
                Toast.makeText(this, "Title or description can not be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val marker = mMap.addMarker(MarkerOptions().position(latLong).title(title).snippet(description))
            markers.add(marker!!)
            alertDialog.dismiss()
        }
    }
}