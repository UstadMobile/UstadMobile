package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzAssignmentEditBinding
import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.locale.entityconstants.FileTypeConstants
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.impl.locale.entityconstants.SubmissionPolicyConstants
import com.ustadmobile.core.impl.locale.entityconstants.TextLimitTypeConstants
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.viewmodel.ClazzAssignmentEditUiState
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.lib.db.entities.ext.shallowCopyCourseBlockWithEntity
import com.ustadmobile.port.android.view.binding.isSet
import com.ustadmobile.port.android.view.composable.UstadDateEditTextField
import com.ustadmobile.port.android.view.composable.*
import com.ustadmobile.port.android.view.composable.UstadMessageIdOptionExposedDropDownMenuField
import com.ustadmobile.port.android.view.composable.UstadTextEditField


class ClazzAssignmentEditFragment: UstadEditFragment<CourseBlockWithEntity>(), ClazzAssignmentEditView {

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
            it.groupSetEnabled = true
            it.caEditCommonFields.caDeadlineDateTextinput.setEndIconOnClickListener(clearDeadlineListener)
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
            mBinding?.gracePeriodVisibility = if(deadlineDate.isSet){
                View.VISIBLE
            }else{
                View.GONE
            }
            mBinding?.fileSubmissionVisibility = if(value?.assignment?.caRequireFileSubmission == true)
                View.VISIBLE else View.GONE

            mBinding?.textSubmissionVisibility = if(value?.assignment?.caRequireTextSubmission == true)
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

    private val onFileRequiredChanged: CompoundButton.OnCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        mBinding?.fileSubmissionVisibility = if(isChecked) View.VISIBLE else View.GONE
    }

    private val onTextRequiredChanged: CompoundButton.OnCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        mBinding?.textSubmissionVisibility = if(isChecked) View.VISIBLE else View.GONE
    }

}

@Composable
private fun ClazzAssignmentEditScreen(
    uiState: ClazzAssignmentEditUiState = ClazzAssignmentEditUiState(),
    onChangeCourseBlockWithEntity: (CourseBlockWithEntity?) -> Unit = {},
    onChangeCourseBlock: (CourseBlock?) -> Unit = {},
    onCaStartDateValueChange: (Long?) -> Unit = {},
    onClickSubmissionType: () -> Unit = {},
    onChangedFileRequired: (Boolean) -> Unit = {},
    onChangedTextRequired: (Boolean) -> Unit = {},
    onChangedAllowClassComments: (Boolean) -> Unit = {},
    onChangedAllowPrivateCommentsFromStudents: (Boolean) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        UstadTextEditField(
            value = uiState.entity?.assignment?.caTitle ?: "",
            label = stringResource(id = R.string.title),
            error = uiState.caTitleError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onChangeCourseBlockWithEntity(
                    uiState.entity?.shallowCopyCourseBlockWithEntity {
                        assignment?.caTitle = it
                })
            },
        )

        UstadTextEditField(
            value = uiState.entity?.assignment?.caDescription ?: "",
            label = stringResource(id = R.string.description).addOptionalSuffix(),
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onChangeCourseBlockWithEntity(
                    uiState.entity?.shallowCopyCourseBlockWithEntity{
                        assignment?.caDescription = it
                })
            },
        )

        UstadCourseBlockEdit(
            uiState = uiState.courseBlockEditUiState,
            onCourseBlockChange = onChangeCourseBlock
        )

        UstadTextEditField(
            value = uiState.groupSet?.cgsName ?: "",
            label = stringResource(id = R.string.submission_type),
            enabled = uiState.groupSetEnabled,
            onValueChange = {},
            onClick = onClickSubmissionType,
            modifier = Modifier.weight(0.5F)
        )

        SwitchRow(
            text = stringResource(id = R.string.require_file_submission),
            checked = uiState.entity?.assignment?.caRequireFileSubmission ?: false,
            onChange = { onChangedFileRequired(it) },
        )


        if (uiState.fileSubmissionVisible){
            UstadMessageIdOptionExposedDropDownMenuField(
                value = uiState.entity?.assignment?.caFileType ?: 0,
                label = stringResource(R.string.file_type),
                options = FileTypeConstants.FILE_TYPE_MESSAGE_IDS,
                onOptionSelected = {
                    onChangeCourseBlockWithEntity(
                        uiState.entity?.shallowCopyCourseBlockWithEntity{
                            assignment?.caFileType = it.value
                    })
                },
                enabled = uiState.fieldsEnabled,
            )

            UstadTextEditField(
                value = (uiState.entity?.assignment?.caSizeLimit ?: 0).toString(),
                label = stringResource(id = R.string.size_limit),
                enabled = uiState.fieldsEnabled,
                onValueChange = { newString ->
                    onChangeCourseBlockWithEntity(
                        uiState.entity?.shallowCopyCourseBlockWithEntity{
                            assignment?.caSizeLimit = newString.filter { it.isDigit() }.toIntOrNull() ?: 0
                    })
                },
                modifier = Modifier.weight(0.5F)
            )

            UstadTextEditField(
                value = (uiState.entity?.assignment?.caNumberOfFiles ?: 0).toString(),
                label = stringResource(id = R.string.number_of_files),
                enabled = uiState.fieldsEnabled,
                onValueChange = { newString ->
                    onChangeCourseBlockWithEntity(
                        uiState.entity?.shallowCopyCourseBlockWithEntity{
                            assignment?.caNumberOfFiles = newString.filter { it.isDigit() }.toIntOrNull() ?: 0
                    })
                },
                modifier = Modifier.weight(0.5F)
            )
        }

        SwitchRow(
            text = stringResource(id = R.string.require_text_submission),
            checked = uiState.entity?.assignment?.caRequireTextSubmission ?: false,
            onChange = { onChangedTextRequired(it) }
        )

        if (uiState.textSubmissionVisible) {
            UstadMessageIdOptionExposedDropDownMenuField(
                value = uiState.entity?.assignment?.caTextLimitType ?: 0,
                label = stringResource(R.string.limit),
                options = TextLimitTypeConstants.TEXT_LIMIT_TYPE_MESSAGE_IDS,
                onOptionSelected = {
                    onChangeCourseBlockWithEntity(
                        uiState.entity?.shallowCopyCourseBlockWithEntity{
                            assignment?.caTextLimitType = it.value
                    })
                },
                enabled = uiState.fieldsEnabled,
            )

            UstadTextEditField(
                value = (uiState.entity?.assignment?.caTextLimit ?: 0).toString(),
                label = stringResource(id = R.string.maximum),
                enabled = uiState.fieldsEnabled,
                onValueChange = { newString ->
                    onChangeCourseBlockWithEntity(
                        uiState.entity?.shallowCopyCourseBlockWithEntity{
                            assignment?.caTextLimit = newString.filter { it.isDigit() }.toIntOrNull() ?: 0
                    })
                },
                modifier = Modifier.weight(0.5F)
            )
        }

        UstadMessageIdOptionExposedDropDownMenuField(
            value = uiState.entity?.assignment?.caSubmissionPolicy ?: 0,
            label = stringResource(R.string.submission_policy),
            options = SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS,
            onOptionSelected = {
                onChangeCourseBlockWithEntity(
                    uiState.entity?.shallowCopyCourseBlockWithEntity{
                        assignment?.caSubmissionPolicy = it.value
                })
            },
            enabled = uiState.fieldsEnabled,
        )
    }

    SwitchRow(
        text = stringResource(id = R.string.allow_class_comments),
        checked = uiState.entity?.assignment?.caClassCommentEnabled ?: false,
        onChange = { onChangedAllowClassComments(it) }
    )

    SwitchRow(
        text = stringResource(id = R.string.allow_private_comments_from_students),
        checked = uiState.entity?.assignment?.caPrivateCommentsEnabled ?: false,
        onChange = { onChangedAllowPrivateCommentsFromStudents(it) }
    )
}

