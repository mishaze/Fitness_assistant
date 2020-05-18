package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
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
import androidx.core.app.NotificationCompat
import com.example.myapplication.TimeListActivity
import com.example.myapplication.MyApp.Companion.context
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

@Suppress("SENSELESS_COMPARISON")
class MainActivity : AppCompatActivity() {
    companion object {
        private const val SECOND_KEY = "Seconds"
        private const val TIME_LIST_KEY = "TimeList"
    }

    private lateinit var mLocationRequest: LocationRequest
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 2000
    private val FASTEST_INTERVAL: Long = 1000
    private var lastLocation: Location? = null
    private val REQUEST_PERMISSION_LOCATION = 10
    private var secondTime = 0
    private var isRunning = false
    private var timeList: ArrayList<String>? = null
    private var txtChronometer: TextView? = null
    private var txtDistanc: TextView? = null
    private var totalDistance: Long = 0L



    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saveList()
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback,
            Looper.myLooper())
    }

    private val mLocationCallback :LocationCallback = object : LocationCallback() {
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
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }

    override fun onStart() {
        super.onStart()
        //verifyInitTime()
    }

    override fun onPause() {
        super.onPause()
        if (isRunning)
            isRunning = false
    }

    override fun onResume() {
        super.onResume()
        //verifyInitTime()
        //verifyIntent()
        restartChronometer()
    }

    override fun onStop() {
        super.onStop()
        if (isRunning)
            isRunning = false
    }

    override fun onRestart() {
        super.onRestart()
        if (!isRunning)
            isRunning = false
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
            totalDistance=0L
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
        //listIntent.putExtra(SECOND_KEY, secondTime.toString())
        //listIntent.putStringArrayListExtra(TIME_LIST_KEY, timeList)
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


                handler.postDelayed(this, 1000)
            }
        }
        )
    }

    private fun verifyInitTime() {
        if (txtChronometer!!.text != getString(R.string.time) && !isRunning)
            isRunning = true
    }

   // private fun verifyIntent() {
     //   if (intent.getStringArrayListExtra(TIME_LIST_KEY) != null && timeList == null)
       //     timeList = intent.getStringArrayListExtra(TIME_LIST_KEY)

        //if (intent.getStringExtra(SECOND_KEY) != null && secondTime == 0)
          //  secondTime = Integer.parseInt(intent.getStringExtra(SECOND_KEY))
//    }

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
        if (timeList!=null){
        timeList!!.clear()}

        context?.deleteFile("myfile")
        context?.openFileOutput("myfile", Context.MODE_PRIVATE)
        Toast.makeText(this, R.string.messageDelete, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SdCardPath")
    private fun saveTimes() {
        if (timeList == null)
            timeList = ArrayList()

        timeList!!.add("${txtChronometer!!.text}   ${String.format("%.2f",(totalDistance/1000.0))}        ${String.format("%.2f",(totalDistance/(secondTime+0.1))*(36/10))}          ${String.format("%.2f",(((((16.5 *((totalDistance/(secondTime+0.1))*(36/10)))) *65)*secondTime/60.0)/1000.0))}")


          context?.openFileOutput("myfile", Context.MODE_PRIVATE).use {
              timeList!!.forEach { i->
                  it?.write("$i\n".toByteArray())
              }
          }

        Toast.makeText(this, R.string.messageList, Toast.LENGTH_SHORT).show()
    }

    private fun getNotification(): Notification? {

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("gps_tracker", "GPS Tracker")
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            ""
        }

        val b = NotificationCompat.Builder(this, channelId)

        b.setOngoing(true)
            .setContentTitle("Currently tracking GPS location...")
            .setSmallIcon(R.mipmap.ic_launcher)

        return b.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    fun saveList(){
        if (timeList == null)
            timeList = ArrayList()

        val file1 = File(context?.filesDir, "myfile")
        val timeList1 = file1.readLines()

        timeList1.forEach(){i->timeList!!.add(i)}

    }


}
