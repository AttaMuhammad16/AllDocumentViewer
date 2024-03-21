package com.easytec.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.easytec.ui.activities.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class PermissionHelper(val context: Activity, private val requestPermissionLauncher: ActivityResultLauncher<String>) {

    fun showAndroid11PlusPermissionDialog(context: Activity) {
        val intent = Intent().apply {
            action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    fun requestReadExternalStoragePermission(context: Activity) {
        when {
            checkPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
//                        Toast.makeText(context, "Permission already granted", Toast.LENGTH_SHORT).show()
                    } else {
//                        showAndroid11PlusPermissionDialog(context)
                    }
                } else {
//                    Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
                }
            }else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    fun checkPermission(context: Activity, permission: String) = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun checkPermissionIsGrantedOrNot(context: Activity):Boolean {
        val readPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        val writePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE

        val readPermissionGranted = checkPermission(context, readPermission)
        val writePermissionGranted = checkPermission(context, writePermission)

        if (!readPermissionGranted || !writePermissionGranted) {
            if (shouldShowRequestPermissionRationale(context, readPermission)) {
                showRationaleDialog(context)
                return false
            } else {
                return true
            }
        } else {
            return true
        }
    }

    fun showRationaleDialog(context: Activity) {
        AlertDialog.Builder(context).setTitle("Permission Required").setMessage("We need the permission for app functionality.").setPositiveButton("OK") { _, _ ->
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
//            MainActivity().showViews()
        }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
        }.create().show()

    }
}