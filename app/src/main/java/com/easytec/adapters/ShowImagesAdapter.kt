package com.easytec.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.easytec.R
import com.easytec.models.ImageFolder
import com.easytec.ui.activities.FullImageViewActivity
import com.easytec.ui.activities.ShowImagesActivity

class ShowImagesAdapter(var list: ArrayList<Uri>, var context: Context) : RecyclerView.Adapter<ShowImagesAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var images = itemView.findViewById<ImageView>(R.id.images)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.show_images_item, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var data=list[position]
        Glide.with(context).load(data).placeholder(R.drawable.img).into(holder.images);
        holder.itemView.setOnClickListener {
            var intent=Intent(context,FullImageViewActivity::class.java)
            intent.putExtra("imgUri",data.toString())
            context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int {
        return list.size
    }
}