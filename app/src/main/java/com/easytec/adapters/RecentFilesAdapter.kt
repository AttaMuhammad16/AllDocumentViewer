package com.easytec.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.easytec.R
import com.easytec.models.CacheDirModel
import com.easytec.models.TotalFilesModel
import com.easytec.ui.activities.AllDocumentsViewActivity
import com.easytec.ui.activities.UnRarActivity
import com.easytec.ui.activities.UnZipActivity
import com.easytec.utils.Utils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class RecentFilesAdapter(var list: List<CacheDirModel>, var context: Context) : RecyclerView.Adapter<RecentFilesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fileName = itemView.findViewById<TextView>(R.id.fileName)
        var fileIcon = itemView.findViewById<ImageView>(R.id.fileIcon)
        var fileSize = itemView.findViewById<TextView>(R.id.fileSize)
        var date = itemView.findViewById<TextView>(R.id.date)
        var drop_down_arrow = itemView.findViewById<ImageView>(R.id.drop_down_arrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.document_item_row, parent, false)
        return ViewHolder(v)
    }

    @DelicateCoroutinesApi
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var data=list[position]
        var date= Utils.formatDateString(data.dateTime)
        var fileSize= Utils.formatFileSize(data.fileSize)

        holder.fileName.text=data.fileName
        holder.date.text=date
        holder.fileSize.text=fileSize
        holder.drop_down_arrow.setOnClickListener {
            Utils.rippleEffect(context, it)
        }

        val fileExtension = Utils.getFileExtension(data.fileName)
        val iconResource = Utils.getFileIconResource(fileExtension)
        holder.fileIcon.setImageResource(iconResource)

        holder.itemView.setOnClickListener {
            val intent: Intent
            if (Utils.getFileExtension(data.fileName) ==".zip"){
                intent= Intent(context, UnZipActivity::class.java)
            }else if(Utils.getFileExtension(data.fileName) ==".rar"){
                intent= Intent(context, UnRarActivity::class.java)
            } else{
                intent= Intent(context, AllDocumentsViewActivity::class.java)
            }

            var totalFileModel=TotalFilesModel(path = data.path, fileName = data.fileName, fileSize = data.fileSize, dateTime = data.dateTime, type = data.type)

            intent.putExtra("document",totalFileModel)
            context.startActivity(intent)

            GlobalScope.launch(Dispatchers.IO) {
                var cacheDir= CacheDirModel(data.path,data.fileName,data.fileSize,data.dateTime,data.type)
                val fileName = "${data.fileName}.json"
                val file = File(context.cacheDir, fileName)
                file.writeText(cacheDir.toJson())
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}