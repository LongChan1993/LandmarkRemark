package com.example.landmarkremark.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.landmarkremark.LandmarkRemarkApplication
import com.example.landmarkremark.R
import com.example.landmarkremark.util.Constant
import com.example.landmarkremark.util.ResourceProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class LocationViewModel(tempResourceProvider: ResourceProvider): ViewModel() {

    //Initial Variable
    val fireStoreDB = FirebaseFirestore.getInstance()
    lateinit var mMap: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var lastLocation: Location
    private var resourceProvider = tempResourceProvider

    //Initialize empty array lists
    val note = ArrayList<String>()
    val userName = ArrayList<String>()
    val latitude = ArrayList<Double>()
    val longitude = ArrayList<Double>()
    private val latLon = ArrayList<String>()
    val markers = ArrayList<Marker>()

    //Initialize view model
    init {
        Log.i(resourceProvider.getString(R.string.location_log_title), resourceProvider.getString(R.string.location_log))
    }

    /**
     * Fetch data from firebase cloud store
     */
    fun fetchFirebaseData() {
        //clear the array lists before appending
        note.clear()
        userName.clear()
        latitude.clear()
        longitude.clear()
        latLon.clear()
        markers.clear()

        val icon = BitmapDescriptorFactory.fromResource(R.drawable.check)

        //fetch data from firebase
        fireStoreDB.collection(Constant.COLLECTION_DB_FIRESTORE)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    //store data in local arraylists
                    note.add("${document.data["note"]}")
                    userName.add("${document.data["username"]}")
                    latitude.add("${document.data["latitude"]}".toDouble())
                    longitude.add("${document.data["longitude"]}".toDouble())
                    latLon.add("${document.data["latitude"]}"+","+"${document.data["longitude"]}")

                    //add marker to saved locations
                    val location = LatLng("${document.data["latitude"]}".toDouble(),"${document.data["longitude"]}".toDouble())
                    val markerOptions = MarkerOptions().position(location)
                    markerOptions.title(resourceProvider.getString(R.string.note_str) + "${document.data["note"]}")
                        .snippet(resourceProvider.getString(R.string.username_str) + "${document.data["username"]}"
                            + "\n" + resourceProvider.getString(R.string.latitude_str) + "${document.data["latitude"]}"
                            + "\n" + resourceProvider.getString(R.string.longitude_str) + "${document.data["longitude"]}")
                        .icon(icon)
                    mMap.setOnMarkerClickListener { false }

                    //add markers to map
                    mMap.addMarker(markerOptions)
                    markers.add(mMap.addMarker(markerOptions))

                }
            }
            .addOnFailureListener { exception ->
                Log.i(resourceProvider.getString(R.string.google_activity_log), resourceProvider.getString(R.string.google_activity_log_content), exception)
            }
    }

    /**
     * Function to check if google map is initialized
     * @return Boolean
     */
    fun checkInitialized(): Boolean {
        return ::mMap.isInitialized
    }

    /**
     * Function to add a marker to location
     * @param location
     */
    fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        mMap.addMarker(markerOptions)
    }

    /**
     * Companion object to pass parameter resourceProvider to ViewModel
     */
    companion object {
        val factory : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as LandmarkRemarkApplication)
                LocationViewModel(application.resourceProvider)
            }
        }
    }
}