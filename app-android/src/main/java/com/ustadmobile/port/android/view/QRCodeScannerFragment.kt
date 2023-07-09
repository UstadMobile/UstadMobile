package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.ustadmobile.core.viewmodel.QRCodeScannerViewModel
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding

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

    val context = LocalContext.current

    val barcodeLauncher:ActivityResultLauncher<ScanOptions> = rememberLauncherForActivityResult(
        ScanContract()
    ) { result ->
        if(result.contents == null) {
            Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Scanned: " + result.contents, Toast.LENGTH_LONG).show();
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding(),
    ) {

        item {
            TextButton(
                modifier = Modifier
                    .defaultItemPadding(),
                onClick = {
                    barcodeLauncher.launch(ScanOptions())
                }
            ) {
                Text("Scan Barcode")
            }
        }
    }

}


@Composable
@Preview
fun QRCodeScannerScreenPreview() {

    MdcTheme {
        BarcodeScannerScreen()
    }
}