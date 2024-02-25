// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("lifecycle_version", "2.7.0")
        set("navigation_compose_version", "2.7.7")
        set("arch_version", "2.2.0")
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

    id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
    id("com.google.firebase.crashlytics") version "2.9.1" apply false

}