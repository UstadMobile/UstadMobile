package com.ustadmobile.libuicompose.view.coursegroupset.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailUiState
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailViewModel
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadPersonAvatar

@Composable
fun CourseGroupSetDetailScreen(
    viewModel: CourseGroupSetDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState(CourseGroupSetDetailUiState())
    CourseGroupSetDetailScreen(uiState = uiState)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseGroupSetDetailScreen(
    uiState: CourseGroupSetDetailUiState
){

    LazyColumn(
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
                        style = MaterialTheme.typography.body1
                    )
                }

                items(
                    items = members,
                    key = { it.personUid }
                ){
                    ListItem(
                        text = {
                            Text(text = "${it.name}")
                        },
                        icon = {
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