@Composable
fun TextValueRow(
    onValueChange: (Long) -> Unit = {},
    dateValue: Long,
    descriptionLabel: String,
    descriptionError: String?,
    descriptionMessage: String?,
    value: String,
    label: String,
    enabled: Boolean
){
    Row {
        Column(
            modifier = Modifier.weight(0.5F)
        ) {
            UstadDateEditTextField(
                value = dateValue,
                label = descriptionLabel,
                error = descriptionError,
                onValueChange = onValueChange,
            )

            Text(text = descriptionMessage ?: "")
        }

        Spacer(modifier = Modifier.width(10.dp))

        UstadTextEditField(
            value = value,
            label = label,
            enabled = enabled,
            onValueChange = {},
            modifier = Modifier.weight(0.5F)
        )
    }
}

@Composable
fun CompletionCriteriaRow(
    uiState: ClazzAssignmentEditUiState = ClazzAssignmentEditUiState(),
    onValueChange: (CourseBlockWithEntity?) -> Unit = {},
){
    Row {

        UstadMessageIdOptionExposedDropDownMenuField(
            value = uiState.entity?.cbCompletionCriteria ?: 0,
            label = stringResource(R.string.completion_criteria),
            options = PersonConstants.GENDER_MESSAGE_IDS,
            onOptionSelected = {
                onValueChange(uiState.entity?.shallowCopyCourseBlockWithEntity{
                    cbCompletionCriteria = it.value
                })
            },
            enabled = uiState.fieldsEnabled,
        )

        Spacer(modifier = Modifier.width(10.dp))

        if (uiState.minScoreVisible){
            UstadTextEditField(
                value = uiState.entity?.cbMaxPoints.toString(),
                label = stringResource(id = R.string.points),
                enabled = uiState.fieldsEnabled,
                onValueChange = {},
                modifier = Modifier.weight(0.5F)
            )
        }
    }
}

@Composable
private fun SwitchRow(
    text: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
){
    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ){

        Text(text)

        Switch(
            checked= checked,
            onCheckedChange = { onChange(it) }
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
            minScoreVisible = true,
            gracePeriodVisible = true,
        ),
    )
    MdcTheme {
        ClazzAssignmentEditScreen(uiStateVal)
    }
}