package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.*
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.android.view.binding.dateWithTimeFormat
import com.ustadmobile.port.android.view.binding.dateWithTimeFormatWithPrepend
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@AdbScreenRecord("ClazzWork (Assignments) Detail overview tests")
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

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    private val MS_PER_HOUR = 3600000
    private val MS_PER_MIN = 60000
    private fun scheduleTimeToDate(msSinceMidnight: Int) : Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, msSinceMidnight / 3600000)
        cal.set(Calendar.MINUTE, msSinceMidnight.rem(MS_PER_HOUR) / MS_PER_MIN)
        return Date(cal.timeInMillis)
    }

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

        val studentScenario = launchFragmentInContainer(
                bundleOf(UstadView.ARG_ENTITY_UID to clazzWork.clazzWorkUid.toString()),
                themeResId = R.style.UmTheme_App) {
            ClazzWorkDetailOverviewFragment().also{
                it.installNavController(systemImplNavRule.navController)
            }
        }

        studentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.fragmentClazzWorkWithSubmissionDetailRv
        }
        Thread.sleep(2000)
    }

    @AdbScreenRecord("ClazzWorkDetailOverview: When logged in as student should show all fields")
    @Test
    fun givenValidClazzWorkUid_whenLoadedAsStudent_thenShouldShow() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        var clazzWork = ClazzWork().apply {
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

        val studentMember = testClazzWork.clazzAndMembers.studentList.get(0)
        dbRule.account.personUid = studentMember.clazzMemberPersonUid

        val activeAccount = UmAccount(studentMember.clazzMemberPersonUid, "bond", "", "http://localhost")
        UmAccountManager.setActiveAccount(activeAccount, ApplicationProvider.getApplicationContext())

        reloadFragment(testClazzWork.clazzWork)

        onView(withId(R.id.item_clazzwork_detail_description_cl)).check(matches(isDisplayed()))
        onView(withId(R.id.item_clazzwork_detail_description_title)).check(matches(isDisplayed()))
        onView(withText(clazzWork.clazzWorkInstructions)).check(matches(isDisplayed()))

        val startDateString =  dateWithTimeFormat.format(
                arrayOf(clazzWork.clazzWorkStartDateTime, scheduleTimeToDate(clazzWork.clazzWorkStartTime.toInt()), ""))
        val dueDateString =  dateWithTimeFormatWithPrepend.format(
                arrayOf("Due date", clazzWork.clazzWorkDueDateTime, scheduleTimeToDate(clazzWork.clazzWorkDueTime.toInt()), ""))
        onView(withText(startDateString)).check(matches(isDisplayed()))
        onView(withText(dueDateString)).check(matches(isDisplayed()))
        onView(withText("Content")).check(matches(isDisplayed()))
        onView(withText(contentList[0].title)).check(matches(isDisplayed()))
        onView(withText(contentList[1].title)).check(matches(isDisplayed()))

        //None type does not exist
        onView(withId(R.id.item_simpl_button_button_tv)).check(doesNotExist())

        //Test comments loaded (if any)
        runBlocking {
            db.insertPublicAndPrivateComments(UMCalendarUtil.getDateInMilliPlusDays(0),
                    testClazzWork.clazzWork, testClazzWork.clazzAndMembers)
        }
        reloadFragment(testClazzWork.clazzWork)
        onView(withText("Class comments")).check(matches(isEnabled()))

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInSimpleHeading("Private comments")))
        onView(withText("Private comments")).check(matches(isEnabled()))

        //Change type:
        clazzWork.clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
        runBlocking{
            db.clazzWorkDao.updateAsync(clazzWork)
        }

        reloadFragment(testClazzWork.clazzWork)

        //Submission exists:
        onView(withText("Submission")).check(matches(isDisplayed()))
        onView(withId(R.id.item_simpl_button_button_tv)).check(matches(isEnabled()))
        onView(withId(R.id.item_clazzwork_submission_text_entry_et)).check(matches(isDisplayed()))

        //Change type to quiz
        var clazzWorkQuizStuff : TestClazzWorkWithQuestionAndOptionsAndResponse? = null
        runBlocking{
            clazzWorkQuizStuff = db.insertQuizQuestionsAndOptions(clazzWork, false, 0,
                    0, 0, true)
            clazzWork = clazzWorkQuizStuff?.clazzWork!!
        }

        reloadFragment(testClazzWork.clazzWork)

        //Submission exists:
        onView(withText("Submission")).check(matches(isDisplayed()))
        onView(withId(R.id.item_clazzwork_submission_text_entry_et)).check(doesNotExist())
        onView(withText("Question 1")).check(matches(isEnabled()))
        onView(withText("Question 1 Option 1")).check(matches(isEnabled()))
        onView(withText("Question 1 Option 2")).check(matches(isEnabled()))
        onView(withText("Question 1 Option 3")).check(matches(isEnabled()))

        onView(withText("Question 2")).check(matches(isEnabled()))

        val q3uid = clazzWorkQuizStuff?.questionsAndOptions?.get(2)?.clazzWorkQuestion?.clazzWorkQuestionUid

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q3uid!!)))

        onView(withText("Question 3")).check(matches(isEnabled()))
        onView(withText("Question 3 Option 1")).check(matches(isEnabled()))
        onView(withText("Question 3 Option 2")).check(matches(isEnabled()))
        onView(withText("Question 3 Option 3")).check(matches(isEnabled()))

        val q4uid = clazzWorkQuizStuff?.questionsAndOptions?.get(3)?.clazzWorkQuestion?.clazzWorkQuestionUid

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q4uid!!)))
        onView(withText("Question 4")).check(matches(isEnabled()))
        val q5uid = clazzWorkQuizStuff?.questionsAndOptions?.get(4)?.clazzWorkQuestion?.clazzWorkQuestionUid

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q5uid!!)))
        onView(withText("Question 5")).check(matches(isEnabled()))
        onView(withText("Question 5 Option 1")).check(matches(isEnabled()))
        onView(withText("Question 5 Option 2")).check(matches(isEnabled()))
        onView(withText("Question 5 Option 3")).check(matches(isEnabled()))

        //TODO: Edit button doesn't exist?

    }

    @AdbScreenRecord("ClazzWorkDetailOverview: When logged in as teacher should show all relevant fields")
    @Test
    fun givenValidClazzWorkUid_whenLoadedAsTeacher_thenShouldShow() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        var clazzWork = ClazzWork().apply {
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
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE, true,
                    0,false, false)
        }

        //TODO: Test with content:
