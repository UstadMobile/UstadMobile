package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.FragmentClazzList2Binding
import com.toughra.ustadmobile.databinding.ItemClazzlist2ClazzBinding
import com.ustadmobile.core.controller.ClazzList2Presenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.SortOption
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.port.android.view.binding.SortOptionSelectedListener

class ClazzList2Fragment: UstadBaseFragment(), ClazzList2View, SortOptionSelectedListener{

    private var mPresenter: ClazzList2Presenter? = null

    private var rootViewBinding: FragmentClazzList2Binding? = null

    private var dbRepo: UmAppDatabase? = null

    class ClazzList2RecyclerAdapter(var presenter: ClazzList2Presenter?): PagedListAdapter<ClazzWithNumStudents, ClazzList2RecyclerAdapter.ClazzList2ViewHolder>(DIFF_CALLBACK) {

        class ClazzList2ViewHolder(val itemBinding: ItemClazzlist2ClazzBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzList2ViewHolder {
            val itemBinding = ItemClazzlist2ClazzBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            return ClazzList2ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ClazzList2ViewHolder, position: Int) {
            holder.itemBinding.clazz = getItem(position)
            holder.itemBinding.presenter = presenter
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootViewBinding = FragmentClazzList2Binding.inflate(inflater, container, false)
        mPresenter = ClazzList2Presenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UstadMobileSystemImpl.instance)
        mPresenter?.onCreate(null)
        rootViewBinding?.presenter = mPresenter
        rootViewBinding?.onSortSelected = this
        return rootViewBinding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootViewBinding = null
        mPresenter = null
    }

    override var addButtonVisible: Boolean = false
        get() = field
        set(value) {
            rootViewBinding?.addVisible = value
            field = value
        }

    override var sortOptions: List<ClazzList2Presenter.SortOrder>? = null
        get() = field
        set(value) {
            rootViewBinding?.sortOptions = value?.map { ClazzList2Presenter.ClazzListSortOption(it, requireContext()) }
            field = value
        }

    override var clazzList: DataSource.Factory<Int, ClazzWithNumStudents>? = null
        get() = field
        set(value) {
            val dbRepoVal = dbRepo ?: return
            val adapter = ClazzList2RecyclerAdapter(mPresenter)
            val liveData = value?.asRepositoryLiveData(dbRepoVal)
            liveData?.observe(this, Observer {
                adapter.submitList(it)
            })
        }

    override val viewContext: Any
        get() = requireContext()

    override fun onSortOptionSelected(view: AdapterView<*>?, sortOption: SortOption) {
        if(sortOption !is ClazzList2Presenter.ClazzListSortOption) return
        mPresenter?.handleClickSortOrder(sortOption.sortOrder)
    }

    override fun onNoSortItemSelected(view: AdapterView<*>?) {
        //do nothing
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzWithNumStudents> = object
            : DiffUtil.ItemCallback<ClazzWithNumStudents>() {
            override fun areItemsTheSame(oldItem: ClazzWithNumStudents,
                                         newItem: ClazzWithNumStudents): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }

            override fun areContentsTheSame(oldItem: ClazzWithNumStudents,
                                            newItem: ClazzWithNumStudents): Boolean {
                return oldItem == newItem
            }
        }
    }
}