package com.ustadmobile.libuicompose.view.clazz.invitevialink

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.InviteViaLinkUiState


@Composable
@Preview
fun  InviteViaLinkScreenPreview() {
    val uiStateVal = InviteViaLinkUiState(
        entityName = "Course",
        inviteLink = "http://wwww.ustadmobile.com/ClazzJoin?code=12ASDncd",
    )

    InviteViaLinkScreen(
        uiState = uiStateVal
    )
}