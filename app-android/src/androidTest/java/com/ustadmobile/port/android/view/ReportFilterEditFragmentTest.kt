package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.controller.ReportFilterEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.ReportFilter.Companion.FIELD_CONTENT_ENTRY
import com.ustadmobile.lib.db.entities.UidAndLabel
import com.ustadmobile.port.android.screen.ReportFilterEditScreen
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.setMessageIdOption
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertContentEntryWithTranslations
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("Report filter edit screen tests")
class ReportFilterEditFragmentTest: TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @AdbScreenRecord("with no report present, fill all the fields and navigate to detail")
    @Test
    fun givenNoReportFilterPresentYet_whenPersonGenderFilledIn_thenShouldNavigateBackToReportEditScreen() {
        val defaultFilter = ReportFilter().apply {
            reportFilterUid = 1
        }
        val jsonStr = Gson().toJson(defaultFilter)

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadEditView.ARG_ENTITY_JSON to jsonStr)) {
            ReportFilterEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ReportFilterEditScreen{

                setMessageIdOption(fieldTextValue,
                        systemImplNavRule.impl.getString(MessageID.field_person_gender,
                                ApplicationProvider.getApplicationContext()))

                setMessageIdOption(conditionTextValue,
                        systemImplNavRule.impl.getString(MessageID.condition_is_not,
                                ApplicationProvider.getApplicationContext()))

                setMessageIdOption(valueDropDownTextValue,
                        systemImplNavRule.impl.getString(MessageID.unset,
                                ApplicationProvider.getApplicationContext()))


            }

            fragmentScenario.clickOptionMenu(R.id.menu_done)



        }

    }


    @AdbScreenRecord("given existing report filter, update the age value and navigate to report edit screen")
    @Test
    fun givenExistingReportFilter_whenPersonAgeUpdated_thenReturnUpdatedValueAndNavigateBackToReportEditScreen() {
        val existingReportFilter = ReportFilter().apply{
            reportFilterUid = 1
            reportFilterField = ReportFilter.FIELD_PERSON_AGE
            reportFilterCondition = ReportFilter.CONDITION_GREATER_THAN
            reportFilterValue = 13.toString()
        }

        val jsonStr = Gson().toJson(existingReportFilter)

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadEditView.ARG_ENTITY_JSON to jsonStr)) {
            ReportFilterEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ReportFilterEditScreen{

                fieldTextValue{
                    hasText(systemImplNavRule.impl.getString(
                            ReportFilterEditPresenter.FieldOption.PERSON_AGE.messageId,
                            ApplicationProvider.getApplicationContext()))
                }

                conditionTextValue{
                    hasText(systemImplNavRule.impl.getString(
                            ReportFilterEditPresenter.ConditionOption.GREATER_THAN_CONDITION.messageId,
                            ApplicationProvider.getApplicationContext()))
                }

                valuesDropDownTextInputLayout{
                    isGone()
                }

                valueIntegerTextInputLayout{
                    edit{
                        hasText(existingReportFilter.reportFilterValue!!)
                        clearText()
                        replaceText("16")
                    }
                }

            }


        }

    }


    @AdbScreenRecord("given existing report filter when field option changes then other fields are cleared")
    @Test
    fun givenExistingReportFilter_whenFieldOptionsChanges_thenOtherFieldsAreCleared() {
        val existingReportFilter = ReportFilter().apply{
            reportFilterUid = 1
            reportFilterField = ReportFilter.FIELD_PERSON_AGE
            reportFilterCondition = ReportFilter.CONDITION_GREATER_THAN
            reportFilterValue = 13.toString()
        }

        val jsonStr = Gson().toJson(existingReportFilter)

        launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadEditView.ARG_ENTITY_JSON to jsonStr)) {
            ReportFilterEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ReportFilterEditScreen{

                // before change
                fieldTextValue{
                    hasText(systemImplNavRule.impl.getString(
                            ReportFilterEditPresenter.FieldOption.PERSON_AGE.messageId,
                            ApplicationProvider.getApplicationContext()))
                }

                conditionTextValue{
                    hasText(systemImplNavRule.impl.getString(
                            ReportFilterEditPresenter.ConditionOption.GREATER_THAN_CONDITION.messageId,
                            ApplicationProvider.getApplicationContext()))
                }

                valuesDropDownTextInputLayout{
                    isGone()
                }

                // make the change
                setMessageIdOption(fieldTextValue,
                        systemImplNavRule.impl.getString(
                                ReportFilterEditPresenter.FieldOption.PERSON_GENDER.messageId,
                                ApplicationProvider.getApplicationContext()))


                // after change
                fieldTextValue{
                    hasText(systemImplNavRule.impl.getString(
                            ReportFilterEditPresenter.FieldOption.PERSON_GENDER.messageId,
                            ApplicationProvider.getApplicationContext()))
                }


                conditionTextValue{
                    hasEmptyText()
                }

                valueIntegerTextInputLayout{
                    edit{
                        hasEmptyText()
                    }
                }


            }


        }

    }

    @AdbScreenRecord("given existing report filter with entry list, when changed then check changes successful")
    @Test
    fun givenExistingReportFilterWithListOfEntries_whenChanged_thenCheckChanges() {

        runBlocking{

            dbRule.repo.contentEntryDao.insertListAsync(listOf(
                    ContentEntry().apply{
                        contentEntryUid = 1
                        title = "Khan Academy"
                    },
                    ContentEntry().apply{
                        contentEntryUid = 2
                        title = "Ustad Mobile"
                    },
                    ContentEntry().apply{
                        contentEntryUid = 3
                        title = "Phet Slides"
                    }
            ))

        }
        val existingReportFilter = ReportFilter().apply{
            reportFilterUid = 1
            reportFilterField = ReportFilter.FIELD_CONTENT_ENTRY
            reportFilterCondition = ReportFilter.CONDITION_IN_LIST
            reportFilterValue = "1, 3"
        }

        val jsonStr = Gson().toJson(existingReportFilter)

        val scenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadEditView.ARG_ENTITY_JSON to jsonStr)) {
            ReportFilterEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ReportFilterEditScreen{

                fieldTextValue{
                    hasText(systemImplNavRule.impl.getString(
                            ReportFilterEditPresenter.FieldOption.CONTENT_ENTRY.messageId,
                            ApplicationProvider.getApplicationContext()))
                }

                conditionTextValue{
                    hasText(systemImplNavRule.impl.getString(
                            ReportFilterEditPresenter.ConditionOption.IN_LIST_CONDITION.messageId,
                            ApplicationProvider.getApplicationContext()))
                }

                uidAndLabelRecycler{
                    firstChild<ReportFilterEditScreen.UidAndLabel> {
                        labelName{
                            hasText("Khan Academy")
                        }
                    }
                    lastChild<ReportFilterEditScreen.UidAndLabel> {
                        labelName{
                            hasText("Phet Slides")
                        }
                        deleteButton.click()
                    }

                    hasSize(1)

                    scenario.onFragment {
                        it.findNavController().currentBackStackEntry?.savedStateHandle
                                ?.set("ContentEntry", defaultGson().toJson(listOf(ContentEntry().apply{
                                    contentEntryUid = 2
                                    title = "Ustad Mobile"
                                })))
                    }

                    hasSize(2)

                    lastChild<ReportFilterEditScreen.UidAndLabel> {
                        labelName{
                            hasText("Ustad Mobile")
                        }
                    }

                }
            }
        }

    }




}