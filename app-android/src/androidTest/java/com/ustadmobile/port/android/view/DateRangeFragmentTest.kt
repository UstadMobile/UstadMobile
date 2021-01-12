package com.ustadmobile.port.android.view

import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.ustadmobile.port.android.screen.DateRangeScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.DateRangePresenter
import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.schedule.localEndOfDay

import com.ustadmobile.lib.db.entities.Moment
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.DateRangeMoment
import org.junit.Before


@AdbScreenRecord("DateRange screen Test")
class DateRangeFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @AdbScreenRecord("given Moment not present when filled then should save to database")
    @Test
    fun givenNoMomentPresentYet_whenFilledInAndSaveClicked_thenShouldFinishResult() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            DateRangeFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init {

        }.run {

            DateRangeScreen {

                fromRelRadio.click()

                fromFixedDateTextInput.isGone()

                fromRelDateRelInput.isVisible()

                fromRelDateRelOffSetInput {
                    isVisible()
                    edit {
                        replaceText("10")
                    }
                }

                setMessageIdOption(fromRelDateRelUnitTextView,
                        systemImplNavRule.impl.getString(
                                DateRangePresenter.RelUnitOption.MONTH.messageId, context))


                toRelDateRelInput.isGone()
                toRelDateRelOffSetInput.isGone()
                toRelDateRelUnitInput.isGone()

                setDateField(R.id.fragment_date_range_fixed_date_toDate_textInputLayout,
                        DateTime.nowLocal().utc.unixMillisLong)

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                val entity = fragmentScenario.letOnFragment { it.entity }!!

                Assert.assertEquals("fromMoment matches",
                        Moment().apply {
                            typeFlag = Moment.TYPE_FLAG_RELATIVE
                            relOffSet = -10
                            relUnit = Moment.MONTHS_REL_UNIT
                            relTo = Moment.TODAY_REL_TO
                        }, entity.fromMoment)

                Assert.assertEquals("toMoment matches",
                        Moment().apply {
                            typeFlag = Moment.TYPE_FLAG_FIXED
                            fixedTime = entity.toMoment.fixedTime
                        }, entity.toMoment)

            }


        }
    }


    @AdbScreenRecord("given Moment exists when updated then should be updated on database")
    @Test
    fun givenMomentNotPresentYet_whenValuesForFixedDateNotSet_showErrors() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            DateRangeFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init {

        }.run {

            DateRangeScreen {

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                fromFixedDateTextInput{
                    isErrorEnabled()
                    hasError(systemImplNavRule.impl.getString(MessageID.field_required_prompt, context))
                }

                toFixedDateTextInput{
                    isErrorEnabled()
                    hasError(systemImplNavRule.impl.getString(MessageID.field_required_prompt, context))
                }

            }

        }
    }

}