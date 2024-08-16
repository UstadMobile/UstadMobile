package com.ustadmobile.libuicompose.view.coursegroupset.edit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditUiState
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.edit.appendGroupNumIfNotInList
import com.ustadmobile.lib.db.entities.*
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadExposedDropDownMenuField
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadNumberTextField
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle


@Composable
fun CourseGroupSetEditScreen(
    viewModel: CourseGroupSetEditViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        CourseGroupSetEditUiState(), Dispatchers.Main.immediate)

    CourseGroupSetEditScreen(
        uiState = uiState,
        onCourseGroupSetChange = viewModel::onEntityChanged,
        onClickAssignRandomly = viewModel::onClickAssignRandomly,
        onChangeGroupAssignment = viewModel::onChangeGroupAssignment
    )
}

@Composable
fun CourseGroupSetEditScreen(
    uiState: CourseGroupSetEditUiState,
    onCourseGroupSetChange: (CourseGroupSet?) -> Unit = {},
    onClickAssignRandomly: () -> Unit = {},
    onChangeGroupAssignment: (personUid: Long, groupNumber: Int) -> Unit = { _, _ ->},
){
    UstadLazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        item(key = "title_item") {
            OutlinedTextField(
                value = uiState.courseGroupSet?.cgsName ?: "",
                modifier = Modifier
                    .testTag("title")
                    .defaultItemPadding()
                    .fillMaxWidth(),
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onCourseGroupSetChange(uiState.courseGroupSet?.shallowCopy{
                        cgsName = it
                    })
                },
                label = { Text(stringResource(MR.strings.title) + "*") },
                isError = uiState.courseTitleError != null,
                singleLine = true,
                supportingText = {
                    Text(uiState.courseTitleError ?: stringResource(MR.strings.required))
                }
            )
        }

        item(key = "num_groups_item") {
            UstadNumberTextField(
                modifier = Modifier
                    .testTag("number_of_groups")
                    .defaultItemPadding()
                    .fillMaxWidth(),
                enabled = uiState.fieldsEnabled,
                value = uiState.courseGroupSet?.cgsTotalGroups?.toFloat() ?: 0f,
                label = { Text(stringResource(MR.strings.number_of_groups) + "*") },
                isError = uiState.numOfGroupsError != null,
                onValueChange = {
                    onCourseGroupSetChange(uiState.courseGroupSet?.shallowCopy{
                        cgsTotalGroups = it.toInt()
                    })
                },
                supportingText = {
                    Text(uiState.numOfGroupsError ?: stringResource(MR.strings.required))
                }
            )
        }

        item {
            OutlinedButton(
                onClick = onClickAssignRandomly,
                enabled = uiState.fieldsEnabled,
                modifier = Modifier
                    .testTag("assign_to_random_groups_button")
                    .fillMaxWidth()
                    .defaultItemPadding(8.dp),

            ) {
                Text(stringResource(MR.strings.assign_to_random_groups))
            }
        }

        val groups = (0..(uiState.courseGroupSet?.cgsTotalGroups ?: 1)).toList()

        val itemTextFn: @Composable (groupNum: Int) -> String = {
            if(it == 0) {
                stringResource(MR.strings.unassigned)
            }else {
                "${stringResource(MR.strings.group)} $it"
            }
        }

        items(uiState.membersList, itemContent = { member ->
            val currentGroupNum = member.cgm?.cgmGroupNumber ?: 0
            ListItem (
                headlineContent = {
                    Text(text = member.name ?: "")
                },
                leadingContent = {
                    UstadPersonAvatar(
                        personName = member.name,
                        pictureUri = member.pictureUri,
                    )
                },
                trailingContent = {
                    UstadExposedDropDownMenuField<Int>(
                        value = currentGroupNum,
                        label = "",
                        options = groups.appendGroupNumIfNotInList(currentGroupNum),
                        onOptionSelected = { groupNum ->
                            onChangeGroupAssignment(member.personUid, groupNum)
                        },
                        itemText =  itemTextFn,
                        modifier = Modifier.width(120.dp),
                        isError = currentGroupNum !in groups,
                    )
                }
            )
        })

    }
}