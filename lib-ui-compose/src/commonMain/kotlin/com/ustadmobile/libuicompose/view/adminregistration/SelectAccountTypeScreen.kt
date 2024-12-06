package com.ustadmobile.libuicompose.view.adminregistration

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.images.UstadImage
import com.ustadmobile.libuicompose.images.ustadAppImagePainter
import dev.icerock.moko.resources.compose.stringResource


@Composable
fun SelectAccountTypeScreen() {
    Column(
        modifier = Modifier.fillMaxHeight().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                AccountOptionItem(
                    image = UstadImage.ILLUSTRATION_ONBOARDING1,
                    title = stringResource(MR.strings.personal),
                    description = stringResource(MR.strings.allows_you_and_family_members),
                    onClick = { /* handle click */ }
                )
            }

            item {
                AccountOptionItem(
                    image = UstadImage.ILLUSTRATION_ONBOARDING2,
                    title = stringResource(MR.strings.learning_space),
                    description = stringResource(MR.strings.allows_you_to_connect_with_others),
                    onClick = { /* handle click */ }
                )
            }
        }
    }
}

@Composable
fun AccountOptionItem(
    image: UstadImage,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    ListItem(
        leadingContent = {
            Image(
                painter = ustadAppImagePainter(image),
                contentDescription = null,
                modifier = Modifier.size(42.dp),
            )
        },
        headlineContent = {
            Text(
                text = title,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            )
        },
        supportingContent = {
            Text(
                text = description,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}
