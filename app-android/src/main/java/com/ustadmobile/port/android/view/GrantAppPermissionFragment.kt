package com.ustadmobile.port.android.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.GrantAppPermissionUiState
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding

@Composable
private fun GrantAppPermissionScreen(
    uiState: GrantAppPermissionUiState = GrantAppPermissionUiState(),
    onClickGrant: () -> Unit = {},
    onClickCancel: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        uiState.grantToIcon?.also { grantToIcon ->
            Image(
                painter = painterResource(id = grantToIcon),
                contentDescription = "",
                modifier = Modifier.size(64.dp)
            )
        }

        Text(uiState.grantToAppName)

        Text(stringResource(id = R.string.this_app_will_receive),
            modifier = Modifier.defaultItemPadding())

        Button(
            onClick = onClickGrant,
            enabled = uiState.fieldsEnabled,
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.secondaryColor)
            )
        ) {
            Text(
                stringResource(R.string.accept).uppercase(),
                color = contentColorFor(
                    colorResource(id = R.color.secondaryColor)
                )
            )
        }

        OutlinedButton(
            onClick = onClickCancel,
            modifier = Modifier
                .fillMaxWidth(),
            enabled = uiState.fieldsEnabled,
        ) {
            Text(stringResource(R.string.cancel).uppercase())
        }
    }
}

@Composable
@Preview
fun GrantAppPermissionPreview() {
    val uiStateVal = GrantAppPermissionUiState(
        grantToAppName = "App Name",
        grantToIcon = R.drawable.article_24px
    )

    MdcTheme {
        GrantAppPermissionScreen(uiStateVal)
    }
}