package com.alldocumentviewerapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.models.Videos
import com.alldocumentviewerapp.ui.activities.VideoPlayerActivity

class GetVideosAdapter(var list: ArrayList<Videos>, var context: Context) : RecyclerView.Adapter<GetVideosAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fileName = itemView.findViewById<TextView>(R.id.fileName)
        var sizeDateLinear = itemView.findViewById<LinearLayout>(R.id.sizeDateLinear)
        var fileIcon = itemView.findViewById<ImageView>(R.id.fileIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.document_item_row, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data=list[position]
        holder.fileIcon.setImageResource(R.drawable.video)
        holder.sizeDateLinear.visibility=View.GONE
        holder.fileName.text=data.videoTitle

        holder.itemView.setOnClickListener {
            val intent=Intent(context,VideoPlayerActivity::class.java)
            intent.putExtra("data",data)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}