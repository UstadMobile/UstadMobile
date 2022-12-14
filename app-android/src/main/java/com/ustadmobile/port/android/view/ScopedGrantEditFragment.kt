package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.databinding.FragmentScopedGrantEditBinding
import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.viewmodel.ScopedGrantEditUiState
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.port.android.util.compose.messageIdResource


interface ScopedGrantEditFragmentEventHandler {

}

class ScopedGrantEditFragment: UstadEditFragment<ScopedGrant>(), ScopedGrantEditView,
    ScopedGrantEditFragmentEventHandler, Observer<List<BitmaskFlag>> {

    private var mBinding: FragmentScopedGrantEditBinding? = null

    private var mPresenter: ScopedGrantEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ScopedGrant>?
        get() = mPresenter

    private var mRecyclerAdapter: BitmaskRecyclerViewAdapter? = null

    override var bitmaskList: LiveData<List<BitmaskFlag>>? = null
        set(value) {
            field?.removeObserver(this)
            field = value
            field?.observe(viewLifecycleOwner, this)
        }

    override fun onChanged(t: List<BitmaskFlag>?) {
        mRecyclerAdapter?.submitList(t)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mRecyclerAdapter = BitmaskRecyclerViewAdapter()
        mBinding = FragmentScopedGrantEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentScopedGrantEditRecyclerView.adapter = mRecyclerAdapter
            it.fragmentScopedGrantEditRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

        mPresenter = ScopedGrantEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mBinding?.fragmentScopedGrantEditRecyclerView?.adapter = null
        mRecyclerAdapter = null
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: ScopedGrant? = null
        get() = field
        set(value) {
            field = value
            mBinding?.scopedGrant = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScopedGrantEditScreen(
    uiState: ScopedGrantEditUiState = ScopedGrantEditUiState(),
    onChangedBitmask: (BitmaskFlag?) -> Unit = {},
) {
    uiState.bitmaskList.forEach { bitmask ->

        ListItem(
            text = { Text(messageIdResource(id = bitmask.messageId)) },
            trailing = {
                Switch(
                    checked = bitmask.enabled,
                    onCheckedChange = {
                        onChangedBitmask(bitmask.copy(
                            enabled = it
                        ))
                    }
                )
            }
        )
    }
}

@Composable
@Preview
fun ScopedGrantEditScreenPreview() {
    val uiState = ScopedGrantEditUiState(
        bitmaskList = listOf(
            BitmaskFlag(
                messageId = MessageID.incident_id,
                flagVal = 0
            )
        )
    )

    MdcTheme {
        ScopedGrantEditScreen(uiState)
    }
}