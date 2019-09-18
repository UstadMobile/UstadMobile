package com.ustadmobile.port.android.view

import android.os.Bundle
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
}