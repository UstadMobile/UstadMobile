package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentLeavingReasonEditBinding
import com.ustadmobile.core.controller.LeavingReasonEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.LeavingReasonEditView
import com.ustadmobile.core.viewmodel.LeavingReasonEditUiState
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.composable.UstadTextEditField


interface LeavingReasonEditFragmentEventHandler {

}

class LeavingReasonEditFragment: UstadEditFragment<LeavingReason>(), LeavingReasonEditView, LeavingReasonEditFragmentEventHandler {

    private var mBinding: FragmentLeavingReasonEditBinding? = null

    private var mPresenter: LeavingReasonEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, LeavingReason>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentLeavingReasonEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = LeavingReasonEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.new_leaving_reason, R.string.edit_leaving_reason)

        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: LeavingReason? = null
        get() = field
        set(value) {
            field = value
            mBinding?.leavingReason = value
        }

    override var reasonTitleError: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.reasonTitleError = value
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
fun LeavingReasonEditScreen(
    uiState: LeavingReasonEditUiState,
    onLeavingReasonChanged: (LeavingReason?) -> Unit = {},
){
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UstadTextEditField(
            value = uiState.leavingReason?.leavingReasonTitle ?: "",
            label = stringResource(id = R.string.description),
            error = uiState.reasonTitleError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onLeavingReasonChanged(uiState.leavingReason?.shallowCopy{
                    leavingReasonTitle = it
                })
            }
        )
    }
}

@Composable
@Preview
fun LeavingReasonEditPreview(){
    LeavingReasonEditScreen(
        uiState = LeavingReasonEditUiState(
            leavingReason = LeavingReason().apply {
                leavingReasonTitle = "Leaving because of something..."
            }
        )
    )
}