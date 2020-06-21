package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter

class ClazzListFragment(): UstadListViewFragment<Clazz, ClazzWithNumStudents>(),
        ClazzList2View, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ClazzListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzWithNumStudents>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())
        mPresenter = ClazzListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this,
            requireContext().getString(R.string.create_new,
                    requireContext().getString(R.string.clazz)))
        mDataRecyclerViewAdapter = ClazzListRecyclerAdapter(mPresenter)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getText(R.string.clazz)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }


    override fun onClick(v: View?) {
        if(view?.id == R.id.item_createnew_layout)
            navigateToEditEntity(null, R.id.clazz_edit_dest, Clazz::class.java)
    }

    override fun onMessageIdOptionSelected(view: AdapterView<*>?, messageIdOption: MessageIdOption) {
        mPresenter?.handleClickSortOrder(messageIdOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {
        //do nothing
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