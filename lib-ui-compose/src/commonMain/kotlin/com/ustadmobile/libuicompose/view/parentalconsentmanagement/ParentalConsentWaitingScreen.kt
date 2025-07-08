package com.ustadmobile.libuicompose.view.parentalconsentmanagement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentConsentWaitingScreenViewModel
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ParentalConsentWaitingScreen(
    viewModel: ParentConsentWaitingScreenViewModel
) {
    ParentalConsentWaitingScreen()
}

@Composable
fun ParentalConsentWaitingScreen(

) {
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(MR.strings.wait_for_parent_to_consent),
                modifier = Modifier.defaultItemPadding(),
            )
        }

    }
}
