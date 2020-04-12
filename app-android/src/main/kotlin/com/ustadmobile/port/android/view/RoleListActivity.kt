package com.ustadmobile.port.android.view

import android.os.Bundle
import com.toughra.ustadmobile.R
import androidx.activity.ComponentActivity
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.port.android.view.util.CrudListActivityResultContract


fun ComponentActivity.prepareRolePickFromListCall(callback: (List<Role>?) -> Unit)
        = prepareCall(CrudListActivityResultContract(this, Role::class.java,
            RoleListActivity::class.java)) {
    callback.invoke(it)
}

class RoleListActivity: UstadListViewActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_listfragment_holder)
        setSupportActionBar(findViewById(R.id.activity_listfragment_toolbar))

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_listfragment_frame,
                            RoleListFragment.newInstance(intent.extras))
                    .commit()
        }
    }

}