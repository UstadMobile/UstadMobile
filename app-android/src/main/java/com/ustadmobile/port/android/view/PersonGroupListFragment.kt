package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemPersongroupListItemBinding
import com.ustadmobile.core.controller.PersonGroupListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonGroupListView
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.PersonGroupWithMemberCount
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class PersonGroupListFragment(): UstadListViewFragment<PersonGroup, PersonGroupWithMemberCount>(),
        PersonGroupListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: PersonGroupListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonGroupWithMemberCount>?
        get() = mPresenter

    class PersonGroupListViewHolder(val itemBinding: ItemPersongroupListItemBinding): RecyclerView.ViewHolder(itemBinding.root)

    class PersonGroupListRecyclerAdapter(var presenter: PersonGroupListPresenter?)
        : SelectablePagedListAdapter<PersonGroupWithMemberCount, PersonGroupListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonGroupListViewHolder {
            val itemBinding = ItemPersongroupListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PersonGroupListViewHolder(itemBinding)

        }

        override fun onBindViewHolder(holder: PersonGroupListViewHolder, position: Int) {
            holder.itemBinding.personGroup = getItem(position)
            holder.itemBinding.presenter = presenter
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())
        mPresenter = PersonGroupListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mDataBinding?.presenter = mPresenter
        mDataBinding?.onSortSelected = this
        mDataRecyclerViewAdapter = PersonGroupListRecyclerAdapter(mPresenter)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
        return view
    }

    override fun onClick(view: View?) {
//        activity?.prepareGroupListEditCall {
//            if(it != null) {
//                finishWithResult(it)
//            }
//        }.launchGroupListEdit(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }


    override fun onMessageIdOptionSelected(view: AdapterView<*>?, messageIdOption: MessageIdOption) {
        mPresenter?.handleClickSortOrder(messageIdOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {
        //do nothing
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.personGroupDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonGroupWithMemberCount> = object
            : DiffUtil.ItemCallback<PersonGroupWithMemberCount>() {
            override fun areItemsTheSame(oldItem: PersonGroupWithMemberCount,
                                         newItem: PersonGroupWithMemberCount): Boolean {
                TODO("e.g. insert primary keys here return oldItem.personGroup == newItem.personGroup")
            }

            override fun areContentsTheSame(oldItem: PersonGroupWithMemberCount,
                                            newItem: PersonGroupWithMemberCount): Boolean {
                return oldItem == newItem
            }
        }

        fun newInstance(bundle: Bundle?) : PersonGroupListFragment {
            return PersonGroupListFragment().apply {
                arguments = bundle
            }
        }
    }
}