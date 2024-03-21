package com.alldocumentviewerapp.ui.activities.barcodescanner

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.ui.viewmodels.QrCodeScannerViewModel
import com.alldocumentviewerapp.utils.Utils
import com.alldocumentviewerapp.utils.Utils.REQUEST_CAMERA_PERMISSION
import com.alldocumentviewerapp.utils.Utils.copyToClipboard
import com.alldocumentviewerapp.utils.Utils.handleCameraPermissionResult
import com.alldocumentviewerapp.utils.Utils.isValidURL
import com.alldocumentviewerapp.utils.Utils.myToast
import com.alldocumentviewerapp.utils.Utils.openInChrome
import com.alldocumentviewerapp.utils.Utils.shareText
import com.alldocumentviewerapp.utils.Utils.startCameraSource
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

class BarCodeScanner : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var tvBarcodeValue: TextView
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private lateinit var edt: EditText
    private lateinit var search_btn: Button
    private lateinit var copyBtn: Button
    private lateinit var shareBtn: Button
    var url1: String = ""
    val qrCodeScannerViewModel: QrCodeScannerViewModel by viewModels()
    lateinit var backArrowImg: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar_code_scanner)
        Utils.statusBarColor(this)
        var  backArrowImg = findViewById<ImageView>(R.id.backArrowImg)
        surfaceView = findViewById(R.id.surface_view)
        tvBarcodeValue = findViewById(R.id.tv_barcode_value)
        edt = findViewById(R.id.edt)
        search_btn = findViewById(R.id.search_btn)
        shareBtn = findViewById(R.id.shareBtn)
        copyBtn = findViewById(R.id.copy)
        surfaceView.holder.addCallback(this)

        backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, backArrowImg) {
                onBackPressed()
            }
        }

        barcodeDetector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()
        cameraSource = CameraSource.Builder(this, barcodeDetector).setRequestedPreviewSize(1920, 1080).setAutoFocusEnabled(true).build()

        qrCodeScannerViewModel.setBarcodeProcessor(barcodeDetector)
        qrCodeScannerViewModel.data.observe(this, Observer { barcodes ->
            if (barcodes.size() > 0) {
                edt.setText(barcodes.valueAt(0)?.displayValue.toString())
                url1 = barcodes.valueAt(0)?.displayValue.toString()
            }
        })

        search_btn.setOnClickListener {
            var a = isValidURL(url1)
            if (a) {
                openInChrome(this, url1)
            } else {
                myToast(this, "not valid", Toast.LENGTH_SHORT)
            }
        }

        shareBtn.setOnClickListener {
            if (edt.text.toString().isNotEmpty()) {
                var text = edt.text.toString()
                shareText(this, "Must Tools App", text)
            } else {
                myToast(this, "Please Enter Your Text", Toast.LENGTH_SHORT)
            }
        }

        copyBtn.setOnClickListener {
            if (edt.text.toString().isNotEmpty()) {
                var t = edt.text.toString()
                copyToClipboard(this, t)
                myToast(this, "Text Copied", Toast.LENGTH_SHORT)
            } else {
                myToast(this, "Not Valid", Toast.LENGTH_SHORT)
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        cameraSource.stop()
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraSource(cameraSource, this, surfaceView)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handleCameraPermissionResult(
            requestCode,
            grantResults,
            onPermissionGranted = {
                startCameraSource(cameraSource, this, surfaceView)
            },
            onPermissionDenied = {
                myToast(this, "Camera permission denied!", Toast.LENGTH_SHORT)
            }
        )
    }

}