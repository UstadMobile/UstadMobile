package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentScopedGrantEditBinding
import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.viewmodel.ScopedGrantEditUiState
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.ScopedGrant


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

@Composable
fun ScopedGrantEditScreen(
    uiState: ScopedGrantEditUiState = ScopedGrantEditUiState(),
    onClickEdit: () -> Unit = {},
    onClickDelete: () -> Unit = {},
) {
    uiState.bitmaskList.forEach { bitmask ->
        TextButton(
            onClick = onClickEdit
        ){
            Row{

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "valueText",
                        style = MaterialTheme.typography.body1,
                        color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                    )

                    Text(
                        text = "labelText",
                        style = MaterialTheme.typography.subtitle1,
                        color = colorResource(id = R.color.list_subheader),
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                TextButton(
                    onClick = onClickDelete
                ){
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = ""
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun ScopedGrantEditScreenPreview() {
    val uiState = ScopedGrantEditUiState(
        bitmaskList = listOf()
    )

    MdcTheme {
        ScopedGrantEditScreen(uiState)
    }
}