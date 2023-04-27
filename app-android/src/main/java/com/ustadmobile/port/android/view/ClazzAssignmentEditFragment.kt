package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.widget.doAfterTextChanged
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzAssignmentEditBinding
import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.locale.entityconstants.*
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.core.viewmodel.ClazzAssignmentEditUiState
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.lib.db.entities.ext.shallowCopyWithEntity
import com.ustadmobile.port.android.util.compose.courseTerminologyEntryResource
import com.ustadmobile.port.android.util.compose.rememberCourseTerminologyEntries
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.binding.isSet
import com.ustadmobile.port.android.view.composable.*


class ClazzAssignmentEditFragment: UstadEditFragment<CourseBlockWithEntity>(), ClazzAssignmentEditView,
    DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<IdOption>  {

    private var mBinding: FragmentClazzAssignmentEditBinding? = null

    private var mPresenter: ClazzAssignmentEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseBlockWithEntity>?
        get() = mPresenter


    private var clearDeadlineListener: View.OnClickListener = View.OnClickListener {
        val entityVal = entity
        deadlineDate = Long.MAX_VALUE
        gracePeriodDate = Long.MAX_VALUE
        deadlineTime = 0
        gracePeriodTime = 0
        entityVal?.cbLateSubmissionPenalty = 0
        entity = entityVal
    }

    var currentDeadlineDate: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzAssignmentEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fileRequiredListener = onFileRequiredChanged
            it.textRequiredListener = onTextRequiredChanged
            it.markingTypeSelectionListener = this
            it.groupSetEnabled = true
            it.markingTypeEnabled = true
            it.caEditCommonFields.caDeadlineDateTextinput.setEndIconOnClickListener(clearDeadlineListener)
            // onClick on viewBinding caused problems so set here on the fragment
            it.caEditAssignReviewersButton.setOnClickListener {
                mPresenter?.handleAssignReviewersClicked()
            }
            it.caEditCommonFields.caDeadlineDate.doAfterTextChanged{ editable ->
                if(editable.isNullOrEmpty()){
                    return@doAfterTextChanged
                }
                if(editable.toString() == currentDeadlineDate){
                    mBinding?.takeIf { bind -> bind.gracePeriodVisibility == View.GONE }.also {
                        mBinding?.gracePeriodVisibility = View.VISIBLE
                    }
                    return@doAfterTextChanged
                }
                mBinding?.gracePeriodVisibility = View.VISIBLE
                currentDeadlineDate = it.toString()
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.new_assignment, R.string.edit_assignment)

        mPresenter = ClazzAssignmentEditPresenter(requireContext(), arguments.toStringMap(),
                this, viewLifecycleOwner, di)

        mBinding?.mPresenter = mPresenter
        mPresenter?.onCreate(backStackSavedState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: CourseBlockWithEntity? = null
        get() = field
        set(value) {
            field = value
            mBinding?.blockWithAssignment = value

            mBinding?.gracePeriodVisibility = if(deadlineDate.isSet)
                View.VISIBLE else View.GONE

            mBinding?.fileSubmissionVisibility = if(value?.assignment?.caRequireFileSubmission == true)
                View.VISIBLE else View.GONE

            mBinding?.textSubmissionVisibility = if(value?.assignment?.caRequireTextSubmission == true)
                View.VISIBLE else View.GONE

            mBinding?.peersVisibility = if(value?.assignment?.caMarkingType == ClazzAssignment.MARKED_BY_PEERS)
                View.VISIBLE else View.GONE
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var caGracePeriodError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.caGracePeriodError = value
        }
    override var caDeadlineError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.caDeadlineError = value
        }

    override var caTitleError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.caTitleError = value
        }
    override var caStartDateError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.caStartDateError = value
        }

    override var caMaxPointsError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.caMaxPointsError = value
        }

    override var startDate: Long
        get() = mBinding?.startDate ?: 0
        set(value) {
            mBinding?.startDate = value
        }

    override var startTime: Long
        get() = mBinding?.startTime ?: 0
        set(value) {
            mBinding?.startTime = value
        }

    override var deadlineDate: Long
        get() = mBinding?.deadlineDate ?: Long.MAX_VALUE
        set(value) {
            mBinding?.deadlineDate = value
        }

    override var deadlineTime: Long
        get() = mBinding?.deadlineTime ?: 0
        set(value) {
            mBinding?.deadlineTime = value
        }

    override var gracePeriodDate: Long
        get() = mBinding?.gracePeriodDate ?: Long.MAX_VALUE
        set(value) {
            mBinding?.gracePeriodDate = value
        }

    override var gracePeriodTime: Long
        get() = mBinding?.gracePeriodTime ?: 0
        set(value) {
            mBinding?.gracePeriodTime = value
        }

    override var timeZone: String? = null
        set(value) {
            mBinding?.timeZone = value
            field = value
        }
    override var groupSet: CourseGroupSet? = null
        set(value) {
            field = value
            mBinding?.groupSet = value
        }

    override var reviewerCountError: String? = null
        set(value) {
            field = value
            mBinding?.reviewerCountError = value
        }

    override var submissionPolicyOptions: List<ClazzAssignmentEditPresenter.SubmissionPolicyOptionsMessageIdOption>? = null
        get() = field
        set(value) {
            field = value
            mBinding?.submissionPolicy = value
        }

    override var fileTypeOptions: List<ClazzAssignmentEditPresenter.FileTypeOptionsMessageIdOption>? = null
        get() = field
        set(value) {
            field = value
            mBinding?.fileTypeOptions = value
        }


    override var textLimitTypeOptions: List<ClazzAssignmentEditPresenter.TextLimitTypeOptionsMessageIdOption>? = null
        set(value) {
            field = value
            mBinding?.textLimitTypeOptions = textLimitTypeOptions
        }

    override var completionCriteriaOptions: List<ClazzAssignmentEditPresenter.CompletionCriteriaOptionsMessageIdOption>? = null
        get() = field
        set(value) {
            field = value
            mBinding?.completionCriteriaOptions = value
        }

    override var markingTypeOptions: List<IdOption>? = null
        get() = field
        set(value) {
            field = value
            mBinding?.markingTypeOptions = value
        }

    override var groupSetEnabled: Boolean = true
        get() = field
        set(value) {
            if(field == value){
                return
            }
            field = value
            mBinding?.groupSetEnabled = value
        }

    override var markingTypeEnabled: Boolean = true
        get() = field
        set(value) {
            if(field == value){
                return
            }
            field = value
            mBinding?.markingTypeEnabled = value
        }

    private val onFileRequiredChanged: CompoundButton.OnCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        mBinding?.fileSubmissionVisibility = if(isChecked) View.VISIBLE else View.GONE
    }

    private val onTextRequiredChanged: CompoundButton.OnCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        mBinding?.textSubmissionVisibility = if(isChecked) View.VISIBLE else View.GONE
    }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: IdOption) {
        mBinding?.peersVisibility = if(selectedOption.optionId == ClazzAssignment.MARKED_BY_PEERS)
            View.VISIBLE else View.GONE
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {

    }

}

