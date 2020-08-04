package com.ustadmobile.port.android.view

import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.binding.setClazzWorkMarking
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import com.ustadmobile.util.test.ext.TestClazzWork
import com.ustadmobile.util.test.ext.createTestContentEntriesAndJoinToClazzWork
import com.ustadmobile.util.test.ext.insertTestClazzWorkAndQuestionsAndOptionsWithResponse
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.*

@AdbScreenRecord("ClazzWork (Assignments) Progress List Tests")
class ClazzWorkDetailProgressListFragmentTest  {

    //lateinit var recyclerViewIdlingResource: RecyclerViewIdlingResource

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true,
            account = UmAccount(7L, "bond", "", "http://localhost"))

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
        //recyclerViewIdlingResource = RecyclerViewIdlingResource(null, 3)
    }

    @After
    fun tearDown(){
        UstadMobileSystemImpl.instance.navController = null
    }

    @AdbScreenRecord("ClazzWorkDetailProgressList: Should show correct list when content/progress does NOT exist")
    @Test
    fun givenValidClazzWorkUid_whenStudentsPresentInClazzWithComments_thenShouldUpdateView() {

        //IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

        val clazzWork = ClazzWork().apply {
            clazzWorkTitle = "Test ClazzWork A"
            clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
            clazzWorkInstructions = "Pass espresso test for ClazzWork"
            clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
            clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
            clazzWorkCommentsEnabled = true
            clazzWorkMaximumScore = 120
            clazzWorkActive = true
        }

        val testClazzWork = runBlocking {
            dbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                    true,0, submitted = false,
                    isStudentToClazz = true)
        }

        val contentEntriesWithJoin = runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(
                    testClazzWork.clazzWork,2)
        }

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid

        reloadFragment(testClazzWork.clazzWork)

        checkProgressList(testClazzWork)

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

        checkProgressList(testClazzWork)

    }

    @AdbScreenRecord("ClazzWorkDetailProgressList: Should show correct list when content/progress exists")
    @Test
    fun givenValidClazzWorkUidWithoutContent_whenStudentsPresentInClazzWithComments_thenShouldUpdateView() {

        //IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

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

        reloadFragment(testClazzWork.clazzWork)

        checkProgressList(testClazzWork)

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

        checkProgressList(testClazzWork)

    }

    @AdbScreenRecord("ClazzWorkDetailProgressList: Should show correct progress ")
    @Test
    fun givenValidClazzWorkUid_whenStudentSubmittedAndContentProgressed_thenShouldUpdateView() {

        //IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

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
                    true,0, submitted = true, isStudentToClazz = true)
        }

        val contentEntriesWithJoin = runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }
        val contentList = contentEntriesWithJoin.contentList

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid

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
                    contentEntryProgressProgress = 42
                    contentEntryProgressStatusFlag = ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED
                    contentEntryProgressUid = dbRule.db.contentEntryProgressDao.insertAsync(this)
                }

                ContentEntryProgress().apply {
                    contentEntryProgressActive = true
                    contentEntryProgressContentEntryUid = it.contentEntryUid
                    contentEntryProgressPersonUid = student3.clazzMemberPersonUid
                    contentEntryProgressProgress = 24
                    contentEntryProgressStatusFlag = ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED
                    contentEntryProgressUid = dbRule.db.contentEntryProgressDao.insertAsync(this)
                }

                ContentEntryProgress().apply {
                    contentEntryProgressActive = true
                    contentEntryProgressContentEntryUid = it.contentEntryUid
                    contentEntryProgressPersonUid = student4.clazzMemberPersonUid
                    contentEntryProgressProgress = 100
                    contentEntryProgressStatusFlag = ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED
                    contentEntryProgressUid = dbRule.db.contentEntryProgressDao.insertAsync(this)
                }
            }
        }


        reloadFragment(testClazzWork.clazzWork)
        checkProgressList(testClazzWork)

    }

    @AdbScreenRecord("ClazzWorkDetailProgressList: Should show correct progress for Quiz as well ")
    @Test
    fun givenValidClazzWorkUidWithQuiz_whenStudentSubmittedAndContentProgressed_thenShouldUpdateView() {

        //IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

        val clazzWork = ClazzWork().apply {
            clazzWorkTitle = "Test ClazzWork A"
            clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ
            clazzWorkInstructions = "Pass espresso test for ClazzWork"
            clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
            clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
            clazzWorkCommentsEnabled = true
            clazzWorkMaximumScore = 120
        }

        val testClazzWork = runBlocking {
            dbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ,
                    true,0, submitted = true, isStudentToClazz = true)
        }

        val contentEntriesWithJoin = runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }
        val contentList = contentEntriesWithJoin.contentList

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid

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
                    contentEntryProgressProgress = 42
                    contentEntryProgressStatusFlag = ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED
                    contentEntryProgressUid = dbRule.db.contentEntryProgressDao.insertAsync(this)
                }

                ContentEntryProgress().apply {
                    contentEntryProgressActive = true
                    contentEntryProgressContentEntryUid = it.contentEntryUid
                    contentEntryProgressPersonUid = student3.clazzMemberPersonUid
                    contentEntryProgressProgress = 24
                    contentEntryProgressStatusFlag = ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED
                    contentEntryProgressUid = dbRule.db.contentEntryProgressDao.insertAsync(this)
                }

                ContentEntryProgress().apply {
                    contentEntryProgressActive = true
                    contentEntryProgressContentEntryUid = it.contentEntryUid
                    contentEntryProgressPersonUid = student4.clazzMemberPersonUid
                    contentEntryProgressProgress = 100
                    contentEntryProgressStatusFlag = ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED
                    contentEntryProgressUid = dbRule.db.contentEntryProgressDao.insertAsync(this)
                }
            }
        }


        reloadFragment(testClazzWork.clazzWork)
        checkProgressList(testClazzWork)

    }

    @AdbScreenRecord("ClazzWorkDetailProgressList: When clicked on a submission should go to marking ")
    @Test
    fun givenValidClazzWorkUid_whenTeacherSeesStudentListedAndClicked_thenShouldGoToMarking() {

        //IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

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
                    true,0, submitted = true,
                    isStudentToClazz = true)
        }

        val contentEntriesWithJoin = runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }
        val contentList = contentEntriesWithJoin.contentList

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid


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
        checkProgressList(testClazzWork)
        clickStudent(student4)

        Assert.assertEquals("After clicking on student," +
                " fragment goes to marking for that student",
                systemImplNavRule.navController.currentDestination?.id,
                R.id.clazzworksubmission_marking_edit)

    }


    private fun reloadFragment(clazzWork: ClazzWork){

        launchFragmentInContainer(
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to clazzWork.clazzWorkUid.toString()),
                themeResId = R.style.UmTheme_App
        ) {
            ClazzWorkDetailProgressListFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        crudIdlingResourceRule.idlingResource.excludedViewIds.add(R.id.progressBar2)


    }

    private fun withTagInClazzMemberWithProgressList(clazzMemberUid: Long): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                ClazzWorkProgressListRecyclerAdapter.ClazzWorkProgressListViewHolder>(
                ClazzWorkProgressListRecyclerAdapter.ClazzWorkProgressListViewHolder::class.java) {
            override fun matchesSafely(
                    item: ClazzWorkProgressListRecyclerAdapter.ClazzWorkProgressListViewHolder): Boolean {
                return item.itemView.tag == clazzMemberUid
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with member: $clazzMemberUid")
            }
        }
    }

    private fun checkProgressList(testClazzWork: TestClazzWork){
        val list: List<ClazzMemberWithClazzWorkProgress> = runBlocking {
            dbRule.db.clazzWorkDao.findStudentProgressByClazzWorkTest(
                    testClazzWork.clazzWork.clazzWorkUid)
        }
        for(item in list){
            //Scroll to Member
                onView(withId(R.id.fragment_list_recyclerview)).perform(
                    RecyclerViewActions.scrollToHolder(withTagInClazzMemberWithProgressList(
                            item.mClazzMember!!.clazzMemberUid)))

            //Check Name displayed OK
            //onView(withText(item.fullName())).check(matches(isDisplayed()))
            onView(allOf(withText(item.fullName()),
                    withTagValue(`is`(item.mClazzMember!!.clazzMemberUid as Any)))).check(
                    matches(isDisplayed()))


            //Check submission line 2
            val textView = TextView(ApplicationProvider.getApplicationContext())
            textView.setClazzWorkMarking(item)
            //onView(withText(textView.text.toString())).check(matches(isDisplayed()))
            onView(allOf(withText(textView.text.toString()),
                    withTagValue(`is`(item.mClazzMember!!.clazzMemberUid as Any)))).check(
                    matches(isDisplayed()))

            //Check comment if any
            if(item.mLatestPrivateComment != null && item.mLatestPrivateComment!!.commentsText != null){
                onView(withText(item.mLatestPrivateComment!!.commentsText)).check(matches(isDisplayed()))
            }

            val hasContent = runBlocking {
                dbRule.db.clazzWorkContentJoinDao.findAllContentByClazzWorkUid(
                        testClazzWork.clazzWork.clazzWorkUid, dbRule.account.personUid)
            }

            //Check progress visibility

            onView(withId(R.id.fragment_list_recyclerview)).perform(
                    RecyclerViewActions.scrollToHolder(withTagInClazzMemberWithProgressList(
                            item.mClazzMember!!.clazzMemberUid)))

            if(hasContent.isNotEmpty()){
                onView(allOf(withId(R.id.progressBar2),
                        withTagValue(`is`(item.mClazzMember!!.clazzMemberUid as Any)))).check(
                        matches(isDisplayed()))
            }else{
                onView(allOf(withId(R.id.progressBar2),
                        withTagValue(`is`(item.mClazzMember!!.clazzMemberUid as Any)))).check(
                        matches(withEffectiveVisibility(Visibility.GONE)))
            }
        }
    }

    private fun clickStudent( studentToGo: ClazzMember){

        //Scroll to Member
        onView(withId(R.id.fragment_list_recyclerview)).perform(
                RecyclerViewActions.scrollToHolder(withTagInClazzMemberWithProgressList(
                        studentToGo.clazzMemberUid)))

        //Click it
        onView(withId(R.id.fragment_list_recyclerview)).perform(
                RecyclerViewActions.scrollToHolder(withTagInClazzMemberWithProgressList(
                        studentToGo.clazzMemberUid))).perform(click())

    }



}