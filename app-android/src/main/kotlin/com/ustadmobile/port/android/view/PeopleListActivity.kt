package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.R

class PeopleListActivity : UstadBaseActivity(){


    lateinit var toolbar:Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_list)

        //Toolbar:
        toolbar = findViewById(R.id.activity_people_list_toolbar)
        toolbar!!.title = getText(R.string.users)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val currentFrag = PeopleListFragment.newInstance(true)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_people_list_framelayout, currentFrag)
                    .commit()
        }
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