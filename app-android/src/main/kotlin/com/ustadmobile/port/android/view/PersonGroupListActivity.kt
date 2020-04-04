package com.ustadmobile.port.android.view

import android.os.Bundle
import com.toughra.ustadmobile.R
import androidx.activity.ComponentActivity
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.port.android.view.util.CrudListActivityResultContract


fun ComponentActivity.preparePersonGroupPickFromListCall(callback: (List<PersonGroup>?) -> Unit)
        = prepareCall(CrudListActivityResultContract(this, PersonGroup::class.java,
            PersonGroupListActivity::class.java)) {
    callback.invoke(it)
}

class PersonGroupListActivity: UstadBaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_listfragment_holder)
        setSupportActionBar(findViewById(R.id.activity_listfragment_toolbar))

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_listfragment_frame,
                            PersonGroupListFragment.newInstance(intent.extras))
                    .commit()
        }
    }

}