package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.screen.JoinWithCodeScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("JoinWithCode Fragment Tests")
class JoinWithCodeFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("given Fragment Loaded when Clazz Code Given then Show On Screen")
    @Test
    fun givenFragmentLoaded_whenClazzCodeGiven_thenShowOnScreen() {

        init{

            val clazz = Clazz().apply{
                clazzCode="lulz42"
                clazzName = "Class A"
                clazzUid = dbRule.repo.clazzDao.insert(this)
            }

            val person = Person().apply{
                firstNames = "Test"
                lastName = "One"
                personUid = dbRule.repo.personDao.insert(this)
            }

            dbRule.insertPersonAndStartSession(Person().apply {
                admin = true
                firstNames = "Test"
                lastName = "User"
            })
            val bundle = bundleOf(
                    UstadView.ARG_CODE to clazz.clazzCode.toString(),
                    UstadView.ARG_CODE_TABLE to Clazz.TABLE_ID.toString())

            launchFragmentInContainer(themeResId = R.style.UmTheme_App, fragmentArgs = bundle) {
                JoinWithCodeFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                    systemImplNavRule.navController.navigate(R.id.join_with_code_dest)
                }
            }

        }.run{
            JoinWithCodeScreen{
                codeEditLayout{
                    edit{
                        hasText("lulz42")
                    }
                }
            }
        }

    }



}