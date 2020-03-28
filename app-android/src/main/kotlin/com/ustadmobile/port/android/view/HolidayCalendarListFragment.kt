package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemHolidaycalendarListItemBinding
import com.ustadmobile.core.controller.HolidayCalendarListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries
import com.ustadmobile.port.android.util.ext.putExtraResultAsJson
import com.ustadmobile.port.android.view.util.AbstractCrudActivityResultContract.Companion.EXTRA_RESULT_KEY
import com.ustadmobile.port.android.view.util.PagedListAdapterWithNewItem
import com.ustadmobile.port.android.view.util.getDataItemViewHolder

class HolidayCalendarListFragment(): UstadListViewFragment<HolidayCalendar, HolidayCalendarWithNumEntries>(),
        HolidayCalendarListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: HolidayCalendarListPresenter? = null

    private var dbRepo: UmAppDatabase? = null

    class HolidayCalendarListRecyclerAdapter(var presenter: HolidayCalendarListPresenter?, newItemVisible: Boolean, onClickNewItem: View.OnClickListener)
        : PagedListAdapterWithNewItem<HolidayCalendarWithNumEntries>(DIFF_CALLBACK, newItemVisible = newItemVisible, onClickNewItem = onClickNewItem) {

        class HolidayCalendarListViewHolder(val itemBinding: ItemHolidaycalendarListItemBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if(viewType == ITEMVIEWTYPE_NEW) {
                return super.onCreateViewHolder(parent, viewType)
            }else {
                val itemBinding = ItemHolidaycalendarListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return HolidayCalendarListViewHolder(itemBinding)
            }

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val itemHolder = holder.getDataItemViewHolder()
            if(itemHolder is HolidayCalendarListViewHolder) {
                itemHolder.itemBinding.holidayCalendar = getItem(position)
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
        mPresenter = HolidayCalendarListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mDataBinding?.presenter = mPresenter
        mDataBinding?.onSortSelected = this
        mRecyclerViewAdapter = HolidayCalendarListRecyclerAdapter(mPresenter, false, this)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
        return view
    }

    override fun onClick(view: View?) {
        activity?.prepareHolidayCalendarPickFromListCall {
            if(it != null) {
                finishWithResult(it)
            }
        }?.launch(mapOf())
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

    override fun finishWithResult(result: List<HolidayCalendar>) {
        val resultIntent = Intent().apply {
            putExtraResultAsJson(EXTRA_RESULT_KEY, result)
        }
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        activity?.finish()
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.holidayCalendarDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<HolidayCalendarWithNumEntries> = object
            : DiffUtil.ItemCallback<HolidayCalendarWithNumEntries>() {
            override fun areItemsTheSame(oldItem: HolidayCalendarWithNumEntries,
                                         newItem: HolidayCalendarWithNumEntries): Boolean {
                return oldItem.umCalendarUid == newItem.umCalendarUid
            }

            override fun areContentsTheSame(oldItem: HolidayCalendarWithNumEntries,
                                            newItem: HolidayCalendarWithNumEntries): Boolean {
                return oldItem == newItem
            }
        }

        fun newInstance(bundle: Bundle?): HolidayCalendarListFragment {
            return HolidayCalendarListFragment().apply {
                arguments = bundle
            }
        }
    }
}