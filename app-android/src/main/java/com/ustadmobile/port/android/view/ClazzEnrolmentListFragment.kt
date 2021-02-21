package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
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
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter


class ClazzEnrolmentListFragment(): UstadListViewFragment<ClazzEnrolment, ClazzEnrolmentWithLeavingReason>(),
        ClazzEnrolmentListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var clazzHeaderAdapter: SimpleHeadingRecyclerAdapter? = null
    private var profileHeaderAdapter: ClazzEnrolmentProfileHeaderAdapter? = null

    private var mPresenter: ClazzEnrolmentListPresenter? = null

    override var autoMergeRecyclerViewAdapter: Boolean = false

    override val listPresenter: UstadListPresenter<*, in ClazzEnrolment>?
        get() = mPresenter

    private var selectedPersonUid: Long = 0

    class ClazzEnrolmentProfileHeaderAdapter(val personUid: Long, var presenter: ClazzEnrolmentListPresenter?):
            SingleItemRecyclerViewAdapter<ClazzEnrolmentProfileHeaderAdapter.ClazzEnrolmentPersonHeaderViewHolder>(true){

        class ClazzEnrolmentPersonHeaderViewHolder(val itemBinding: ItemClazzEnrolmentPersonHeaderListBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzEnrolmentPersonHeaderViewHolder {
            return ClazzEnrolmentPersonHeaderViewHolder(
                    ItemClazzEnrolmentPersonHeaderListBinding.inflate(LayoutInflater
                    .from(parent.context), parent, false).also {
                        it.presenter = presenter
                        it.personUid = personUid
                    })
        }

        override fun onBindViewHolder(holder: ClazzEnrolmentPersonHeaderViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            holder.itemView.tag = personUid
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }

    }

    class ClazzEnrolmentRecyclerAdapter(var presenter: ClazzEnrolmentListPresenter?):
            SelectablePagedListAdapter<ClazzEnrolmentWithLeavingReason, ClazzEnrolmentRecyclerAdapter
            .ClazzEnrolmentListViewHolder>(DIFF_CALLBACK_ENROLMENT) {

        class ClazzEnrolmentListViewHolder(val itemBinding: ItemClazzEnrolmentListBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzEnrolmentListViewHolder {
            val itemBinding = ItemClazzEnrolmentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            return ClazzEnrolmentListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ClazzEnrolmentListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.clazzEnrolment = item
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        selectedPersonUid = arguments?.getString(ARG_PERSON_UID)?.toLong() ?: 0
        mPresenter = ClazzEnrolmentListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner)
        profileHeaderAdapter = ClazzEnrolmentProfileHeaderAdapter(selectedPersonUid, mPresenter)

        clazzHeaderAdapter = SimpleHeadingRecyclerAdapter("Person")
        mDataRecyclerViewAdapter = ClazzEnrolmentRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this)

        mMergeRecyclerViewAdapter = MergeAdapter(profileHeaderAdapter,clazzHeaderAdapter,
                mDataRecyclerViewAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getText(R.string.enrolment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = false
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

    override var clazz: Clazz? = null
        get() = field
        set(value){
            field = value
            val personInClazzStr = requireContext().getString(
                    R.string.person_enrolment_in_class, person?.personFullName(), value?.clazzName)
            clazzHeaderAdapter?.visible = true
            clazzHeaderAdapter?.headingText = personInClazzStr
        }

    companion object {

        val DIFF_CALLBACK_ENROLMENT: DiffUtil.ItemCallback<ClazzEnrolmentWithLeavingReason> = object
            : DiffUtil.ItemCallback<ClazzEnrolmentWithLeavingReason>() {
            override fun areItemsTheSame(oldItem: ClazzEnrolmentWithLeavingReason,
                                         newItem: ClazzEnrolmentWithLeavingReason): Boolean {
                return oldItem.clazzEnrolmentUid == newItem.clazzEnrolmentUid
            }

            override fun areContentsTheSame(oldItem: ClazzEnrolmentWithLeavingReason,
                                            newItem: ClazzEnrolmentWithLeavingReason): Boolean {
                return oldItem == newItem
            }
        }
    }


}