package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemClazzmemberListItemBinding
import com.ustadmobile.core.controller.ClazzMemberListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson
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

        }

    private var mNewStudentListRecyclerViewAdapter: NewItemRecyclerViewAdapter? = null

    private var mStudentListRecyclerViewAdapter: ClazzMemberListRecyclerAdapter? = null

    private var mStudentListObserver: Observer<PagedList<ClazzMemberWithPerson>>? = null

    override var list: DataSource.Factory<Int, ClazzMemberWithPerson>?
        get() = super.list
        set(value) {
            currentLiveData?.removeObserver(this)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ClazzMemberListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

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
                requireContext().getString(R.string.students_literal))
        mNewStudentListRecyclerViewAdapter = NewItemRecyclerViewAdapter(this,
                addStudentText, headerStringId = R.string.students_literal,
                headerLayoutId = R.layout.item_simple_list_header)
        mMergeRecyclerViewAdapter = MergeAdapter(mNewItemRecyclerViewAdapter,
            mDataRecyclerViewAdapter, mNewStudentListRecyclerViewAdapter,
            mStudentListRecyclerViewAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {



        super.onViewCreated(view, savedInstanceState)


    }

    override fun onResume() {
        super.onResume()

    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzMemberDao

    companion object {
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