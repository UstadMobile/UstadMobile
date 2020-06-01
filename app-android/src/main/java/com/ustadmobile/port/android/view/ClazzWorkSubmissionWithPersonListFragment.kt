package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzWorkSubmissionWithPersonListBinding
import com.ustadmobile.core.controller.ClazzWorkSubmissionWithPersonListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzWorkSubmissionWithPersonListView
import com.ustadmobile.lib.db.entities.ClazzWorkSubmissionWithPerson

import com.ustadmobile.core.view.GetResultMode
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter

class ClazzWorkSubmissionWithPersonListFragment()
    : UstadListViewFragment<ClazzWorkSubmissionWithPerson, ClazzWorkSubmissionWithPerson>(),
        ClazzWorkSubmissionWithPersonListView, MessageIdSpinner.OnMessageIdOptionSelectedListener,
        View.OnClickListener{

    private var mPresenter: ClazzWorkSubmissionWithPersonListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzWorkSubmissionWithPerson>?
        get() = mPresenter

    class ClazzWorkSubmissionWithPersonListViewHolder(val itemBinding: ItemClazzWorkSubmissionWithPersonListBinding): RecyclerView.ViewHolder(itemBinding.root)

    class ClazzWorkSubmissionWithPersonListRecyclerAdapter(var presenter: ClazzWorkSubmissionWithPersonListPresenter?)
        : SelectablePagedListAdapter<ClazzWorkSubmissionWithPerson, ClazzWorkSubmissionWithPersonListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkSubmissionWithPersonListViewHolder {
            val itemBinding = ItemClazzWorkSubmissionWithPersonListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return ClazzWorkSubmissionWithPersonListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ClazzWorkSubmissionWithPersonListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.clazzWorkSubmissionWithPerson = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ClazzWorkSubmissionWithPersonListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        mDataRecyclerViewAdapter = ClazzWorkSubmissionWithPersonListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.submission))
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.submission)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout) {
            //navigateToEditEntity(null, R.id.clazzworksubmissionwithperson_edit_dest, ClazzWorkSubmissionWithPerson::class.java)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = TODO("Provide repo e.g. dbRepo.ClazzWorkSubmissionWithPersonDao")

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzWorkSubmissionWithPerson> = object
            : DiffUtil.ItemCallback<ClazzWorkSubmissionWithPerson>() {
            override fun areItemsTheSame(oldItem: ClazzWorkSubmissionWithPerson,
                                         newItem: ClazzWorkSubmissionWithPerson): Boolean {
                TODO("e.g. insert primary keys here return oldItem.clazzWorkSubmissionWithPerson == newItem.clazzWorkSubmissionWithPerson")
            }

            override fun areContentsTheSame(oldItem: ClazzWorkSubmissionWithPerson,
                                            newItem: ClazzWorkSubmissionWithPerson): Boolean {
                return oldItem == newItem
            }
        }
    }
}