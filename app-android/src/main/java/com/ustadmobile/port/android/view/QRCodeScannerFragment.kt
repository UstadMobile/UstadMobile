package com.ustadmobile.port.android.view

import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.ustadmobile.core.viewmodel.QRCodeScannerViewModel
import com.ustadmobile.port.android.view.bar.BarcodeImageAnalyzer
import com.ustadmobile.port.android.view.bar.BarcodeResultBoundaryAnalyzer
import com.ustadmobile.port.android.view.bar.BarcodeScanningDecorationLayout
import com.ustadmobile.port.android.view.bar.model.BarCodeResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QRCodeScannerFragment: UstadBaseMvvmFragment(){

    private val viewModel: QRCodeScannerViewModel by ustadViewModels(::QRCodeScannerViewModel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    QRCodeScannerScreen(viewModel)
                }
            }
        }
    }
}

@Composable
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun QRCodeScannerScreen(viewModel: QRCodeScannerViewModel) {

    BarcodeScannerScreen(
        onQRCodeDetected = viewModel::onQRCodeDetected
    )
}

@Composable
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
fun BarcodeScannerScreen(
    onQRCodeDetected: (String) -> Unit = {}
) {

    val barcodeResultBoundaryAnalyzer = BarcodeResultBoundaryAnalyzer()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var preview by remember { mutableStateOf<androidx.camera.core.Preview?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val width = remember { mutableStateOf(0) }
        val height = remember { mutableStateOf(0) }
        AndroidView(
            factory = { AndroidViewContext ->
                PreviewView(AndroidViewContext).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize().onGloballyPositioned { layoutCoordinates ->
                width.value = layoutCoordinates.size.width
                height.value = layoutCoordinates.size.height
                barcodeResultBoundaryAnalyzer.onCameraBoundaryReady(
                    RectF(
                        0F,
                        0F,
                        width.value.toFloat(),
                        height.value.toFloat()
                    )
                )
            },
            update = { previewView ->
                val cameraSelector: CameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
                val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                    ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    preview = androidx.camera.core.Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                    val barcodeAnalyser = BarcodeImageAnalyzer()
                    barcodeAnalyser.setProcessListener(
                        listener = object : BarcodeImageAnalyzer.ProcessListenerAdapter() {
                            override fun onSucceed(results: List<Barcode>, inputImage: InputImage) {
                                super.onSucceed(results, inputImage)
                                if (results.isNotEmpty()){
                                    val scanningResult = barcodeResultBoundaryAnalyzer.analyze(results, inputImage)
                                    if (scanningResult.message.equals("PerfectMatch")) {
                                        scanningResult.barCode?.rawValue?.let { onQRCodeDetected(it) }
                                    }
                                }

                            }
                        }
                    )
                    val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, barcodeAnalyser)
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.d("TAG", "CameraPreview: ${e.localizedMessage}")
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        BarcodeScanningDecorationLayout(
            width = width.value,
            height = height.value,
            onScanningAreaReady = {
                barcodeResultBoundaryAnalyzer.onBarcodeScanningAreaReady(it)
            },
            scanningResult = BarCodeResult()
        )
    }
}


@Composable
fun dp2Px(dp: Dp) = with(LocalDensity.current) { dp.toPx() }

@Composable
@Preview
fun QRCodeScannerScreenPreview() {

    BarcodeScannerScreen()
}