package com.ustadmobile.port.android.view

import android.os.Bundle
import com.toughra.ustadmobile.R
import androidx.activity.ComponentActivity
import com.github.clans.fab.FloatingActionButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.ustadmobile.lib.db.entities.PersonGroup



class PersonGroupListActivity: UstadBaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_listfragment_holder)
        setSupportActionBar(findViewById(R.id.activity_listfragment_toolbar))

        val frag = PersonGroupListFragment.newInstance(intent.extras)
        findViewById<ExtendedFloatingActionButton>(R.id.activity_listfragment_fab)
                .setOnClickListener {

                }

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_listfragment_frame,
                            frag)
                    .commit()
        }
    }

}