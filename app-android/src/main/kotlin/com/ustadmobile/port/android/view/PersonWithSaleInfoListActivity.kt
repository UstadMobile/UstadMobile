package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.paging.DataSource
import com.ustadmobile.core.view.PersonWithSaleInfoListView
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo

class PersonWithSaleInfoListActivity : UstadBaseActivity(), PersonWithSaleInfoListView{


    override fun setWEListFactory(factory: DataSource.Factory<Int, PersonWithSaleInfo>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateSortSpinner(presets: Array<String?>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //TODO
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //TODO:
        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO:
    }

    companion object{

    }
}