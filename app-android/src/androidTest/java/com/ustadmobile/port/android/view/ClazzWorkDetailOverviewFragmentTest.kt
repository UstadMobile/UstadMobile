package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.android.screen.ClazzWorkDetailOverviewScreen
import com.ustadmobile.port.android.view.binding.dateWithTimeFormat
import com.ustadmobile.port.android.view.binding.dateWithTimeFormatWithPrepend
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import com.ustadmobile.util.test.ext.*
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.util.*


@AdbScreenRecord("ClazzWork (Assignments) Detail overview tests")
class ClazzWorkDetailOverviewFragmentTest : TestCase() {

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
    val dataBindingIdlingResourceRule =
            ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @AdbScreenRecord("ClazzWorkDetailOverview: When logged in as student should " +
            "show all fields")
    @Test
    fun givenValidClazzWorkUid_whenLoadedAsStudent_thenShouldShow() {

        var clazzWork: ClazzWork? = null
        var contentList = listOf<ContentEntry>()
        var testClazzWork: TestClazzWork? = null
        before {
            //Create ClazzWork accordingly
            clazzWork = ClazzWork().apply {
                clazzWorkTitle = "Test ClazzWork A"
                clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
                clazzWorkInstructions = "Pass espresso test for ClazzWork"
                clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
                clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
                clazzWorkCommentsEnabled = true
                clazzWorkMaximumScore = 120

            }

            testClazzWork = runBlocking {
                dbRule.repo.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                        clazzWork!!, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                        true, 0, false, true)
            }

            //Assign content
            val contentEntriesWithJoin = runBlocking {
                dbRule.repo.createTestContentEntriesAndJoinToClazzWork(testClazzWork!!.clazzWork, 2)
            }
            contentList = contentEntriesWithJoin.contentList

            //Log in as student
            val studentMember = testClazzWork!!.clazzAndMembers.studentList.get(0)
            dbRule.account.personUid = studentMember.clazzMemberPersonUid

            //Insert public and private comments
            runBlocking {
                dbRule.repo.insertPublicAndPrivateComments(UMCalendarUtil.getDateInMilliPlusDays(0),
                        testClazzWork!!.clazzWork, testClazzWork!!.clazzAndMembers)
            }


            reloadFragment(testClazzWork!!.clazzWork)

        }.after {

        }.run {

            checkClazzWorkBasicDetailDisplayOk(clazzWork!!, contentList)

        }

    }

    @AdbScreenRecord("ClazzWorkDetailOverview: When logged in as student should " +
            "show all fields in Submission type ClazzWork")
    @Test
    fun givenValidClazzWorkUidSubmission_whenLoadedAsStudent_thenShouldShow() {

        var clazzWork: ClazzWork? = null
        var contentList = listOf<ContentEntry>()
        var testClazzWork: TestClazzWork? = null
        before {
            //Create ClazzWork accordingly
            clazzWork = ClazzWork().apply {
                clazzWorkTitle = "Test ClazzWork A"
                clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
                clazzWorkInstructions = "Pass espresso test for ClazzWork"
                clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
                clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
                clazzWorkCommentsEnabled = true
                clazzWorkMaximumScore = 120

            }

            testClazzWork = runBlocking {
                dbRule.repo.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                        clazzWork!!, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                        true, 0, false, true)
            }

            //Assign content
            val contentEntriesWithJoin = runBlocking {
                dbRule.repo.createTestContentEntriesAndJoinToClazzWork(testClazzWork!!.clazzWork, 2)
            }
            contentList = contentEntriesWithJoin.contentList

            //Log in as student
            val studentMember = testClazzWork!!.clazzAndMembers.studentList.get(0)
            dbRule.account.personUid = studentMember.clazzMemberPersonUid

            //Insert public and private comments
            runBlocking {
                dbRule.repo.insertPublicAndPrivateComments(UMCalendarUtil.getDateInMilliPlusDays(0),
                        testClazzWork!!.clazzWork, testClazzWork!!.clazzAndMembers)
            }


            clazzWork!!.clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
            runBlocking {
                dbRule.repo.clazzWorkDao.updateAsync(clazzWork!!)
            }


        }.after {

        }.run {


            reloadFragment(testClazzWork!!.clazzWork)

            //Check overview page
            checkClazzWorkBasicDetailDisplayOk(clazzWork!!, contentList)

        }

    }

    @AdbScreenRecord("ClazzWorkDetailOverview: When logged in as student should " +
            "show all fields in Quiz type ClazzWork")
    @Test
    fun givenValidClazzWorkUidQuizType_whenLoadedAsStudent_thenShouldShow() {

        var clazzWork: ClazzWork? = null
        var contentList = listOf<ContentEntry>()
        var testClazzWork: TestClazzWork? = null
        before {
            //Create ClazzWork accordingly
            clazzWork = ClazzWork().apply {
                clazzWorkTitle = "Test ClazzWork A"
                clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
                clazzWorkInstructions = "Pass espresso test for ClazzWork"
                clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
                clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
                clazzWorkCommentsEnabled = true
                clazzWorkMaximumScore = 120

            }

            testClazzWork = runBlocking {
                dbRule.repo.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                        clazzWork!!, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                        true, 0, false, true)
            }

            //Assign content
            val contentEntriesWithJoin = runBlocking {
                dbRule.repo.createTestContentEntriesAndJoinToClazzWork(testClazzWork!!.clazzWork, 2)
            }
            contentList = contentEntriesWithJoin.contentList

            //Log in as student
            val studentMember = testClazzWork!!.clazzAndMembers.studentList.get(0)
            dbRule.account.personUid = studentMember.clazzMemberPersonUid

            //Insert public and private comments
            runBlocking {
                dbRule.repo.insertPublicAndPrivateComments(UMCalendarUtil.getDateInMilliPlusDays(0),
                        testClazzWork!!.clazzWork, testClazzWork!!.clazzAndMembers)
            }


            clazzWork!!.clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
            runBlocking {
                dbRule.repo.clazzWorkDao.updateAsync(clazzWork!!)
            }




        }.after {

        }.run {

            //Change type to quiz
            var clazzWorkQuizStuff: TestClazzWorkWithQuestionAndOptionsAndResponse? = null
            runBlocking {
                clazzWorkQuizStuff = dbRule.repo.insertQuizQuestionsAndOptions(clazzWork!!, false, 0,
                        0, 0, true)
                clazzWork = clazzWorkQuizStuff?.clazzWork!!
            }
            reloadFragment(clazzWorkQuizStuff!!.clazzWork)

            //Check overview page
            checkClazzWorkBasicDetailDisplayOk(clazzWork!!, contentList)

            checkQuizQuestionsDisplayOk(clazzWorkQuizStuff)

        }

    }

    @AdbScreenRecord("ClazzWorkDetailOverview: When logged in as teacher should" +
            " show all relevant fields of No Submission type Clazz Work")
    @Test
    fun givenValidClazzWorkUidNoType_whenLoadedAsTeacher_thenShouldShow() {

        var clazzWork: ClazzWork? = null
        var contentList = listOf<ContentEntry>()
        var testClazzWork: TestClazzWork? = null
        before {

            clazzWork = ClazzWork().apply {
                clazzWorkTitle = "Test ClazzWork A"
                clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
                clazzWorkInstructions = "Pass espresso test for ClazzWork"
                clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
                clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
                clazzWorkCommentsEnabled = true
                clazzWorkMaximumScore = 120

            }

            testClazzWork = runBlocking {
                dbRule.repo.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                        clazzWork!!, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                        true, 0, false, false)
            }

            val contentEntriesWithJoin = runBlocking {
                dbRule.repo.createTestContentEntriesAndJoinToClazzWork(testClazzWork!!.clazzWork,
                        2)
            }
            contentList = contentEntriesWithJoin.contentList

            val teacherMember = testClazzWork!!.clazzAndMembers.teacherList.get(0)
            dbRule.account.personUid = teacherMember.clazzMemberPersonUid

            reloadFragment(testClazzWork!!.clazzWork)

        }.after {

        }.run {

            //Check overview page
            checkClazzWorkBasicDetailDisplayOk(testClazzWork!!.clazzWork, contentList, true)

        }

    }

    @AdbScreenRecord("ClazzWorkDetailOverview: When logged in as teacher should" +
            " show all relevant fields of Short Text type ClazzWork")
    @Test
    fun givenValidClazzWorkUidText_whenLoadedAsTeacher_thenShouldShow() {

        var clazzWork: ClazzWork? = null
        var contentList = listOf<ContentEntry>()
        var testClazzWork: TestClazzWork? = null
        before {

            clazzWork = ClazzWork().apply {
                clazzWorkTitle = "Test ClazzWork A"
                clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
                clazzWorkInstructions = "Pass espresso test for ClazzWork"
                clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
                clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
                clazzWorkCommentsEnabled = true
                clazzWorkMaximumScore = 120

            }

            testClazzWork = runBlocking {
                dbRule.repo.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                        clazzWork!!, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
                        true, 0, false, false)
            }

            val contentEntriesWithJoin = runBlocking {
                dbRule.repo.createTestContentEntriesAndJoinToClazzWork(testClazzWork!!.clazzWork,
                        2)
            }
            contentList = contentEntriesWithJoin.contentList

            val teacherMember = testClazzWork!!.clazzAndMembers.teacherList.get(0)
            dbRule.account.personUid = teacherMember.clazzMemberPersonUid



        }.after {

        }.run {


            //Change type:
            clazzWork!!.clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
            runBlocking {
                dbRule.repo.clazzWorkDao.updateAsync(clazzWork!!)
            }

            reloadFragment(testClazzWork!!.clazzWork)

            //Check overview page
            checkClazzWorkBasicDetailDisplayOk(testClazzWork!!.clazzWork, contentList, true)


        }

    }


