package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentBitmaskEditBinding
import com.ustadmobile.core.controller.BitmaskEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.LongWrapper
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.BitmaskEditView
import com.ustadmobile.core.R as CR

interface BitmaskEditFragmentEventHandler {

}

class BitmaskEditFragment: UstadEditFragment<LongWrapper>(), BitmaskEditView,
        BitmaskEditFragmentEventHandler, Observer<List<BitmaskFlag>?> {

    private var mBinding: FragmentBitmaskEditBinding? = null

    private var mPresenter: BitmaskEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, LongWrapper>?
        get() = mPresenter

    override var entity: LongWrapper? = null
        get() = field
        set(value) {
            field = value
            mBinding?.bitmask = value?.longValue
        }

    override var bitmaskList: LiveData<List<BitmaskFlag>>? = null
        get() = field
        set(value) {
            field?.removeObserver(this)
            field = value
            value?.observe(this, this)
        }

    override fun onChanged(t: List<BitmaskFlag>?) {
        mRecyclerViewAdapter?.submitList(t)
    }

    private var mRecyclerViewAdapter: BitmaskRecyclerViewAdapter? = null

    private var mRecyclerView: RecyclerView? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentBitmaskEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            mRecyclerView = it.fragmentBitmaskEditRecyclerView
        }

        mPresenter = BitmaskEditPresenter(requireContext(), arguments.toStringMap(), this,
                 di, viewLifecycleOwner).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        mRecyclerViewAdapter = BitmaskRecyclerViewAdapter()
        mRecyclerView?.adapter = mRecyclerViewAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(CR.string.features_enabled, CR.string.features_enabled)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }


    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }


}