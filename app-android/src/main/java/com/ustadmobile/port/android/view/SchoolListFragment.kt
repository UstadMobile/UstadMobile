package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemSchoolListItemBinding
import com.ustadmobile.core.controller.SchoolListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SchoolListView
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.SchoolWithMemberCountAndLocation
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class SchoolListFragment(): UstadListViewFragment<School, SchoolWithMemberCountAndLocation>(),
        SchoolListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: SchoolListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in SchoolWithMemberCountAndLocation>?
        get() = mPresenter

    class SchoolListViewHolder(val itemBinding: ItemSchoolListItemBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    class SchoolListRecyclerAdapter(var presenter: SchoolListPresenter?)
        : SelectablePagedListAdapter<SchoolWithMemberCountAndLocation,
            SchoolListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolListViewHolder {
            val itemBinding = ItemSchoolListItemBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)
            itemBinding.presenter = presenter
            return SchoolListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: SchoolListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.school = item
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
        mPresenter = SchoolListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        mDataRecyclerViewAdapter = SchoolListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.schools))
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fabManager?.text = requireContext().getText(R.string.school)
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.school)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout) {
            navigateToEditEntity(null, R.id.school_edit_dest, School::class.java)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
        mDataRecyclerViewAdapter = null
        mDataBinding = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.schoolDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SchoolWithMemberCountAndLocation> = object
            : DiffUtil.ItemCallback<SchoolWithMemberCountAndLocation>() {
            override fun areItemsTheSame(oldItem: SchoolWithMemberCountAndLocation,
                                         newItem: SchoolWithMemberCountAndLocation): Boolean {

                return oldItem.schoolUid == newItem.schoolUid
            }

            override fun areContentsTheSame(oldItem: SchoolWithMemberCountAndLocation,
                                            newItem: SchoolWithMemberCountAndLocation): Boolean {
                return oldItem == newItem
            }
        }
    }
}