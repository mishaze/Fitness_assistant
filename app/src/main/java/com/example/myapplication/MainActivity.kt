package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Double.NaN
import java.lang.Math.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private lateinit var fusedLocationClient: FusedLocationProviderClient

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity() {

    companion object {
        private const val SECOND_KEY = "Seconds"
        private const val TIME_LIST_KEY = "TimeList"
    }

    private var secondTime = 0
    private var isRunning = false
    private var timeList: ArrayList<String>?=null
    private var txtChronometer: TextView? = null
    private var txtDistanc: TextView? = null
    private var calculatedDistance: Long = 0

    private var latitude1:Double=0.0
    private var longitude1:Double=0.0

    private var latitude2:Double=0.0
    private var longitude2:Double=0.0

    private var range:Double = 0.0

    private val userTime = UserTime()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkPermission()
        txtDistanc=findViewById(R.id.distance)
        txtChronometer = findViewById(R.id.txtChrono)
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


        runChronometer()
    }


    override fun onStart() {
        super.onStart()
        verifyInitTime()
    }

    override fun onPause() {
        super.onPause()

        if (isRunning)
            isRunning = true
    }

    override fun onResume() {
        super.onResume()

        verifyInitTime()
        verifyIntent()
        restartChronometer()
    }

    override fun onStop() {
        super.onStop()

        if (isRunning)
            isRunning = true
    }

    override fun onRestart() {
        super.onRestart()

        if (!isRunning)
            isRunning = true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.history_main)
            clearTimes()

        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "ResourceAsColor")
    fun onClickStart(view: View) {
       if( !isRunning) {
           range=0.0
           isRunning= true
           fusedLocationClient.lastLocation
               .addOnSuccessListener { location: Location? ->
                   latitude1 = location?.latitude!!
                   longitude1 = location.longitude
               }
       imbStart.setText(R.string.stop)
       }
       else{
           isRunning = false
           saveTimes()
           secondTime = 0
           imbStart.setText(R.string.start)
           stopService(this.intent)
       }
    }


    fun onActivityListChronometer(view: View) {
        val listIntent = Intent(this, TimeListActivity::class.java)
        listIntent.putExtra(SECOND_KEY, secondTime.toString())
        listIntent.putStringArrayListExtra(TIME_LIST_KEY, timeList)

        startActivity(listIntent)
    }

    private fun runChronometer() {
        val handler = Handler()

        handler.post(object : Runnable {
            @SuppressLint("StringFormatMatches", "SetTextI18n")
            override fun run() {
                val hours = secondTime / 3600
                val minutes = secondTime % 3600/60
                val seconds = secondTime % 60

                txtChronometer!!.text = getString(R.string.format_chronometer, hours, minutes, seconds)

                if (isRunning)
                    secondTime++


                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        latitude2 = location?.latitude!!
                        longitude2 = location.longitude
                    }

                range=range+(6356*2*Math.asin(sqrt(pow(sin((latitude2-latitude1)*
                        (3.1415926535/180.0)/2),2.0)+ cos(latitude1*(3.1415926535/180.0))
                        *sin(latitude2*(3.1415926535/180.0))* pow(
                    sin((longitude2-longitude1)*(3.1415926535/180.0) /2) ,2.0))))



                latitude1 =latitude2
                longitude1 = longitude2

                txtDistanc!!.text = range.toString()

                handler.postDelayed(this, 1000)
            }
        }
        )
    }

    private fun verifyInitTime() {
        if (txtChronometer!!.text != getString(R.string.time) && !isRunning)
            isRunning = true
    }

    private fun verifyIntent() {
        if (intent.getStringArrayListExtra(TIME_LIST_KEY) != null && timeList == null)
            timeList = intent.getStringArrayListExtra(TIME_LIST_KEY)

        if (intent.getStringExtra(SECOND_KEY) != null && secondTime == 0)
            secondTime = Integer.parseInt(intent.getStringExtra(SECOND_KEY))
    }

    private fun restartChronometer() {
        if (secondTime > 0) {
            var hours = 0
            var minutes = 0
            var seconds = 0

            for (i in 0..secondTime) {
                hours = secondTime / 3600
                minutes = secondTime % 3600 / 100
                seconds = secondTime % 100
            }

            txtChronometer!!.text = getString(R.string.format_chronometer, hours, minutes, seconds)
            isRunning = true
        }
    }

    private fun clearTimes() {
        timeList!!.clear()
        Toast.makeText(this, R.string.messageDelete, Toast.LENGTH_SHORT).show()
    }

    private fun saveTimes() {
        if (timeList == null)
            timeList = ArrayList()

            timeList!!.add(txtChronometer!!.text.toString())

            Toast.makeText(this, R.string.messageList, Toast.LENGTH_SHORT).show()


    }


    private fun checkPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 200)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
}
