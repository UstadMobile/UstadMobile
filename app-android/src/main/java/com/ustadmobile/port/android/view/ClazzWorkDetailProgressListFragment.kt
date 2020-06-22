package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemClazzMemberWithClazzWorkProgressListBinding
import com.toughra.ustadmobile.databinding.ItemClazzworkProgressDetailBinding
import com.ustadmobile.core.controller.ClazzWorkDetailProgressListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzWorkDetailProgressListView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazzWorkProgress
import com.ustadmobile.lib.db.entities.ClazzWorkWithMetrics
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class ClazzWorkDetailProgressListFragment(): UstadListViewFragment<ClazzMemberWithClazzWorkProgress,
        ClazzMemberWithClazzWorkProgress>(), ClazzWorkDetailProgressListView,
        MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ClazzWorkDetailProgressListPresenter? = null

    override var autoMergeRecyclerViewAdapter: Boolean = false

    private var metricsRecyclerAdapter : ClazzWorkProgressRecyclerAdapter? = null

    class ClazzWorkProgressRecyclerAdapter(clazzWork: ClazzWorkWithMetrics?,
                                               visible: Boolean = false)
        : ListAdapter<ClazzWorkWithMetrics,
            ClazzWorkProgressRecyclerAdapter.ClazzWorkProgressViewHolder>(
            DU_CLAZZWORKWITHMETRICS) {

        var visible: Boolean = visible
            set(value) {
                if(field == value)
                    return

                field = value
            }

        class ClazzWorkProgressViewHolder(var itemBinding: ItemClazzworkProgressDetailBinding)
            : RecyclerView.ViewHolder(itemBinding.root)

        private var viewHolder: ClazzWorkProgressViewHolder? = null
        private var clazzWorkVal : ClazzWorkWithMetrics? = clazzWork

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkProgressViewHolder {
            return ClazzWorkProgressViewHolder(
                    ItemClazzworkProgressDetailBinding.inflate(LayoutInflater.from(parent.context),
                            parent, false).also {
                    })
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            viewHolder = null
        }

        override fun getItemCount(): Int {
            return if(visible) 1 else 0
        }

        override fun onBindViewHolder(holder: ClazzWorkProgressViewHolder, position: Int) {

            holder.itemView.tag = clazzWorkVal?.clazzWorkUid?:0L
            if(currentList.size > 0){
                holder.itemBinding.clazzWorkWithMetrics = getItem(0)
                holder.itemView.tag = getItem(position).clazzWorkUid
            }else {
                holder.itemBinding.clazzWorkWithMetrics = clazzWorkVal
            }
        }
    }

    override val listPresenter: UstadListPresenter<*, in ClazzMemberWithClazzWorkProgress>?
        get() = mPresenter

    class ClazzMemberWithClazzWorkProgressListViewHolder(val itemBinding
        : ItemClazzMemberWithClazzWorkProgressListBinding): RecyclerView.ViewHolder(itemBinding.root)

    class ClazzMemberWithClazzWorkProgressListRecyclerAdapter(
            var presenter: ClazzWorkDetailProgressListPresenter?)
        : SelectablePagedListAdapter<ClazzMemberWithClazzWorkProgress,
            ClazzMemberWithClazzWorkProgressListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : ClazzMemberWithClazzWorkProgressListViewHolder {
            val itemBinding = ItemClazzMemberWithClazzWorkProgressListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return ClazzMemberWithClazzWorkProgressListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ClazzMemberWithClazzWorkProgressListViewHolder,
                                      position: Int) {
            val item = getItem(position)
            holder.itemBinding.clazzMemberWithClazzWorkProgress = item
            holder.itemView.tag = item?.mClazzMember?.clazzMemberUid?:0L
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
        mPresenter = ClazzWorkDetailProgressListPresenter(requireContext(),
                UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        metricsRecyclerAdapter = ClazzWorkProgressRecyclerAdapter(clazzWorkWithMetricsFlat, false)
        mDataRecyclerViewAdapter = ClazzMemberWithClazzWorkProgressListRecyclerAdapter(mPresenter)

        mMergeRecyclerViewAdapter = MergeAdapter(metricsRecyclerAdapter,
                mDataRecyclerViewAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter

        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.student_progress)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        //TODO: Check if we need this or its been sent to Presenter
//        if(view?.id == R.id.item_createnew_layout)
//            navigateToEditEntity(null, R.id.clazzmemberwithclazzworkprogress_edit_dest,
//                    ClazzMemberWithClazzWorkProgress::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzWorkDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzMemberWithClazzWorkProgress> = object
            : DiffUtil.ItemCallback<ClazzMemberWithClazzWorkProgress>() {
            override fun areItemsTheSame(oldItem: ClazzMemberWithClazzWorkProgress,
                                         newItem: ClazzMemberWithClazzWorkProgress): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: ClazzMemberWithClazzWorkProgress,
                                            newItem: ClazzMemberWithClazzWorkProgress): Boolean {
                return oldItem == newItem
            }
        }

        val DU_CLAZZWORKWITHMETRICS = object: DiffUtil.ItemCallback<ClazzWorkWithMetrics>() {
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

    private val metricsObserver = Observer<List<ClazzWorkWithMetrics>?>{
        t -> metricsRecyclerAdapter?.submitList(t)
    }


    override var clazzWorkWithMetrics: DoorLiveData<ClazzWorkWithMetrics>? = null
        get() = field
        set(value) {
            metricsRecyclerAdapter?.submitList(listOf(value?.value))
            metricsRecyclerAdapter?.visible = true
        }

    override var clazzWorkWithMetricsFlat: ClazzWorkWithMetrics? = null
        get() = field
        set(value) {
            metricsRecyclerAdapter?.submitList(listOf(value))
            metricsRecyclerAdapter?.visible = true
        }

}