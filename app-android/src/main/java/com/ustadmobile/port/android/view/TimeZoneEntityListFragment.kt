package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemTimezoneentityListItemBinding
import com.ustadmobile.core.controller.TimeZoneEntityListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.TimeZoneEntityListView
import com.ustadmobile.lib.db.entities.TimeZoneEntity
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class TimeZoneEntityListFragment(): UstadListViewFragment<TimeZoneEntity, TimeZoneEntity>(),
        TimeZoneEntityListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: TimeZoneEntityListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in TimeZoneEntity>?
        get() = mPresenter

    class TimeZoneEntityListViewHolder(val itemBinding: ItemTimezoneentityListItemBinding): RecyclerView.ViewHolder(itemBinding.root)

    class TimeZoneEntityListRecyclerAdapter(var presenter: TimeZoneEntityListPresenter?)
        : SelectablePagedListAdapter<TimeZoneEntity, TimeZoneEntityListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeZoneEntityListViewHolder {
            val itemBinding = ItemTimezoneentityListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return TimeZoneEntityListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: TimeZoneEntityListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.timeZoneEntity = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())
        mPresenter = TimeZoneEntityListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, kodein)

        mDataRecyclerViewAdapter = TimeZoneEntityListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.timezone))
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.timezone)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        //we do not allow creating a new timezone
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.timeZoneEntityDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<TimeZoneEntity> = object
            : DiffUtil.ItemCallback<TimeZoneEntity>() {
            override fun areItemsTheSame(oldItem: TimeZoneEntity,
                                         newItem: TimeZoneEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TimeZoneEntity,
                                            newItem: TimeZoneEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}