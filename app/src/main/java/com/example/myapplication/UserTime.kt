package com.example.myapplication

import java.util.ArrayList

class UserTime {
    private var timeList: List<String>? = null

    private fun getUsertime(): List<String>? {
        return timeList}

    private fun setUsertime(timeListUser: List<String>? ){
    timeList = timeListUser
    }
}
