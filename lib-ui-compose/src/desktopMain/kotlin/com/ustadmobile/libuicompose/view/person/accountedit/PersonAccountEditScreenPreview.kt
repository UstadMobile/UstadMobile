package com.ustadmobile.libuicompose.view.person.accountedit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditUiState


@Composable
@Preview
fun PersonAccountEditScreenPreview() {

    PersonAccountEditScreen(PersonAccountEditUiState(fieldsEnabled = true))

}
