package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context


@SuppressLint("Registered")
class MyApp : Application() {
    override fun onCreate() {
        instance = this
        super.onCreate()
    }

    companion object {
        private var instance: MyApp? = null

        val context: Context?
            get() = instance
    }
}