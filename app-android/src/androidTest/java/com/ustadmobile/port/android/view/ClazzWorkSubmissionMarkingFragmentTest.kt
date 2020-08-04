package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.db.entities.ContentEntryProgress
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
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.*

@AdbScreenRecord("ClazzWork (Assignments) Teacher Marking tests")
class ClazzWorkSubmissionMarkingFragmentTest {

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

    private fun createQuizDbScenario(): TestClazzWork{
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

        val dateNow: Long = UMCalendarUtil.getDateInMilliPlusDays(0)

        val testClazzWork = runBlocking {
            dbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, true, -1,
                    true,0, submitted = true,
                    isStudentToClazz = true, dateNow = dateNow, marked = false)
        }

        //Add content
        runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid


        return testClazzWork
    }

    private fun createQuizDbScenarioWith2Submissions(): TestClazzWork{
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

        val dateNow: Long = UMCalendarUtil.getDateInMilliPlusDays(0)

        val testClazzWork = runBlocking {
            dbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, true, -1,
                    true,0, submitted = true,
                    isStudentToClazz = true, dateNow = dateNow, marked = false,
                    multipleSubmissions = true)
        }

        //Add content
        val contentList = runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2).contentList
        }

        val student1 = testClazzWork.clazzAndMembers.studentList.get(0)
        val student3 = testClazzWork.clazzAndMembers.studentList.get(2)
        val student4 = testClazzWork.clazzAndMembers.studentList.get(3)


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


        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid


        return testClazzWork
    }

    private fun createQuizDbPartialScenario(): TestClazzWork{
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

        val dateNow: Long = UMCalendarUtil.getDateInMilliPlusDays(0)

        val testClazzWork = runBlocking {
            dbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, true, -1,
                    true,0, submitted = true,
                    isStudentToClazz = true, dateNow = dateNow, marked = false, partialFilled = true)
        }

        //Add content
        runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid


        return testClazzWork
    }

    private fun createFreeTextDbScenario(): TestClazzWork{
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

        val dateNow: Long = UMCalendarUtil.getDateInMilliPlusDays(0)

        val testClazzWork = runBlocking {
            dbRule.db.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, true, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT,
                    true,0, submitted = true,
                    isStudentToClazz = true, dateNow = dateNow, marked = false)
        }

        //Add content
        runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
        }

        val teacherMember = testClazzWork.clazzAndMembers.teacherList.get(0)
        dbRule.account.personUid = teacherMember.clazzMemberPersonUid


        return testClazzWork
    }

    private fun reloadFragment(clazzWorkUid: Long, clazzMemberUid: Long)
            : FragmentScenario<ClazzWorkSubmissionMarkingFragment>{

        val fragmentScenario = launchFragmentInContainer(
                fragmentArgs = bundleOf(UstadView.ARG_CLAZZWORK_UID to clazzWorkUid.toString(),
                        UstadView.ARG_CLAZZMEMBER_UID to clazzMemberUid.toString()),
                themeResId = R.style.UmTheme_App) {
            ClazzWorkSubmissionMarkingFragment(). also {
                it.installNavController(systemImplNavRule.navController)
                it.arguments = bundleOf(UstadView.ARG_CLAZZWORK_UID to clazzWorkUid.toString(),
                        UstadView.ARG_CLAZZMEMBER_UID to clazzMemberUid.toString())
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        fragmentScenario.onFragment {
            recyclerViewIdlingResource.recyclerView =
                    it.mBinding!!.fragmentClazzWorkSubmissionMarkingRv
        }

        return fragmentScenario
    }

    @AdbScreenRecord("ClazzWorkSubmissionMarking: Should show marking (no Quiz) ")
    @Test
    fun givenNoClazzWorkSubmissionMarkingPresentYetForQuiz_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        val testClazzWork = createQuizDbScenario()
        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        reloadFragment(clazzWorkUid, clazzMemberUid)

        fillMarkingAndReturn(testClazzWork)

        //Check database
        val submissionPostSubmit = runBlocking {
            dbRule.db.clazzWorkSubmissionDao.findByUidAsync(
                    testClazzWork.submissions!!.get(0).clazzWorkSubmissionUid)
        }
        Assert.assertEquals("Marked OK", 42,
                submissionPostSubmit?.clazzWorkSubmissionScore)
    }

    @AdbScreenRecord("ClazzWorkSubmissionMarking: Should show marking (partially filled Quiz) ")
    @Test
    fun givenNoClazzWorkSubmissionMarkingPresentYetForPartiallyFilledQuiz_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        val testClazzWork = createQuizDbPartialScenario()
        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        reloadFragment(clazzWorkUid, clazzMemberUid)

        fillMarkingAndReturn(testClazzWork)

        //Check database
        val submissionPostSubmit = runBlocking {
            dbRule.db.clazzWorkSubmissionDao.findByUidAsync(
                    testClazzWork.submissions!!.get(0).clazzWorkSubmissionUid)
        }
        Assert.assertEquals("Marked OK", 42,
                submissionPostSubmit?.clazzWorkSubmissionScore)
    }

    @AdbScreenRecord("ClazzWorkSubmissionMarking: Should show marking (for Free Text Submission) ")
    @Test
    fun givenNoClazzWorkSubmissionMarkingPresentYetForFreeText_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        val testClazzWork = createFreeTextDbScenario()
        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        reloadFragment(clazzWorkUid, clazzMemberUid)
        //TODO: Check why it fails to see one et
        Thread.sleep(1000)
        fillMarkingAndReturn(testClazzWork)

        //Check database
        val submissionPostSubmit = runBlocking {
            dbRule.db.clazzWorkSubmissionDao.findByUidAsync(
                    testClazzWork.submissions!!.get(0).clazzWorkSubmissionUid)
        }
        Assert.assertEquals("Marked OK", 42,
                submissionPostSubmit?.clazzWorkSubmissionScore)

    }

    @AdbScreenRecord("ClazzWorkSubmissionMarking: When marked should persist for Quiz 2 submissions should show NEXT")
    @Test
    fun givenNoClazzWorkSubmissionMarkingPresentYetForQuiz_whenFilledInAndReturnAndMarkNextClicked_thenShouldSaveToDatabaseAndLoadNextInQueue() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        val testClazzWork = createQuizDbScenarioWith2Submissions()
        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        reloadFragment(clazzWorkUid, clazzMemberUid)

        fillMarkingAndReturn(testClazzWork, false)

        //Verify button
        onView(withText("Return and mark next")).check(matches(isDisplayed()))

        //Click return button
        Espresso.onView(ViewMatchers.withId(
                R.id.item_clazzworksubmission_marking_button_with_extra_button)).perform(click())

        Assert.assertEquals("After clicking on return and mark next," +
                " fragment goes to marking for next submission",
                systemImplNavRule.navController.currentDestination?.id, R.id.clazzworksubmission_marking_edit)

        //Check database
        val submissionPostSubmit = runBlocking {
            dbRule.db.clazzWorkSubmissionDao.findByUidAsync(
                    testClazzWork.submissions!!.get(0).clazzWorkSubmissionUid)
        }

        Assert.assertEquals("Marked OK", 42,
                submissionPostSubmit?.clazzWorkSubmissionScore)

    }

    @AdbScreenRecord("ClazzWorkSubmissionMarking: When marked should persist for Quiz ( One submission) should Finish")
    @Test
    fun givenNoClazzWorkSubmissionMarkingPresentYetForQuizAndNoMoreSubmission_whenFilledInAndReturnAndMarkNextClicked_thenShouldSaveToDatabaseAndFinishView() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        val testClazzWork = createQuizDbScenario()
        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        reloadFragment(clazzWorkUid, clazzMemberUid)

        fillMarkingAndReturn(testClazzWork, false)

        //Verify button
        onView(withText("Return and finish")).check(matches(isDisplayed()))

        //Click return button
        onView(ViewMatchers.withId(
                R.id.item_clazzworksubmission_marking_button_with_extra_button)).perform(click())

        //Check database
        val submissionPostSubmit = runBlocking {
            dbRule.db.clazzWorkSubmissionDao.findByUidAsync(
                    testClazzWork.submissions!!.get(0).clazzWorkSubmissionUid)
        }

        Assert.assertEquals("Marked OK", 42,
                submissionPostSubmit?.clazzWorkSubmissionScore)

    }

    @AdbScreenRecord("ClazzWorkSubmissionMarking: When teacher comments should show OK")
    @Test
    fun givenNoClazzWorkSubmissionMarkingPresentForQuiz_whenTeacherComments_thenShouldSaveToDatabaseAndUpdateView() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        val testClazzWork = createQuizDbScenario()
        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        //Add a private comment by the student
        val studentComment = runBlocking {
            Comments().apply {
                commentsText = "Can we get help from parents?"
                commentsPersonUid = testClazzWork.submissions!!.get(0).clazzWorkSubmissionPersonUid
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsUid = dbRule.db.commentsDao.insertAsync(this)
            }

        }

        reloadFragment(clazzWorkUid, clazzMemberUid)

        //Scroll to Comment
        Espresso.onView(ViewMatchers.withId(R.id.fragment_clazz_work_submission_marking_rv)).perform(
                scrollToHolder(withTagInComment(studentComment.commentsUid)))

        //Check it is displayed OK
        onView(withText(studentComment.commentsText)).check(matches(isDisplayed()))


        val teacherComment = "Private comment to the student from teacher 1"
        fillMarkingAndReturn(testClazzWork, false)

        addPrivateComment(teacherComment, true)

        //Check database
        val commentPosted = runBlocking {
            dbRule.db.commentsDao.findPrivateCommentsByEntityTypeAndUidAndPersonAndPersonToTest(
                    ClazzWork.CLAZZ_WORK_TABLE_ID, testClazzWork.clazzWork.clazzWorkUid,
                    dbRule.account.personUid,
                    testClazzWork.submissions!!.get(0).clazzWorkSubmissionPersonUid
            )
        }

        Assert.assertTrue("Comments db not empty", commentPosted.isNotEmpty())

        val latestComment = commentPosted[0]

        Assert.assertEquals("Comment in DB OK", teacherComment, latestComment.commentsText)

        //Scroll to Comment
        Espresso.onView(ViewMatchers.withId(R.id.fragment_clazz_work_submission_marking_rv)).perform(
                scrollToHolder(withTagInComment(latestComment.commentsUid)))

        //Check it is displayed OK
        onView(withText(teacherComment)).check(matches(isDisplayed()))

    }

    private fun fillMarkingAndReturn(testClazzWork: TestClazzWork, hitReturn: Boolean = true){
        //Scroll to Marking
        Espresso.onView(ViewMatchers.withId(R.id.fragment_clazz_work_submission_marking_rv)).perform(
                scrollToHolder(withTagInMarking(
                        testClazzWork.submissions!!.get(0).clazzWorkSubmissionUid)))

        //Type marking value
        Espresso.onView(ViewMatchers.withId(R.id.item_clazzwork_submission_score_edit_et))
                .perform(ViewActions.clearText(), ViewActions.typeText("42"),
                        ViewActions.closeSoftKeyboard())

        //Scroll to Return
        Espresso.onView(ViewMatchers.withId(R.id.fragment_clazz_work_submission_marking_rv)).perform(
                scrollToHolder(withTagInMarkingSubmit(
                        testClazzWork.clazzWork.clazzWorkUid)))

        if(hitReturn) {
            //Click return button
            Espresso.onView(ViewMatchers.withId(
                    R.id.item_clazzworksubmission_marking_button_with_extra_button)).perform(click())
        }
    }

    private fun addPrivateComment(comment: String, hitReturn: Boolean = true){

        //Scroll to Private comments
        onView(ViewMatchers.withId(R.id.fragment_clazz_work_submission_marking_rv)).perform(
                scrollToHolder(withTagInSimpleHeading("Private comments")))
        onView(withText("Private comments")).check(matches(ViewMatchers.isEnabled()))


        //Type marking value
        Espresso.onView(ViewMatchers.withId(R.id.item_comment_new_comment_et))
                .perform(ViewActions.clearText(), ViewActions.typeText(
                        comment),
                        ViewActions.closeSoftKeyboard())
        if(hitReturn) {
            //Click return button
            Espresso.onView(ViewMatchers.withId(
                    R.id.item_comment_new_send_ib)).perform(click())
        }
    }

    private fun withTagInMarking(quid: Long): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                ClazzWorkSubmissionScoreEditRecyclerAdapter.ScoreEditViewHolder>(
                ClazzWorkSubmissionScoreEditRecyclerAdapter.ScoreEditViewHolder::class.java) {
            override fun matchesSafely(
                    item: ClazzWorkSubmissionScoreEditRecyclerAdapter.ScoreEditViewHolder): Boolean {
                return item.itemView.tag.equals(quid)
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $quid")
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

    private fun withTagInMarkingSubmit(clazzWorkUid: Long): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                ClazzWorkSubmissionMarkingSubmitWithMetricsRecyclerAdapter.ClazzWorkProgressViewHolder>(
                ClazzWorkSubmissionMarkingSubmitWithMetricsRecyclerAdapter.ClazzWorkProgressViewHolder::class.java) {
            override fun matchesSafely(
                    item: ClazzWorkSubmissionMarkingSubmitWithMetricsRecyclerAdapter.ClazzWorkProgressViewHolder): Boolean {
                return item.itemView.tag.equals(clazzWorkUid)
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $clazzWorkUid")
            }
        }
    }

    private fun withTagInComment(commentUid: Long): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                CommentsRecyclerAdapter.CommentsWithPersonViewHolder>(
                CommentsRecyclerAdapter.CommentsWithPersonViewHolder::class.java) {
            override fun matchesSafely(
                    item: CommentsRecyclerAdapter.CommentsWithPersonViewHolder): Boolean {
                return item.itemView.tag.equals(commentUid)
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with comment uid: $commentUid")
            }
        }
    }

}