package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.*
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.android.view.binding.dateWithTimeFormat
import com.ustadmobile.port.android.view.binding.dateWithTimeFormatWithPrepend
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import com.ustadmobile.util.test.ext.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

@AdbScreenRecord("ClazzWork (Assignments) Detail overview tests")
class ClazzWorkDetailOverviewFragmentTest {

    lateinit var recyclerViewIdlingResource: RecyclerViewIdlingResource

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule =
            ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val crudIdlingResourceRule =
            ScenarioIdlingResourceRule(CrudIdlingResource())

    @Before
    fun setup() {
        recyclerViewIdlingResource = RecyclerViewIdlingResource(null, 3)
    }

    @After
    fun tearDown(){
        UstadMobileSystemImpl.instance.navController = null
    }


    private val MS_PER_HOUR = 3600000
    private val MS_PER_MIN = 60000
    private fun scheduleTimeToDate(msSinceMidnight: Int) : Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, msSinceMidnight / 3600000)
        cal.set(Calendar.MINUTE, msSinceMidnight.rem(MS_PER_HOUR) / MS_PER_MIN)
        return Date(cal.timeInMillis)
    }

    private fun reloadFragment(clazzWork: ClazzWork){
        val fragmentScenario = launchFragmentInContainer(
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to clazzWork.clazzWorkUid.toString()),
                themeResId = R.style.UmTheme_App
        ) {
            ClazzWorkDetailOverviewFragment().also {
                it.installNavController(systemImplNavRule.navController)
                it.arguments = bundleOf(UstadView.ARG_ENTITY_UID to clazzWork.clazzWorkUid.toString())
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        fragmentScenario.onFragment {
            recyclerViewIdlingResource.recyclerView =
                    it.mBinding!!.fragmentClazzWorkWithSubmissionDetailRv
        }


    }

    private fun checkClazzWorkBasicDetailDisplayOk(clazzWork: ClazzWork,
                                                   contentList: List<ContentEntry>,
                                                   teacher: Boolean = false){
        //Scroll to top
        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToPosition<RecyclerView.ViewHolder>(0)
        )
        onView(withId(R.id.item_clazzwork_detail_description_cl)).check(matches(
                withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.item_clazzwork_detail_description_title)).check(matches(
                withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText(clazzWork.clazzWorkInstructions)).check(matches(
                withEffectiveVisibility(Visibility.VISIBLE)))

        val startDateString =  dateWithTimeFormat.format(arrayOf(clazzWork.clazzWorkStartDateTime,
                scheduleTimeToDate(clazzWork.clazzWorkStartTime.toInt()), ""))
        val dueDateString =  dateWithTimeFormatWithPrepend.format(
                arrayOf("Due date", clazzWork.clazzWorkDueDateTime,
                        scheduleTimeToDate(clazzWork.clazzWorkDueTime.toInt()), ""))
        onView(withText(startDateString)).check(matches(
                withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText(dueDateString)).check(matches(
                withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("Content")).check(matches(
                withEffectiveVisibility(Visibility.VISIBLE)))
        if(contentList.isNotEmpty()) {
            onView(withText(contentList[0].title)).check(matches(
                    withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withText(contentList[1].title)).check(matches(
                    withEffectiveVisibility(Visibility.VISIBLE)))
        }

        //Scroll to class comments
        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInSimpleHeading("Class comments")))

        onView(withText("Class comments")).check(matches(isDisplayed()))

        if(!teacher) {
            if (clazzWork.clazzWorkCommentsEnabled) {
                onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                        scrollToHolder(withTagInSimpleHeading("Private comments")))
                //onView(withText("Private comments")).check(matches(isDisplayed()))
            }
        }else{
            onView(withText("Private comments")).check(doesNotExist())
        }
    }

    private fun checkQuizQuestionsDisplayOk(
            clazzWorkQuizStuff : TestClazzWorkWithQuestionAndOptionsAndResponse?,
            teacher: Boolean = false){
        //Thread.sleep(500)

        //Scroll to Submission
        if(teacher){
            onView(withText("Submission")).check(doesNotExist())
            onView(withId(R.id.item_simpl_button_button_tv)).check(doesNotExist())
            onView(withId(R.id.item_clazzwork_submission_text_entry_et)).check(doesNotExist())
        }else {
            onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                    scrollToHolder(withTagInSimpleHeading("Submission")))
            onView(withText("Submission")).check(matches(
                    withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.item_clazzwork_submission_text_entry_et)).check(doesNotExist())
        }

        val q1uid = clazzWorkQuizStuff?.questionsAndOptions?.get(0)?.clazzWorkQuestion
                ?.clazzWorkQuestionUid

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q1uid!!)))

        Thread.sleep(1000)
        onView(allOf(withText("Question 1"),
            withId(R.id.item_clazzworkquestionandoptionswithresponse_title_tv))).check(
                matches(isDisplayed()))

        val q2uid = clazzWorkQuizStuff?.questionsAndOptions?.get(1)?.clazzWorkQuestion
                ?.clazzWorkQuestionUid

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q2uid!!)))
        onView(withText("Question 2")).check(matches(isDisplayed()))

        onView(withText("Question 1 Option 1")).check(matches(isDisplayed()))
        onView(withText("Question 1 Option 2")).check(matches(isDisplayed()))
        onView(withText("Question 1 Option 3")).check(matches(isDisplayed()))

        val q3uid = clazzWorkQuizStuff?.questionsAndOptions?.get(2)?.clazzWorkQuestion
                ?.clazzWorkQuestionUid

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q3uid!!)))



        val q4uid = clazzWorkQuizStuff?.questionsAndOptions?.get(3)?.clazzWorkQuestion
                ?.clazzWorkQuestionUid

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q4uid!!)))
        onView(withText("Question 4")).check(matches(isDisplayed()))

        onView(withText("Question 3")).check(matches(isDisplayed()))
        onView(withText("Question 3 Option 1")).check(matches(isDisplayed()))
        onView(withText("Question 3 Option 2")).check(matches(isDisplayed()))
        onView(withText("Question 3 Option 3")).check(matches(isDisplayed()))

        val q5uid = clazzWorkQuizStuff?.questionsAndOptions?.get(4)?.clazzWorkQuestion
                ?.clazzWorkQuestionUid

        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q5uid!!)))
        onView(withText("Question 5")).check(matches(isDisplayed()))
        onView(withText("Question 5 Option 1")).check(matches(isDisplayed()))
        onView(withText("Question 5 Option 2")).check(matches(isDisplayed()))
        onView(withText("Question 5 Option 3")).check(matches(isDisplayed()))

    }

    @AdbScreenRecord("ClazzWorkDetailOverview: When logged in as student should " +
            "show all fields")
  //  @Test
    fun givenValidClazzWorkUid_whenLoadedAsStudent_thenShouldShow() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

        //Create ClazzWork accordingly
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
            dbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                    true,0,false, true)
        }

        //Assign content
        val contentEntriesWithJoin = runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }
        val contentList = contentEntriesWithJoin.contentList

        //Log in as student
        val studentMember = testClazzWork.clazzAndMembers.studentList.get(0)
        dbRule.account.personUid = studentMember.clazzMemberPersonUid

        //Insert public and private comments
        runBlocking {
            dbRule.db.insertPublicAndPrivateComments(UMCalendarUtil.getDateInMilliPlusDays(0),
                    testClazzWork.clazzWork, testClazzWork.clazzAndMembers)
        }

        reloadFragment(testClazzWork.clazzWork)

        //Check overview page:
        checkClazzWorkBasicDetailDisplayOk(clazzWork, contentList)


        //Change type:
        clazzWork.clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
        runBlocking{
            dbRule.db.clazzWorkDao.updateAsync(clazzWork)
        }

        reloadFragment(testClazzWork.clazzWork)

        //Check overview page
        checkClazzWorkBasicDetailDisplayOk(clazzWork, contentList)

        //Change type to quiz
        var clazzWorkQuizStuff : TestClazzWorkWithQuestionAndOptionsAndResponse? = null
        runBlocking{
            clazzWorkQuizStuff = dbRule.db.insertQuizQuestionsAndOptions(clazzWork, false, 0,
                    0, 0, true)
            clazzWork = clazzWorkQuizStuff?.clazzWork!!
        }

        reloadFragment(clazzWorkQuizStuff!!.clazzWork)

        //Check overview page
        checkClazzWorkBasicDetailDisplayOk(clazzWork, contentList)

        checkQuizQuestionsDisplayOk(clazzWorkQuizStuff)
    }

    @AdbScreenRecord("ClazzWorkDetailOverview: When logged in as teacher should" +
            " show all relevant fields")
  //  @Test
    fun givenValidClazzWorkUid_whenLoadedAsTeacher_thenShouldShow() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

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
            dbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                    true,0,false, false)
        }

        val contentEntriesWithJoin = runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork,
                    2)
        }
        val contentList = contentEntriesWithJoin.contentList

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid

        reloadFragment(testClazzWork.clazzWork)

        //Check overview page
        checkClazzWorkBasicDetailDisplayOk(testClazzWork.clazzWork, contentList, true)

        //Change type:
        clazzWork.clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
        runBlocking{
            dbRule.db.clazzWorkDao.updateAsync(clazzWork)
        }

        reloadFragment(testClazzWork.clazzWork)

        //Check overview page
        checkClazzWorkBasicDetailDisplayOk(testClazzWork.clazzWork, contentList, true)

        //Change type to quiz
        var clazzWorkQuizStuff: TestClazzWorkWithQuestionAndOptionsAndResponse? = null
        runBlocking{
            clazzWorkQuizStuff = dbRule.db.insertQuizQuestionsAndOptions(clazzWork,
                    false, 0,
                    0, 0, true)
            clazzWork = clazzWorkQuizStuff!!.clazzWork
        }

        reloadFragment(testClazzWork.clazzWork)

        //Check questions
        checkQuizQuestionsDisplayOk(clazzWorkQuizStuff, true)


    }

    @AdbScreenRecord("ClazzWorkDetailOverview: When student answers questions and " +
            "hits submit, the view should be updated")
   // @Test
    fun givenValidClazzWorkUid_whenQuestionAnsweredAsStudentAndSubmitted_thenShouldUpdateView() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

        var clazzWork = ClazzWork().apply {
            clazzWorkTitle = "Test ClazzWork A"
            clazzWorkInstructions = "Pass espresso test for ClazzWork"
            clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
            clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
            clazzWorkCommentsEnabled = true
            clazzWorkMaximumScore = 120

        }
        val testClazzWork = runBlocking {
            dbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, false, -1, true,
                    0,false, true)
        }

        val contentEntriesWithJoin = runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork,
                    2)
        }
        val contentList = contentEntriesWithJoin.contentList

        val studentMember = testClazzWork.clazzAndMembers.studentList.get(0)
        dbRule.account.personUid = studentMember.clazzMemberPersonUid

        reloadFragment(testClazzWork.clazzWork)

        //Check overview page
        checkClazzWorkBasicDetailDisplayOk(testClazzWork.clazzWork, contentList)


        val q1uid = testClazzWork.quizQuestionsAndOptions?.questionsAndOptions?.get(0)
                ?.clazzWorkQuestion?.clazzWorkQuestionUid
        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q1uid!!)))
        onView(withText("Question 1")).check(matches(isEnabled()))
        onView(withText("Question 1 Option 1")).check(matches(isEnabled()))
        onView(withText("Question 1 Option 2")).check(matches(isEnabled()))
        onView(withText("Question 1 Option 3")).check(matches(isEnabled()))
        onView(withText("Question 1 Option 1")).perform(click())

        onView(withText("Question 2")).check(matches(isEnabled()))

        val q2uid = testClazzWork.quizQuestionsAndOptions?.questionsAndOptions?.get(1)
                ?.clazzWorkQuestion?.clazzWorkQuestionUid
        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q2uid!!)))

        onView(allOf(withId(R.id.item_clazzworkquestionandoptionswithresponse_answer_et),
                withTagValue(`is`(q2uid)))).perform(
                ViewActions.clearText(), ViewActions.typeText("Cow says moo"),
                ViewActions.closeSoftKeyboard())

        val q3uid = testClazzWork.quizQuestionsAndOptions?.questionsAndOptions?.get(2)
                ?.clazzWorkQuestion?.clazzWorkQuestionUid
        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q3uid!!)))
        onView(withText("Question 3")).check(matches(isEnabled()))
        onView(withText("Question 3 Option 1")).check(matches(isEnabled()))
        onView(withText("Question 3 Option 2")).check(matches(isEnabled()))
        onView(withText("Question 3 Option 3")).check(matches(isEnabled()))
        onView(withText("Question 3 Option 2")).perform(click())

        val q4uid = testClazzWork.quizQuestionsAndOptions?.questionsAndOptions?.get(3)
                ?.clazzWorkQuestion?.clazzWorkQuestionUid
        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
                scrollToHolder(withTagInQuestion(q4uid!!)))
        onView(withText("Question 4")).check(matches(isEnabled()))
        onView(allOf(withId(R.id.item_clazzworkquestionandoptionswithresponse_answer_et),
                withTagValue(`is`(q4uid)))).perform(
                ViewActions.clearText(), ViewActions.typeText("Dog says woof"),
                ViewActions.closeSoftKeyboard())

        val q5uid = testClazzWork.quizQuestionsAndOptions?.questionsAndOptions?.get(4)
                ?.clazzWorkQuestion?.clazzWorkQuestionUid
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

    @AdbScreenRecord("ClazzWorkDetailOverview: When teacher marks a submitted " +
            "submission, the view should be updated accordingly.")
    @Test
    fun givenValidClazzWorkUid_whenSubmissionMarkedByTeacherAndStudentLogsIn_thenShouldUpdateScore() {
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
                    clazzWork, true, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ,
                    true,0,true, true)
        }

        //Login as student who has submitted
        val studentMember = testClazzWork.clazzAndMembers.studentList.get(1)
        dbRule.account.personUid = studentMember.clazzMemberPersonUid

        reloadFragment(testClazzWork.clazzWork)

