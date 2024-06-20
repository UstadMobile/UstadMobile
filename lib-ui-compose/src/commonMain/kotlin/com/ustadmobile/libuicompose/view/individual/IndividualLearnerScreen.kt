package com.ustadmobile.libuicompose.view.individual


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

        Text(
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            text = stringResource(MR.strings.individual_title),
        )

        Spacer(modifier = Modifier.height(10.dp))


        Text(
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            text = stringResource(MR.strings.individual_sub_title),
        )


        Spacer(modifier = Modifier.height(26.dp))

        GoogleButton(onClick = {})


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.onClickContinueWithoutLogin() },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
                contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSecondary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(MR.strings.continue_without_account))

        }


    }


}

@Composable
fun GoogleButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().height(40.dp),
        border = ButtonDefaults.outlinedBorder,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.primary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
        ) {
            Image(
                painter = ustadAppImagePainter(UstadImage.GOOGLE_ICON),
                contentDescription = "Google Icon",
                modifier = Modifier.size(22.dp).clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                fontSize = 14.sp,
                text = stringResource(MR.strings.continue_with_google),
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}