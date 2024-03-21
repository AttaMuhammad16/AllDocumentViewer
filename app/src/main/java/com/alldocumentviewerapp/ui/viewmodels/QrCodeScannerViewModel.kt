package com.alldocumentviewerapp.ui.viewmodels

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.BarcodeDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QrCodeScannerViewModel @Inject constructor() : ViewModel() {
    private var _data: MutableLiveData<SparseArray<com.google.android.gms.vision.barcode.Barcode>> = MutableLiveData()
    val data: LiveData<SparseArray<com.google.android.gms.vision.barcode.Barcode>> = _data
    fun setBarcodeProcessor(barcodeDetector: BarcodeDetector) {
        barcodeDetector.setProcessor(object : Detector.Processor<com.google.android.gms.vision.barcode.Barcode> {
            override fun release() {}
            override fun receiveDetections(detections: Detector.Detections<com.google.android.gms.vision.barcode.Barcode>) {
                val barcodes = detections.detectedItems
                _data.postValue(barcodes)
            }
        })
    }
}
