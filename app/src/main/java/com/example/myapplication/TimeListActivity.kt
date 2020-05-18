package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.support.*
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.MyApp.Companion.context
import kotlinx.android.synthetic.main.activity_time_list.*
import java.io.File
import java.io.FileInputStream
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class TimeListActivity : AppCompatActivity() {

    companion object {
        private const val SECOND_KEY = "Seconds"
        private const val TIME_LIST_KEY = "TimeList"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_list)

        //setListFromIntent()
        val file = File(context?.filesDir, "myfile")
        val timeList = file.readLines()

        timeRV.layoutManager = LinearLayoutManager(this)
        timeRV.adapter = TimeListAdapter(timeList.reversed())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}


