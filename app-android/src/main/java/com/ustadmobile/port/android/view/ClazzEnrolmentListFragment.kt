package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemClazzEnrolmentListBinding
import com.ustadmobile.core.controller.ClazzEnrolmentListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzEnrolmentListView
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class ClazzEnrolmentListFragment(): UstadListViewFragment<ClazzEnrolment, ClazzEnrolment>(),
        ClazzEnrolmentListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ClazzEnrolmentListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzEnrolment>?
        get() = mPresenter


    class ClazzEnrolmentRecyclerAdapter(var presenter: ClazzEnrolmentListPresenter?): SelectablePagedListAdapter<ClazzEnrolment, ClazzEnrolmentRecyclerAdapter.ClazzEnrolmentListViewHolder>(DIFF_CALLBACK) {

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
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
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

        mDataRecyclerViewAdapter = ClazzEnrolmentRecyclerAdapter(mPresenter)

        val createNewText = requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.clazzEnrolment))

        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(
                this, createNewText)
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.clazzEnrolment)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout)
            navigateToEditEntity(null, R.id.clazzEnrolment_edit_dest, ClazzEnrolment::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo.clazzEnrolmentDao

    override var person: Person? = null
        get() = field
        set(value) {
            field = value
            // TODO update adapter with name, picture and profile
        }


    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzEnrolment> = object
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
    }


}