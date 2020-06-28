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
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.createTestContentEntriesAndJoinToClazzWork
import com.ustadmobile.util.test.ext.insertTestClazzAndMembers
import com.ustadmobile.util.test.ext.insertTestClazzWorkAndQuestionsAndOptionsWithResponse
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.equalTo
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClazzWorkDetailProgressListFragmentTest  {

    lateinit var recyclerViewIdlingResource: RecyclerViewIdlingResource

    lateinit var navController: NavController

    private lateinit var db: UmAppDatabase

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

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

    private fun reloadFragment(clazzWork: ClazzWork){

        val teacherScenario = launchFragmentInContainer(
                bundleOf(UstadView.ARG_ENTITY_UID to clazzWork.clazzWorkUid.toString()),
                themeResId = R.style.UmTheme_App) {
            ClazzWorkDetailProgressListFragment().also{
                it.installNavController(systemImplNavRule.navController)
            }
        }

        teacherScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            recyclerViewIdlingResource.recyclerView = it.mDataBinding!!.fragmentListRecyclerview
        }
        Thread.sleep(2000)
    }

    @Test
    fun givenValidClazzWorkUid_whenStudentsPresentInClazzWithComments_thenShouldUpdateView() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        val clazzWork = ClazzWork().apply {
            clazzWorkTitle = "Test ClazzWork A"
            clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
            clazzWorkInstructions = "Pass espresso test for ClazzWork"
            clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
            clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
            clazzWorkCommentsEnabled = true
            clazzWorkMaximumScore = 120
        }

        val testClazzWork = runBlocking {
            db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                    true,0,false, true)
        }

        val contentEntriesWithJoin = runBlocking {
            db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }
        val contentList = contentEntriesWithJoin.contentList

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid

        val activeAccount = UmAccount(teacherMember.clazzMemberPersonUid, "bond", "", "http://localhost")
        UmAccountManager.setActiveAccount(activeAccount, ApplicationProvider.getApplicationContext())

        reloadFragment(testClazzWork.clazzWork)

        onView(withText("Marked")).check(matches(isDisplayed()))
        //TODO: Test that it shows stuff ok

        val student1 = testClazzWork.clazzAndMembers.studentList.get(0)
        val student3 = testClazzWork.clazzAndMembers.studentList.get(2)

        runBlocking {
            Comments().apply {
                commentsText = "Student 1 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = student1.clazzMemberPersonUid
                commentsUid = db.commentsDao.insertAsync(this)
            }
            Comments().apply {
                commentsText = "Student 3 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = student3.clazzMemberPersonUid
                commentsUid = db.commentsDao.insertAsync(this)
            }

        }

        reloadFragment(testClazzWork.clazzWork)
        onView(withText("Marked")).check(matches(isDisplayed()))
        //TODO: Test comments


    }

    @Test
    fun givenValidClazzWorkUidWithoutContent_whenStudentsPresentInClazzWithComments_thenShouldUpdateView() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        val clazzWork = ClazzWork().apply {
            clazzWorkTitle = "Test ClazzWork A"
            clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
            clazzWorkInstructions = "Pass espresso test for ClazzWork"
            clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
            clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
            clazzWorkCommentsEnabled = true
            clazzWorkMaximumScore = 120
        }

        val testClazzWork = runBlocking {
            db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                    true,0,false, true)
        }

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid

        val activeAccount = UmAccount(teacherMember.clazzMemberPersonUid, "bond", "", "http://localhost")
        UmAccountManager.setActiveAccount(activeAccount, ApplicationProvider.getApplicationContext())

        reloadFragment(testClazzWork.clazzWork)

        onView(withText("Marked")).check(matches(isDisplayed()))
        //TODO: Test that it shows stuff ok

        val student1 = testClazzWork.clazzAndMembers.studentList.get(0)
        val student3 = testClazzWork.clazzAndMembers.studentList.get(2)

        runBlocking {
            Comments().apply {
                commentsText = "Student 1 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = student1.clazzMemberPersonUid
                commentsUid = db.commentsDao.insertAsync(this)
            }
            Comments().apply {
                commentsText = "Student 3 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = student3.clazzMemberPersonUid
                commentsUid = db.commentsDao.insertAsync(this)
            }

        }

        reloadFragment(testClazzWork.clazzWork)
        onView(withText("Marked")).check(matches(isDisplayed()))
        //TODO: Test comments


    }

    @Test
    fun givenValidClazzWorkUid_whenStudentSubmittedAndContentProgressed_thenShouldUpdateView() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        val clazzWork = ClazzWork().apply {
            clazzWorkTitle = "Test ClazzWork A"
            clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
            clazzWorkInstructions = "Pass espresso test for ClazzWork"
            clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
            clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
            clazzWorkCommentsEnabled = true
            clazzWorkMaximumScore = 120
        }

        val testClazzWork = runBlocking {
            db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                    true,0,true, true)
        }

        val contentEntriesWithJoin = runBlocking {
            db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }
        val contentList = contentEntriesWithJoin.contentList

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid

        val activeAccount = UmAccount(teacherMember.clazzMemberPersonUid, "bond", "", "http://localhost")
        UmAccountManager.setActiveAccount(activeAccount, ApplicationProvider.getApplicationContext())

        val student1 = testClazzWork.clazzAndMembers.studentList.get(0)
        val student3 = testClazzWork.clazzAndMembers.studentList.get(2)
        val student4 = testClazzWork.clazzAndMembers.studentList.get(3)

        runBlocking {
            Comments().apply {
                commentsText = "Student 1 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = student1.clazzMemberPersonUid
                commentsUid = db.commentsDao.insertAsync(this)
            }
            Comments().apply {
                commentsText = "Student 3 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = student3.clazzMemberPersonUid
                commentsUid = db.commentsDao.insertAsync(this)
            }

        }

        contentList.forEach{
            runBlocking {
                ContentEntryProgress().apply {
                    contentEntryProgressActive = true
                    contentEntryProgressContentEntryUid = it.contentEntryUid
                    contentEntryProgressPersonUid = student1.clazzMemberPersonUid
                    contentEntryProgressProgress = 42.0F
                    contentEntryProgressStatusFlag = ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED
                    contentEntryProgressUid = db.contentEntryProgressDao.insertAsync(this)
                }

                ContentEntryProgress().apply {
                    contentEntryProgressActive = true
                    contentEntryProgressContentEntryUid = it.contentEntryUid
                    contentEntryProgressPersonUid = student3.clazzMemberPersonUid
                    contentEntryProgressProgress = 24.0F
                    contentEntryProgressStatusFlag = ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED
                    contentEntryProgressUid = db.contentEntryProgressDao.insertAsync(this)
                }

                ContentEntryProgress().apply {
                    contentEntryProgressActive = true
                    contentEntryProgressContentEntryUid = it.contentEntryUid
                    contentEntryProgressPersonUid = student4.clazzMemberPersonUid
                    contentEntryProgressProgress = 100.0F
                    contentEntryProgressStatusFlag = ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED
                    contentEntryProgressUid = db.contentEntryProgressDao.insertAsync(this)
                }
            }
        }


        Thread.sleep(2000)
        reloadFragment(testClazzWork.clazzWork)
        onView(withText("Marked")).check(matches(isDisplayed()))
        //TODO: Test line2 and progress

    }

    @Test
    fun givenValidClazzWorkUid_whenTeacherSeesStudentListedAndClicked_thenShouldGoToMarking() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        val clazzWork = ClazzWork().apply {
            clazzWorkTitle = "Test ClazzWork A"
            clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
            clazzWorkInstructions = "Pass espresso test for ClazzWork"
            clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
            clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
            clazzWorkCommentsEnabled = true
            clazzWorkMaximumScore = 120
        }

        val testClazzWork = runBlocking {
            db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                    true,0,true, true)
        }

        val contentEntriesWithJoin = runBlocking {
            db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }
        val contentList = contentEntriesWithJoin.contentList

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid

        val activeAccount = UmAccount(teacherMember.clazzMemberPersonUid, "bond", "", "http://localhost")
        UmAccountManager.setActiveAccount(activeAccount, ApplicationProvider.getApplicationContext())


        val student1 = testClazzWork.clazzAndMembers.studentList.get(0)
        val student3 = testClazzWork.clazzAndMembers.studentList.get(2)

        runBlocking {
            Comments().apply {
                commentsText = "Student 1 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = student1.clazzMemberPersonUid
                commentsUid = db.commentsDao.insertAsync(this)
            }
            Comments().apply {
                commentsText = "Student 3 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = student3.clazzMemberPersonUid
                commentsUid = db.commentsDao.insertAsync(this)
            }

        }


        //Make submissions Student 1 and Student 4
        val student4 = testClazzWork.clazzAndMembers.studentList.get(3)


        reloadFragment(testClazzWork.clazzWork)

        Thread.sleep(2000)

        Thread.sleep(1000)
        onView(withText("Marked")).check(matches(isDisplayed()))
        //TODO: Test clicking on one goes to Marking

    }



}