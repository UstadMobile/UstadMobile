package com.ustadmobile.port.android.view.clazzassignment.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.impl.locale.entityconstants.*
import com.ustadmobile.core.viewmodel.clazzassignment.ClazzAssignmentViewModelConstants.MarkingType
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.core.viewmodel.clazzassignment.edit.ClazzAssignmentEditUiState
import com.ustadmobile.core.viewmodel.clazzassignment.edit.ClazzAssignmentEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import com.ustadmobile.port.android.util.compose.courseTerminologyEntryResource
import com.ustadmobile.port.android.util.compose.rememberCourseTerminologyEntries
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.composable.*
import com.ustadmobile.core.R as CR
import com.ustadmobile.core.MR

class ClazzAssignmentEditFragment: UstadBaseMvvmFragment(){

    private val viewModel: ClazzAssignmentEditViewModel by
        ustadViewModels(::ClazzAssignmentEditViewModel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ClazzAssignmentEditScreen(viewModel)
                }
            }
        }
    }
}

@Composable
private fun ClazzAssignmentEditScreen(viewModel: ClazzAssignmentEditViewModel) {
    val uiState by viewModel.uiState.collectAsState(initial = ClazzAssignmentEditUiState())

    ClazzAssignmentEditScreen(
        uiState = uiState,
        onChangeAssignment = viewModel::onAssignmentChanged,
        onChangeCourseBlock = viewModel::onCourseBlockChanged,
        onClickAssignReviewers =  viewModel::onClickAssignReviewers,
        onClickSubmissionType = viewModel::onClickSubmissionType,
        onClickEditDescription = viewModel::onClickEditDescription,
    )
}

