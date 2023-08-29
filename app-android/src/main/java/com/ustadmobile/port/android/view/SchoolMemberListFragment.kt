package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemSchoolmemberListItemBinding
import com.toughra.ustadmobile.databinding.ItemSchoolmemberPendingListItemBinding
import com.ustadmobile.core.controller.SchoolMemberListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL
import com.ustadmobile.core.view.SchoolMemberListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class SchoolMemberListFragment : UstadListViewFragment<SchoolMember, SchoolMemberWithPerson>(),
        SchoolMemberListView, View.OnClickListener {

    private var mPresenter: SchoolMemberListPresenter? = null

    private var addNewStringId: Int = 0

    private var filterBySchoolUid: Long = 0

    private var filterByRole: Int = 0

    override val listPresenter: UstadListPresenter<*, in SchoolMemberWithPerson>?
        get() = mPresenter

    private lateinit var addPersonKeyName: String

    override var autoMergeRecyclerViewAdapter: Boolean = false

    private var mCurrentPendingStudentListLiveData: LiveData<PagedList<SchoolMemberWithPerson>>? = null

    private var mCurrentStudentListLiveData: LiveData<PagedList<SchoolMemberWithPerson>>? = null

    private var mPendingStudentsHeaderRecyclerViewAdapter: ListHeaderRecyclerViewAdapter? = null

    private var mPendingStudentListRecyclerViewAdapter:
            PendingSchoolMemberListRecyclerAdapter? = null


    private val pendingStudentsObserver =
        Observer<PagedList<SchoolMemberWithPerson>> {
            mPendingStudentListRecyclerViewAdapter?.submitList(it)
            mPendingStudentsHeaderRecyclerViewAdapter?.headerLayoutId = if (it != null && !it.isEmpty()) {
                R.layout.item_simple_list_header
            } else {
                0
            }
        }

    class PendingSchoolMemberListViewHolder(val itemBinding: ItemSchoolmemberPendingListItemBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    class PendingSchoolMemberListRecyclerAdapter(var presenter: SchoolMemberListPresenter?)
        : PagedListAdapter<SchoolMemberWithPerson, PendingSchoolMemberListViewHolder>(
            DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingSchoolMemberListViewHolder {
            val itemBinding = ItemSchoolmemberPendingListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            return PendingSchoolMemberListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: PendingSchoolMemberListViewHolder, position: Int) {
            holder.itemBinding.schoolMember = getItem(position)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

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

        addPersonKeyName = "Person_${arguments?.get(UstadView.ARG_FILTER_BY_ROLE)}"

        filterByRole = arguments?.get(UstadView.ARG_FILTER_BY_ROLE)?.toString()?.toInt() ?: 0

        addNewStringId = if (filterByRole == Role.ROLE_SCHOOL_STAFF_UID) {
            R.string.teacher
        } else {
            R.string.student
        }

        filterBySchoolUid = arguments?.getString(UstadView.ARG_FILTER_BY_SCHOOLUID)?.toLong() ?: 0

        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = SchoolMemberListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = SchoolMemberListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.add_new,
                requireContext().getString(addNewStringId))

        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this, createNewText,
                onClickSort = this, sortOrderOption = mPresenter?.sortOptions?.get(0))

        mPendingStudentListRecyclerViewAdapter = PendingSchoolMemberListRecyclerAdapter(mPresenter)
        mPendingStudentsHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(null,
                "", R.string.pending_requests, headerLayoutId = 0)

        mMergeRecyclerViewAdapter = ConcatAdapter(mUstadListHeaderRecyclerViewAdapter,
                mDataRecyclerViewAdapter, mPendingStudentsHeaderRecyclerViewAdapter,
                mPendingStudentListRecyclerViewAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Person::class.java, addPersonKeyName) {
            val memberAdded = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleEnrolMember(filterBySchoolUid, memberAdded.personUid,
                    arguments?.getString(UstadView.ARG_FILTER_BY_ROLE)?.toInt() ?: 0)
        }
    }

    override fun onResume() {
        super.onResume()

        filterByRole = arguments?.get(UstadView.ARG_FILTER_BY_ROLE)?.toString()?.toInt() ?: 0

        addNewStringId = if (filterByRole == Role.ROLE_SCHOOL_STAFF_UID) {
            R.string.teacher
        } else {
            R.string.student
        }

        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(addNewStringId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(v: View?) {
        if (v?.id == R.id.item_createnew_layout)
            mPresenter?.handleClickAddNewItem(
                arguments.toStringMap(),
                PersonListViewModel.RESULT_PERSON_KEY)
        else {
            super.onClick(v)
        }
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
        val bundle = if (addPersonKeyName == "Person_" + Role.ROLE_SCHOOL_STAFF_UID.toString()) {
            bundleOf(ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL to filterBySchoolUid.toString())
        } else {
            bundleOf(ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL to filterBySchoolUid.toString(),
                    UstadView.ARG_CODE_TABLE to School.TABLE_ID.toString())
        }
        mPresenter?.handleClickAddNewItem(bundle.toStringMap(), addPersonKeyName)
    }

    override var pendingStudentList: DataSource.Factory<Int, SchoolMemberWithPerson>? = null
        get() = field
        set(value) {
            val repoDao = displayTypeRepo ?: return

            mCurrentPendingStudentListLiveData?.removeObserver(pendingStudentsObserver)
            mCurrentStudentListLiveData = value?.asRepositoryLiveData(repoDao)
            mCurrentStudentListLiveData?.observe(viewLifecycleOwner, pendingStudentsObserver)
            field = value
        }

}