//    @AdbScreenRecord("ClazzWorkDetailOverview: When logged in as teacher should" +
//            " show all relevant fields of Quiz ClazzWork")
//    @Test
//    fun givenValidClazzWorkUidQuiz_whenLoadedAsTeacher_thenShouldShow() {
//
//        var clazzWork: ClazzWork? = null
//        var contentList = listOf<ContentEntry>()
//        var testClazzWork: TestClazzWork? = null
//        before {
//
//            clazzWork = ClazzWork().apply {
//                clazzWorkTitle = "Test ClazzWork A"
//                clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
//                clazzWorkInstructions = "Pass espresso test for ClazzWork"
//                clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
//                clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
//                clazzWorkCommentsEnabled = true
//                clazzWorkMaximumScore = 120
//
//            }
//
//            testClazzWork = runBlocking {
//                dbRule.repo.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
//                        clazzWork!!, false, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE,
//                        true, 0, false, false)
//            }
//
//            val contentEntriesWithJoin = runBlocking {
//                dbRule.repo.createTestContentEntriesAndJoinToClazzWork(testClazzWork!!.clazzWork,
//                        2)
//            }
//            contentList = contentEntriesWithJoin.contentList
//
//            val teacherMember = testClazzWork!!.clazzAndMembers.teacherList.get(0)
//            dbRule.account.personUid = teacherMember.clazzMemberPersonUid
//
//
//
//        }.after {
//
//        }.run {
//
//            //Change type to quiz
//            var clazzWorkQuizStuff: TestClazzWorkWithQuestionAndOptionsAndResponse? = null
//            runBlocking {
//                clazzWorkQuizStuff = dbRule.repo.insertQuizQuestionsAndOptions(clazzWork!!,
//                        false, 0,
//                        0, 0, true)
//                clazzWork = clazzWorkQuizStuff!!.clazzWork
//            }
//
//            reloadFragment(testClazzWork!!.clazzWork)
//
//            //Check questions
//            checkQuizQuestionsDisplayOk(clazzWorkQuizStuff, true)
//
//        }
//
//    }

    @AdbScreenRecord("ClazzWorkDetailOverview: When student answers questions and " +
            "hits submit, the view should be updated")
    @Test
    fun givenValidClazzWorkUid_whenQuestionAnsweredAsStudentAndSubmitted_thenShouldUpdateView() {

        var clazzWork: ClazzWork? = null
        var contentList = listOf<ContentEntry>()
        var testClazzWork: TestClazzWork? = null
        before {

            clazzWork = ClazzWork().apply {
                clazzWorkTitle = "Test ClazzWork A"
                clazzWorkInstructions = "Pass espresso test for ClazzWork"
                clazzWorkStartDateTime = UMCalendarUtil.getDateInMilliPlusDays(0)
                clazzWorkDueDateTime = UMCalendarUtil.getDateInMilliPlusDays(10)
                clazzWorkCommentsEnabled = true
                clazzWorkMaximumScore = 120

            }

            testClazzWork = runBlocking {
                dbRule.repo.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                        clazzWork!!, false, -1, true,
                        0, false, true)
            }

            val contentEntriesWithJoin = runBlocking {
                dbRule.repo.createTestContentEntriesAndJoinToClazzWork(testClazzWork!!.clazzWork,
                        2)
            }
            contentList = contentEntriesWithJoin.contentList

            val studentMember = testClazzWork!!.clazzAndMembers.studentList.get(0)
            dbRule.account.personUid = studentMember.clazzMemberPersonUid

            reloadFragment(testClazzWork!!.clazzWork)

        }.after {

        }.run {

            checkClazzWorkBasicDetailDisplayOk(testClazzWork!!.clazzWork, contentList)

            ClazzWorkDetailOverviewScreen {

                recycler {

                    //Check overview page
                    val q1uid = testClazzWork!!.quizQuestionsAndOptions?.questionsAndOptions?.get(0)
                            ?.clazzWorkQuestion?.clazzWorkQuestionUid
                    scrollTo {
                        withTag(q1uid!!)
                    }
                    childWith<ClazzWorkDetailOverviewScreen.QuestionSet> {
                        withDescendant { withText("Question 1") }
                    } perform {
                        questionTitle {
                            hasText("Question 1")
                            isDisplayed()
                        }
                        radioOptions {
                            isDisplayed()
                            hasDescendant { withText("Question 1 Option 1") }
                            hasDescendant { withText("Question 1 Option 2") }
                            hasDescendant { withText("Question 1 Option 3") }
                        }
                    }

                    onView(withText("Question 1 Option 1")).perform(ViewActions.click())


                    val q2uid = testClazzWork!!.quizQuestionsAndOptions?.questionsAndOptions?.get(1)
                            ?.clazzWorkQuestion?.clazzWorkQuestionUid
                    scrollTo {
                        withTag(q2uid!!)
                    }
                    childWith<ClazzWorkDetailOverviewScreen.QuestionSet> {
                        withDescendant { withText("Question 2") }
                    } perform {
                        questionTitle {
                            hasText("Question 2")
                            isEnabled()
                        }
                        answerEditText {
                            clearText()
                            typeText("Cow says moo")
                            ViewActions.closeSoftKeyboard()
                        }
                    }

                    val q3uid = testClazzWork!!.quizQuestionsAndOptions?.questionsAndOptions?.get(2)
                            ?.clazzWorkQuestion?.clazzWorkQuestionUid
                    scrollTo {
                        withTag(q3uid!!)
                    }
                    childWith<ClazzWorkDetailOverviewScreen.QuestionSet> {
                        withDescendant { withText("Question 3") }
                    } perform {
                        questionTitle {
                            hasText("Question 3")
                            isDisplayed()
                        }
                        radioOptions {
                            isDisplayed()
                            hasDescendant { withText("Question 3 Option 1") }
                            hasDescendant { withText("Question 3 Option 2") }
                            hasDescendant { withText("Question 3 Option 3") }
                        }
                    }
                    onView(withText("Question 3 Option 2")).perform(ViewActions.click())

                    val q4uid = testClazzWork!!.quizQuestionsAndOptions?.questionsAndOptions?.get(3)
                            ?.clazzWorkQuestion?.clazzWorkQuestionUid
                    scrollTo {
                        withTag(q4uid!!)
                    }
                    childWith<ClazzWorkDetailOverviewScreen.QuestionSet> {
                        withDescendant { withText("Question 4") }
                    } perform {
                        questionTitle {
                            hasText("Question 4")
                            isEnabled()
                        }
                        answerEditText {
                            clearText()
                            typeText("Dog says woof")
                            ViewActions.closeSoftKeyboard()
                        }
                    }

                    val q5uid = testClazzWork!!.quizQuestionsAndOptions?.questionsAndOptions?.get(4)
                            ?.clazzWorkQuestion?.clazzWorkQuestionUid
                    scrollTo {
                        withTag(q5uid!!)
                    }
                    childWith<ClazzWorkDetailOverviewScreen.QuestionSet> {
                        withDescendant { withText("Question 5") }
                    } perform {
                        questionTitle {
                            hasText("Question 5")
                            isDisplayed()
                        }
                        radioOptions {
                            isDisplayed()
                            hasDescendant { withText("Question 5 Option 1") }
                            hasDescendant { withText("Question 5 Option 2") }
                            hasDescendant { withText("Question 5 Option 3") }
                        }
                    }
                    onView(withText("Question 5 Option 3")).perform(ViewActions.click())

                    scrollTo {
                        withTag("Submit")
                    }
                    childWith<ClazzWorkDetailOverviewScreen.SubmitSubmission> {
                        withDescendant { withText("Submit") }
                    } perform {
                        submitButton {
                            click()
                        }
                    }

                }

            }


        }

    }

    @AdbScreenRecord("ClazzWorkDetailOverview: When teacher marks a submitted " +
            "submission, the view should be updated accordingly.")
    @Test
    fun givenValidClazzWorkUid_whenSubmissionMarkedByTeacherAndStudentLogsIn_thenShouldUpdateScore() {

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
            dbRule.repo.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
                    clazzWork, true, ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_QUIZ,
                    true,0,true, true)
        }

        //Login as student who has submitted
        val studentMember = testClazzWork.clazzAndMembers.studentList.get(1)
        dbRule.account.personUid = studentMember.clazzMemberPersonUid

        reloadFragment(testClazzWork.clazzWork)

        ClazzWorkDetailOverviewScreen{

            recycler{

                childWith<ClazzWorkDetailOverviewScreen.Submission> {
                    withDescendant { withText("89/120") }
                }perform{
                    isVisible()
                    isDisplayed()
                }

            }

        }

    }

    @AdbScreenRecord("ClazzWorkDetailOverview: One student can make a private comment " +
            "that is not seen by another student")
    @Test
    fun givenLValidClazzWorkUid_whenPrivateCommentedByOneUser_thenOtherUsershallNotSee() {

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
            dbRule.repo.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
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
                commentsUid = dbRule.repo.commentsDao.insertAsync(this)
            }
            Comments().apply {
                commentsText = "Student 2 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = studentMember2.clazzMemberPersonUid
                commentsUid = dbRule.repo.commentsDao.insertAsync(this)
            }

        }

        init{

        }.run {

            dbRule.account.personUid = studentMember1.clazzMemberPersonUid

            reloadFragment(testClazzWork.clazzWork)

            ClazzWorkDetailOverviewScreen{

                recycler{

                    childWith<ClazzWorkDetailOverviewScreen.Comments> {
                        withDescendant { withId(R.id.item_comments_list_line2_text) }
                    } perform{
                        commentTextView{
                            hasText("Student 1 private comment")
                            isEnabled()
                            hasNoText("Student 2 private comment")
                        }
                    }
                }

            }
        }

    }

    @AdbScreenRecord("ClazzWorkDetailOverview: One student can make a private comment " +
            "that is not seen by another student2 ")
    @Test
    fun givenLValidClazzWorkUid_whenPrivateCommentedByOneUser_thenOtherUsershallNotSee2() {

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
            dbRule.repo.insertTestClazzWorkAndQuestionsAndOptionsWithResponse(
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
                commentsUid = dbRule.repo.commentsDao.insertAsync(this)
            }
            Comments().apply {
                commentsText = "Student 2 private comment"
                commentsDateTimeAdded = UMCalendarUtil.getDateInMilliPlusDays(0)
                commentsEntityType = ClazzWork.CLAZZ_WORK_TABLE_ID
                commentsEntityUid = testClazzWork.clazzWork.clazzWorkUid
                commentsPublic = false
                commentsPersonUid = studentMember2.clazzMemberPersonUid
                commentsUid = dbRule.repo.commentsDao.insertAsync(this)
            }

        }

        init{

        }.run {

            dbRule.account.personUid = studentMember1.clazzMemberPersonUid


            ClazzWorkDetailOverviewScreen{

                recycler{


                    //Student 2 logged in user. Cannot see private comment.
                    dbRule.account.personUid = studentMember2.clazzMemberPersonUid

                    reloadFragment(testClazzWork.clazzWork)

                    childWith<ClazzWorkDetailOverviewScreen.Comments> {
                        withDescendant { withId(R.id.item_comments_list_line2_text) }
                    } perform{
                        commentTextView{
                            hasText("Student 2 private comment")
                            hasNoText("Student 1 private comment")
                            isEnabled()
                        }
                    }
                }

            }
        }

    }


    private fun checkClazzWorkBasicDetailDisplayOk(clazzWork: ClazzWork, contentList: List<ContentEntry>,
                                                   teacher: Boolean = false) {

        val startDateString = dateWithTimeFormat.format(arrayOf(clazzWork.clazzWorkStartDateTime,
                scheduleTimeToDate(clazzWork.clazzWorkStartTime.toInt()), ""))
        val dueDateString = dateWithTimeFormatWithPrepend.format(
                arrayOf("Due date", clazzWork.clazzWorkDueDateTime,
                        scheduleTimeToDate(clazzWork.clazzWorkDueTime.toInt()), ""))

        ClazzWorkDetailOverviewScreen {
            recycler {

                scrollTo {
                    hasDescendant(withText(clazzWork.clazzWorkInstructions))
                }

                childWith<ClazzWorkDetailOverviewScreen.ClazzWorkBasicDetail> {
                    withDescendant { withText(clazzWork.clazzWorkInstructions!!) }
                } perform {
                    title {
                        isVisible()
                        hasText(clazzWork.clazzWorkInstructions!!)
                    }
                    startDate.hasText(startDateString)
                    dueDate.hasText(dueDateString)
                }
                childAt<ClazzWorkDetailOverviewScreen.SimpleHeading>(1) {
                    headingTitleTextView {
                        hasText("Content")
                        isVisible()
                    }
                }
                if (contentList.isNotEmpty()) {

                    childWith<ClazzWorkDetailOverviewScreen.ContentEntryList> {
                        withDescendant { withText(contentList[0].title!!) }
                    } perform {
                        entryTitle {
                            isVisible()
                            isDisplayed()
                        }
                    }
                    childWith<ClazzWorkDetailOverviewScreen.ContentEntryList> {
                        withDescendant { withText(contentList[1].title!!) }
                    } perform {
                        entryTitle {
                            isVisible()
                            isDisplayed()
                        }
                    }
                }

                scrollTo {
                    withTag("Class comments")
                }
                childWith<ClazzWorkDetailOverviewScreen.SimpleHeading> {
                    withDescendant { withText("Class comments") }
                } perform {
                    headingTitleTextView {
                        isVisible()
                        isDisplayed()
                    }
                }

                scrollToEnd()
                childWith<ClazzWorkDetailOverviewScreen.SimpleHeading> {
                    withDescendant { withText("Private comments") }
                } perform {
                    headingTitleTextView {
                        if (!teacher) {
                            if (clazzWork.clazzWorkCommentsEnabled) {
                                isDisplayed()
                                isVisible()
                            }
                        } else {
                            doesNotExist()
                        }
                    }
                }
                scrollToStart()
            }
        }
    }


    private fun checkQuizQuestionsDisplayOk(
            clazzWorkQuizStuff: TestClazzWorkWithQuestionAndOptionsAndResponse?,
            teacher: Boolean = false) {

        //Scroll to Submission
        ClazzWorkDetailOverviewScreen {

            recycler {
                scrollToStart()
                if (!teacher) {
                    scrollTo {
                        withTag("Submission")
                    }
                }
                childWith<ClazzWorkDetailOverviewScreen.SimpleHeading> {
                    withDescendant { withText("Submission") }
                } perform {
                    headingTitleTextView {
                        if (teacher) {
                            doesNotExist()
                        } else {
                            isVisible()
                        }

                    }
                }

                childWith<ClazzWorkDetailOverviewScreen.Submission> {
                    withDescendant { withId(R.id.item_clazzwork_submission_text_entry_et) }
                } perform {
                    submissionEditText {
                        doesNotExist()
                    }
                }

                if(teacher){
                    childWith<ClazzWorkDetailOverviewScreen.SubmitSubmission> {
                        withDescendant { withId(R.id.item_simpl_button_button_tv) }
                    } perform {
                        submitButton {
                            doesNotExist()
                        }
                    }
                }

                val q1uid = clazzWorkQuizStuff?.questionsAndOptions?.get(0)?.clazzWorkQuestion
                        ?.clazzWorkQuestionUid
                scrollTo {
                    withTag(q1uid!!)
                }

                childWith<ClazzWorkDetailOverviewScreen.QuestionSet> {
                    withDescendant { withText("Question 1") }
                } perform {
                    questionTitle {
                        hasText("Question 1")
                        isDisplayed()
                    }
                    radioOptions {
                        hasDescendant { withText("Question 1 Option 1") }
                        hasDescendant { withText("Question 1 Option 2") }
                        hasDescendant { withText("Question 1 Option 3") }
                        isDisplayed()
                    }
                }

                val q2uid = clazzWorkQuizStuff?.questionsAndOptions?.get(1)?.clazzWorkQuestion
                        ?.clazzWorkQuestionUid
                scrollTo {
                    withTag(q2uid!!)
                }
                childWith<ClazzWorkDetailOverviewScreen.QuestionSet> {
                    withDescendant { withText("Question 2") }
                } perform {
                    questionTitle {
                        hasText("Question 2")
                        isDisplayed()
                    }
                }

                val q4uid = clazzWorkQuizStuff?.questionsAndOptions?.get(3)?.clazzWorkQuestion
                        ?.clazzWorkQuestionUid
                scrollTo {
                    withTag(q4uid!!)
                }

                childWith<ClazzWorkDetailOverviewScreen.QuestionSet> {
                    withDescendant { withText("Question 4") }
                } perform {
                    questionTitle {
                        hasText("Question 4")
                        isDisplayed()
                    }
                }

                val q5uid = clazzWorkQuizStuff?.questionsAndOptions?.get(3)?.clazzWorkQuestion
                        ?.clazzWorkQuestionUid
                scrollTo {
                    withTag(q5uid!!)
                }

                childWith<ClazzWorkDetailOverviewScreen.QuestionSet> {
                    withDescendant { withText("Question 5") }
                } perform {
                    questionTitle {
                        hasText("Question 5")
                        isDisplayed()
                    }
                    radioOptions {
                        hasDescendant { withText("Question 5 Option 1") }
                        hasDescendant { withText("Question 5 Option 2") }
                        hasDescendant { withText("Question 5 Option 3") }
                        isDisplayed()
                    }
                }
                scrollToStart()
            }

        }

    }


    private val MS_PER_HOUR = 3600000
    private val MS_PER_MIN = 60000
    private fun scheduleTimeToDate(msSinceMidnight: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, msSinceMidnight / 3600000)
        cal.set(Calendar.MINUTE, msSinceMidnight.rem(MS_PER_HOUR) / MS_PER_MIN)
        return Date(cal.timeInMillis)
    }


    private fun reloadFragment(clazzWork: ClazzWork) {

        launchFragmentInContainer(
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to
                        clazzWork.clazzWorkUid.toString()),
                themeResId = R.style.UmTheme_App) {
            ClazzWorkDetailOverviewFragment().also {
                it.installNavController(systemImplNavRule.navController)
                it.arguments = bundleOf(UstadView.ARG_ENTITY_UID to
                        clazzWork.clazzWorkUid.toString())
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)


    }

}

