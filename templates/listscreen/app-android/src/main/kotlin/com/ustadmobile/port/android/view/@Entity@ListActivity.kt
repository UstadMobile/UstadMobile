package com.ustadmobile.port.android.view

import android.os.Bundle
import com.toughra.ustadmobile.R
import androidx.activity.ComponentActivity
import com.ustadmobile.lib.db.entities.@Entity@
import com.ustadmobile.port.android.view.util.CrudListActivityResultContract


fun ComponentActivity.prepare@Entity@PickFromListCall(callback: (List<@Entity@>?) -> Unit)
        = prepareCall(CrudListActivityResultContract(this, @Entity@::class.java,
            @Entity@ListActivity::class.java)) {
    callback.invoke(it)
}

class @Entity@ListActivity: UstadListViewActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_listfragment_holder)
        setSupportActionBar(findViewById(R.id.activity_listfragment_toolbar))

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_listfragment_frame,
                            @Entity@ListFragment.newInstance(intent.extras))
                    .commit()
        }
    }

}