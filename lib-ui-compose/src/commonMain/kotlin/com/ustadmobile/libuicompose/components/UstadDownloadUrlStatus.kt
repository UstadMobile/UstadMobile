package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase

/**
 * Show the downloading status in a circular (determinative) progress indicator or show error (if any)
 */
@Composable
fun UstadDownloadUrlStatus(
    state: GetStoragePathForUrlUseCase.GetStoragePathForUrlState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when(state.status) {
            GetStoragePathForUrlUseCase.GetStoragePathForUrlState.Status.IN_PROGRESS -> {
                if(state.totalBytes > 0) {
                    CircularProgressIndicator(
                        progress = state.bytesTransferred.toFloat()/state.totalBytes.toFloat()
                    )
                }else {
                    CircularProgressIndicator()
                }
                Text(stringResource(MR.strings.downloading))
            }

            GetStoragePathForUrlUseCase.GetStoragePathForUrlState.Status.FAILED -> {
                Icon(Icons.Default.Error, contentDescription = null)
                Text(state.error ?: "")
            }

            else -> {
                //nothing
            }
        }
    }

}