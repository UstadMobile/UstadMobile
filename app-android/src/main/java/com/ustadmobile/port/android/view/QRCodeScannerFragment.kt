package com.ustadmobile.port.android.view

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.themeadapter.material.MdcTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.ustadmobile.core.viewmodel.QRCodeScannerViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
private fun QRCodeScannerScreen(viewModel: QRCodeScannerViewModel) {

    QRCodeScannerScreen(
        onQRCodeDetected = viewModel::onQRCodeDetected,
    )
}

@SuppressLint("UnsafeOptInUsageError")
class BarCodeAnalyser(
    private val onBarcodeDetected: (barcodes: List<Barcode>) -> Unit,
): ImageAnalysis.Analyzer {
    private var lastAnalyzedTimeStamp = 0L

    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimeStamp >= TimeUnit.SECONDS.toMillis(1)) {
            image.image?.let { imageToAnalyze ->
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()
                val barcodeScanner = BarcodeScanning.getClient(options)
                val imageToProcess = InputImage.fromMediaImage(imageToAnalyze, image.imageInfo.rotationDegrees)

                barcodeScanner.process(imageToProcess)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            onBarcodeDetected(barcodes)
                        } else {
                            Log.d("TAG", "analyze: No barcode Scanned")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("TAG", "BarcodeAnalyser: Something went wrong $exception")
                    }
                    .addOnCompleteListener {
                        image.close()
                    }
            }
            lastAnalyzedTimeStamp = currentTimestamp
        } else {
            image.close()
        }
    }
}

@OptIn(
    ExperimentalPermissionsApi::class,
    ExperimentalMaterialApi::class,
)
@Composable
@ExperimentalGetImage
private fun QRCodeScannerScreen(
    onQRCodeDetected: (String) -> Unit = {},
    onCameraBoundaryReady: (RectF) -> Unit = {},
    onBarcodeScanningAreaReady: (RectF) -> Unit = {},
    getBarcodeImageAnalyzer: () -> Unit = {},
) {

    val barcodeResult = viewModel.scanningResult.observeAsState(ScanningResult.Initial())
    val freezeCameraPreview = viewModel.freezeCameraPreview.observeAsState(false)
    val resultBottomSheetStateModel =
        viewModel.resultBottomSheetState.observeAsState(BarcodeScannerBottomSheetState.Hidden)

    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA,
        onPermissionResult = {
            onPermissionGrantedResult.invoke(it)
        }
    )


    BarcodeScannerContentLayout(
        imageAnalyzer = getBarcodeImageAnalyzer(),
        onScanningAreaReady = {
            onBarcodeScanningAreaReady(it)
        },
        onCameraBoundaryReady = {
            onCameraBoundaryReady(it)
        },
        scanningResult = barcodeResult.value,
        freezeCameraPreview = freezeCameraPreview.value,
    )
}

@OptIn(
    ExperimentalMaterialApi::class,
)
@ExperimentalGetImage
@Composable
fun BarcodeScannerContentLayout(
    imageAnalyzer: BarcodeImageAnalyzer,
    scanningResult: ScanningResult,
    onScanningAreaReady: (RectF) -> Unit,
    onCameraBoundaryReady: (RectF) -> Unit,
    freezeCameraPreview: Boolean,
) {
    val screenWidth = remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutCoordinates ->
                screenWidth.value = layoutCoordinates.size.width
            }
    ) {
        BarcodeScannerLayout(
            imageAnalyzer = imageAnalyzer,
            onScanningAreaReady = onScanningAreaReady,
            onCameraBoundaryReady = onCameraBoundaryReady,
            scanningResult = scanningResult,
            freezeCameraPreview = freezeCameraPreview,
        )

        BarcodeScannerBottomSheetLayout(
            resultBottomSheetState = resultBottomSheetState,
            resultBottomSheetStateModel = resultBottomSheetStateModel,
            onCloseIconClicked = onBottomSheetCloseIconClicked,
        )
    }
}

@Composable
fun BarcodeScanningDecorationLayout(
    width: Int,
    height: Int,
    barcode: String = "",
    onScanningAreaReady: (String) -> Unit = {},
) {
    fun calculateScanningRect(size: Int, centerPoint: PointF): RectF {
        val scanningAreaSize = size * 0.5F
        val left = centerPoint.x - scanningAreaSize * 0.5F
        val top = centerPoint.y - scanningAreaSize * 0.4F
        val right = centerPoint.x + scanningAreaSize * 0.5F
        val bottom = centerPoint.y + scanningAreaSize * 0.3F
        return RectF(left, top, right, bottom)
    }

    val scanningAreaPath: Path by remember { mutableStateOf(Path()) }
    val cameraBoundaryPath: Path by remember { mutableStateOf(Path()) }
    val barcodeBoundaryPath: Path by remember { mutableStateOf(Path()) }

    val centerPoint = PointF(width * 0.5F, height * 0.5F)
    val scanningAreaRect = calculateScanningRect(size = minOf(width, height), centerPoint)
    val scanningFrameStrokeSize = dp2Px(dp = 4.dp)
    val scanningFrameCornerRadius = dp2Px(dp = 6.dp)

    Canvas(
        modifier = Modifier.fillMaxSize()
            .onGloballyPositioned {
                if (barcode.isNotEmpty()){
                    onScanningAreaReady.invoke(barcode)
                }
            },
        onDraw = {
            scanningAreaPath.reset()
            cameraBoundaryPath.reset()
            barcodeBoundaryPath.reset()

            // draw the area outside of scanning rectangle
            scanningAreaPath.addRect(
                Rect(
                    left = scanningAreaRect.left,
                    top = scanningAreaRect.top,
                    right = scanningAreaRect.right,
                    bottom = scanningAreaRect.bottom,
                )
            )
            cameraBoundaryPath.addRect(Rect(Offset.Zero, Offset(width.toFloat(), height.toFloat())))
            drawPath(
                path = Path.combine(operation = PathOperation.Xor, scanningAreaPath, cameraBoundaryPath),
                color = Color.Black,
                alpha = 0.5F
            )

            // draw the scanning area frame
            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(x = scanningAreaRect.left, y = scanningAreaRect.top),
                size = Size(width = scanningAreaRect.width(), height = scanningAreaRect.height()),
                alpha = 0.8F,
                style = Stroke(
                    join = StrokeJoin.Round,
                    width = scanningFrameStrokeSize
                ),
                cornerRadius = CornerRadius(x = scanningFrameCornerRadius, y = scanningFrameCornerRadius)
            )

        }
    )
}

@Composable
fun dp2Px(dp: Dp) = with(LocalDensity.current) { dp.toPx() }

@Composable
@Preview
fun QRCodeScannerScreenPreview() {

    QRCodeScannerScreen()
}