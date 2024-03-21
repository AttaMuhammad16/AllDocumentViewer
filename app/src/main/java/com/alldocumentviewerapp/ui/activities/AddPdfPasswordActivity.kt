package com.alldocumentviewerapp.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.databinding.ActivityAddPdfPasswordBinding
import com.alldocumentviewerapp.utils.Utils
import com.itextpdf.kernel.pdf.EncryptionConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import java.io.File
import java.io.FileOutputStream


class AddPdfPasswordActivity : AppCompatActivity() {
    lateinit var binding:ActivityAddPdfPasswordBinding
    val REQUEST_CODE_SELECT_PDF=12
    lateinit var uri:Uri
    lateinit var newFilePath:File

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this@AddPdfPasswordActivity,R.layout.activity_add_pdf_password)
        Utils.statusBarColor(this)

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        binding.selectPdfFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/pdf"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(Intent.createChooser(intent, "Select PDF"), REQUEST_CODE_SELECT_PDF)
        }

        binding.shareImg.setOnClickListener {
            val fileUri: Uri = FileProvider.getUriForFile(this@AddPdfPasswordActivity, "com.alldocumentviewerapp.fileprovider", newFilePath)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            startActivity(Intent.createChooser(shareIntent, "Share PDF"))
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_PDF && resultCode == Activity.RESULT_OK) {
            uri = data?.data?: Uri.parse("")
            if (uri.toString().isNotEmpty()){
                showCustomDialog()
            }else{
                Toast.makeText(this@AddPdfPasswordActivity, "Select File", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addPasswordToPdfAndCreateNew(context: Context, sourceUri: Uri, password: String, newFileName: String) {
        try {

            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val finalFileName = if (!newFileName.endsWith(".pdf")) "$newFileName.pdf" else newFileName
            newFilePath = File(directory, finalFileName)
            FileOutputStream(newFilePath).use { fileOutputStream ->
                PdfReader(inputStream).use { reader ->
                    PdfWriter(fileOutputStream, WriterProperties().setStandardEncryption(
                        password.toByteArray(),
                        null,
                        EncryptionConstants.ALLOW_PRINTING,
                        EncryptionConstants.ENCRYPTION_AES_128
                    )).use { writer ->
                        PdfDocument(reader, writer).use {
                        }
                    }
                }
            }
            Utils.refreshMediaScanner(context, newFilePath.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun showCustomDialog() {

        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.set_password_dialog, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

        val pathTv: TextView = view.findViewById(R.id.pathTv)
        val fileNameEdt: EditText = view.findViewById(R.id.fileNameEdt)
        val passwordEdt: EditText = view.findViewById(R.id.passwordEdt)
        val cancelBtn: Button = view.findViewById(R.id.cancel_button)
        val okBtn: Button =view.findViewById(R.id.okBtn)

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        pathTv.text="File Saved:$directory"

        okBtn.setOnClickListener {
            var pass=passwordEdt.text.toString()
            var fileName=fileNameEdt.text.toString().trim().replace("\\s+".toRegex(), "").replace("[\\\\/:*?\"<>|]".toRegex(), "").lowercase()

            if (pass.isNotEmpty()&&fileName.isNotEmpty()){
                addPasswordToPdfAndCreateNew(this@AddPdfPasswordActivity,uri,pass,fileName)
                binding.shareImg.visibility=View.VISIBLE
                dialog.dismiss()
                Toast.makeText(this@AddPdfPasswordActivity, "File Saved ${newFilePath.absolutePath}", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@AddPdfPasswordActivity, "Enter Password or Name", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }



}