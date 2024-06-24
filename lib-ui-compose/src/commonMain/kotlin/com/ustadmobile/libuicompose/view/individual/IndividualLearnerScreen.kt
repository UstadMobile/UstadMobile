package com.ustadmobile.libuicompose.view.individual


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.individual.IndividualLearnerViewModel
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


        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {



            item {
                UstadTitleDescriptionButton(
                    title = stringResource(MR.strings.create_new_local_account_title),
                    description = stringResource(MR.strings.create_new_local_account_description),
                    onClick = { viewModel.onClickContinueWithoutLogin() }
                )
            }



            item {
                UstadTitleDescriptionButton(
                    title = stringResource(MR.strings.restore_local_account_title),
                    description = stringResource(MR.strings.restore_local_account_description),
                    onClick = {}
                )
            }


        }
    }


}

@Composable
fun UstadTitleDescriptionButton(
    title: String,
    description: String,
    titleColor: Color = Color.Black,
    titleSize: Float = 16f,
    descriptionSize: Float = 12f,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable { onClick() }
    ) {

        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = titleSize.sp
                )
            },
            supportingContent = {
                Text(
                    text = description,
                    fontSize = descriptionSize.sp)
            },
        )
        HorizontalDivider()

    }
}