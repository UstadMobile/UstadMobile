package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemSchoolmemberListItemBinding
import com.ustadmobile.core.controller.SchoolMemberListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.view.ListViewAddMode
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL
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
        SchoolMemberListView, MessageIdSpinner.OnMessageIdOptionSelectedListener,
        View.OnClickListener{

    private var mPresenter: SchoolMemberListPresenter? = null

    private var addNewStringId : Int = 0

    private var filterBySchoolUid : Long = 0

    override val listPresenter: UstadListPresenter<*, in SchoolMemberWithPerson>?
        get() = mPresenter

    class SchoolMemberListViewHolder(val itemBinding: ItemSchoolmemberListItemBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    class SchoolMemberListRecyclerAdapter(var presenter: SchoolMemberListPresenter?)
        : SelectablePagedListAdapter<SchoolMemberWithPerson,
            SchoolMemberListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : SchoolMemberListViewHolder {
            val itemBinding = ItemSchoolmemberListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        addMode = ListViewAddMode.FAB

        addNewStringId =
            if(arguments?.containsKey(UstadView.ARG_SCHOOLMEMBER_FILTER_STAFF) == true){
                R.string.teacher
            }else{
                R.string.student
            }

        filterBySchoolUid = arguments?.getString(UstadView.ARG_SCHOOL_UID)?.toLong()?:0

        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = SchoolMemberListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, kodein)

        mDataRecyclerViewAdapter = SchoolMemberListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.add_new,
                requireContext().getString(addNewStringId))

        //mDataBinding?.presenter = mPresenter
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Person::class.java, KEY_MEMBER_SELECTED_STAFF) {
            val memberAdded = it.firstOrNull() ?: return@observeResult
           mPresenter?.handleEnrolMember(filterBySchoolUid, memberAdded.personUid,
                   SchoolMember.SCHOOL_ROLE_TEACHER)
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Person::class.java, KEY_MEMBER_SELECTED_STUDENT) {
            val memberAdded = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleEnrolMember(filterBySchoolUid, memberAdded.personUid,
                    SchoolMember.SCHOOL_ROLE_STUDENT)
        }
    }

    override fun onResume() {
        super.onResume()

        addNewStringId =
            if(arguments?.containsKey(UstadView.ARG_SCHOOLMEMBER_FILTER_STAFF)
                == true){
                R.string.teacher
            }else{
                R.string.student
            }
        addMode = ListViewAddMode.FAB

        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(addNewStringId)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {       if(view?.id == R.id.item_createnew_layout)
            navigateToEditEntity(null, R.id.person_detail_dest, Person::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
        mDataBinding = null
        mDataRecyclerViewAdapter = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.schoolMemberDao

    companion object {

        const val KEY_MEMBER_SELECTED_STAFF = "member_list_staff"
        const val KEY_MEMBER_SELECTED_STUDENT = "member_list_student"

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
        val memberSelected =
            if(arguments?.containsKey(UstadView.ARG_SCHOOLMEMBER_FILTER_STAFF) == true){
                KEY_MEMBER_SELECTED_STAFF
            }else{
                KEY_MEMBER_SELECTED_STUDENT
            }
        navigateToPickEntityFromList(Person::class.java,  R.id.person_list_dest,
                bundleOf(ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL to filterBySchoolUid.toString()),
                memberSelected,true)
    }
}