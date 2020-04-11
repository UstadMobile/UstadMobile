package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemClazzlist2ClazzBinding
import com.ustadmobile.core.controller.ClazzList2Presenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.port.android.view.util.PagedListAdapterWithNewItem
import com.ustadmobile.port.android.view.util.getDataItemViewHolder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ClazzList2Fragment(): UstadListViewFragment<Clazz, ClazzWithNumStudents>(),
        ClazzList2View, MessageIdSpinner.OnMessageIdOptionSelectedListener{

    private var mPresenter: ClazzList2Presenter? = null

    private var dbRepo: UmAppDatabase? = null

    override val listPresenter: UstadListPresenter<*, in ClazzWithNumStudents>?
        get() = mPresenter

    class ClazzList2RecyclerAdapter(var presenter: ClazzList2Presenter?): PagedListAdapterWithNewItem<ClazzWithNumStudents>(DIFF_CALLBACK) {

        class ClazzList2ViewHolder(val itemBinding: ItemClazzlist2ClazzBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if(viewType == ITEMVIEWTYPE_NEW) {
                return super.onCreateViewHolder(parent, viewType)
            }else {
                val itemBinding = ItemClazzlist2ClazzBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ClazzList2ViewHolder(itemBinding)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val itemHolder = holder.getDataItemViewHolder()
            if(itemHolder is ClazzList2ViewHolder) {
                itemHolder.itemBinding.clazz = getItem(position)
                itemHolder.itemBinding.presenter = presenter
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())
        mPresenter = ClazzList2Presenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mDataBinding?.presenter = mPresenter
        mDataBinding?.onSortSelected = this
        mRecyclerViewAdapter = ClazzList2RecyclerAdapter(mPresenter)
        mPresenter?.onCreate(savedInstanceState.toStringMap())

        GlobalScope.launch {
            val clazzList = (1..20).map { Clazz("Clazz $it")}

            dbRepo?.clazzDao?.insertList(clazzList)
        }
        return view
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

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text = requireContext().getText(R.string.clazz)
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzWithNumStudents> = object
            : DiffUtil.ItemCallback<ClazzWithNumStudents>() {
            override fun areItemsTheSame(oldItem: ClazzWithNumStudents,
                                         newItem: ClazzWithNumStudents): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }

            override fun areContentsTheSame(oldItem: ClazzWithNumStudents,
                                            newItem: ClazzWithNumStudents): Boolean {
                return oldItem == newItem
            }
        }
    }
}