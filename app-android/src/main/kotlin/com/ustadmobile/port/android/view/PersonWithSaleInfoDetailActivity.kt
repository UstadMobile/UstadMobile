package com.ustadmobile.port.android.view

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PersonWithSaleInfoDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.ustadmobile.core.view.PersonWithSaleInfoListView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo
import com.ustadmobile.lib.db.entities.SaleListDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class PersonWithSaleInfoDetailActivity :UstadBaseActivity(), PersonWithSaleInfoDetailView{
    override fun updatePersonOnView(person: Person) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setSalesFactory(factory: DataSource.Factory<Int, SaleListDetail>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var toolbar: Toolbar
    private lateinit var mPresenter: PersonWithSaleInfoDetailPresenter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var sortSpinner: AppCompatSpinner
    private lateinit var searchView:SearchView


}