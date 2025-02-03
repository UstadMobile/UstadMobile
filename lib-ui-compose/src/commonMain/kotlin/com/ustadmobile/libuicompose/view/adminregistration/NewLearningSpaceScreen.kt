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
fun NewLearningSpaceScreen() {
    Column(
        modifier = Modifier.fillMaxHeight().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                LearningSpaceItem(
                    image = UstadImage.ILLUSTRATION_ONBOARDING1,
                    title = stringResource(MR.strings.enter_link_manually),
                    description = stringResource(MR.strings.type_or_paste_link_to_access_type),
                    onClick = { }
                )
            }
            item {
                LearningSpaceItem(
                    image = UstadImage.ILLUSTRATION_ONBOARDING1,
                    title = stringResource(MR.strings.create_new_learning_space),
                    description = stringResource(MR.strings.start_creating_new_learning_space),
                    onClick = { }
                )
            }

        }
    }
}

@Composable
fun LearningSpaceItem(
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

