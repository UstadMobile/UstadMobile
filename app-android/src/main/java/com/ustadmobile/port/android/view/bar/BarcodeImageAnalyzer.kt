package com.ustadmobile.port.android.view.bar

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@ExperimentalGetImage
class BarcodeImageAnalyzer: ImageAnalysis.Analyzer {

    private val executor = Executors.newSingleThreadExecutor()
    private var processing = false
    private var processListener: ProcessListener? = null

    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image ?: return
        imageProxy.image?.let { imageToAnalyze ->
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
            val barcodeScanner = BarcodeScanning.getClient(options)
            val imageToProcess = InputImage.fromMediaImage(imageToAnalyze, imageProxy.imageInfo.rotationDegrees)

            if (processing) return
            processing = true
            barcodeScanner.process(imageToProcess)
                .addOnSuccessListener { results ->
                    processListener?.onSucceed(results, imageToProcess)
                }
                .addOnCanceledListener {
                    processListener?.onCanceled()
                }
                .addOnFailureListener {
                    processListener?.onFailed(it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                    processListener?.onCompleted()
                    processing = false
                }
        }
    }

    fun setProcessListener(listener: ProcessListener) {
        processListener = listener
    }

    abstract class ProcessListenerAdapter : ProcessListener {
        override fun onSucceed(results: List<Barcode>, inputImage: InputImage) {}

        override fun onCanceled() {}

        override fun onCompleted() {}

        override fun onFailed(exception: Exception) {}
    }

    interface ProcessListener {
        fun onSucceed(results: List<Barcode>, inputImage: InputImage)
        fun onCanceled()
        fun onCompleted()
        fun onFailed(exception: Exception)
    }
}
