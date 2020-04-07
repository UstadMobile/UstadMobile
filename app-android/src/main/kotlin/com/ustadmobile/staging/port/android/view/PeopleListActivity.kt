package com.ustadmobile.staging.port.android.view

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.UstadBaseActivity

class PeopleListActivity : UstadBaseActivity(){

    lateinit var toolbar:Toolbar
    lateinit var currentFrag : PeopleListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_list)

        //Toolbar:
        toolbar = findViewById(R.id.activity_people_list_toolbar)
        toolbar.title = getText(R.string.users)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        currentFrag = PeopleListFragment.newInstance(true)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_people_list_framelayout, currentFrag)
                    .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu?.findItem(R.id.action_search)?.actionView as SearchView

        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(componentName))
        searchView.queryHint = getText(R.string.name)

        searchView.maxWidth = Integer.MAX_VALUE

        // listening to search query text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                // filter recycler view when query submitted
                currentFrag.searchPeople(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                // filter recycler view when query submitted
                currentFrag.searchPeople(query)
                return false
            }
        })

        searchView.setOnCloseListener {
            val query=""
            // filter recycler view when query submitted
            currentFrag.searchPeople(query)
            false
        }
        return true
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}