@Composable
private fun ClazzAssignmentEditScreen(
    uiState: ClazzAssignmentEditUiState = ClazzAssignmentEditUiState(),
    onChangeCourseBlockWithEntity: (CourseBlockWithEntity?) -> Unit = {},
    onChangeCourseBlock: (CourseBlock?) -> Unit = {},
    onClickSubmissionType: () -> Unit = {},
    onChangedFileRequired: (Boolean) -> Unit = {},
    onChangedTextRequired: (Boolean) -> Unit = {},
    onChangedAllowClassComments: (Boolean) -> Unit = {},
    onChangedAllowPrivateCommentsFromStudents: (Boolean) -> Unit = {},
    onClickAssignReviewers: () -> Unit = {},
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
            onCourseBlockChange = onChangeCourseBlock
        )

        UstadClickableTextField(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("cgsName"),
            value = uiState.groupSet?.cgsName ?: "",
            label = { Text(stringResource(id = R.string.submission_type)) },
            enabled = uiState.groupSetEnabled,
            onClick = onClickSubmissionType,
            onValueChange = {}
        )

        UstadSwitchField(
            modifier = Modifier
                .defaultItemPadding()
                .testTag("caRequireFileSubmission"),
            label = stringResource(id = R.string.require_file_submission),
            checked = uiState.entity?.assignment?.caRequireFileSubmission ?: false,
            onChange = { onChangedFileRequired(it) },
            enabled = uiState.fieldsEnabled
        )

        if (uiState.fileSubmissionVisible){
            UstadMessageIdOptionExposedDropDownMenuField(
                modifier = Modifier
                    .defaultItemPadding()
                    .testTag("caFileType"),
                value = uiState.entity?.assignment?.caFileType ?: 0,
                label = stringResource(R.string.file_type),
                options = FileTypeConstants.FILE_TYPE_MESSAGE_IDS,
                enabled = uiState.fieldsEnabled,
                onOptionSelected = {
                    onChangeCourseBlockWithEntity(
                        uiState.entity?.shallowCopyWithEntity{
                            assignment = uiState.entity?.assignment?.shallowCopy {
                                caFileType = it.value
                            }
                        })
                },
            )

            UstadNumberTextField(
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth()
                    .testTag("caSizeLimit"),
                value = (uiState.entity?.assignment?.caSizeLimit ?: 0).toFloat(),
                label = { Text(stringResource(id = R.string.size_limit)) },
                enabled = uiState.fieldsEnabled,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
                onValueChange = {
                    onChangeCourseBlockWithEntity(
                        uiState.entity?.shallowCopyWithEntity{
                            assignment = uiState.entity?.assignment?.shallowCopy {
                                caSizeLimit = it.toInt()
                            }
                        })
                },
            )

            UstadNumberTextField(
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth()
                    .testTag("caNumberOfFiles"),
                value = (uiState.entity?.assignment?.caNumberOfFiles ?: 0).toFloat(),
                label = { Text(stringResource(id = R.string.number_of_files)) },
                enabled = uiState.fieldsEnabled,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = {
                    onChangeCourseBlockWithEntity(
                        uiState.entity?.shallowCopyWithEntity{
                            assignment = uiState.entity?.assignment?.shallowCopy {
                                caNumberOfFiles = it.toInt()
                            }
                        })
                },
            )
        }

        UstadSwitchField(
            modifier = Modifier
                .defaultItemPadding()
                .testTag("caRequireTextSubmission"),
            label = stringResource(id = R.string.require_text_submission),
            checked = uiState.entity?.assignment?.caRequireTextSubmission ?: false,
            onChange = { onChangedTextRequired(it) },
            enabled = uiState.fieldsEnabled
        )

        if (uiState.textSubmissionVisible) {
            UstadMessageIdOptionExposedDropDownMenuField(
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth()
                    .testTag("caTextLimitType"),
                value = uiState.entity?.assignment?.caTextLimitType ?: 0,
                label = stringResource(R.string.limit),
                options = TextLimitTypeConstants.TEXT_LIMIT_TYPE_MESSAGE_IDS,
                enabled = uiState.fieldsEnabled,
                onOptionSelected = {
                    onChangeCourseBlockWithEntity(
                        uiState.entity?.shallowCopyWithEntity{
                            assignment = uiState.entity?.assignment?.shallowCopy {
                                caTextLimitType = it.value
                            }
                        })
                },
            )

            UstadNumberTextField(
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth()
                    .testTag("caTextLimit"),
                value = (uiState.entity?.assignment?.caTextLimit ?: 0).toFloat(),
                label = { Text(stringResource(id = R.string.maximum)) },
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onChangeCourseBlockWithEntity(
                        uiState.entity?.shallowCopyWithEntity{
                            assignment = uiState.entity?.assignment?.shallowCopy {
                                caTextLimit = it.toInt()
                            }
                        })
                },
            )

        }

        UstadMessageIdOptionExposedDropDownMenuField(
            modifier = Modifier
                .defaultItemPadding()
                .testTag("caSubmissionPolicy"),
            value = uiState.entity?.assignment?.caSubmissionPolicy ?: 0,
            label = stringResource(R.string.submission_policy),
            options = SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS,
            enabled = uiState.fieldsEnabled,
            onOptionSelected = {
                onChangeCourseBlockWithEntity(
                    uiState.entity?.shallowCopyWithEntity{
                        assignment = uiState.entity?.assignment?.shallowCopy {
                            caSubmissionPolicy = it.value
                        }
                    })
            },
        )

        UstadExposedDropDownMenuField(
            modifier = Modifier
                .defaultItemPadding()
                .testTag("caMarkingType"),
            value = uiState.entity?.assignment?.caMarkingType ?: ClazzAssignment.MARKED_BY_COURSE_LEADER,
            label = stringResource(R.string.marked_by),
            options = MarkingTypeConstants.MARKING_TYPE_MESSAGE_IDS,
            enabled = uiState.markingTypeEnabled,
            itemText = { markingType ->
                val messageId = MarkingTypeConstants.MARKING_TYPE_MESSAGE_IDS.first {
                    it.value == markingType
                }.messageId
                courseTerminologyEntryResource(
                    terminologyEntries, messageId)
            },
            onOptionSelected = {
                onChangeCourseBlockWithEntity(
                    uiState.entity?.shallowCopyWithEntity{
                        assignment = uiState.entity?.assignment?.shallowCopy {
                            caMarkingType = (it as MessageIdOption2).value
                        }
                    })
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
                        label = { Text(stringResource(id = R.string.reviews_per_user_group)) },
                        enabled = uiState.fieldsEnabled,
                        isError = uiState.reviewerCountError != null,
                        onValueChange = {
                            onChangeCourseBlockWithEntity(
                                uiState.entity?.shallowCopyWithEntity{
                                    assignment = uiState.entity?.assignment?.shallowCopy {
                                        caPeerReviewerCount = it.toInt()
                                    }
                                })
                        },
                    )
                }

                OutlinedButton(
                    onClick = onClickAssignReviewers,
                    modifier = Modifier
                        .weight(0.3F)
                        .testTag("buttonAssignReviewers"),
                    enabled = uiState.fieldsEnabled,
                ) {
                    Text(stringResource(R.string.assign_reviewers))
                }
            }
        }

        UstadSwitchField(
            modifier = Modifier
                .defaultItemPadding()
                .testTag("caClassCommentEnabled"),
            label = stringResource(id = R.string.allow_class_comments),
            checked = uiState.entity?.assignment?.caClassCommentEnabled ?: false,
            onChange = { onChangedAllowClassComments(it) },
            enabled = uiState.fieldsEnabled
        )

        UstadSwitchField(
            modifier = Modifier
                .defaultItemPadding()
                .testTag("caPrivateCommentsEnabled"),
            label = stringResource(id = R.string.allow_private_comments_from_students),
            checked = uiState.entity?.assignment?.caPrivateCommentsEnabled ?: false,
            onChange = { onChangedAllowPrivateCommentsFromStudents(it) },
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
            gracePeriodVisible = true,
        ),
        minScoreVisible = true,
        textSubmissionVisible = true,
        fileSubmissionVisible = true,
        entity = CourseBlockWithEntity().apply {
            assignment = ClazzAssignment().apply {
                caMarkingType = ClazzAssignment.MARKED_BY_PEERS
            }
        }
    )

    ClazzAssignmentEditScreen(uiStateVal)
}