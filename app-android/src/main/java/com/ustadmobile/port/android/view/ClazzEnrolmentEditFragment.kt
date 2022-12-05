package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzEnrolmentBinding
import com.ustadmobile.core.controller.ClazzEnrolmentEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.impl.locale.entityconstants.RoleConstants
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzEnrolmentEditView
import com.ustadmobile.core.viewmodel.ClazzEnrolmentEditUiState
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.binding.MODE_END_OF_DAY
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY
import com.ustadmobile.port.android.view.composable.UstadMessageIdOptionExposedDropDownMenuField
import java.util.*



class ClazzEnrolmentEditFragment: UstadEditFragment<ClazzEnrolmentWithLeavingReason>(),
        ClazzEnrolmentEditView,
        DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<IdOption>  {

    private var mBinding: FragmentClazzEnrolmentBinding? = null

    private var mPresenter: ClazzEnrolmentEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzEnrolmentWithLeavingReason>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mBinding = FragmentClazzEnrolmentBinding.inflate(inflater, container, false).also {
            it.presenter = mPresenter
            it.statusSelectorListener = this
        }

        mPresenter = ClazzEnrolmentEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mBinding?.presenter = mPresenter

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ClazzEnrolmentEditScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.new_enrolment, R.string.edit_enrolment)
        val navController = findNavController()

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: ClazzEnrolmentWithLeavingReason? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzEnrolment = value
            mBinding?.dateTimeMode = MODE_START_OF_DAY
            mBinding?.dateTimeModeEnd = MODE_END_OF_DAY
            mBinding?.timeZoneId = value?.timeZone?:"UTC"
        }

    override var roleList: List<IdOption>? = null
        get() = field
        set(value) {
            field = value
            mBinding?.roleOptions = value
        }
    override var statusList: List<IdOption>? = null
        get() = field
        set(value) {
            field = value
            mBinding?.statusOptions = value
        }

    override var startDateErrorWithDate: Pair<String, Long>? = null
        get() = field
        set(value) {
            field = value
            val startDateValue: String? = if(value?.first?.contains("%1\$s") == true){
                val dateFormat = DateFormat.getDateFormat(requireContext())
                value.first.replace("%1\$s", dateFormat.format(Date(value.second)))
            }else{
                value?.first
            }
            mBinding?.startDateError = startDateValue
        }

    override var endDateError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.endDateError = value
        }

    override var roleSelectionError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.roleSelectedError = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: IdOption) {
        mBinding?.clazzEnrolmentEditReasonTextinputlayout?.isEnabled = selectedOption.optionId != ClazzEnrolment.OUTCOME_IN_PROGRESS
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {

    }
}

@Composable
fun ClazzEnrolmentEditScreen(
    uiState: ClazzEnrolmentEditUiState = ClazzEnrolmentEditUiState(),
    onClazzEnrolmentChanged: (ClazzEnrolmentWithLeavingReason?) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
    )  {

        UstadMessageIdOptionExposedDropDownMenuField(
            value = uiState.clazzEnrolment?.clazzEnrolmentRole ?: 0,
            label = stringResource(R.string.role),
            options = RoleConstants.ROLE_MESSAGE_IDS,
            error = uiState.roleSelectedError,
            enabled = uiState.fieldsEnabled,
            onOptionSelected = {
                onClazzEnrolmentChanged(uiState.clazzEnrolment?.shallowCopy{
                    clazzEnrolmentRole = it.value
                })
            },
        )

        Spacer(modifier = Modifier.width(5.dp))

    }
}

@Composable
@Preview
fun ClazzEnrolmentEditScreenPreview() {
    val uiState = ClazzEnrolmentEditUiState(
        clazzEnrolment = ClazzEnrolmentWithLeavingReason().apply {
        },
    )

    MdcTheme {
        ClazzEnrolmentEditScreen(uiState)
    }
}