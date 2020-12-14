package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.paging.PagedListAdapter

import com.toughra.ustadmobile.databinding.ItemCategoryListBinding
import com.ustadmobile.core.controller.CategoryListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CategoryListView
import com.ustadmobile.lib.db.entities.Category

import com.ustadmobile.core.view.GetResultMode
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter


class CategoryListFragment(): UstadListViewFragment<Category, Category>(),
        CategoryListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: CategoryListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in Category>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = CategoryListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = AllCategoryListRecyclerAdapter(mPresenter, requireContext())
        val createNewText = requireContext().getString(R.string.create_category)
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.category)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout) {
            navigateToEditEntity(null, R.id.category_edit_dest, Category::class.java)
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
        get() = dbRepo?.categoryDao

    override fun updateIsAdmin(isAdmin: Boolean) {
        (mDataRecyclerViewAdapter as AllCategoryListRecyclerAdapter).isAdmin = isAdmin
    }

}