package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PersonWithSaleInfoListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.PersonWithSaleInfoListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter


class PersonWithSaleInfoListFragment(): UstadListViewFragment<PersonWithSaleInfo, PersonWithSaleInfo>(),
        PersonWithSaleInfoListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: PersonWithSaleInfoListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithSaleInfo>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = PersonWithSaleInfoListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = PersonWithSaleInfoListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.add_we  )
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.wes)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout) {
            navigateToEditEntity(null, R.id.person_detail_dest, PersonWithSaleInfo::class.java
                    ,argBundle = bundleOf(UstadView.ARG_FILTER_PERSON_WE to "true")
            )
        }else{
            super.onClick(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.saleDao

}