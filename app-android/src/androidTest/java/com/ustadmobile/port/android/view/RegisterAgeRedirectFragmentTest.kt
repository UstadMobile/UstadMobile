package com.ustadmobile.port.android.view

import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.soywiz.klock.years
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.screen.RegisterAgeRedirectScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class RegisterAgeRedirectFragmentTest: TestCase() {

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()


    @JvmField
    @Parameterized.Parameter(value = 0)
    var age: Int = 0

    @JvmField
    @Parameterized.Parameter(value = 1)
    var destScreen: Int = 0

    /**
     *
     */
    @Test
    fun givenAgeIsSet_whenClickNext_thenShouldGoToCorrectDestination() {
        init {
            launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
                RegisterAgeRedirectFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }
        }.run {
            RegisterAgeRedirectScreen {
                datePicker {
                    val tenYearsYoung = DateTime.now() - age.years
                    setDate(tenYearsYoung.yearInt, tenYearsYoung.month0, tenYearsYoung.dayOfMonth)
                }

                nextButton {
                    click()
                }

                flakySafely {
                    Assert.assertEquals("Navigated to expected destination",
                        destScreen, systemImplNavRule.navController.currentDestination?.id)
                }
            }
        }
    }


    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun initParams(): Collection<Array<Any>> {
            return arrayListOf(
                    arrayOf<Any>(10, R.id.person_edit_register_dest),
                    arrayOf<Any>(20, R.id.site_terms_detail_accept_dest)
            )
        }

    }


}