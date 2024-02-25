package com.example.landmarkremark.activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.landmarkremark.BuildConfig
import com.example.landmarkremark.R
import com.example.landmarkremark.databinding.ActivityGoogleMapBinding
import com.example.landmarkremark.databinding.CustomDialogBinding
import com.example.landmarkremark.databinding.CustomInfoWindowBinding
import com.example.landmarkremark.util.Constant
import com.example.landmarkremark.util.Util
import com.example.landmarkremark.viewmodel.LocationViewModel
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class GoogleMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener,
    GoogleMap.InfoWindowAdapter {

    // Initialize variable
    private lateinit var marker: Marker
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var dialog: Dialog
    private lateinit var binding: ActivityGoogleMapBinding
    private val sharedPrefFile = Constant.USER_PREF
    private var sharedNameValue: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Use view binding to bind data to view
        binding = ActivityGoogleMapBinding.inflate(layoutInflater)
        val googleMapView = binding.root
        setContentView(googleMapView)

        //Call viewModel
        locationViewModel = ViewModelProvider(
            this,
            factory = LocationViewModel.factory
        )[LocationViewModel::class.java]

        //Get username from shared preferences
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            sharedPrefFile,
            Context.MODE_PRIVATE
        )
        sharedNameValue =
            sharedPreferences.getString(Constant.USER_NAME_VALUE, getString(R.string.default_user))

        //set action bar title and background color
        supportActionBar?.title = getString(R.string.welcome) + sharedNameValue
        supportActionBar?.setBackgroundDrawable(getDrawable(R.color.teal_gradiant_end))

        //fetch data from firebase database
        locationViewModel.fetchFirebaseData()

        //show loading snack bar message
        Util.warningSnackBar(binding.root, getString(R.string.load_map), Constant.TYPE_MESSAGE)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //set up the location permissions. If they don't accept, they will still be able to see other user's landmarks
        locationViewModel.fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        //Request current location permission
        if (!Util.checkLocationPermission(this)) {
            Util.requestCurrentLocationPermission(this)
        }
    }

    override fun onResume() {
        super.onResume()

        // Call function to check if location permission is granted to the user
        mostRecentLocationAvailable(locationViewModel)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu, this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.actionbar_menu, menu)

        //search view bar and item
        val searchItem = menu?.findItem(R.id.search_location)
        val searchView = searchItem?.actionView as SearchView
        searchView.queryHint = getString(R.string.search)

        //search for a note based on contained text or user-name
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                //convert all notes or username to lowercase
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    locationViewModel.note.replaceAll(String::lowercase)
                    locationViewModel.userName.replaceAll(String::lowercase)
                }

                when (newText.lowercase()) {
                    in locationViewModel.note -> searchUsernameOrNote(
                        newText.lowercase(),
                        Constant.SEARCH_TYPE_NOTE
                    )

                    in locationViewModel.userName -> searchUsernameOrNote(
                        newText.lowercase(),
                        Constant.SEARCH_TYPE_USERNAME
                    )
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
        })

        //when close button of search view is clicked
        searchView.setOnCloseListener { //clear map
            locationViewModel.mMap.clear()
            val icon = BitmapDescriptorFactory.fromResource(R.drawable.check)
            //reset map markers to original state
            for ((index) in locationViewModel.userName.withIndex()) {
                updateResultMarker(locationViewModel, index, icon)
            }
            false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var id: Int = item.itemId

        //menu bar item buttons to search and logOut
        when (id) {
            R.id.search_location_google -> loadPlaceAPI()
            R.id.logout -> logOut()
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Override from google api function OnMapReadyCallback
     * Called when the map is ready to be used
     * @param googleMap
     */
    override fun onMapReady(googleMap: GoogleMap) {
        //Initialise GoogleMap
        locationViewModel.mMap = googleMap

        //code to enable the zoom controls on the map
        locationViewModel.mMap.uiSettings.isZoomControlsEnabled = true
        locationViewModel.mMap.setOnMarkerClickListener(this)
        locationViewModel.mMap.setOnMapClickListener(this)
        locationViewModel.mMap.setOnInfoWindowClickListener(this)

        setUpGoogleMap()
    }

    /**
     * Override from google api function OnMapClickListener
     * Function called when any other random location is clicked on the map
     * @param location
     */
    override fun onMapClick(location: LatLng) {
        Log.i(getString(R.string.map_title_log), getString(R.string.map_log))

        val icon = BitmapDescriptorFactory.fromResource(R.drawable.check)
        //to add notes to random locations on map
        locationViewModel.mMap.setOnMarkerClickListener(this)
        val markerOptions = MarkerOptions().position(location).icon(icon)
        marker = locationViewModel.mMap.addMarker(markerOptions)
        if (location != null) {
            showDialog(marker)
        }
    }

    /**
     * Override from google api function OnMarkerClickListener
     * Function called when marker is clicked or tapped which displays a dialog to add note
     * @param location
     * @return Boolean
     */
    override fun onMarkerClick(location: Marker): Boolean {
        Log.i(getString(R.string.mark_title_log), getString(R.string.mark_log))

        //display dialog to add a note when location is valid
        if (location != null) {
            showDialog(location)
        }
        return true
    }

    /**
     * Override from google api function OnInfoWindowClickListener
     * @param location
     */
    override fun onInfoWindowClick(location: Marker) {
        Log.i(getString(R.string.mark_info_log), location.position.longitude.toString())
        if (location != null) {

            // Show dialog to edit an existing note of the user and restricting users to edit other notes
            if (location.snippet.contains(sharedNameValue.toString())) {
                showDialog(location, true)
            } else {
                // Call error snack bar
                Util.warningSnackBar(
                    this.binding.root,
                    this.getString(R.string.error_edit),
                    Constant.TYPE_ERROR
                )
            }
        }
    }

    /**
     * Override from google api function InfoWindowAdapter
     * @param p0
     * @return View
     */
    override fun getInfoContents(p0: Marker?): View? {
        return null
    }

    /**
     * Override from google api function InfoWindowAdapter
     * Info adapter methods to display custom info window
     * @param p0
     * @return View
     */
    override fun getInfoWindow(p0: Marker?): View? {
        // Getting view from the layout file
        val bindingInfo: CustomInfoWindowBinding = CustomInfoWindowBinding.inflate(layoutInflater)
        val v: View = bindingInfo.root

        //declare layout views
        val title: String? = p0?.title
        val notes: String? = p0?.snippet

        if (title == null || notes == null) {
            return null
        }

        //set text and snippet of marker options to layout views
        bindingInfo.tvNote.text = title
        bindingInfo.tvSnippet.text = notes

        return v
    }

    /**
     * Search map marker based on username or note
     * @param newText
     * @param type
     */
    private fun searchUsernameOrNote(newText: String, type: Int) {
        //Calculate the markers to get their position
        var count = 0
        val b = LatLngBounds.Builder()
        val icon = BitmapDescriptorFactory.fromResource(R.drawable.check)
        var listSearchResult = locationViewModel.userName.withIndex()
        if (type == Constant.SEARCH_TYPE_NOTE) {
            listSearchResult = locationViewModel.note.withIndex()
        }
        locationViewModel.mMap.clear()

        //search all markers corresponding to the typed username
        for ((index, value) in listSearchResult) {
            if (value == newText) {
                b.include(locationViewModel.markers[index].position)
                count += 1
                updateResultMarker(locationViewModel, index, null)
            } else {
                updateResultMarker(locationViewModel, index, icon)
            }
        }

        displaySearchResult(b, count)
    }

    /**
     * Update markers on google map
     * @param locationViewModel
     * @param index
     * @param icon
     */
    private fun updateResultMarker(
        locationViewModel: LocationViewModel,
        index: Int,
        icon: BitmapDescriptor?
    ) {
        val location =
            LatLng(locationViewModel.latitude[index], locationViewModel.longitude[index])
        val markerOptions = MarkerOptions().position(location)
        markerOptions.title(locationViewModel.note[index])
            .snippet(
                getString(R.string.username_str) + locationViewModel.userName[index]
                        + "\n" + getString(R.string.latitude_str) + locationViewModel.latitude[index]
                        + "\n" + getString(R.string.longitude_str) + locationViewModel.longitude[index]
            )
        icon?.let {
            markerOptions.icon(icon)
        }

        locationViewModel.mMap.addMarker(markerOptions)
    }

    /**
     * Display search result on google map and move camera
     * @param b
     * @param count
     */
    private fun displaySearchResult(b: LatLngBounds.Builder, count: Int) {
        val bounds = b.build()

        //Change the padding as per needed and animate camera to show all the markers found
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 50, 50, 5)
        locationViewModel.mMap.animateCamera(cu)

        //dismiss soft input keyboard
        Util.dismissKeyboard(this)

        //display toast with count of found result
        displayResultSnackBar(count)
    }

    /**
     * This method creates a new builder for an intent to start
     * the Place API and then starts the autocomplete intent
     */
    private fun loadPlaceAPI() {
        try {
            if (!Places.isInitialized()) {
                val googleApiKey = BuildConfig.GOOGLE_MAP_API_KEY
                Places.initialize(applicationContext, googleApiKey)
            }
            // Set the fields to specify which types of place data to return.
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
            // Start the autocomplete intent.
            val intent = Autocomplete
                .IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this)

            resultLauncher.launch(intent)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    /**
     * Process result of autocomplete intent of google API
     * Set red marker on google map
     */
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(result.data)
                locationViewModel.placeMarkerOnMap(place.latLng)
                locationViewModel.mMap.setOnMarkerClickListener(this)
            }
        }

    /**
     * Function sign out from current user
     */
    private fun logOut() {
        //Update shared preferences
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            sharedPrefFile,
            Context.MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constant.USER_NAME_VALUE, "")
        editor.putBoolean(Constant.LOGGED_IN_VALUE, false)
        editor.apply()
        editor.commit()

        //Transit to Login Screen
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Function to setup map
     */
    private fun setUpGoogleMap() {
        //code to check if the app has been granted the location access , if not we request the user
        if (!Util.checkLocationPermission(this)) {
            Util.requestCurrentLocationPermission(this)
            return
        }
        mostRecentLocationAvailable(locationViewModel)
    }

    /**
     * Check when location permission is granted to the user
     * @param locationViewModel
     */
    private fun mostRecentLocationAvailable(locationViewModel: LocationViewModel) {
        //check if google map is initialized
        if (Util.checkLocationPermission(this) && locationViewModel.checkInitialized()) {
            // Draws a light blue dot on the user’s location & adds a button to the map that,
            // when tapped, centers the map on the user’s location.

            locationViewModel.mMap.isMyLocationEnabled = true
            //this code gives you the most recent location currently available.
            locationViewModel.fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    locationViewModel.lastLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    locationViewModel.mMap.setInfoWindowAdapter(this)
                    locationViewModel.mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            currentLatLng,
                            12f
                        )
                    )
                }
            }
        }
    }

    /**
     * Show dialog to add or edit information of marker
     * @param marker
     * @param isUpdate
     */
    private fun showDialog(marker: Marker, isUpdate: Boolean = false) {
        //Initialize dialog and its attributes
        val bindingDialog: CustomDialogBinding = CustomDialogBinding.inflate(layoutInflater)
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(bindingDialog.root)

        //add dialog title
        bindingDialog.tvDialogTitle.text = getString(R.string.add_note)
        if(isUpdate) {
            bindingDialog.tvDialogTitle.text = getString(R.string.edit_note_place)
        }

        //add dialog attributes
        Util.addDialogAttribute(dialog)

        //show dialog
        dialog.show()

        //code when ok button is clicked
        bindingDialog.btnOk.setOnClickListener {
            if (bindingDialog.edtNote.text.toString() == "") {
                // Display error snack bar to enter a note
                Util.warningSnackBar(
                    this.binding.root,
                    this.getString(R.string.error_note),
                    Constant.TYPE_ERROR
                )
            } else {
                val lat = marker.position.latitude
                val lon = marker.position.longitude

                //note data structure
                val note = hashMapOf(
                    "username" to sharedNameValue,
                    "note" to bindingDialog.edtNote.text.toString(),
                    "latitude" to lat,
                    "longitude" to lon
                )

                // Add or update document
                var document =
                    locationViewModel.fireStoreDB.collection(Constant.COLLECTION_DB_FIRESTORE)
                        .document("$lat,$lon")
                if (isUpdate) {
                    //update cloud firestore
                    document.update(Constant.NOTE, bindingDialog.edtNote.text.toString())
                } else {
                    document.set(note)
                }
                    .addOnSuccessListener {
                        Log.d(
                            Constant.TAG_GOOGLE_ACTIVITY,
                            getString(R.string.document_update_log)
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.w(
                            Constant.TAG_GOOGLE_ACTIVITY,
                            getString(R.string.error_document_update_log),
                            e
                        )
                    }


                //code to dismiss keyboard
                Util.dismissKeyboard(this)

                //code to refresh activity and dismiss dialog
                recreate()
                dialog.dismiss()
            }
        }

        //code when cancel button is clicked
        bindingDialog.btnCancel.setOnClickListener {
            //code to dismiss keyboard, dialog and remove marker from position
            if (!isUpdate) {
                locationViewModel.mMap.setOnMarkerClickListener { false }
                marker.remove()
            }
            Util.dismissKeyboard(this)
            dialog.dismiss()
        }
    }

    /**
     * Display snack bar with count of searched locations
     * @param count
     */
    private fun displayResultSnackBar(count: Int) {
        if (count > 0) {
            Util.warningSnackBar(
                binding.root,
                getString(R.string.location_found_log, count.toString()),
                Constant.TYPE_ERROR
            )
        }
    }
}