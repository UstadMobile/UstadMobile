package com.ustadmobile.port.android.view

import android.os.Bundle
import com.toughra.ustadmobile.R
import androidx.activity.ComponentActivity
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.SelQuestionSet
import com.ustadmobile.port.android.view.util.CrudListActivityResultContract


fun ComponentActivity.prepareSelQuestionSetPickFromListCall(callback: (List<SelQuestionSet>?) -> Unit)
        = prepareCall(CrudListActivityResultContract(this, SelQuestionSet::class.java,
            SelQuestionSetListActivity::class.java)) {
    callback.invoke(it)
}

class SelQuestionSetListActivity: UstadListViewActivity(), UstadListViewActivityWithFab{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = if(intent?.getStringExtra(UstadView.ARG_LISTMODE) ==
                ListViewMode.PICKER.toString()) {
            resources.getString(R.string.select_item, resources.getString(R.string.sel_question_set))
        }else {
            resources.getString(R.string.sel_question_set)
        }

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.activity_listfragment_frame,
                            SelQuestionSetListFragment.newInstance(intent.extras))
                    .commit()
        }
    }

}