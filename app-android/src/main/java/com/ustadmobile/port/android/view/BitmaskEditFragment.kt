package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentBitmaskEditBinding
import com.toughra.ustadmobile.databinding.ItemBitmaskBinding
import com.ustadmobile.core.controller.BitmaskEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.LongWrapper
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.BitmaskEditView
import org.kodein.di.android.x.di

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

    class BitmaskViewHolder(val itemBinding: ItemBitmaskBinding): RecyclerView.ViewHolder(itemBinding.root)

    class BitmaskRecyclerViewAdapter: ListAdapter<BitmaskFlag, BitmaskViewHolder>(DIFFUTIL_BITMASKFLAG) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BitmaskViewHolder {
            return BitmaskViewHolder(ItemBitmaskBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
        }

        override fun onBindViewHolder(holder: BitmaskViewHolder, position: Int) {
            holder.itemBinding.bitmaskFlag = getItem(position)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentBitmaskEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            mRecyclerView = it.fragmentBitmaskEditRecyclerView
        }

        mPresenter = BitmaskEditPresenter(requireContext(), arguments.toStringMap(), this,
                this, kodein)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        mRecyclerViewAdapter = BitmaskRecyclerViewAdapter()
        mRecyclerView?.adapter = mRecyclerViewAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.features_enabled)
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
            field = value
            mBinding?.fieldsEnabled = value
        }

    companion object {
        val DIFFUTIL_BITMASKFLAG = object: DiffUtil.ItemCallback<BitmaskFlag>() {
            override fun areItemsTheSame(oldItem: BitmaskFlag, newItem: BitmaskFlag): Boolean {
                return oldItem.flagVal == newItem.flagVal
            }

            override fun areContentsTheSame(oldItem: BitmaskFlag, newItem: BitmaskFlag): Boolean {
                return oldItem == newItem
            }
        }
    }
}