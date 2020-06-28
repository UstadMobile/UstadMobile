package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchool
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.setDateField
import com.ustadmobile.test.rules.DataBindingIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withDataBindingIdlingResource
import com.ustadmobile.util.test.ext.TestClazzWork
import com.ustadmobile.util.test.ext.createTestContentEntriesAndJoinToClazzWork
import com.ustadmobile.util.test.ext.insertTestClazzWorkAndQuestionsAndOptionsWithResponse
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.*

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
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @Before
    fun setup() {
        recyclerViewIdlingResource = RecyclerViewIdlingResource(null, 3)
    }

    @After
    fun tearDown(){
        UstadMobileSystemImpl.instance.navController = null
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
        }.withDataBindingIdlingResource(dataBindingIdlingResourceRule)

        fragmentScenario.onFragment {
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.fragmentClazzWorkSubmissionMarkingRv
        }

        return fragmentScenario
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
        runBlocking {
            dbRule.db.createTestContentEntriesAndJoinToClazzWork(testClazzWork.clazzWork, 2)
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

    @Test
    fun givenNoClazzWorkSubmissionMarkingPresentYetForQuiz_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        val testClazzWork = createQuizDbScenario()
        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        val fragmentScenario = reloadFragment(clazzWorkUid, clazzMemberUid)

        fragmentScenario.onFragment {
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.fragmentClazzWorkSubmissionMarkingRv
        }

        fillMarkingAndReturn(testClazzWork)

        //Check database
        val submissionPostSubmit = runBlocking {
            dbRule.db.clazzWorkSubmissionDao.findByUidAsync(
                    testClazzWork.submissions!!.get(0).clazzWorkSubmissionUid)
        }
        Assert.assertEquals("Marked OK", 42,
                submissionPostSubmit?.clazzWorkSubmissionScore)
    }

    private fun fillMarkingAndReturn(testClazzWork: TestClazzWork, hitReturn: Boolean = true){
        //Scroll to Marking
        Espresso.onView(ViewMatchers.withId(R.id.fragment_clazz_work_submission_marking_rv)).perform(
                RecyclerViewActions.scrollToHolder(withTagInMarking(
                        testClazzWork.submissions!!.get(0).clazzWorkSubmissionUid)))

        //Type marking value
        Espresso.onView(ViewMatchers.withId(R.id.item_clazzwork_submission_score_edit_et))
                .perform(ViewActions.clearText(), ViewActions.typeText("42"),
                        ViewActions.closeSoftKeyboard())

        //Scroll to Return
        Espresso.onView(ViewMatchers.withId(R.id.fragment_clazz_work_submission_marking_rv)).perform(
                RecyclerViewActions.scrollToHolder(withTagInMarkingSubmit(
                        testClazzWork.clazzWork.clazzWorkUid)))

        if(hitReturn) {
            //Click return button
            Espresso.onView(ViewMatchers.withId(
                    R.id.item_clazzworksubmission_marking_button_with_extra_button)).perform(click())
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

    @Test
    fun givenNoClazzWorkSubmissionMarkingPresentYetForPartiallyFilledQuiz_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        val testClazzWork = createQuizDbPartialScenario()
        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        val fragmentScenario = reloadFragment(clazzWorkUid, clazzMemberUid)

        fragmentScenario.onFragment {
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.fragmentClazzWorkSubmissionMarkingRv
        }

        fillMarkingAndReturn(testClazzWork)

        //Check database
        val submissionPostSubmit = runBlocking {
            dbRule.db.clazzWorkSubmissionDao.findByUidAsync(
                    testClazzWork.submissions!!.get(0).clazzWorkSubmissionUid)
        }
        Assert.assertEquals("Marked OK", 42,
                submissionPostSubmit?.clazzWorkSubmissionScore)
    }


    @Test
    fun givenNoClazzWorkSubmissionMarkingPresentYetForFreeText_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        val testClazzWork = createFreeTextDbScenario()
        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        val fragmentScenario = reloadFragment(clazzWorkUid, clazzMemberUid)

        fragmentScenario.onFragment {
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.fragmentClazzWorkSubmissionMarkingRv
        }

        fillMarkingAndReturn(testClazzWork)

        //Check database
        val submissionPostSubmit = runBlocking {
            dbRule.db.clazzWorkSubmissionDao.findByUidAsync(
                    testClazzWork.submissions!!.get(0).clazzWorkSubmissionUid)
        }
        Assert.assertEquals("Marked OK", 42,
                submissionPostSubmit?.clazzWorkSubmissionScore)

    }


    @Test
    fun givenNoClazzWorkSubmissionMarkingPresentYetForQuiz_whenFilledInAndReturnAndMarkNextClicked_thenShouldSaveToDatabaseAndLoadNextInQueue() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        val testClazzWork = createQuizDbScenarioWith2Submissions()
        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        val fragmentScenario = reloadFragment(clazzWorkUid, clazzMemberUid)

        fragmentScenario.onFragment {
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.fragmentClazzWorkSubmissionMarkingRv
        }

        fillMarkingAndReturn(testClazzWork)

        //TODO: Check if navController has the right arguments

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

        //TODO: Add that next submission loaded.

    }

    @Test
    fun givenNoClazzWorkSubmissionMarkingPresentYetForQuizAndNoMoreSubmission_whenFilledInAndReturnAndMarkNextClicked_thenShouldSaveToDatabaseAndFinishView() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        val testClazzWork = createQuizDbScenario()
        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        val fragmentScenario = reloadFragment(clazzWorkUid, clazzMemberUid)

        fragmentScenario.onFragment {
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.fragmentClazzWorkSubmissionMarkingRv
        }

        //Check database
        val submissionPostSubmit = runBlocking {
            dbRule.db.clazzWorkSubmissionDao.findByUidAsync(
                    testClazzWork.submissions!!.get(0).clazzWorkSubmissionUid)
        }
        Assert.assertEquals("Marked OK", 42,
                submissionPostSubmit?.clazzWorkSubmissionScore)

        //TODO: Check view.finish called 
    }

    companion object {
        fun fillFields(fragmentScenario: FragmentScenario<ClazzWorkSubmissionMarkingFragment>,
                       clazz: ClazzWithHolidayCalendarAndSchool,
                       clazzOnForm: ClazzWithHolidayCalendarAndSchool?,
                       schedules: List<Schedule> = listOf(),
                       schedulesOnForm: List<Schedule>? = null,
                       setFieldsRequiringNavigation: Boolean = true) {

            clazz.clazzName?.takeIf { it != clazzOnForm?.clazzName }?.also {
                Espresso.onView(ViewMatchers.withId(R.id.activity_clazz_edit_name_text)).perform(ViewActions.clearText(), ViewActions.typeText(it))
            }

            clazz.clazzDesc?.takeIf { it != clazzOnForm?.clazzDesc }?.also {
                Espresso.onView(ViewMatchers.withId(R.id.activity_clazz_edit_desc_text)).perform(ViewActions.clearText(), ViewActions.typeText(it))
            }

            clazz.clazzStartTime.takeIf { it != clazzOnForm?.clazzStartTime }?.also {
                setDateField(R.id.activity_clazz_edit_start_date_edittext, it)
            }
            clazz.clazzEndTime.takeIf { it != clazzOnForm?.clazzEndTime }?.also {
                setDateField(R.id.activity_clazz_edit_end_date_edittext, it)
            }


            if (!setFieldsRequiringNavigation) {
                return
            }


            schedules.filter { schedulesOnForm == null || it !in schedulesOnForm }.forEach { schedule ->
                fragmentScenario.onFragment {
                    it.findNavController().currentBackStackEntry?.savedStateHandle
                            ?.set("Schedule", defaultGson().toJson(listOf(schedule)))
                }
                Espresso.onIdle()
            }

            fragmentScenario.onFragment { fragment ->
                fragment.takeIf { clazz.holidayCalendar != clazzOnForm?.holidayCalendar }
                        ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                        ?.set("HolidayCalendar", defaultGson().toJson(listOf(clazz.holidayCalendar)))
            }

        }
    }

}