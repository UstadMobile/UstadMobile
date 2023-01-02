package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzLogEditBinding
import com.ustadmobile.core.controller.ClazzLogEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzLogEditView
import com.ustadmobile.core.viewmodel.ClazzLogEditUiState
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY
import com.ustadmobile.port.android.view.composable.UstadDateTimeEditTextField


interface ClazzLogEditFragmentEventHandler {

}

class ClazzLogEditFragment: UstadEditFragment<ClazzLog>(), ClazzLogEditView, ClazzLogEditFragmentEventHandler {

    private var mBinding: FragmentClazzLogEditBinding? = null

    private var mPresenter: ClazzLogEditPresenter? = null

    override var date: Long
        get() = mBinding?.date ?: 0
        set(value) {
            mBinding?.date = value
        }

    override var time: Long
        get() = mBinding?.time ?: 0
        set(value) {
            mBinding?.time = value
        }

    override var dateError: String?
        get() = mBinding?.dateError
        set(value) {
            mBinding?.dateError = value
        }

    override var timeZone: String?
        get() = mBinding?.timeZoneId
        set(value) {
            mBinding?.timeZoneId = value
        }

    override var timeError: String?
        get() = mBinding?.timeError
        set(value) {
            mBinding?.timeError = value
        }

    override val mEditPresenter: UstadEditPresenter<*, ClazzLog>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzLogEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = ClazzLogEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: ClazzLog? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzLog = value
            mBinding?.dateTimeMode = MODE_START_OF_DAY

        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }
}

@Composable
private fun ClazzLogEditScreen(
    uiState: ClazzLogEditUiState = ClazzLogEditUiState(),
    onChangeClazzLog: (ClazzLog?) -> Unit = {},
) {
    UstadDateTimeEditTextField(
        value = uiState.clazzLog?.logDate ?: 0L,
        dateLabel = stringResource(id = R.string.date),
        timeLabel = stringResource(R.string.time),
        timeZoneId = uiState.timeZone,
        modifier = Modifier.fillMaxWidth(),
        onValueChange = {
            onChangeClazzLog(uiState.clazzLog?.shallowCopy {
                logDate = it
            })
        },
    )
}

@Composable
@Preview
fun ClazzLogEditScreenPreview() {
    MdcTheme {
        ClazzLogEditScreen()
    }
}