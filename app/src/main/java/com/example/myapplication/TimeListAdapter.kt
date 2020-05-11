package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_statistic.view.*

class TimeListAdapter(
    private val timeOfUser: ArrayList<String>
): RecyclerView.Adapter<TimeListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeListAdapter.ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_statistic,parent,false))

    override fun getItemCount(): Int = timeOfUser.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(timeOfUser[position])
    }
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(text: String ){
            itemView.timeTV.text = text
        }
    }

}