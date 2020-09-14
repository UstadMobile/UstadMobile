package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onIdle
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Class edit screen tests")
class JoinWithCodeFragmentTest  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule =
            ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule =
            ScenarioIdlingResourceRule(CrudIdlingResource())


    @AdbScreenRecord("")
    @Test
    fun givenLoadedIncorrect_whenLoaded_shouldShowError() {


        val clazz = Clazz().apply{
            clazzCode="lulz42"
            clazzName = "Class A"
            clazzUid = dbRule.db.clazzDao.insert(this)
        }

        val person = Person().apply{
            firstNames = "Test"
            lastName = "One"
            personUid = dbRule.db.personDao.insert(this)
        }

        //dbRule.account.personUid = person.personUid
        dbRule.insertPersonForActiveUser(Person().apply {
            admin = true
            firstNames = "Test"
            lastName = "User"
        })


        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            JoinWithCodeFragment().also {
                it.installNavController(systemImplNavRule.navController)
                it.arguments = bundleOf(
                        UstadView.ARG_CODE to clazz.clazzCode.toString(),
                        UstadView.ARG_CODE_TABLE to Clazz.TABLE_ID
                )
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        onIdle()

        //fragmentScenario.clickOptionMenu(R.id.menu_done)

        println("")

    }



}