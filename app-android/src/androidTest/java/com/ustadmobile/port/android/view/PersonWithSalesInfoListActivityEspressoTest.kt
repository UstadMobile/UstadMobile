package com.ustadmobile.port.android.view

import androidx.test.espresso.intent.rule.IntentsTestRule
import com.ustadmobile.core.db.UmAppDatabase
import org.junit.Rule

class PersonWithSalesInfoListActivityEspressoTest {


    @get:Rule
    var mActivityRule = IntentsTestRule(PersonWithSaleInfoListActivity::class.java,
            false, false);

    


}