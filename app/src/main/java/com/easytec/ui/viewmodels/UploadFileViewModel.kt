package com.easytec.ui.viewmodels

import android.app.Activity
import android.net.Uri
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.easytec.models.TotalFilesModel
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class UploadFileViewModel @Inject constructor():ViewModel() {
    private var _url: MutableLiveData<String> = MutableLiveData()
    var liveUrl: LiveData<String> = _url

    val storage= FirebaseStorage.getInstance()
    var storageRef = storage.reference

    fun uploadFile(bundle:TotalFilesModel,filePath:String,pd:ProgressBar,context:Activity){
        val fileRef = storageRef.child(bundle.fileName)
        fileRef.putFile(Uri.fromFile(File(filePath))).addOnSuccessListener { taskSnapshot ->
            fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                if (downloadUrl!=null){
                    viewModelScope.launch {
                        _url.value=downloadUrl.toString()
                        pd.visibility = View.GONE
                    }
                }else{
                    pd.visibility = View.GONE
                    Toast.makeText(context, "File does not able to access.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                pd.visibility = View.GONE
            }
        }.addOnFailureListener { exception ->
           pd.visibility = View.GONE
        }
    }

}