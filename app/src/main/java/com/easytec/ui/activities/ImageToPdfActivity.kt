package com.easytec.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.Image
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.easytec.R
import com.easytec.databinding.ActivityImageToPdfBinding
import com.easytec.utils.Utils
import com.easytec.utils.Utils.PICK_IMAGE_REQUEST
import com.easytec.utils.Utils.refreshMediaScanner
import com.easytec.utils.Utils.selectMedia
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.utils.PdfMerger
import com.kerols.pdfconverter.ImageToPdf
import com.kerols.pdfconverter.PdfImageSetting
import com.kerols.pdfconverter.*
import com.shockwave.pdfium.PdfDocument
import java.io.File


class ImageToPdfActivity : AppCompatActivity() {
    lateinit var binding:ActivityImageToPdfBinding
    lateinit var imageToPdf: ImageToPdf
    lateinit var pdfPage: PdfPage
    lateinit var pdfImageSetting:PdfImageSetting
    lateinit var data:Intent
    var fileName=""
    var pdfPath=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this@ImageToPdfActivity,R.layout.activity_image_to_pdf)
        Utils.statusBarColor(this)

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        binding.selectBtn.setOnClickListener {
            selectMedia(this@ImageToPdfActivity,"image/*",Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI,PICK_IMAGE_REQUEST)
            imageToPdfConverter()
        }

        binding.convertBtn.setOnClickListener {
            if (::data.isInitialized){
                showCustomDialog()
            }else{
                Toast.makeText(this@ImageToPdfActivity, "Please Select The Image.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.shareImg.setOnClickListener {
            val file = File(pdfPath)
            val fileUri: Uri = FileProvider.getUriForFile(this@ImageToPdfActivity, "com.easytec.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            startActivity(Intent.createChooser(shareIntent, "Share PDF"))
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val selectedImageUri: Uri? = data.data
                binding.img.setImageURI(selectedImageUri)
                this.data=data

            }
        }
    }



    fun imageToPdfConverter(){
        pdfPage = PdfPage(this@ImageToPdfActivity)
        val pageSize = 1100
        pdfPage.setPageSize(pageSize, pageSize)

        val pdfImageSetting = PdfImageSetting()
        pdfImageSetting.setImageSize(InSize.IMAGE_SIZE)

        val imageWidth = pdfImageSetting.imageWidth
        val imageHeight = pdfImageSetting.imageHeight
        val leftMargin = (pageSize - imageWidth) / 2
        val bottomMargin = (pageSize - imageHeight) / 2

        pdfImageSetting.setMargin(leftMargin, bottomMargin, 0,0)
        pdfPage.add(pdfImageSetting)
        imageToPdf = ImageToPdf(pdfPage, this@ImageToPdfActivity)

    }

    fun conversion(){
        imageToPdf.DataToPDF(data, File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath,"${fileName}.pdf"),object:CallBacks{
            override fun onFinish(path: String?) {
                Toast.makeText(this@ImageToPdfActivity, "Converted Successfully.", Toast.LENGTH_SHORT).show()
                binding.shareImg.visibility= View.VISIBLE
                pdfPath=path!!
                refreshMediaScanner(this@ImageToPdfActivity,pdfPath)
            }
            override fun onError(throwable: Throwable?) {
                Toast.makeText(this@ImageToPdfActivity, "$throwable", Toast.LENGTH_SHORT).show()
            }

            @SuppressLint("SetTextI18n")
            override fun onProgress(progress: Int, max: Int) {
                binding.pd.max=max
                binding.pd.progress=progress
                binding.pdValue.text = "${(progress * 100 / max)}%"
            }

            override fun onCancel() {
                Toast.makeText(this@ImageToPdfActivity, "Cancel.", Toast.LENGTH_SHORT).show()
            }
            override fun onStart() {
                Toast.makeText(this@ImageToPdfActivity, "Starting...", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCustomDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.filename_dialog, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()

        val fileNameEdt: EditText = view.findViewById(R.id.fileNameEdt)
        val cancelBtn: Button = view.findViewById(R.id.cancel_button)
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        val createBtn:Button=view.findViewById(R.id.add_button)
        createBtn.setOnClickListener {
            if (fileNameEdt.text.isNotEmpty()){
                fileName = fileNameEdt.text.toString().trim().replace("\\s+".toRegex(), "").lowercase()
                conversion()
                dialog.dismiss()
            }else{
                Toast.makeText(this@ImageToPdfActivity, "Please Enter FileName", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }

}