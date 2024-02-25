package com.example.landmarkremark.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.landmarkremark.R
import com.example.landmarkremark.databinding.ActivityLoginBinding
import com.example.landmarkremark.util.Constant
import com.example.landmarkremark.util.Util

class LoginActivity : AppCompatActivity() {
    //Initialize variable
    private val sharedPrefFile = Constant.USER_PREF
    private lateinit var binding: ActivityLoginBinding
    private var sharedSignedValue: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Using data binding to bind data to view
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        //Login button is clicked
        binding.btnLogin.setOnClickListener {
            if (binding.edtUsername.text.toString() == "") {
                Util.warningSnackBar(
                    binding.root,
                    getString(R.string.userwarning),
                    Constant.TYPE_ERROR
                )
                return@setOnClickListener
            }
            loginIn()
        }
    }

    public override fun onStart() {
        super.onStart()
        //get username from shared preferences, if user is already signed in then navigate directly to GoogleMapActivity
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            sharedPrefFile,
            Context.MODE_PRIVATE
        )
        sharedSignedValue = sharedPreferences.getBoolean(Constant.LOGGED_IN_VALUE, false)
        if (sharedSignedValue == true) {
            startGoogleMapsActivity()
        }
    }

    /**
     * Navigate to GoogleMapActivity
     */
    private fun startGoogleMapsActivity() {
        val intent = Intent(this, GoogleMapActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Login Function
     * Use shared preference to save information user
     */
    private fun loginIn() {
        //save username to shared preferences which is local storage
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            sharedPrefFile,
            Context.MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(Constant.USER_NAME_VALUE, binding.edtUsername.text.toString())
        editor.putBoolean(Constant.LOGGED_IN_VALUE, true)
        editor.apply()
        editor.commit()

        //Call GoogleMapActivity when user is successfully authenticated
        startGoogleMapsActivity()
    }

}