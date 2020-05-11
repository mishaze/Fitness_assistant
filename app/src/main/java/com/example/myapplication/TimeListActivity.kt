package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.support.*
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_time_list.*
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class TimeListActivity : AppCompatActivity() {

    companion object {
        private const val SECOND_KEY = "Seconds"
        private const val TIME_LIST_KEY = "TimeList"
    }

    //val list = listOf("1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23","1:00:23")
    private var listTime: ArrayList<String>? = null
    private var results: String = ""

    private lateinit var textTimeList: TextView
    private lateinit var seconds: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_list)


        setListFromIntent()
        timeRV.layoutManager = LinearLayoutManager(this)
        listTime?.reverse()
        timeRV.adapter = listTime?.let { TimeListAdapter(it) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backIntent()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                backIntent()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setListFromIntent() {
        listTime = intent.getStringArrayListExtra(TIME_LIST_KEY)
        seconds = intent.getStringExtra(SECOND_KEY)
    }

    private fun backIntent() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putStringArrayListExtra(TIME_LIST_KEY, listTime)
        intent.putExtra(SECOND_KEY, seconds)
        startActivity(intent)
    }
}
