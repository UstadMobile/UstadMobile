package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertTestClazzAndMembers
import com.ustadmobile.util.test.ext.insertClazzLogs
import com.ustadmobile.util.test.ext.insertTestClazzWorkAndQuestionsAndOptionsWithResponse
import it.xabaras.android.espresso.recyclerviewchildactions.RecyclerViewChildActions.Companion.childOfViewAtPositionWithMatcher
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClazzWorkDetailOverviewFragmentTest {

    lateinit var recyclerViewIdlingResource: RecyclerViewIdlingResource

    lateinit var navController: NavController

    private lateinit var db: UmAppDatabase

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @Before
    fun setup() {
        recyclerViewIdlingResource = RecyclerViewIdlingResource(null)
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        UstadMobileSystemImpl.instance.navController = navController

        val activeAccount = UmAccount(7L, "bond", "",
                "http://localhost")
        UmAccountManager.setActiveAccount(activeAccount,
                ApplicationProvider.getApplicationContext())

        db = UmAccountManager.getActiveDatabase(ApplicationProvider.getApplicationContext())
        db.clearAllTables()
    }

    @After
    fun tearDown(){
        UstadMobileSystemImpl.instance.navController = null
    }

    @Test
    fun givenLValidClazzWorkUid_whenLoadedAsStudent_thenShouldShow() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        val testClazzWork = runBlocking {
            db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    ClazzWork(), false, 0, true,
                    0,false, true)
        }


        val studentScenario = launchFragmentInContainer(
                bundleOf(UstadView.ARG_ENTITY_UID to testClazzWork.clazzWork.clazzWorkUid.toString()),
                themeResId = R.style.Theme_UstadTheme) {
            ClazzWorkDetailOverviewFragment().also{
                it.installNavController(systemImplNavRule.navController)
            }
        }

        studentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.fragmentClazzWorkWithSubmissionDetailRv
        }

       //TODO: Test the values set for detail, the right questions, etc

        //TODO: Test comments loaded (if any)

        //TODO: Test Submit button and headings are shown OK

    }

    @Test
    fun givenLValidClazzWorkUid_whenQuestionAnsweredAsStudentAndSubmitted_thenShouldUpdateView() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        val testClazzWork = runBlocking {
            db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    ClazzWork(), false, 0, true,
                    0,false, true)
        }


        val studentScenario = launchFragmentInContainer(
                bundleOf(UstadView.ARG_ENTITY_UID to testClazzWork.clazzWork.clazzWorkUid.toString()),
                themeResId = R.style.Theme_UstadTheme) {
            ClazzWorkDetailOverviewFragment().also{
                it.installNavController(systemImplNavRule.navController)
            }
        }

        studentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.fragmentClazzWorkWithSubmissionDetailRv
        }

        //TODO: Answer questions


        //TODO: Hit Submit

        //TODO: Check view

    }

    @Test
    fun givenLValidClazzWorkUid_whenSubmissionMarkedByTeacherAndStudentLogsIn_thenShouldUpdateScore() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        val testClazzWork = runBlocking {
            db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    ClazzWork(), false, 0, true,
                    0,false, true)
        }


        val studentScenario = launchFragmentInContainer(
                bundleOf(UstadView.ARG_ENTITY_UID to testClazzWork.clazzWork.clazzWorkUid.toString()),
                themeResId = R.style.Theme_UstadTheme) {
            ClazzWorkDetailOverviewFragment().also{
                it.installNavController(systemImplNavRule.navController)
            }
        }

        studentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.fragmentClazzWorkWithSubmissionDetailRv
        }

        //TODO: Set user Student
        //TODO: Answer questions

        //TODO: Hit Submit

        //TODO: Set user Teacher
        //TODO: Mark score

        //TODO: Set user student
        //TODO: Check score ok on view and unable to edit

    }

    @Test
    fun givenLValidClazzWorkUid_whenLoadedAsTeacher_thenShouldShow() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        val testClazzWork = runBlocking {
            db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    ClazzWork(), false, 0, true,
                    0,false, true)
        }
        //TODO Add comments, etc


        val studentScenario = launchFragmentInContainer(
                bundleOf(UstadView.ARG_ENTITY_UID to testClazzWork.clazzWork.clazzWorkUid.toString()),
                themeResId = R.style.Theme_UstadTheme) {
            ClazzWorkDetailOverviewFragment().also{
                it.installNavController(systemImplNavRule.navController)
            }
        }

        studentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.fragmentClazzWorkWithSubmissionDetailRv
        }

        //TODO: Test the values set for detail, the right questions, etc

        //TODO :Public comments show OK

        //TODO: Edit button goes to edit screen OK

    }


    @Test
    fun givenLValidClazzWorkUid_whenPrivateCommentedByOneUser_thenOtherUsershallNotSee() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        val testClazzWork = runBlocking {
            db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    ClazzWork(), false, 0, true,
                    0,false, true)
        }


        val studentScenario = launchFragmentInContainer(
                bundleOf(UstadView.ARG_ENTITY_UID to testClazzWork.clazzWork.clazzWorkUid.toString()),
                themeResId = R.style.Theme_UstadTheme) {
            ClazzWorkDetailOverviewFragment().also{
                it.installNavController(systemImplNavRule.navController)
            }
        }

        studentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.fragmentClazzWorkWithSubmissionDetailRv
        }

        //TODO: Student 1 logged in user. Makes private comment .

        //TODO: Student 2 logged in user. Cannot see private comment.

    }



}