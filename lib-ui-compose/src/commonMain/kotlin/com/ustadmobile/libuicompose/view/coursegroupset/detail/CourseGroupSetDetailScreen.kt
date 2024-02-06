package com.ustadmobile.libuicompose.view.coursegroupset.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailUiState
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailViewModel
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadPersonAvatar

@Composable
fun CourseGroupSetDetailScreen(
    viewModel: CourseGroupSetDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState(CourseGroupSetDetailUiState())
    CourseGroupSetDetailScreen(uiState = uiState)
}

@Composable
fun CourseGroupSetDetailScreen(
    uiState: CourseGroupSetDetailUiState
){

    UstadLazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){
        (1..(uiState.courseGroupSet?.cgsTotalGroups ?: 1)).forEach { groupNum ->
            val members = uiState.membersList.filter {
                it.cgm?.cgmGroupNumber == groupNum
            }

            if(members.isNotEmpty()) {
                item(key = "header_${groupNum}") {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        text = "${stringResource(MR.strings.group)} $groupNum",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                items(
                    items = members,
                    key = { it.personUid }
                ){
                    ListItem(
                        headlineContent = {
                            Text(text = "${it.name}")
                        },
                        leadingContent = {
                            UstadPersonAvatar(
                                pictureUri = it.pictureUri,
                                personName = it.name,
                            )
                        }
                    )
                }
            }
        }
    }
}