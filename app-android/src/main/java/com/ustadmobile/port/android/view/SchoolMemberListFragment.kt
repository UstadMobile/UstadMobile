package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemSchoolmemberListItemBinding
import com.ustadmobile.core.controller.SchoolMemberListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SchoolMemberListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.SchoolMember
import com.ustadmobile.lib.db.entities.SchoolMemberWithPerson
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class SchoolMemberListFragment(): UstadListViewFragment<SchoolMember, SchoolMemberWithPerson>(),
        SchoolMemberListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: SchoolMemberListPresenter? = null

    private var addNewStringId : Int = 0

    override val listPresenter: UstadListPresenter<*, in SchoolMemberWithPerson>?
        get() = mPresenter

    class SchoolMemberListViewHolder(val itemBinding: ItemSchoolmemberListItemBinding): RecyclerView.ViewHolder(itemBinding.root)

    class SchoolMemberListRecyclerAdapter(var presenter: SchoolMemberListPresenter?)
        : SelectablePagedListAdapter<SchoolMemberWithPerson, SchoolMemberListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolMemberListViewHolder {
            val itemBinding = ItemSchoolmemberListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return SchoolMemberListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: SchoolMemberListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.schoolMember = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        addNewStringId = if(arguments?.containsKey(UstadView.ARG_SCHOOLMEMBER_FILTER_STAFF) == true){
            R.string.teacher
        }else{
            R.string.student
        }

        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = SchoolMemberListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        mDataRecyclerViewAdapter = SchoolMemberListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.add_new,
                requireContext().getString(addNewStringId))
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }

    override fun onResume() {
        super.onResume()

        addNewStringId = if(arguments?.containsKey(UstadView.ARG_SCHOOLMEMBER_FILTER_STAFF) == true){
            R.string.teacher
        }else{
            R.string.student
        }

        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(addNewStringId)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout)
            navigateToEditEntity(null, R.id.person_detail_dest, Person::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.schoolMemberDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SchoolMemberWithPerson> = object
            : DiffUtil.ItemCallback<SchoolMemberWithPerson>() {
            override fun areItemsTheSame(oldItem: SchoolMemberWithPerson,
                                         newItem: SchoolMemberWithPerson): Boolean {
                return oldItem.person?.personUid == newItem.person?.personUid
            }

            override fun areContentsTheSame(oldItem: SchoolMemberWithPerson,
                                            newItem: SchoolMemberWithPerson): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun addMember() {
        //onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(Person::class.java,  R.id.person_list_dest)
    }
}