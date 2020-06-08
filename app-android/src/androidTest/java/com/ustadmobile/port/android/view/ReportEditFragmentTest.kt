package com.ustadmobile.port.android.view

import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Rule
import org.junit.Test

class ReportEditFragmentTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @Test
    fun givenNoReportPresentYet_whenFilledInAndSaveClicked_thenShouldGoToDetailWithJson() {


    }

    @Test
    fun givenReportExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {



    }


    companion object {


    }

}