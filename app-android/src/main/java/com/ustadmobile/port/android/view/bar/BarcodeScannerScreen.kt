package com.ustadmobile.port.android.view.bar

import android.Manifest
import android.graphics.RectF
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.ustadmobile.port.android.view.bar.model.BarCodeResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(
    ExperimentalPermissionsApi::class,
)
@Composable
@ExperimentalGetImage


private fun handleBarcodeResults(
    results: List<Barcode>,
    inputImage: InputImage,
    barcodeResultBoundaryAnalyzer: BarcodeResultBoundaryAnalyzer
) {

    val scanningResult = barcodeResultBoundaryAnalyzer.analyze(results, inputImage)
    if (scanningResult.message.equals("PerfectMatch")) {
        loadProductDetailsWithBarcodeResult(scanningResult)
    }
//    viewModelScope.launch(
//        CoroutineExceptionHandler { _, exception ->
//            notifyErrorResult(exception)
//            Log.e("TAG", "$exception")
//        }
//    ) {
//        if (isResultBottomSheetShowing) {
//            // skip the analyzer process if the result bottom sheet is showing
//            return@launch
//        }
//
//        val scanningResult = barcodeResultBoundaryAnalyzer.analyze(results, inputImage)
//        _scanningResult.value = scanningResult
//        if (scanningResult is ScanningResult.PerfectMatch) {
//            loadProductDetailsWithBarcodeResult(scanningResult)
//        }
//    }
}

private fun notifyErrorResult(exception: Throwable) {
//        _resultBottomSheetState.value =
//            BarcodeScannerBottomSheetState.Error.Generic
}

private fun loadProductDetailsWithBarcodeResult(scanningResult: BarCodeResult) {
    val productCode = scanningResult.barCode?.displayValue
    if (productCode != null) {
//        loadingProductCode = productCode
//        isResultBottomSheetShowing = true
//        // mock API call to fetch information with barcode result
//        delay(2000)
        println("Result is fghjhf  ${scanningResult.barCode} and position ${scanningResult.globalPosition}")

//        this.barcodeResult = scanningResult.barCodeResult
    } else {
        // Show Error Information
    }
}
