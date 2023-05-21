package com.ustadmobile.port.android.view

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
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRectF
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.ustadmobile.core.viewmodel.QRCodeScannerViewModel
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

data class BarCodeResult(
    val barCode: Barcode? = null,
    val globalPosition: RectF = RectF(), // transformed position of the barcode rectangle
    val message: String = "",
)

data class CoordinatesTransformUiModel(
    val scaleX: Float = 0F,
    val scaleY: Float = 0F,
    val offsetX: Float = 0F,
    val offsetY: Float = 0F,
)


class BarcodeResultBoundaryAnalyzer {

    private var barcodeScanningAreaRect: RectF = RectF()
    private var cameraBoundaryRect: RectF = RectF()
    private val barcodeSizeRateThreshold = 0.2F

    fun onBarcodeScanningAreaReady(scanningArea: RectF) {
        barcodeScanningAreaRect = scanningArea
    }

    fun onCameraBoundaryReady(cameraBoundary: RectF) {
        cameraBoundaryRect = cameraBoundary
    }

    fun analyze(results: List<Barcode>, inputImage: InputImage): BarCodeResult {
        results.forEach { barcode ->
            val transformInfo = getTransformInfo(
                cameraBoundary = cameraBoundaryRect,
                capturedImageBoundary = RectF(0F, 0F, inputImage.width.toFloat(), inputImage.height.toFloat()),
                imageRotationDegree = inputImage.rotationDegrees
            )
            val transformedPosition = transformBarcodeBoundaryToGlobalPosition(
                barcode = barcode,
                transformUiModel = transformInfo,
            )

            return generateScanningResultByBoundary(transformedPosition, barcode)
        }
        return BarCodeResult()
    }

    private fun transformBarcodeBoundaryToGlobalPosition(
        barcode: Barcode,
        transformUiModel: CoordinatesTransformUiModel,
    ): RectF {
        val barcodeBoundary = barcode.boundingBox
        return when (barcodeBoundary?.isEmpty) {
            false -> {
                RectF(
                    transformUiModel.scaleX * (barcodeBoundary.left.toFloat() - transformUiModel.offsetX),
                    transformUiModel.scaleY * (barcodeBoundary.top.toFloat() - transformUiModel.offsetY),
                    transformUiModel.scaleX * (barcodeBoundary.right.toFloat() + transformUiModel.offsetX),
                    transformUiModel.scaleY * (barcodeBoundary.bottom.toFloat() + transformUiModel.offsetY),
                )
            }
            else -> RectF()
        }
    }

    private fun getTransformInfo(
        cameraBoundary: RectF,
        capturedImageBoundary: RectF,
        imageRotationDegree: Int,
    ): CoordinatesTransformUiModel {
        return when ((imageRotationDegree / 90) % 2) {
            0 -> { // 0, 180, 360
                val scaleX = cameraBoundary.width() / capturedImageBoundary.width()
                val scaleY = cameraBoundary.height() / capturedImageBoundary.height()
                CoordinatesTransformUiModel(
                    scaleX = scaleX,
                    scaleY = scaleY,
                    offsetX = 0F,
                    offsetY = 50F
                )
            }
            1 -> { // 90, 270
                val scaleX = cameraBoundary.width() / capturedImageBoundary.height()
                val scaleY = cameraBoundary.height() / capturedImageBoundary.width()
                CoordinatesTransformUiModel(
                    scaleX = scaleX,
                    scaleY = scaleY,
                    offsetX = 50F,
                    offsetY = 0F
                )
            }
            else -> CoordinatesTransformUiModel()
        }
    }

    private fun generateScanningResultByBoundary(
        barcodeGlobalPosition: RectF,
        barcode: Barcode
    ): BarCodeResult {
        return when {
            checkRectangleInside(
                barcodeGlobalPosition,
                barcodeScanningAreaRect
            ) -> {
                if (checkDistanceMatched(barcodeGlobalPosition, barcodeScanningAreaRect)) {
                    // Perfect match
                    BarCodeResult(
                        barCode = barcode,
                        globalPosition = barcodeGlobalPosition,
                        message = "PerfectMatch"
                    )
                } else {

                    // Move closer
                    BarCodeResult(
                        barCode = barcode,
                        globalPosition = barcodeGlobalPosition,
                        message = "InsideBoundary"
                    )
                }
            }
            !checkRectangleNotOverlap(barcodeGlobalPosition, barcodeScanningAreaRect) -> {
                BarCodeResult(
                    barCode = barcode,
                    globalPosition = barcodeGlobalPosition,
                    message = "BoundaryOverLap"
                )
            }
            else -> {
                BarCodeResult(message = "OutOfBoundary")
            }
        }
    }

