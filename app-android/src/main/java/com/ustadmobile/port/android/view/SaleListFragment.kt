package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SaleListView
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleListDetail
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter


class SaleListFragment(): UstadListViewFragment<Sale, SaleListDetail>(),
        SaleListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: SaleListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in SaleListDetail>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = SaleListPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = SaleListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.sale)
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }


    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.sale)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout) {
            //TODO
            //navigateToEditEntity(null, R.id.sale_edit_dest, Sale::class.java)
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