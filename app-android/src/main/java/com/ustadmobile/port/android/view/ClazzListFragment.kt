package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import com.toughra.ustadmobile.R
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ClazzListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class ClazzListFragment(): UstadListViewFragment<Clazz, ClazzWithNumStudents>(),
        ClazzList2View, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ClazzListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzWithNumStudents>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val accountManager: UstadAccountManager by instance()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = TAG_REPO)
        mPresenter = ClazzListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner)
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this,
            requireContext().getString(R.string.add_a_new,
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
        if(v?.id == R.id.item_createnew_layout) {
            var args = bundleOf()
            val filterExcludeMembersOfSchool =
                    arguments?.get(PersonListView.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL)?.toString()?.toLong()?:0L
            if(filterExcludeMembersOfSchool != 0L){
                args = bundleOf(UstadView.ARG_SCHOOL_UID to filterExcludeMembersOfSchool.toString())
            }
            navigateToEditEntity(null, R.id.clazz_edit_dest, Clazz::class.java,
                    argBundle = args)
        }
    }

    override fun onMessageIdOptionSelected(view: AdapterView<*>?, messageIdOption: MessageIdOption) {
        mPresenter?.handleClickSortOrder(messageIdOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {
        //do nothing
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzDao


}