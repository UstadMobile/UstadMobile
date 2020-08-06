package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemClazzWorkListBinding
import com.ustadmobile.core.controller.ClazzWorkListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzWorkListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.ClazzWorkWithMetrics
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class ClazzWorkListFragment(): UstadListViewFragment<ClazzWork, ClazzWorkWithMetrics>(),
        ClazzWorkListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ClazzWorkListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzWorkWithMetrics>?
        get() = mPresenter

    class ClazzWorkListViewHolder(val itemBinding: ItemClazzWorkListBinding): RecyclerView.ViewHolder(itemBinding.root)

    class ClazzWorkListRecyclerAdapter(var presenter: ClazzWorkListPresenter?,
                                       var canSeeResult: Boolean = false)
        : SelectablePagedListAdapter<ClazzWorkWithMetrics, ClazzWorkListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkListViewHolder {
            val itemBinding = ItemClazzWorkListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            itemBinding.showMetrics = canSeeResult
            return ClazzWorkListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ClazzWorkListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.clazzWork = item
            holder.itemBinding.showMetrics = canSeeResult
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ClazzWorkListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, this)

        mDataRecyclerViewAdapter = ClazzWorkListRecyclerAdapter(mPresenter, hasResultViewPermission)

        val createNewText = requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.clazz_work))
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.clazz_work)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout) {
            val clazzUid = arguments?.get(UstadView.ARG_FILTER_BY_CLAZZUID).toString().toLong() ?: 0L
            val newClazzWork: ClazzWork = ClazzWork().apply {
                clazzWorkClazzUid = clazzUid
            }
            navigateToEditEntity(null, R.id.clazzwork_edit_dest, ClazzWork::class.java)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzWorkDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzWorkWithMetrics> = object
            : DiffUtil.ItemCallback<ClazzWorkWithMetrics>() {
            override fun areItemsTheSame(oldItem: ClazzWorkWithMetrics,
                                         newItem: ClazzWorkWithMetrics): Boolean {
                return oldItem.clazzWorkUid == newItem.clazzWorkUid
            }

            override fun areContentsTheSame(oldItem: ClazzWorkWithMetrics,
                                            newItem: ClazzWorkWithMetrics): Boolean {
                return oldItem == newItem
            }
        }
    }

    override var hasResultViewPermission: Boolean = false
        get() = field
        set(value){
            (mDataRecyclerViewAdapter as ClazzWorkListRecyclerAdapter).canSeeResult = value
        }
}