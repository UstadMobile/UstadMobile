package com.ustadmobile.libuicompose.view.individual


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.individual.IndividualLearnerViewModel
import com.ustadmobile.libuicompose.components.UstadHorizontalDivider
import com.ustadmobile.libuicompose.components.UstadTitleDescriptionButton
import com.ustadmobile.libuicompose.images.UstadImage
import com.ustadmobile.libuicompose.images.ustadAppImagePainter
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun IndividualLearnerScreen(viewModel: IndividualLearnerViewModel) {
    IndividualLearnerScreenContent(viewModel)
}

@Composable
fun IndividualLearnerScreenContent(viewModel: IndividualLearnerViewModel) {

    Column(
        modifier = Modifier.fillMaxHeight().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = ustadAppImagePainter(UstadImage.ILLUSTRATION_CONNECT),
            contentDescription = "",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            UstadHorizontalDivider()

            UstadTitleDescriptionButton(
                title = stringResource(MR.strings.Create_new_local_account_title),
                description = stringResource(MR.strings.Create_new_local_account_description),
                onClick = {viewModel.onClickContinueWithoutLogin()}
            )

            UstadHorizontalDivider()

            UstadTitleDescriptionButton(
                title = stringResource(MR.strings.Restore_local_account_title),
                description = stringResource(MR.strings.Restore_local_account_description),
                onClick = {}

            )

            UstadHorizontalDivider()

        }

    }


}
