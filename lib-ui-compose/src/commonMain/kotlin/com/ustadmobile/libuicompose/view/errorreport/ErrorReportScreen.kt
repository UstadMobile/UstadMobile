package com.ustadmobile.libuicompose.view.errorreport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.ErrorReportUiState
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ErrorReportScreen(
    uiState: ErrorReportUiState,
    onTakeMeHomeClick: () -> Unit = {},
    onCopyIconClick: () -> Unit = {},
    onShareIconClick: () -> Unit = {}
){
    UstadVerticalScrollColumn (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ){

        //To be restore if/when this screen is brought back
//        Image(
//            modifier = Modifier.padding(vertical = 16.dp),
//            painter = painterResource(id = R.drawable.ic_undraw_access_denied),
//            contentDescription = null,
//            contentScale = ContentScale.Fit,
//        )

        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            text = stringResource(MR.strings.sorry_something_went_wrong)
        )

        Button(
            onClick = onTakeMeHomeClick,
            enabled = uiState.fieldsEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(stringResource(MR.strings.take_me_home))
        }

        Divider(thickness = 1.dp)

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = uiState.errorReport?.errUid.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(MR.strings.incident_id),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row {
                IconButton(
                    onClick = {
                        onCopyIconClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FileCopy,
                        contentDescription = stringResource(MR.strings.copy_code),
                    )
                }
                IconButton(
                    onClick = {
                        onShareIconClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(MR.strings.share),
                    )
                }
            }
        }

        Divider(thickness = 1.dp)

        Text(
            text = stringResource(MR.strings.error_code, uiState.errorReport?.errorCode ?: ""),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = uiState.errorReport?.message ?: "",
            style = MaterialTheme.typography.bodySmall
        )

    }
}
