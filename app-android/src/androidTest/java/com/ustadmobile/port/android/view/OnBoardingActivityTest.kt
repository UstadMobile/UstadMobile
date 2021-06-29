package com.ustadmobile.port.android.view

import androidx.test.core.app.launchActivity
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.kakaocup.kakao.common.views.KView
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.port.android.screen.OnBoardingScreen
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


@RunWith(AndroidJUnit4::class)
@AdbScreenRecord("OnBoarding test")
class OnBoardingActivityTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @AdbScreenRecord("given onboarding when user clicks arabic, activity recreated in arabic")
    @Test
    fun givenOnBoardingDisplays_whenUserClicksOnArabic_thenActivityRecreatedInArabic() {



        init{
            launchActivity<OnBoardingActivity>()
        }.run {

            OnBoardingScreen{

                langOption{
                    edit{
                        click()
                    }
                }
                KView{ withText("العربية") } perform {
                    inRoot { isPlatformPopup() }
                    isDisplayed()
                    click()
                }

                Assert.assertEquals("device lang is english", "en", Locale.getDefault().language.substring(0, 2))



            }

        }

    }

    @AdbScreenRecord("given on boarding when user clicks on getstarted then goes to main activity")
    @Test
    fun givenOnBoardingDisplays_whenUserClicksGetStarted_thenGoesToMainActivity() {



        before {
            Intents.init()
            launchActivity<OnBoardingActivity>()
        }.after {
            Intents.release()
        }.run {

            OnBoardingScreen{
                getStartedButton{
                    click()
                }
                intended(hasComponent(hasClassName(MainActivity::class.java.name)))
            }


        }
    }


}
