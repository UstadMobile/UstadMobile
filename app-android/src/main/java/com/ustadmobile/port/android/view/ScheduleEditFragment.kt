package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentScheduleEditBinding
import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.locale.entityconstants.DayConstants
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.core.viewmodel.ScheduleEditUiState
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.view.composable.UstadDateEditTextField
import com.ustadmobile.port.android.view.composable.UstadMessageIdOptionExposedDropDownMenuField

class ScheduleEditFragment: UstadEditFragment<Schedule>(), ScheduleEditView {

    private var mBinding: FragmentScheduleEditBinding? = null

    private var mPresenter: ScheduleEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Schedule>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentScheduleEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = ScheduleEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setEditFragmentTitle(R.string.add_a_schedule, R.string.edit_schedule)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var loading: Boolean = false

    override var entity: Schedule? = null
        get() = field
        set(value) {
            field = value
            mBinding?.schedule = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var dayOptions: List<ScheduleEditPresenter.DayMessageIdOption>? = null
        get() = field
        set(value) {
            mBinding?.dayOptions = value
            field = value
        }


    override var fromTimeError: String?
        get() = mBinding?.fromTimeError
        set(value) {
            mBinding?.fromTimeError = value
        }

    override var toTimeError: String?
        get() = mBinding?.toTimeError
        set(value) {
            mBinding?.toTimeError = value
        }
}

@Composable
private fun ScheduleEditScreen(
    uiState: ScheduleEditUiState = ScheduleEditUiState(),
    onScheduleChanged: (Schedule?) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        UstadMessageIdOptionExposedDropDownMenuField(
            value = uiState.entity?.scheduleDay ?: 0,
            label = stringResource(R.string.day),
            options = DayConstants.DAY_MESSAGE_IDS,
            onOptionSelected = {
                onScheduleChanged(uiState.entity?.shallowCopy{
                    scheduleDay = it.value
                })
            },
            enabled = uiState.fieldsEnabled,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row {
            UstadDateEditTextField(
                modifier = Modifier.weight(0.5F),
                value = uiState.entity?.sceduleStartTime ?: 0,
                label = stringResource(id = R.string.from),
                error = uiState.fromTimeError,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onScheduleChanged(uiState.entity?.shallowCopy{
                        sceduleStartTime = it
                    })
                }
            )

            Spacer(modifier = Modifier.width(10.dp))

            UstadDateEditTextField(
                modifier = Modifier.weight(0.5F),
                value = uiState.entity?.scheduleEndTime ?: 0,
                label = stringResource(id = R.string.to),
                error = uiState.toTimeError,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onScheduleChanged(uiState.entity?.shallowCopy{
                        scheduleEndTime = it
                    })
                }
            )
        }
    }
}

@Composable
@Preview
fun ScheduleEditScreenPreview() {
    MdcTheme {
        ScheduleEditScreen()
    }
}