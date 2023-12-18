package com.ustadmobile.libuicompose.view.person.registerminorwaitforparent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.person.registerminorwaitforparent.RegisterMinorWaitForParentUiState
import com.ustadmobile.libuicompose.components.UstadDetailField2
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.person.registerminorwaitforparent.RegisterMinorWaitForParentViewModel
import dev.icerock.moko.resources.compose.stringResource
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun RegisterMinorWaitForParentScreen(
    viewModel: RegisterMinorWaitForParentViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        RegisterMinorWaitForParentUiState())

    RegisterMinorWaitForParentScreen(
        uiState = uiState,
        onClickOk = viewModel::onClickOK,
    )
}

@Composable
fun RegisterMinorWaitForParentScreen(
    uiState: RegisterMinorWaitForParentUiState = RegisterMinorWaitForParentUiState(),
    onClickOk: () -> Unit = {},
) {
    var passwordVisible: Boolean by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )  {
        UstadDetailField2(
            valueText = uiState.username ?: "",
            labelText = stringResource(MR.strings.username),
            icon = Icons.Default.AccountCircle,
        )

        Spacer(modifier = Modifier.height(20.dp))

        UstadDetailField2(
            valueContent = { Text(if(passwordVisible) { uiState.password } else { "*****" }) },
            labelContent = { Text(stringResource(MR.strings.password)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                )
            },
            trailingContent = {
                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    },
                ) {
                    Icon(
                        imageVector = if(!passwordVisible) {
                            Icons.Filled.Visibility
                        }else {
                            Icons.Filled.VisibilityOff
                        },
                        contentDescription = stringResource(MR.strings.toggle_visibility),
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(MR.strings.we_sent_a_message_to_your_parent,
                uiState.parentContact)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClickOk,
            modifier = Modifier.fillMaxWidth(),

        ) {
            Text(stringResource(MR.strings.ok))
        }
    }
}
