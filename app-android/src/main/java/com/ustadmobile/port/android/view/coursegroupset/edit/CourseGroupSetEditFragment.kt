package com.ustadmobile.port.android.view.coursegroupset.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditUiState
import com.ustadmobile.core.viewmodel.coursegroupset.edit.CourseGroupSetEditViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.edit.appendGroupNumIfNotInList
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.composable.UstadExposedDropDownMenuField
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.port.android.view.composable.UstadNumberTextField
import com.ustadmobile.core.R as CR


class CourseGroupSetEditFragment: UstadBaseMvvmFragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
                )

                MdcTheme {

                }
            }
        }
    }
}

@Composable
fun CourseGroupEditScreen(
    viewModel: CourseGroupSetEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState(CourseGroupSetEditUiState())

    CourseGroupSetEditScreen(
        uiState = uiState,
        onCourseGroupSetChange = viewModel::onEntityChanged,
        onClickAssignRandomly = viewModel::onClickAssignRandomly,
        onChangeGroupAssignment = viewModel::onChangeGroupAssignment
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseGroupSetEditScreen(
    uiState: CourseGroupSetEditUiState,
    onCourseGroupSetChange: (CourseGroupSet?) -> Unit = {},
    onClickAssignRandomly: () -> Unit = {},
    onChangeGroupAssignment: (personUid: Long, groupNumber: Int) -> Unit = { _, _ ->},
){
    LazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        item {
            UstadInputFieldLayout(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                errorText = uiState.courseTitleError
            ) {
                OutlinedTextField(
                    value = uiState.courseGroupSet?.cgsName ?: "",
                    modifier = Modifier
                        .testTag("cgs_name")
                        .fillMaxWidth(),
                    enabled = uiState.fieldsEnabled,
                    onValueChange = {
                        onCourseGroupSetChange(uiState.courseGroupSet?.shallowCopy{
                            cgsName = it
                        })
                    },
                    label = { Text(stringResource(CR.string.title)) },
                    isError = uiState.courseTitleError != null,

                )
            }
        }

        item {
            UstadInputFieldLayout(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                errorText = uiState.numOfGroupsError,
            ) {
                UstadNumberTextField(
                    modifier = Modifier
                        .testTag("num_groups")
                        .fillMaxWidth(),
                    enabled = uiState.fieldsEnabled,
                    value = uiState.courseGroupSet?.cgsTotalGroups?.toFloat() ?: 0f,
                    label = { Text(stringResource(id = CR.string.number_of_groups)) },
                    isError = uiState.numOfGroupsError != null,
                    onValueChange = {
                        onCourseGroupSetChange(uiState.courseGroupSet?.shallowCopy{
                            cgsTotalGroups = it.toInt()
                        })
                    }
                )
            }
        }

        item {
            Button(
                onClick = onClickAssignRandomly,
                enabled = uiState.fieldsEnabled,
                modifier = Modifier
                    .testTag("assign_random_button")
                    .padding(8.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.secondaryColor)
                )
            ) {
                Text(stringResource(CR.string.assign_to_random_groups).uppercase())
            }
        }

        val groups = (0..(uiState.courseGroupSet?.cgsTotalGroups ?: 1)).toList()

        val itemTextFn: @Composable (groupNum: Int) -> String = {
            if(it == 0) {
                stringResource(CR.string.unassigned)
            }else {
                "${stringResource(CR.string.group)} $it"
            }
        }

        items(uiState.membersList, itemContent = { member ->
            val currentGroupNum = member.cgm?.cgmGroupNumber ?: 0
            ListItem (
                text = {
                    Text(text = member.name ?: "")
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null
                    )
                },
                trailing = {
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

@Composable
@Preview
fun CourseGroupSetEditScreenPreview(){
    CourseGroupSetEditScreen(
        uiState = CourseGroupSetEditUiState(
            courseGroupSet = CourseGroupSet().apply {
                cgsName = "ttl"
                cgsTotalGroups = 6
            },
            membersList = listOf(
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Bart Simpson"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Shelly Mackleberry"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Tracy Mackleberry"
                ),
                CourseGroupMemberAndName(
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Nelzon Muntz"
                )
            )
        )
    )
}