@Composable
private fun ClazzAssignmentEditScreen(
    uiState: ClazzAssignmentEditUiState = ClazzAssignmentEditUiState(),
    onChangeAssignment: (ClazzAssignment?) -> Unit = {},
    onChangeCourseBlock: (CourseBlock?) -> Unit = {},
    onClickSubmissionType: () -> Unit = {},
    onClickAssignReviewers: () -> Unit = {},
    onClickEditDescription: () -> Unit = {},
) {

    val terminologyEntries = rememberCourseTerminologyEntries(uiState.courseTerminology)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        UstadCourseBlockEdit(
            uiState = uiState.courseBlockEditUiState,
            onCourseBlockChange = onChangeCourseBlock,
            onClickEditDescription = onClickEditDescription,
        )

        UstadClickableTextField(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("cgsName"),
            value = uiState.entity?.assignmentCourseGroupSetName?.let {
                "${stringResource(CR.string.groups)}: $it"
            } ?: stringResource(CR.string.individual_submission),
            label = { Text(stringResource(id = CR.string.submission_type)) },
            enabled = uiState.groupSetEnabled,
            onClick = onClickSubmissionType,
            onValueChange = {}
        )

        UstadInputFieldLayout(
            modifier = Modifier.defaultItemPadding(),
            errorText = uiState.submissionRequiredError,
        ) {
            UstadSwitchField(
                modifier = Modifier
                    .testTag("caRequireFileSubmission"),
                label = stringResource(id = CR.string.require_file_submission),
                checked = uiState.entity?.assignment?.caRequireFileSubmission ?: false,
                onChange = {
                    onChangeAssignment(
                        uiState.entity?.assignment?.shallowCopy {
                            caRequireFileSubmission = it
                        }
                    )
                },
                enabled = uiState.fieldsEnabled
            )
        }

        if (uiState.fileSubmissionVisible){
            UstadMessageIdOptionExposedDropDownMenuField(
                modifier = Modifier
                    .defaultItemPadding()
                    .testTag("caFileType"),
                value = uiState.entity?.assignment?.caFileType ?: 0,
                label = stringResource(CR.string.file_type),
                options = FileTypeConstants.FILE_TYPE_MESSAGE_IDS,
                enabled = uiState.fieldsEnabled,
                onOptionSelected = {
                    onChangeAssignment(
                        uiState.entity?.assignment?.shallowCopy {
                            caFileType = it.value
                        }
                    )
                },
            )

            UstadNumberTextField(
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth()
                    .testTag("caSizeLimit"),
                value = (uiState.entity?.assignment?.caSizeLimit ?: 0).toFloat(),
                label = { Text(stringResource(id = CR.string.size_limit)) },
                enabled = uiState.fieldsEnabled,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
                onValueChange = {
                    onChangeAssignment(
                        uiState.entity?.assignment?.shallowCopy {
                            caSizeLimit = it.toInt()
                        }
                    )
                },
            )

            UstadNumberTextField(
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth()
                    .testTag("caNumberOfFiles"),
                value = (uiState.entity?.assignment?.caNumberOfFiles ?: 0).toFloat(),
                label = { Text(stringResource(id = CR.string.number_of_files)) },
                enabled = uiState.fieldsEnabled,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = {
                    onChangeAssignment(
                        uiState.entity?.assignment?.shallowCopy {
                            caNumberOfFiles = it.toInt()
                        }
                    )
                },
            )
        }

        UstadInputFieldLayout(
            modifier = Modifier.defaultItemPadding(),
            errorText = uiState.submissionRequiredError,
        ) {
            UstadSwitchField(
                modifier = Modifier
                    .testTag("caRequireTextSubmission"),
                label = stringResource(id = CR.string.require_text_submission),
                checked = uiState.entity?.assignment?.caRequireTextSubmission ?: false,
                onChange = {
                    onChangeAssignment(
                        uiState.entity?.assignment?.shallowCopy {
                            caRequireTextSubmission = it
                        }
                    )
                },
                enabled = uiState.fieldsEnabled
            )
        }


        if (uiState.textSubmissionVisible) {
            UstadMessageIdOptionExposedDropDownMenuField(
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth()
                    .testTag("caTextLimitType"),
                value = uiState.entity?.assignment?.caTextLimitType ?: 0,
                label = stringResource(CR.string.limit),
                options = TextLimitTypeConstants.TEXT_LIMIT_TYPE_MESSAGE_IDS,
                enabled = uiState.fieldsEnabled,
                onOptionSelected = {
                    onChangeAssignment(
                        uiState.entity?.assignment?.shallowCopy {
                            caTextLimitType = it.value
                        }
                    )
                },
            )

            UstadNumberTextField(
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth()
                    .testTag("caTextLimit"),
                value = (uiState.entity?.assignment?.caTextLimit ?: 0).toFloat(),
                label = { Text(stringResource(id = CR.string.maximum)) },
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onChangeAssignment(
                        uiState.entity?.assignment?.shallowCopy {
                            caTextLimit = it.toInt()
                        }
                    )
                },
            )

        }

        UstadMessageIdOptionExposedDropDownMenuField(
            modifier = Modifier
                .defaultItemPadding()
                .testTag("caSubmissionPolicy"),
            value = uiState.entity?.assignment?.caSubmissionPolicy ?: 0,
            label = stringResource(CR.string.submission_policy),
            options = SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS,
            enabled = uiState.fieldsEnabled,
            onOptionSelected = {
                onChangeAssignment(
                    uiState.entity?.assignment?.shallowCopy {
                        caSubmissionPolicy = it.value
                    }
                )
            },
        )

        UstadExposedDropDownMenuField<MarkingType>(
            modifier = Modifier
                .defaultItemPadding()
                .testTag("caMarkingType"),
            value = MarkingType.valueOf(uiState.entity?.assignment?.caMarkingType ?: 0),
            label = stringResource(CR.string.marked_by),
            options = MarkingType.values().toList(),
            enabled = uiState.markingTypeEnabled,
            itemText = { markingType ->
                if(markingType == MarkingType.PEERS) {
                    stringResource(CR.string.peers)
                }else {
                    courseTerminologyEntryResource(
                        terminologyEntries = terminologyEntries,
                        stringResource = MR.strings.teacher,
                    )
                }
            },
            onOptionSelected = {
                onChangeAssignment(
                    uiState.entity?.assignment?.shallowCopy {
                        caMarkingType = it.value
                    }
                )
            },
        )

        if (uiState.peerMarkingVisible) {
            Row(
                modifier = Modifier.defaultItemPadding(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UstadInputFieldLayout(
                    modifier = Modifier.weight(0.7F),
                    errorText = uiState.reviewerCountError,
                ) {
                    UstadNumberTextField(
                        modifier = Modifier
                            .testTag("caPeerReviewerCount"),
                        value = (uiState.entity?.assignment?.caPeerReviewerCount ?: 0).toFloat(),
                        label = { Text(stringResource(id = CR.string.reviews_per_user_group)) },
                        enabled = uiState.fieldsEnabled,
                        isError = uiState.reviewerCountError != null,
                        onValueChange = {
                            onChangeAssignment(
                                uiState.entity?.assignment?.shallowCopy {
                                    caPeerReviewerCount = it.toInt()
                                }
                            )
                        },
                    )
                }

                OutlinedButton(
                    onClick = onClickAssignReviewers,
                    modifier = Modifier
                        .weight(0.3F)
                        .height(IntrinsicSize.Max)
                        .testTag("buttonAssignReviewers"),
                    enabled = uiState.fieldsEnabled,
                ) {
                    Text(stringResource(CR.string.assign_reviewers))
                }
            }
        }

        UstadSwitchField(
            modifier = Modifier
                .defaultItemPadding()
                .testTag("caClassCommentEnabled"),
            label = stringResource(id = CR.string.allow_class_comments),
            checked = uiState.entity?.assignment?.caClassCommentEnabled ?: false,
            onChange = {
                onChangeAssignment(uiState.entity?.assignment?.shallowCopy {
                    caClassCommentEnabled = it
                })
            },
            enabled = uiState.fieldsEnabled
        )

        UstadSwitchField(
            modifier = Modifier
                .defaultItemPadding()
                .testTag("caPrivateCommentsEnabled"),
            label = stringResource(id = CR.string.allow_private_comments_from_students),
            checked = uiState.entity?.assignment?.caPrivateCommentsEnabled ?: false,
            onChange = {
                onChangeAssignment(uiState.entity?.assignment?.shallowCopy {
                    caPrivateCommentsEnabled = it
                })
            },
            enabled = uiState.fieldsEnabled
        )
    }
}

@Composable
@Preview
fun ClazzAssignmentEditScreenPreview() {
    val uiStateVal = ClazzAssignmentEditUiState(
        courseBlockEditUiState = CourseBlockEditUiState(
            courseBlock = CourseBlock().apply {
                cbMaxPoints = 78
                cbCompletionCriteria = 14
            },
        ),
        entity = CourseBlockWithEntity().apply {
            assignment = ClazzAssignment().apply {
                caMarkingType = ClazzAssignment.MARKED_BY_PEERS
            }
        }
    )

    ClazzAssignmentEditScreen(uiStateVal)
}