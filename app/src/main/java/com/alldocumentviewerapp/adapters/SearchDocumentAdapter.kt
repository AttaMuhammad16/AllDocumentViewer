package com.alldocumentviewerapp.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.models.CacheDirModel
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.ui.activities.AllDocumentsViewActivity
import com.alldocumentviewerapp.ui.activities.UnRarActivity
import com.alldocumentviewerapp.ui.activities.UnZipActivity
import com.alldocumentviewerapp.utils.Utils
import com.alldocumentviewerapp.utils.Utils.formatDateString
import com.alldocumentviewerapp.utils.Utils.formatFileSize
import com.alldocumentviewerapp.utils.Utils.getFileExtension
import com.alldocumentviewerapp.utils.Utils.getFileIconResource
import com.alldocumentviewerapp.utils.Utils.rippleEffect
import com.alldocumentviewerapp.utils.Utils.shareFileWithOthers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class SearchDocumentAdapter(var list: ArrayList<TotalFilesModel>, var context: Activity) : RecyclerView.Adapter<SearchDocumentAdapter.ViewHolder>() {

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
        val data=list[position]
        val date=formatDateString(data.dateTime)
        val fileSize=formatFileSize(data.fileSize)

        holder.fileName.text=data.fileName
        holder.date.text=date
        holder.fileSize.text=fileSize
        holder.fileName.isSelected=true

        val fileExtension = getFileExtension(data.fileName)
        val iconResource = getFileIconResource(fileExtension)
        holder.fileIcon.setImageResource(iconResource)

        holder.itemView.setOnClickListener {
            val intent:Intent
            if (getFileExtension(data.fileName)==".zip"){
                intent=Intent(context,UnZipActivity::class.java)
            }
//            else if(getFileExtension(data.fileName)==".rar"){
//                intent=Intent(context,UnRarActivity::class.java)
//            }
            else{
                intent=Intent(context,AllDocumentsViewActivity::class.java)
            }
            intent.putExtra("document",data)
            context.startActivity(intent)

            GlobalScope.launch(Dispatchers.IO) {
                val cacheDir=CacheDirModel(data.path,data.fileName,data.fileSize,data.dateTime,data.type)
                val fileName = "${data.fileName}.json"
                val file = File(context.cacheDir, fileName)
                file.writeText(cacheDir.toJson())
            }

            holder.drop_down_arrow.setOnClickListener {
                rippleEffect(context,it)
                val popupMenu = PopupMenu(context, it)
                popupMenu.menuInflater.inflate(R.menu.share_file_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        when (item.itemId) {
                            R.id.shareApp -> {
                                shareFileWithOthers(context,File(data.path))
                                return true
                            }
                            R.id.delete -> {
                                val fileToDelete = File(data.path)
                                if (fileToDelete.exists()) {
                                    if (fileToDelete.delete()) {
                                        Utils.refreshMediaScanner(context,fileToDelete.path)
                                        val position = list.indexOf(data)
                                        if (position != -1) {
                                            list.removeAt(position)
                                            notifyItemRemoved(position)
                                        }
                                        Toast.makeText(context, "File deleted successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
                                }
                                return true
                            }
                            else -> return false

                        }
                    }
                })
                popupMenu.show()
            }
        }
    }
    override fun getItemCount(): Int {
        return list.size
    }
}