//        val contentEntriesWithJoin = runBlocking {
//            db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
//        }
//        val contentList = contentEntriesWithJoin.contentList

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid

        val activeAccount = UmAccount(teacherMember.clazzMemberPersonUid, "bond", "", "http://localhost")
        UmAccountManager.setActiveAccount(activeAccount, ApplicationProvider.getApplicationContext())

        reloadFragment(testClazzWork.clazzWork)

        onView(withId(R.id.item_clazzwork_detail_description_cl)).check(matches(isDisplayed()))
        onView(withId(R.id.item_clazzwork_detail_description_title)).check(matches(isDisplayed()))
        onView(withText(clazzWork.clazzWorkInstructions)).check(matches(isDisplayed()))

        val startDateString =  dateWithTimeFormat.format(
                arrayOf(clazzWork.clazzWorkStartDateTime, scheduleTimeToDate(clazzWork.clazzWorkStartTime.toInt()), ""))
        val dueDateString =  dateWithTimeFormatWithPrepend.format(
                arrayOf("Due date", clazzWork.clazzWorkDueDateTime, scheduleTimeToDate(clazzWork.clazzWorkDueTime.toInt()), ""))
        onView(withText(startDateString)).check(matches(isDisplayed()))
        onView(withText(dueDateString)).check(matches(isDisplayed()))
        onView(withText("Content")).check(matches(isDisplayed()))

        //Submit button never exists
        onView(withId(R.id.item_simpl_button_button_tv)).check(doesNotExist())

        //Test comments loaded (if any)
        runBlocking {
            db.insertPublicAndPrivateComments(UMCalendarUtil.getDateInMilliPlusDays(0),
                    testClazzWork.clazzWork, testClazzWork.clazzAndMembers)
        }
        reloadFragment(testClazzWork.clazzWork)
        onView(withText("Class comments")).check(matches(isEnabled()))
        onView(withText("Private comments")).check(doesNotExist())

        //Change type:
        clazzWork.clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
        runBlocking{
            db.clazzWorkDao.updateAsync(clazzWork)
        }

        reloadFragment(testClazzWork.clazzWork)

        //Submission doesnt exists:
        onView(withText("Submission")).check(doesNotExist())
        onView(withId(R.id.item_simpl_button_button_tv)).check(doesNotExist())
        onView(withId(R.id.item_clazzwork_submission_text_entry_et)).check(doesNotExist())

        //Change type to quiz
        var clazzWorkQuizStuff: TestClazzWorkWithQuestionAndOptionsAndResponse? = null
        runBlocking{
            clazzWorkQuizStuff = db.insertQuizQuestionsAndOptions(clazzWork, false, 0,
                    0, 0, true)
            clazzWork = clazzWorkQuizStuff!!.clazzWork
        }

        reloadFragment(testClazzWork.clazzWork)

        //Submission exists:
        //Submission doesnt exists:
        onView(withText("Submission")).check(doesNotExist())
        onView(withId(R.id.item_simpl_button_button_tv)).check(doesNotExist())
        onView(withId(R.id.item_clazzwork_submission_text_entry_et)).check(doesNotExist())

        //But can see questions OK
        onView(withText("Question 1")).check(matches(isEnabled()))
        onView(withText("Question 1 Option 1")).check(matches(not(isEnabled())))
        onView(withText("Question 1 Option 2")).check(matches(not(isEnabled())))
        onView(withText("Question 1 Option 3")).check(matches(not(isEnabled())))


        val q2uid = clazzWorkQuizStuff?.questionsAndOptions?.get(1)?.clazzWorkQuestion?.clazzWorkQuestionUid
        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q2uid!!)))
        onView(withText("Question 2")).check(matches(isEnabled()))

        val q3uid = clazzWorkQuizStuff?.questionsAndOptions?.get(2)?.clazzWorkQuestion?.clazzWorkQuestionUid
        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q3uid!!)))
        onView(withText("Question 3")).check(matches(isEnabled()))
        onView(withText("Question 3 Option 1")).check(matches(not(isEnabled())))
        onView(withText("Question 3 Option 2")).check(matches(not(isEnabled())))
        onView(withText("Question 3 Option 3")).check(matches(not(isEnabled())))

        val q4uid = clazzWorkQuizStuff?.questionsAndOptions?.get(3)?.clazzWorkQuestion?.clazzWorkQuestionUid

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q4uid!!)))
        onView(withText("Question 4")).check(matches(isEnabled()))

        val q5uid = clazzWorkQuizStuff?.questionsAndOptions?.get(4)?.clazzWorkQuestion?.clazzWorkQuestionUid

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q5uid!!)))

        onView(withText("Question 5")).check(matches(isEnabled()))
        onView(withText("Question 5 Option 1")).check(matches(not(isEnabled())))
        onView(withText("Question 5 Option 2")).check(matches(not(isEnabled())))
        onView(withText("Question 5 Option 3")).check(matches(not(isEnabled())))


        //TODO: Edit button goes to edit screen OK

    }

    @AdbScreenRecord("ClazzWorkDetailOverview: When student answers questions and hits submit, the view should be updated")
    @Test
    fun givenValidClazzWorkUid_whenQuestionAnsweredAsStudentAndSubmitted_thenShouldUpdateView() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        val testClazzWork = runBlocking {
            db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    ClazzWork(), false, -1, true,
                    0,false, true)
        }

        val studentMember = testClazzWork.clazzAndMembers.studentList.get(0)
        dbRule.account.personUid = studentMember.clazzMemberPersonUid

        val activeAccount = UmAccount(studentMember.clazzMemberPersonUid, "bond",
                "", "http://localhost")
        UmAccountManager.setActiveAccount(activeAccount, ApplicationProvider.getApplicationContext())

        reloadFragment(testClazzWork.clazzWork)

        onView(withText("Submission")).check(matches(isDisplayed()))
        onView(withId(R.id.item_clazzwork_submission_text_entry_et)).check(doesNotExist())

        val q1uid = testClazzWork.quizQuestionsAndOptions?.questionsAndOptions?.get(0)?.clazzWorkQuestion?.clazzWorkQuestionUid
        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q1uid!!)))
        onView(withText("Question 1")).check(matches(isEnabled()))
        onView(withText("Question 1 Option 1")).check(matches(isEnabled()))
        onView(withText("Question 1 Option 2")).check(matches(isEnabled()))
        onView(withText("Question 1 Option 3")).check(matches(isEnabled()))
        onView(withText("Question 1 Option 1")).perform(click())

        onView(withText("Question 2")).check(matches(isEnabled()))

        val q2uid = testClazzWork.quizQuestionsAndOptions?.questionsAndOptions?.get(1)?.clazzWorkQuestion?.clazzWorkQuestionUid
        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q2uid!!)))
        onView(withText("Question 2")).check(matches(isEnabled()))

        onView(allOf(withId(R.id.item_clazzworkquestionandoptionswithresponse_answer_et),
                withTagValue(`is`(q2uid)))).perform(
                ViewActions.clearText(), ViewActions.typeText("Cow says moo"),
                ViewActions.closeSoftKeyboard())

        val q3uid = testClazzWork.quizQuestionsAndOptions?.questionsAndOptions?.get(2)?.clazzWorkQuestion?.clazzWorkQuestionUid
        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q3uid!!)))
        onView(withText("Question 3")).check(matches(isEnabled()))
        onView(withText("Question 3 Option 1")).check(matches(isEnabled()))
        onView(withText("Question 3 Option 2")).check(matches(isEnabled()))
        onView(withText("Question 3 Option 3")).check(matches(isEnabled()))
        onView(withText("Question 3 Option 2")).perform(click())

        val q4uid = testClazzWork.quizQuestionsAndOptions?.questionsAndOptions?.get(3)?.clazzWorkQuestion?.clazzWorkQuestionUid
        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q4uid!!)))
        onView(withText("Question 4")).check(matches(isEnabled()))
        onView(allOf(withId(R.id.item_clazzworkquestionandoptionswithresponse_answer_et),
                withTagValue(`is`(q4uid)))).perform(
                ViewActions.clearText(), ViewActions.typeText("Dog says woof"),
                ViewActions.closeSoftKeyboard())

        val q5uid = testClazzWork.quizQuestionsAndOptions?.questionsAndOptions?.get(4)?.clazzWorkQuestion?.clazzWorkQuestionUid
        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q5uid!!)))
        onView(withText("Question 5")).check(matches(isEnabled()))
        onView(withText("Question 5 Option 1")).check(matches(isEnabled()))
        onView(withText("Question 5 Option 2")).check(matches(isEnabled()))
        onView(withText("Question 5 Option 3")).check(matches(isEnabled()))
        onView(withText("Question 5 Option 3")).perform(click())

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInSimpleButton("Submit")))

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                actionOnItem<RecyclerView.ViewHolder>(withTagValue(equalTo("Submit")),
                        click()))


    }

    @AdbScreenRecord("ClazzWorkDetailOverview: When teacher marks a submitted submission, the view should be updated accordingly.")
    @Test
    fun givenValidClazzWorkUid_whenSubmissionMarkedByTeacherAndStudentLogsIn_thenShouldUpdateScore() {
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
                    clazzWork, true, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ,
                    true,0,true, true)
        }

        val studentMember = testClazzWork.clazzAndMembers.studentList.get(0)
        dbRule.account.personUid = studentMember.clazzMemberPersonUid

        val activeAccount = UmAccount(studentMember.clazzMemberPersonUid, "bond", "", "http://localhost")
        UmAccountManager.setActiveAccount(activeAccount, ApplicationProvider.getApplicationContext())

        reloadFragment(testClazzWork.clazzWork)

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInClazzWorkDetail(testClazzWork.clazzWork.clazzWorkUid)))

        onView(withId(R.id.item_clazzwork_detail_description_cl)).check(matches(isDisplayed()))
        onView(withId(R.id.item_clazzwork_detail_description_title)).check(matches(isDisplayed()))
        onView(withText(clazzWork.clazzWorkInstructions)).check(matches(isDisplayed()))
        onView(withText("89/120")).check(matches(isDisplayed()))

    }

    @AdbScreenRecord("ClazzWorkDetailOverview: One student can make a private comment that is not seen by another student")
    @Test
    fun givenLValidClazzWorkUid_whenPrivateCommentedByOneUser_thenOtherUsershallNotSee() {
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
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE, true,
                    0,false, true)
        }

        //Student 1 logged in user. Makes private comment :
        val studentMember1 = testClazzWork.clazzAndMembers.studentList.get(0)
        val studentMember2 = testClazzWork.clazzAndMembers.studentList.get(1)

        //Test comments loaded (if any)
        runBlocking {
            Comments().apply {
                commentsText = "Student 1 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = studentMember1.clazzMemberPersonUid
                commentsUid = db.commentsDao.insertAsync(this)
            }
            Comments().apply {
                commentsText = "Student 2 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = studentMember2.clazzMemberPersonUid
                commentsUid = db.commentsDao.insertAsync(this)
            }

        }

        dbRule.account.personUid = studentMember1.clazzMemberPersonUid
        var activeAccount = UmAccount(studentMember1.clazzMemberPersonUid, "bond",
                "", "http://localhost")
        UmAccountManager.setActiveAccount(activeAccount, ApplicationProvider.getApplicationContext())
        reloadFragment(testClazzWork.clazzWork)

        onView(withText("Student 1 private comment")).check(matches(isEnabled()))
        onView(withText("Student 2 private comment")).check(doesNotExist())

        //Student 2 logged in user. Cannot see private comment.
        dbRule.account.personUid = studentMember2.clazzMemberPersonUid
        activeAccount = UmAccount(studentMember2.clazzMemberPersonUid, "bond",
                "", "http://localhost")
        UmAccountManager.setActiveAccount(activeAccount, ApplicationProvider.getApplicationContext())
        reloadFragment(testClazzWork.clazzWork)

        onView(withText("Student 1 private comment")).check(doesNotExist())
        onView(withText("Student 2 private comment")).check(matches(isEnabled()))

    }


    private fun withTagInSimpleButton(title: String): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                SimpleButtonRecyclerAdapter.SimpleHeadingViewHolder>(SimpleButtonRecyclerAdapter.SimpleHeadingViewHolder::class.java) {
            override fun matchesSafely(item: SimpleButtonRecyclerAdapter.SimpleHeadingViewHolder): Boolean {
                return item.itemView.tag.equals(title)
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $title")
            }
        }
    }

    fun withTagInSimpleHeading(title: String): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                SimpleHeadingRecyclerAdapter.SimpleHeadingViewHolder>(SimpleHeadingRecyclerAdapter.SimpleHeadingViewHolder::class.java) {
            override fun matchesSafely(item: SimpleHeadingRecyclerAdapter.SimpleHeadingViewHolder): Boolean {
                return item.itemView.tag.equals(title)
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $title")
            }
        }
    }

    private fun withTagInQuestion(quid: Long): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter.ClazzWorkQuestionViewHolder>(ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter.ClazzWorkQuestionViewHolder::class.java) {
            override fun matchesSafely(item: ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter.ClazzWorkQuestionViewHolder): Boolean {
                return item.itemView.tag.equals(quid)
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $quid")
            }
        }
    }

    private fun withTagInQuestionAnswer(quid: Long): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter.ClazzWorkQuestionViewHolder>(ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter.ClazzWorkQuestionViewHolder::class.java) {
            override fun matchesSafely(item: ClazzWorkQuestionAndOptionsWithResponseRecyclerAdapter.ClazzWorkQuestionViewHolder): Boolean {
                return item.binding.itemClazzworkquestionandoptionswithresponseAnswerEt.tag.equals(quid)
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $quid")
            }
        }
    }

    private fun withTagInClazzWorkDetail(clazzWorkUid: Long): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                ClazzWorkDetailOverviewFragment.ClazzWorkBasicDetailsRecyclerAdapter.ClazzWorkDetailViewHolder>(ClazzWorkDetailOverviewFragment.ClazzWorkBasicDetailsRecyclerAdapter.ClazzWorkDetailViewHolder::class.java) {
            override fun matchesSafely(item: ClazzWorkDetailOverviewFragment.ClazzWorkBasicDetailsRecyclerAdapter.ClazzWorkDetailViewHolder): Boolean {
                return item.itemView.tag.equals(clazzWorkUid)
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $clazzWorkUid")
            }
        }
    }





}