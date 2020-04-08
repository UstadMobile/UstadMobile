package com.ustadmobile.port.android.view

import android.os.Bundle
import com.toughra.ustadmobile.R
import androidx.activity.ComponentActivity
import com.ustadmobile.lib.db.entities.SelQuestionSet
import com.ustadmobile.port.android.view.util.CrudListActivityResultContract


fun ComponentActivity.prepareSelQuestionSetPickFromListCall(callback: (List<SelQuestionSet>?) -> Unit)
        = prepareCall(CrudListActivityResultContract(this, SelQuestionSet::class.java,
            SelQuestionSetListActivity::class.java)) {
    callback.invoke(it)
}

class SelQuestionSetListActivity: UstadBaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_listfragment_holder)
        setSupportActionBar(findViewById(R.id.activity_listfragment_toolbar))

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_listfragment_frame,
                            SelQuestionSetListFragment.newInstance(intent.extras))
                    .commit()
        }
    }

}