package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.db.entities.ContentEntryProgress
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import com.ustadmobile.util.test.ext.createTestContentEntriesAndJoinToClazzWork
import com.ustadmobile.util.test.ext.insertTestClazzWorkAndQuestionsAndOptionsWithResponse
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ClazzWorkDetailProgressListFragmentTest  {

    lateinit var recyclerViewIdlingResource: RecyclerViewIdlingResource

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())

    @Before
    fun setup() {
        recyclerViewIdlingResource = RecyclerViewIdlingResource(null, 3)
    }

    @After
    fun tearDown(){
        UstadMobileSystemImpl.instance.navController = null
    }

    private fun reloadFragment(clazzWork: ClazzWork){

        val fragmentScenario = launchFragmentInContainer(
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to clazzWork.clazzWorkUid.toString()),
                themeResId = R.style.UmTheme_App) {
            ClazzWorkDetailProgressListFragment(). also {
                it.installNavController(systemImplNavRule.navController)
                it.arguments = bundleOf(UstadView.ARG_ENTITY_UID to clazzWork.clazzWorkUid.toString())
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        fragmentScenario.onFragment {
            recyclerViewIdlingResource.recyclerView =
                    it.mDataBinding!!.fragmentListRecyclerview
        }
    }

    @Test
    fun givenValidClazzWorkUid_whenStudentsPresentInClazzWithComments_thenShouldUpdateView() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

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
            dbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                    true,0,false, true)
        }

        val contentEntriesWithJoin = runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
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
                commentsUid = dbRule.db.commentsDao.insertAsync(this)
            }
            Comments().apply {
                commentsText = "Student 3 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = student3.clazzMemberPersonUid
                commentsUid = dbRule.db.commentsDao.insertAsync(this)
            }

        }

        reloadFragment(testClazzWork.clazzWork)
        onView(withText("Marked")).check(matches(isDisplayed()))
        //TODO: Test comments


    }

    @Test
    fun givenValidClazzWorkUidWithoutContent_whenStudentsPresentInClazzWithComments_thenShouldUpdateView() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

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
            dbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
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
                commentsUid = dbRule.db.commentsDao.insertAsync(this)
            }
            Comments().apply {
                commentsText = "Student 3 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = student3.clazzMemberPersonUid
                commentsUid = dbRule.db.commentsDao.insertAsync(this)
            }

        }

        reloadFragment(testClazzWork.clazzWork)
        onView(withText("Marked")).check(matches(isDisplayed()))
        //TODO: Test comments


    }

    @Test
    fun givenValidClazzWorkUid_whenStudentSubmittedAndContentProgressed_thenShouldUpdateView() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

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
            dbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                    true,0,true, true)
        }

        val contentEntriesWithJoin = runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
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
                commentsUid = dbRule.db.commentsDao.insertAsync(this)
            }
            Comments().apply {
                commentsText = "Student 3 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = student3.clazzMemberPersonUid
                commentsUid = dbRule.db.commentsDao.insertAsync(this)
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
                    contentEntryProgressUid = dbRule.db.contentEntryProgressDao.insertAsync(this)
                }

                ContentEntryProgress().apply {
                    contentEntryProgressActive = true
                    contentEntryProgressContentEntryUid = it.contentEntryUid
                    contentEntryProgressPersonUid = student3.clazzMemberPersonUid
                    contentEntryProgressProgress = 24.0F
                    contentEntryProgressStatusFlag = ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED
                    contentEntryProgressUid = dbRule.db.contentEntryProgressDao.insertAsync(this)
                }

                ContentEntryProgress().apply {
                    contentEntryProgressActive = true
                    contentEntryProgressContentEntryUid = it.contentEntryUid
                    contentEntryProgressPersonUid = student4.clazzMemberPersonUid
                    contentEntryProgressProgress = 100.0F
                    contentEntryProgressStatusFlag = ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED
                    contentEntryProgressUid = dbRule.db.contentEntryProgressDao.insertAsync(this)
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
            dbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                    true,0,true, true)
        }

        val contentEntriesWithJoin = runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
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
                commentsUid = dbRule.db.commentsDao.insertAsync(this)
            }
            Comments().apply {
                commentsText = "Student 3 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = student3.clazzMemberPersonUid
                commentsUid = dbRule.db.commentsDao.insertAsync(this)
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