    /*
     * Cond1. If A's left edge is to the right of the B's right edge, - then A is Totally to right Of B
     * Cond2. If A's right edge is to the left of the B's left edge, - then A is Totally to left Of B
     * Cond3. If A's top edge is below B's bottom edge, - then A is Totally below B
     * Cond4. If A's bottom edge is above B's top edge, - then A is Totally above B
     *
     * NON-Overlap => Cond1 Or Cond2 Or Cond3 Or Cond4
     * Overlap => NOT (Cond1 Or Cond2 Or Cond3 Or Cond4)
     */
    private fun checkRectangleNotOverlap(areaOne: RectF, areaTwo: RectF): Boolean {
        return areaTwo.left >= areaOne.right || areaTwo.right < areaOne.left || areaTwo.top > areaOne.bottom || areaTwo.bottom < areaOne.top
    }

    private fun checkRectangleInside(smallArea: RectF, largeArea: RectF): Boolean {
        return smallArea.left > largeArea.left &&
                smallArea.top > largeArea.top &&
                smallArea.right < largeArea.right &&
                smallArea.bottom < largeArea.bottom
    }

    private fun checkDistanceMatched(smallArea: RectF, largeArea: RectF): Boolean {
        val rate = (smallArea.width() * smallArea.height()) / (largeArea.width() * largeArea.height())
        return rate > barcodeSizeRateThreshold
    }
}

@Composable
fun BarcodeScanningDecorationLayout(
    width: Int,
    height: Int,
    onScanningAreaReady: (RectF) -> Unit,
    scanningResult: BarCodeResult,
) {
    fun calculateScanningRect(size: Int, centerPoint: PointF): RectF {
        val scanningAreaSize = size * 0.8F
        val left = centerPoint.x - scanningAreaSize * 0.5F
        val top = centerPoint.y - scanningAreaSize * 0.5F
        val right = centerPoint.x + scanningAreaSize * 0.5F
        val bottom = centerPoint.y + scanningAreaSize * 0.1F
        return RectF(left, top, right, bottom)
    }

    fun calculateInstructionTextRect(paint: android.graphics.Paint, text: String): RectF {
        return runCatching {
            val rect: android.graphics.Rect = android.graphics.Rect()
            paint.getTextBounds(text, 0, text.length, rect)
            rect.toRectF()
        }.getOrNull() ?: RectF()
    }

    val scanningAreaPath: Path by remember { mutableStateOf(Path()) }
    val cameraBoundaryPath: Path by remember { mutableStateOf(Path()) }
    val barcodeBoundaryPath: Path by remember { mutableStateOf(Path()) }
    val instructionTextPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = LocalDensity.current.run { 18.sp.toPx() }
        color = Color(0xFF03DAC5).toArgb()
    }

    val centerPoint = PointF(width * 0.5F, height * 0.5F)
    val scanningAreaRect = calculateScanningRect(size = minOf(width, height), centerPoint)
    val scanningFrameStrokeSize = dp2Px(dp = 4.dp)
    val scanningFrameCornerRadius = dp2Px(dp = 6.dp)
    val barcodeResultBoundaryStrokeSize = dp2Px(dp = 4.dp)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                onScanningAreaReady.invoke(scanningAreaRect)
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

            // draw the instruction text
            calculateInstructionTextRect(instructionTextPaint, scanningResult.message).let { textBoundary ->
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        scanningResult.message,
                        centerPoint.x - textBoundary.width() * 0.5F,
                        (scanningAreaRect.top - textBoundary.height()) * 0.5F,
                        instructionTextPaint
                    )
                }
            }

            // draw the bar code result boundary
            scanningResult.globalPosition.let {
                barcodeBoundaryPath.moveTo(x = it.right - it.width() * 0.2F, y = it.top)
                barcodeBoundaryPath.lineTo(x = it.right, y = it.top)

                barcodeBoundaryPath.lineTo(x = it.right, y = it.bottom)
                barcodeBoundaryPath.lineTo(x = it.right - it.width() * 0.2F, y = it.bottom)

                barcodeBoundaryPath.moveTo(x = it.left + it.width() * 0.2F, y = it.bottom)
                barcodeBoundaryPath.lineTo(x = it.left, y = it.bottom)
                barcodeBoundaryPath.lineTo(x = it.left, y = it.top)
                barcodeBoundaryPath.lineTo(x = it.left + it.width() * 0.2F, y = it.top)

                drawPath(
                    path = barcodeBoundaryPath,
                    color = Color(0xFF3700B3),
                    style = Stroke(
                        join = StrokeJoin.Bevel,
                        width = barcodeResultBoundaryStrokeSize,
                    ),
                )
            }
        }
    )
}

@ExperimentalGetImage
class BarcodeImageAnalyzer: ImageAnalysis.Analyzer {

    private var processing = false
    private var processListener: ProcessListener? = null

    override fun analyze(imageProxy: ImageProxy) {

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

@Composable
fun dp2Px(dp: Dp) = with(LocalDensity.current) { dp.toPx() }

@Composable
@Preview
fun QRCodeScannerScreenPreview() {

    BarcodeScannerScreen()
}