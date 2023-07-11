package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.ustadmobile.core.viewmodel.QRCodeScannerViewModel


class QRCodeScannerFragment : UstadBaseMvvmFragment() {

    private val viewModel: QRCodeScannerViewModel by ustadViewModels(::QRCodeScannerViewModel)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    QRCodeScannerScreenForViewModel(viewModel)
                }
            }
        }
    }
}


@Composable
private fun QRCodeScannerScreenForViewModel(
    viewModel: QRCodeScannerViewModel
) {

    BarcodeScannerScreen(
        onQRCodeDetected = viewModel::onQRCodeDetected,
    )
}

@Composable
private fun BarcodeScannerScreen(
    onQRCodeDetected: (String) -> Unit = {}
) {

    val barcodeLauncher:ActivityResultLauncher<ScanOptions> = rememberLauncherForActivityResult(
        ScanContract()
    ) { result ->
        if(result.contents != null) {
            onQRCodeDetected(result.contents)
        }
    }

    val options = ScanOptions()
    options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
    options.setPrompt("")
    options.setBeepEnabled(true)
    options.setOrientationLocked(false)
    LaunchedEffect(Unit) {
        barcodeLauncher.launch(options)
    }
}


@Composable
@Preview
fun QRCodeScannerScreenPreview() {

    MdcTheme {
        BarcodeScannerScreen()
    }
}