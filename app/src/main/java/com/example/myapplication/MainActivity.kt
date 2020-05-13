package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.MyApp.Companion.context
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Double.NaN
import java.lang.Math.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

object DistanceTracker {
    var totalDistance: Long = 0L
}

class MainActivity : AppCompatActivity() {

    companion object {
        private const val SECOND_KEY = "Seconds"
        private const val TIME_LIST_KEY = "TimeList"
    }

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 2000
    private val FASTEST_INTERVAL: Long = 1000

    lateinit var firstLocation: Location
    private var lastLocation: Location? = null

    internal lateinit var mLocationRequest: LocationRequest
    private val REQUEST_PERMISSION_LOCATION = 10

    private var secondTime = 0
    private var isRunning = false
    private var timeList: ArrayList<String>? = null
    private var txtChronometer: TextView? = null
    private var txtDistanc: TextView? = null

    var totalDistance: Long = 0L

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mLocationRequest = LocationRequest()

        txtDistanc = findViewById(R.id.distance)
        txtChronometer = findViewById(R.id.txtChrono)

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }

        runChronometer()
    }


    private fun buildAlertMessageNoGps() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    , 11)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.cancel()
                finish()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

     fun startLocationUpdates() {

        // Create the location request to start receiving updates

        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.setInterval(INTERVAL)
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL)

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback,
            Looper.myLooper())
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            // do work here
            //locationResult.lastLocation
            result?.let {

                if(lastLocation == null){
                    lastLocation = it.lastLocation
                    return@let
                }

                it.lastLocation?.let { its_last ->

                    val distanceInMeters = its_last.distanceTo(lastLocation)

                    totalDistance =totalDistance+ distanceInMeters.toLong()
                    txtDistanc!!.text = getString(R.string.rangeFormat,totalDistance)


                    if(BuildConfig.DEBUG){
                        //Log.d("TRACKER", "Completed: ${DistanceTracker.totalDistance} meters, (added $distanceInMeters)")
                    }

                }

                lastLocation = it.lastLocation

            }

            super.onLocationResult(result)
        }
    }




    private fun stoplocationUpdates() {
        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()

            } else {
                Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // Show the permission request
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }



    override fun onStart() {
        super.onStart()
        verifyInitTime()
    }

    override fun onPause() {
        super.onPause()
        if (isRunning)
            isRunning = false
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


    fun onClickStart(view: View) {
        if (!isRunning) {

            isRunning = true

            if (checkPermissionForLocation(this)) {
                startLocationUpdates()}
            imbStart.setText(R.string.stop)
        } else {
            isRunning = false

            stoplocationUpdates()
            saveTimes()
            secondTime = 0
            imbStart.setText(R.string.start)

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
                val minutes = secondTime % 3600 / 60
                val seconds = secondTime % 60

                txtChronometer!!.text =
                    getString(R.string.format_chronometer, hours, minutes, seconds)

                if (isRunning) {
                    secondTime++
                }

                //range=range+(6356*2*Math.asin(sqrt(pow(sin((latitude2-latitude)*
                  //     (3.1415926535/180.0)/2),2.0)+ cos(latitude*(3.1415926535/180.0))
                    //*sin(latitude2*(3.1415926535/180.0))* pow(
                   //sin((longitude2-longitude)*(3.1415926535/180.0) /2) ,2.0))))

               // txtDistanc!!.text = getString(R.string.rangeFormat,range)

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

}
