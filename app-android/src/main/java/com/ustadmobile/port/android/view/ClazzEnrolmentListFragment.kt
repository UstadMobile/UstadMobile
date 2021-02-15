package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemClazzEnrolmentListBinding
import com.toughra.ustadmobile.databinding.ItemClazzEnrolmentPersonHeaderListBinding
import com.ustadmobile.core.controller.ClazzEnrolmentListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.view.ClazzEnrolmentListView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class ClazzEnrolmentListFragment(): UstadListViewFragment<ClazzEnrolment, ClazzEnrolment>(),
        ClazzEnrolmentListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var profileHeaderAdapter: ClazzEnrolmentProfileHeaderAdapter? = null

    private var mPresenter: ClazzEnrolmentListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzEnrolment>?
        get() = mPresenter


    class ClazzEnrolmentProfileHeaderAdapter(val personUid: Long, var presenter: ClazzEnrolmentListPresenter?):
            ListAdapter<Person, ClazzEnrolmentProfileHeaderAdapter.ClazzEnrolmentPersonHeaderViewHolder>(DIFF_CALLBACK_PERSON){

        class ClazzEnrolmentPersonHeaderViewHolder(val itemBinding: ItemClazzEnrolmentPersonHeaderListBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzEnrolmentPersonHeaderViewHolder {
            val itemBinding = ItemClazzEnrolmentPersonHeaderListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.personUid = personUid
            return ClazzEnrolmentPersonHeaderViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ClazzEnrolmentPersonHeaderViewHolder, position: Int) {

        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }

    }

    class ClazzEnrolmentRecyclerAdapter(var presenter: ClazzEnrolmentListPresenter?):
            SelectablePagedListAdapter<ClazzEnrolment, ClazzEnrolmentRecyclerAdapter
            .ClazzEnrolmentListViewHolder>(DIFF_CALLBACK_ENROLMENT) {

        class ClazzEnrolmentListViewHolder(val itemBinding: ItemClazzEnrolmentListBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzEnrolmentListViewHolder {
            val itemBinding = ItemClazzEnrolmentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return ClazzEnrolmentListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ClazzEnrolmentListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.clazzEnrolment = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK_ENROLMENT)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        mPresenter = ClazzEnrolmentListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner)

        profileHeaderAdapter = ClazzEnrolmentProfileHeaderAdapter(
                arguments?.get(ARG_ENTITY_UID) as Long? ?: 0L, mPresenter)
        mDataRecyclerViewAdapter = ClazzEnrolmentRecyclerAdapter(mPresenter)

        mMergeRecyclerViewAdapter = MergeAdapter(profileHeaderAdapter, mDataRecyclerViewAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter

        return view
    }


    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout)
            navigateToEditEntity(null, R.id.clazz_list_dest, ClazzEnrolment::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
        profileHeaderAdapter = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzEnrolmentDao

    override var person: Person? = null
        get() = field
        set(value) {
            field = value
            ustadFragmentTitle = person?.personFullName()
        }

    companion object {

        val DIFF_CALLBACK_ENROLMENT: DiffUtil.ItemCallback<ClazzEnrolment> = object
            : DiffUtil.ItemCallback<ClazzEnrolment>() {
            override fun areItemsTheSame(oldItem: ClazzEnrolment,
                                         newItem: ClazzEnrolment): Boolean {
                return oldItem.clazzEnrolmentUid == newItem.clazzEnrolmentUid
            }

            override fun areContentsTheSame(oldItem: ClazzEnrolment,
                                            newItem: ClazzEnrolment): Boolean {
                return oldItem == newItem
            }
        }


        val DIFF_CALLBACK_PERSON: DiffUtil.ItemCallback<Person> = object
            : DiffUtil.ItemCallback<Person>() {
            override fun areItemsTheSame(oldItem: Person,
                                         newItem: Person): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: Person,
                                            newItem: Person): Boolean {
                return oldItem == newItem
            }
        }
    }


}