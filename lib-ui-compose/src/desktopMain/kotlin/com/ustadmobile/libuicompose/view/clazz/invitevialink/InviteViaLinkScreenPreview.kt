package com.ustadmobile.libuicompose.view.clazz.invitevialink

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.clazz.invitevialink.ClazzInviteViaLinkUiState


@Composable
@Preview
fun  InviteViaLinkScreenPreview() {
    val uiStateVal = ClazzInviteViaLinkUiState(
        inviteLink = "http://wwww.ustadmobile.com/ClazzJoin?code=12ASDncd",
    )

    ClazzInviteViaLinkScreen(
        uiState = uiStateVal
    )
}