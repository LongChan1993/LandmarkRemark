package com.example.landmarkremark

import android.app.Application

import com.example.landmarkremark.util.ResourceProvider

class LandmarkRemarkApplication: Application() {
    val resourceProvider: ResourceProvider by lazy { ResourceProvider(resources) }
}