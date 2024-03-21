package com.alldocumentviewerapp.adapters

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.models.UnZipModel
import com.alldocumentviewerapp.utils.Utils
import java.io.File

class UnZipAdapter(var list:ArrayList<UnZipModel>,var context:Context):RecyclerView.Adapter<UnZipAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fileIcon=itemView.findViewById<ImageView>(R.id.fileIcon)
        var fileName=itemView.findViewById<TextView>(R.id.fileName)
        var fileSize=itemView.findViewById<TextView>(R.id.fileSize)
        var directory=itemView.findViewById<TextView>(R.id.directory)
        var linearLayout=itemView.findViewById<LinearLayout>(R.id.linearLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
       val inflater=LayoutInflater.from(context).inflate(R.layout.unzip_item_row,parent,false)
       return MyViewHolder(inflater)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
       var data=list[position]
        if (data.isDirectory){
//            holder.fileIcon.visibility=View.GONE
//            holder.directory.visibility=View.VISIBLE

//            holder.fileIcon.setImageResource(R.drawable.folder)
//            holder.fileName.text=data.name
//            holder.directory.text="Directory"

        }else{

            holder.fileIcon.visibility=View.VISIBLE
            holder.linearLayout.visibility=View.VISIBLE
            holder.directory.visibility=View.GONE

            holder.fileIcon.setImageResource(R.drawable.file)
            holder.fileName.text=data.name
            holder.fileSize.text= Utils.formatFileSize(data.fileSize)

            holder.itemView.setOnClickListener {

                val file = File(data.path)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val chooserIntent = Intent.createChooser(intent, "Open with")

                try {
                    context.startActivity(chooserIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No app installed to open this file", Toast.LENGTH_SHORT).show()
                }


            }

        }
    }

    override fun getItemCount(): Int {
       return list.size
    }

}