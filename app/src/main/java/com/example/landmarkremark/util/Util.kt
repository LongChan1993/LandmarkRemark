package com.example.landmarkremark.util

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.landmarkremark.R
import com.example.landmarkremark.viewmodel.LocationViewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

class Util {

    companion object {
        /**
         * Function to check the ACCESS_FINE_LOCATION permission
         * @param activity
         */
        fun checkLocationPermission(activity: Activity) =
            ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        /**
         * Function to request current location permission
         * @param activity
         */
        fun requestCurrentLocationPermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                200
            )
        }

        /**
         * Function to display warning snack bar
         * @param view
         * @param textDisplay
         * @param type
         */
        fun warningSnackBar(view: View, textDisplay: String, type: Int) {
            var colorSB = ContextCompat.getColor(view.context, R.color.black)
            if(type == Constant.TYPE_ERROR){
                colorSB = ContextCompat.getColor(view.context, R.color.heavy_red)
            }
            val snackBar = Snackbar.make(view, textDisplay, Snackbar.LENGTH_LONG)
            snackBar.view.setBackgroundColor(colorSB)
            snackBar.show()
        }

        /**
         * Function to add attribute to dialog
         * @param dialog
         */
        fun addDialogAttribute(dialog: Dialog) {
            val wmlp = dialog.window!!.attributes
            wmlp.gravity = Gravity.TOP
            wmlp.y = 150
        }

        /**
         * Function to dismiss keyboard
         * @param activity
         */
        fun dismissKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
        }

        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        Log.i(context.getString(R.string.internet_log), context.getString(R.string.cellular_log))
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        Log.i(context.getString(R.string.internet_log), context.getString(R.string.wifi_log))
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        Log.i(context.getString(R.string.internet_log), context.getString(R.string.ethernet_log))
                        return true
                    }
                }
            }
            return false
        }
    }
}