//        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
//                scrollToHolder(withTagInSubmissionMarkingDetail(testClazzWork.submissions
//                  ?.get(0)!!.clazzWorkSubmissionUid)))
//
        onView(withText("89/120")).check(matches(isDisplayed()))

    }

    @AdbScreenRecord("ClazzWorkDetailOverview: One student can make a private comment " +
            "that is not seen by another student")
    @Test
    fun givenLValidClazzWorkUid_whenPrivateCommentedByOneUser_thenOtherUsershallNotSee() {
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
                    true,
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
                commentsUid = dbRule.db.commentsDao.insertAsync(this)
            }
            Comments().apply {
                commentsText = "Student 2 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = studentMember2.clazzMemberPersonUid
                commentsUid = dbRule.db.commentsDao.insertAsync(this)
            }

        }

        dbRule.account.personUid = studentMember1.clazzMemberPersonUid

        reloadFragment(testClazzWork.clazzWork)
        Thread.sleep(1000)

        onView(withText("Student 1 private comment")).check(matches(isEnabled()))
        onView(withText("Student 2 private comment")).check(doesNotExist())

        //Student 2 logged in user. Cannot see private comment.
        dbRule.account.personUid = studentMember2.clazzMemberPersonUid

        reloadFragment(testClazzWork.clazzWork)

        Thread.sleep(1000)
        onView(withText("Student 1 private comment")).check(doesNotExist())
        onView(withText("Student 2 private comment")).check(matches(isEnabled()))

    }


    private fun withTagInSimpleButton(title: String): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                SimpleButtonRecyclerAdapter.SimpleHeadingViewHolder>(
                    SimpleButtonRecyclerAdapter.SimpleHeadingViewHolder::class.java) {
            override fun matchesSafely(item: SimpleButtonRecyclerAdapter.SimpleHeadingViewHolder)
                    : Boolean {
                return item.itemView.tag.equals(title)
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $title")
            }
        }
    }

    fun withTagInSimpleHeading(title: String): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                SimpleHeadingRecyclerAdapter.SimpleHeadingViewHolder>(
                    SimpleHeadingRecyclerAdapter.SimpleHeadingViewHolder::class.java) {
            override fun matchesSafely(item: SimpleHeadingRecyclerAdapter.SimpleHeadingViewHolder)
                    : Boolean {
                return item.itemView.tag == title
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $title")
            }
        }
    }

    private fun withTagInQuestion(quid: Long): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
            ClazzWorkQuestionAndOptionsWithResponseEditRecyclerAdapter.ClazzWorkQuestionViewHolder>(
                ClazzWorkQuestionAndOptionsWithResponseEditRecyclerAdapter.
                    ClazzWorkQuestionViewHolder::class.java) {
            override fun matchesSafely(
                    item: ClazzWorkQuestionAndOptionsWithResponseEditRecyclerAdapter
                                .ClazzWorkQuestionViewHolder): Boolean {
                return item.itemView.tag.equals(quid)
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $quid")
            }
        }
    }

    private fun withTagInQuestionAnswer(quid: Long): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                ClazzWorkQuestionAndOptionsWithResponseEditRecyclerAdapter.ClazzWorkQuestionViewHolder>(ClazzWorkQuestionAndOptionsWithResponseEditRecyclerAdapter.ClazzWorkQuestionViewHolder::class.java) {
            override fun matchesSafely(item: ClazzWorkQuestionAndOptionsWithResponseEditRecyclerAdapter.ClazzWorkQuestionViewHolder): Boolean {
                return item.binding.itemClazzworkquestionandoptionswithresponseAnswerEt.tag.equals(quid)
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $quid")
            }
        }
    }

    private fun withTagInClazzWorkDetail(clazzWorkUid: Long): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                ClazzWorkBasicDetailsRecyclerAdapter.ClazzWorkDetailViewHolder>(
                ClazzWorkBasicDetailsRecyclerAdapter.ClazzWorkDetailViewHolder::class.java) {
            override fun matchesSafely(
                    item: ClazzWorkBasicDetailsRecyclerAdapter.ClazzWorkDetailViewHolder): Boolean {
                if(item.itemView.tag != null) {
                    return item.itemView.tag.equals(clazzWorkUid)
                }else{
                    return false
                }
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $clazzWorkUid")
            }
        }
    }


    private fun withTagInSubmissionMarkingDetail(submissionUid: Long): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                SubmissionResultRecyclerAdapter.SubmissionResultViewHolder>(SubmissionResultRecyclerAdapter.SubmissionResultViewHolder::class.java) {
            override fun matchesSafely(item: SubmissionResultRecyclerAdapter.SubmissionResultViewHolder): Boolean {
                if(item.itemView.tag != null) {
                    return item.itemView.tag.equals(submissionUid)
                }else{
                    return false
                }
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $submissionUid")
            }
        }
    }




}