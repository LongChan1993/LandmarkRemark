package com.example.landmarkremark.util

import android.content.res.Resources
import androidx.annotation.StringRes

class ResourceProvider(private var resources: Resources) {

    /**
     * Provide getString to ViewModel and etc...
     */
    fun getString(@StringRes stringResId: Int): String {
        return resources.getString(stringResId)
    }
}