package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemClazzEnrollmentListBinding
import com.ustadmobile.core.controller.ClazzEnrollmentListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzEnrollmentListView
import com.ustadmobile.lib.db.entities.ClazzEnrollment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class ClazzEnrollmentListFragment(): UstadListViewFragment<ClazzEnrollment, ClazzEnrollment>(),
        ClazzEnrollmentListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ClazzEnrollmentListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzEnrollment>?
        get() = mPresenter


    class ClazzEnrollmentRecyclerAdapter(var presenter: ClazzEnrollmentListPresenter?): SelectablePagedListAdapter<ClazzEnrollment, ClazzEnrollmentRecyclerAdapter.ClazzEnrollmentListViewHolder>(DIFF_CALLBACK) {

        class ClazzEnrollmentListViewHolder(val itemBinding: ItemClazzEnrollmentListBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzEnrollmentListViewHolder {
            val itemBinding = ItemClazzEnrollmentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return ClazzEnrollmentListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ClazzEnrollmentListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.clazzEnrollment = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        mPresenter = ClazzEnrollmentListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = ClazzEnrollmentRecyclerAdapter(mPresenter)

        val createNewText = requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.clazzenrollment))

        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(
                this, createNewText)
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.clazzenrollment)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout)
            navigateToEditEntity(null, R.id.clazzenrollment_edit_dest, ClazzEnrollment::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo.clazzEnrollmentDao

    override var person: Person? = null
        get() = field
        set(value) {
            field = value
            // TODO update adapter with name, picture and profile
        }


    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzEnrollment> = object
            : DiffUtil.ItemCallback<ClazzEnrollment>() {
            override fun areItemsTheSame(oldItem: ClazzEnrollment,
                                         newItem: ClazzEnrollment): Boolean {
                return oldItem.clazzEnrollmentUid == newItem.clazzEnrollmentUid
            }

            override fun areContentsTheSame(oldItem: ClazzEnrollment,
                                            newItem: ClazzEnrollment): Boolean {
                return oldItem == newItem
            }
        }
    }


}