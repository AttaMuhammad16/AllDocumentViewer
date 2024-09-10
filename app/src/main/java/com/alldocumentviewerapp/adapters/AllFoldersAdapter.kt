package com.alldocumentviewerapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.models.AllFolders
import com.alldocumentviewerapp.ui.activities.AllFolderFilesViewActivity

class AllFoldersAdapter(var list: ArrayList<AllFolders>, var context: Context) : RecyclerView.Adapter<AllFoldersAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var folderName = itemView.findViewById<TextView>(R.id.fileName)
        var fileIcon = itemView.findViewById<ImageView>(R.id.fileIcon)
        var drop_down_arrow = itemView.findViewById<ImageView>(R.id.drop_down_arrow)
        var date = itemView.findViewById<TextView>(R.id.date)
        var fileSize = itemView.findViewById<TextView>(R.id.fileSize)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.document_item_row, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var data=list[position]
        holder.date.visibility=View.GONE
        holder.drop_down_arrow.visibility=View.GONE
        holder.folderName.text=data.folderName
        holder.fileIcon.setImageResource(R.drawable.folder)
        holder.fileSize.text="${data.totalFiles} items"

        holder.itemView.setOnClickListener {

           var intent=Intent(context,AllFolderFilesViewActivity::class.java)
            intent.putParcelableArrayListExtra("allFiles",data.fileModel)
            context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}