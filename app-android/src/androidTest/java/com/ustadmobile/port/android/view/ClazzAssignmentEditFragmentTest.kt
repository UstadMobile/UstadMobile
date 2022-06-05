package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.controller.ClazzAssignmentEditPresenter
import com.ustadmobile.core.util.OneToManyJoinEditHelperMp
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.screen.ClazzAssignmentEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.direct
import org.kodein.di.instance


@AdbScreenRecord("ClazzAssignmentEdit screen Test")
class ClazzAssignmentEditFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())

    lateinit var gson: Gson

    private lateinit var fragmentScenario: FragmentScenario<ClazzAssignmentEditFragment>

    @Before
    fun setup() {
        gson = getApplicationDi().direct.instance()
    }



    @AdbScreenRecord("given ClazzAssignment not present when filled then should save to database")
    //@Test //TODO navcontroller missing here
    fun givenNoClazzAssignmentPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {

        val testClazz = Clazz().apply {
            clazzUid = dbRule.repo.clazzDao.insert(this)
        }

        val formVals = ClazzAssignment().apply {
            caTitle = "New Clazz Assignment"
            caDescription = "complete quiz"
        }

        init {

            val newClazzAssignment = ClazzAssignment().apply {
                caClazzUid = testClazz.clazzUid
            }
            val jsonStr = Gson().toJson(newClazzAssignment)
            val args = bundleOf(UstadEditView.ARG_ENTITY_JSON to jsonStr)

            fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = args) {
                ClazzAssignmentEditFragment().also {
                    it.installNavController(systemImplNavRule.navController,
                            initialDestId = R.id.clazz_assignment_edit_dest,
                            initialArgs = args)
                }
            }

        }.run {

            ClazzAssignmentEditScreen {

                clazzAssignmentTitleInput {
                    edit {
                        replaceText(formVals.caTitle!!)
                    }
                }

                clazzAssignmentDescInput {
                    edit {
                        replaceText(formVals.caDescription!!)
                    }
                }

                caStartDateText {
                    edit {
                        setDateWithDialog(DateTime(2021, 3, 1).unixMillisLong)
                    }
                }


                fragmentScenario.onFragment {
                    val content = ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
                        contentEntryUid = 1000L
                        title = "Math Quiz"
                    }

                    it.findNavController().currentBackStackEntry?.savedStateHandle
                            ?.set(ClazzAssignmentEditPresenter.ARG_SAVEDSTATE_CONTENT + OneToManyJoinEditHelperMp.SUFFIX_RETKEY_DEFAULT,
                                    gson.toJson(listOf(content)))
                }


                fragmentScenario.clickOptionMenu(R.id.menu_done)

                var clazzAssignment: ClazzAssignment? = null
                while (clazzAssignment == null) {
                    clazzAssignment = dbRule.db.clazzAssignmentDao.findClazzAssignment()
                }


                Assert.assertEquals("ClazzAssignment data set", formVals.caTitle!!,
                        clazzAssignment.caTitle)

            }


        }
    }


    //Disabled 16/Apr/2022 as per Taiga issue 732
//    @AdbScreenRecord("given ClazzAssignment exists when updated then should be updated on database")
//    @Test
    fun givenClazzAssignmentExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val entry = ContentEntry().apply {
            title = "Quiz"
            contentEntryUid = dbRule.repo.contentEntryDao.insert(this)
        }

        val testClazz = Clazz().apply {
            clazzUid = dbRule.repo.clazzDao.insert(this)
        }

        val existingClazzAssignment = ClazzAssignmentWithCourseBlock().apply {
            caTitle = "New ClazzAssignment"
            caRequireFileSubmission = false
            caClazzUid = testClazz.clazzUid
            caUid = dbRule.repo.clazzAssignmentDao.insert(this)
            block = CourseBlock().apply {
                this.cbClazzUid = testClazz.clazzUid
                this.cbEntityUid = caUid
                cbHideUntilDate = DateTime(2021, 1, 20).unixMillisLong
                this.cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                this.cbUid = dbRule.repo.courseBlockDao.insert(this)
            }
        }


        init {

            val args = bundleOf(UstadView.ARG_ENTITY_UID to existingClazzAssignment.caUid.toString(),
                UstadView.ARG_CLAZZUID to testClazz.clazzUid.toString())
            fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = args) {
                ClazzAssignmentEditFragment().also {
                    it.installNavController(systemImplNavRule.navController,
                            initialDestId = R.id.clazz_assignment_edit_dest,
                            initialArgs = args)
                }
            }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                    .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        }.run {

            ClazzAssignmentEditScreen {

                fragmentScenario.waitUntilLetOnFragment { it.entity }

                flakySafely {
                    clazzAssignmentTitleInput {
                        edit {
                            hasText(existingClazzAssignment.caTitle!!)
                            replaceText("New Quiz")
                            hasText("New Quiz")
                        }
                    }
                }

                this.nestedScroll.swipeUp()

                fragmentScenario.clickOptionMenu(R.id.menu_done)


            }

        }

    }
}