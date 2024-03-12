package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ustadmobile.core.domain.blob.openblob.OpeningBlobState
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UstadOpeningBlobInfoBottomSheet(
    openingBlobState: OpeningBlobState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        val errorVal = openingBlobState.error
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if(errorVal == null) {
                Text(
                    text = stringResource(MR.strings.opening_name, openingBlobState.item.fileName),
                    modifier = Modifier.defaultItemPadding(),
                )

                if(openingBlobState.totalBytes > 0) {
                    LinearProgressIndicator(
                        progress = openingBlobState.progress,
                        modifier = Modifier.defaultItemPadding().fillMaxWidth(),
                    )
                    Text(
                        text = UMFileUtil.formatFileSize(openingBlobState.bytesReady) + "/"
                            + UMFileUtil.formatFileSize(openingBlobState.totalBytes),
                        modifier = Modifier.defaultItemPadding(),
                    )
                }else{
                    LinearProgressIndicator(
                        modifier = Modifier.defaultItemPadding().fillMaxWidth()
                    )
                }
            }else {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Error, contentDescription = null)
                }

                Text(text = errorVal, modifier = Modifier.defaultItemPadding())
            }
        }
    }
}
