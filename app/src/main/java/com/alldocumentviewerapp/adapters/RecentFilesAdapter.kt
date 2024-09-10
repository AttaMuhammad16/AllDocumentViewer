package com.alldocumentviewerapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ahmadullahpk.alldocumentreader.activity.All_Document_Reader_Activity
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.models.CacheDirModel
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.ui.activities.AllDocumentsViewActivity
import com.alldocumentviewerapp.ui.activities.rtffiles.ShowRTFFiles
import com.alldocumentviewerapp.utils.Utils
import com.alldocumentviewerapp.utils.Utils.getFileExtension
import com.alldocumentviewerapp.utils.Utils.openFileWithOtherApps
import com.alldocumentviewerapp.utils.Utils.rippleEffect
import com.alldocumentviewerapp.utils.Utils.shareFileWithOthers
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
            rippleEffect(context, it)
        }

        val fileExtension = getFileExtension(data.fileName)
        val iconResource = Utils.getFileIconResource(fileExtension)
        holder.fileIcon.setImageResource(iconResource)

        holder.itemView.setOnClickListener {
            when (getFileExtension(data.fileName)) {
                ".zip" -> {
//                    openActivity(context, UnZipActivity::class.java, data)
                    openFileWithOtherApps(context, data.path)
                }
                ".rar" -> {
                    openFileWithOtherApps(context, data.path)
                }
                ".rtf"->{
                    val intent = Intent(context, ShowRTFFiles::class.java)
                    intent.putExtra("path", data.path)
                    intent.putExtra("name", data.fileName)
                    intent.putExtra("fromAppActivity", true)
                    context.startActivity(intent)
                }
                ".doc",".docx",".xls",".xlsx",".ppt",".pptx",".csv",".json",".html",".xml",".txt",".kt"->{
                    val intent = Intent(context, All_Document_Reader_Activity::class.java)
                    intent.putExtra("path", data.path)
                    intent.putExtra("name", data.fileName)
                    intent.putExtra("fromAppActivity", true)
                    context.startActivity(intent)
                }
                ".pdf"->{
                    openActivity(context, AllDocumentsViewActivity::class.java, data)
                }
            }
        }

        holder.drop_down_arrow.setOnClickListener {
            rippleEffect(context, it)
            showPopupMenu(context, it, data, list.toMutableList())
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun saveCacheData(data: CacheDirModel) {
        GlobalScope.launch(Dispatchers.IO) {
            val cacheDir = CacheDirModel(data.path, data.fileName, data.fileSize, data.dateTime, data.type)
            val fileName = "${data.fileName}.json"
            val file = File(context.cacheDir, fileName)
            file.writeText(cacheDir.toJson())
        }
    }

    private fun openActivity(context : Context, activityClass: Class<*>, data: CacheDirModel) {
        var fileModel= TotalFilesModel(uri = null,path=data.path, fileName = data.fileName, fileSize = data.fileSize, dateTime = data.dateTime, type = data.type)
        val intent = Intent(context, activityClass)
        intent.putExtra("document",fileModel)
        context.startActivity(intent)
    }

    private fun showPopupMenu(context: Context, view: View, data: CacheDirModel, list: MutableList<CacheDirModel>) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.share_file_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.shareApp -> {
                    shareFileWithOthers(context, File(data.path))
                    true
                }
                R.id.delete -> {
                    deleteFile(context, data, list)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun deleteFile(context: Context, data: CacheDirModel, list: MutableList<CacheDirModel>) {
        val fileToDelete = File(data.path)
        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                Utils.refreshMediaScanner(context, fileToDelete.path)
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
    }


}