package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemClazzmemberListItemBinding
import com.toughra.ustadmobile.databinding.ItemClazzmemberPendingListItemBinding
import com.ustadmobile.core.controller.ClazzMemberListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ
import com.ustadmobile.core.view.UstadView.Companion.ARG_CODE_TABLE
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_NAME
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_CLAZZUID
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class ClazzMemberListFragment(): UstadListViewFragment<ClazzMember, ClazzMemberWithPerson>(),
        ClazzMemberListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ClazzMemberListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzMemberWithPerson>?
        get() = mPresenter

    override var autoMergeRecyclerViewAdapter: Boolean = false

    override var studentList: DataSource.Factory<Int, ClazzMemberWithPerson>? = null
        get() = field
        set(value) {
            val studentObserverVal = mStudentListObserver ?: return
            val repoDao = displayTypeRepo ?: return
            mCurrentStudentListLiveData?.removeObserver(studentObserverVal)
            mCurrentStudentListLiveData = value?.asRepositoryLiveData(repoDao)
            mCurrentStudentListLiveData?.observe(viewLifecycleOwner, studentObserverVal)
        }

    private val pendingStudentsObserver = object: Observer<PagedList<ClazzMemberWithPerson>> {
        override fun onChanged(t: PagedList<ClazzMemberWithPerson>?) {
            mPendingStudentListRecyclerViewAdapter?.submitList(t)
            mPendingStudentsHeaderRecyclerViewAdapter?.headerLayoutId = if(t != null && !t.isEmpty()) {
                R.layout.item_simple_list_header
            }else {
                0
            }
        }
    }

    override var pendingStudentList: DataSource.Factory<Int, ClazzMemberWithPerson>? = null
        get() = field
        set(value) {
            val repoDao = displayTypeRepo ?: return

            mCurrentPendingStudentListLiveData?.removeObserver(pendingStudentsObserver)
            mCurrentStudentListLiveData = value?.asRepositoryLiveData(repoDao)
            mCurrentStudentListLiveData?.observe(viewLifecycleOwner, pendingStudentsObserver)
            field = value
        }



    private var mNewStudentListRecyclerViewAdapter: NewItemRecyclerViewAdapter? = null

    private var mStudentListRecyclerViewAdapter: ClazzMemberListRecyclerAdapter? = null

    private var mStudentListObserver: Observer<PagedList<ClazzMemberWithPerson>>? = null

    private var mCurrentStudentListLiveData: LiveData<PagedList<ClazzMemberWithPerson>>? = null

    private var mPendingStudentsHeaderRecyclerViewAdapter: NewItemRecyclerViewAdapter? = null

    private var mPendingStudentListRecyclerViewAdapter: PendingClazzMemberListRecyclerAdapter? = null

    //private var mPendingStudentListObserver: Observer<PagedList<ClazzMemberWithPerson>>? = null

    private var mCurrentPendingStudentListLiveData: LiveData<PagedList<ClazzMemberWithPerson>>? = null

    private var filterByClazzUid: Long = 0

    private val mOnClickAddStudent: View.OnClickListener = View.OnClickListener {
        navigateToPickNewMember(KEY_STUDENT_SELECTED)
    }

    override var addTeacherVisible: Boolean = false
        set(value) {
            field = value
            mNewItemRecyclerViewAdapter?.newItemVisible = value
        }

    override var addStudentVisible: Boolean = false
        set(value) {
            field = value
            mNewStudentListRecyclerViewAdapter?.newItemVisible = value
        }

    class ClazzMemberListViewHolder(val itemBinding: ItemClazzmemberListItemBinding): RecyclerView.ViewHolder(itemBinding.root)

    class ClazzMemberListRecyclerAdapter(var presenter: ClazzMemberListPresenter?)
        : SelectablePagedListAdapter<ClazzMemberWithPerson, ClazzMemberListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzMemberListViewHolder {
            val itemBinding = ItemClazzmemberListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return ClazzMemberListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ClazzMemberListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.clazzMember = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    class PendingClazzMemberListViewHolder(val itemBinding: ItemClazzmemberPendingListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class PendingClazzMemberListRecyclerAdapter(var presenter: ClazzMemberListPresenter?) : PagedListAdapter<ClazzMemberWithPerson, PendingClazzMemberListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingClazzMemberListViewHolder {
            val itemBinding = ItemClazzmemberPendingListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            return PendingClazzMemberListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: PendingClazzMemberListViewHolder, position: Int) {
            holder.itemBinding.clazzMember = getItem(position)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        filterByClazzUid = arguments?.getString(ARG_FILTER_BY_CLAZZUID)?.toLong() ?: 0
        mPresenter = ClazzMemberListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this,  di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = ClazzMemberListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.add_a,
                requireContext().getString(R.string.teacher))
        mStudentListRecyclerViewAdapter = ClazzMemberListRecyclerAdapter(mPresenter).also {
            mStudentListObserver = PagedListSubmitObserver(it)
        }
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText,
            headerStringId = R.string.teachers_literal,
            headerLayoutId = R.layout.item_simple_list_header)
        val addStudentText = requireContext().getString(R.string.add_a,
                requireContext().getString(R.string.students))
        mNewStudentListRecyclerViewAdapter = NewItemRecyclerViewAdapter(mOnClickAddStudent,
                addStudentText, headerStringId = R.string.students,
                headerLayoutId = R.layout.item_simple_list_header)

        mPendingStudentListRecyclerViewAdapter = PendingClazzMemberListRecyclerAdapter(mPresenter)
        mPendingStudentsHeaderRecyclerViewAdapter = NewItemRecyclerViewAdapter(null,
                "", R.string.pending_requests, headerLayoutId = 0)

        mMergeRecyclerViewAdapter = MergeAdapter(mNewItemRecyclerViewAdapter,
            mDataRecyclerViewAdapter, mNewStudentListRecyclerViewAdapter,
            mStudentListRecyclerViewAdapter, mPendingStudentsHeaderRecyclerViewAdapter,
            mPendingStudentListRecyclerViewAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Person::class.java, KEY_TEACHER_SELECTED) {
            val teacherAdded = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleEnrolMember(teacherAdded, ClazzMember.ROLE_TEACHER)
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Person::class.java, KEY_STUDENT_SELECTED) {
            val studentAdded = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleEnrolMember(studentAdded, ClazzMember.ROLE_STUDENT)
        }

        super.onViewCreated(view, savedInstanceState)
        fabManager?.visible = false
    }

    private fun navigateToPickNewMember(keyName: String){

        navigateToPickEntityFromList(Person::class.java, R.id.personlist_dest,
                bundleOf(ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ to filterByClazzUid.toString(),
                ARG_CODE_TABLE to Clazz.TABLE_ID.toString()),
                keyName, true)
    }

    override fun onResume() {
        super.onResume()

    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) = navigateToPickNewMember(KEY_TEACHER_SELECTED)


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzMemberDao

    companion object {
        const val KEY_TEACHER_SELECTED = "Person_Teacher"

        const val KEY_STUDENT_SELECTED = "Person_Student"

        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzMemberWithPerson> = object
            : DiffUtil.ItemCallback<ClazzMemberWithPerson>() {
            override fun areItemsTheSame(oldItem: ClazzMemberWithPerson,
                                         newItem: ClazzMemberWithPerson): Boolean {
                return oldItem.clazzMemberUid == newItem.clazzMemberUid
            }

            override fun areContentsTheSame(oldItem: ClazzMemberWithPerson,
                                            newItem: ClazzMemberWithPerson): Boolean {
                return oldItem == newItem
            }
        }
    }
}