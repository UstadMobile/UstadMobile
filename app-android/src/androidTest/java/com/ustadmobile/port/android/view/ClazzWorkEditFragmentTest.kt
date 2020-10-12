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
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptions
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

@AdbScreenRecord("ClazzWork (Assignments) Edit tests")
class ClazzWorkEditFragmentTest {

    lateinit var contentRVIdlingResource: RecyclerViewIdlingResource
    lateinit var questionsRVIdlingResource: RecyclerViewIdlingResource

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
        contentRVIdlingResource = RecyclerViewIdlingResource(null, 3)
        questionsRVIdlingResource = RecyclerViewIdlingResource(null, 3)
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

    private fun reloadFragment(clazzWork: ClazzWork?){
        val clazzWorkUid = clazzWork?.clazzWorkUid?:0L

        val fragmentScenario = launchFragmentInContainer(
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to
                        clazzWorkUid.toString()),
                themeResId = R.style.UmTheme_App) {
            ClazzWorkEditFragment(). also {
                it.installNavController(systemImplNavRule.navController)
                it.arguments = bundleOf(UstadView.ARG_ENTITY_UID to
                        clazzWorkUid.toString())
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        fragmentScenario.onFragment {
            contentRVIdlingResource.recyclerView =
                    it.mBinding!!.fragmentClazzWorkEditContentRv
            questionsRVIdlingResource.recyclerView =
                    it.mBinding!!.fragmentClazzWorkEditQuestionsRv
        }


    }

    private fun checkClazzWorkBasicEditDisplayOk(clazzWork: ClazzWork?, contentList: List<ContentEntry>){
        //Scroll to top
//        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
//                scrollToPosition<RecyclerView.ViewHolder>(0)
//        )
//        onView(withId(R.id.item_clazzwork_detail_description_cl)).check(matches(
//                withEffectiveVisibility(Visibility.VISIBLE)))
//        onView(withId(R.id.item_clazzwork_detail_description_title)).check(matches(
//                withEffectiveVisibility(Visibility.VISIBLE)))
//        onView(withText(clazzWork?.clazzWorkInstructions?:"")).check(matches(
//                withEffectiveVisibility(Visibility.VISIBLE)))
//
//        val startDateString =  dateWithTimeFormat.format(arrayOf(clazzWork?.clazzWorkStartDateTime,
//                scheduleTimeToDate(clazzWork?.clazzWorkStartTime?.toInt()?:0), ""))
//        val dueDateString =  dateWithTimeFormatWithPrepend.format(
//                arrayOf("Due date", clazzWork?.clazzWorkDueDateTime,
//                        scheduleTimeToDate(clazzWork?.clazzWorkDueTime?.toInt()?:0), ""))
//        onView(withText(startDateString)).check(matches(
//                withEffectiveVisibility(Visibility.VISIBLE)))
//        onView(withText(dueDateString)).check(matches(
//                withEffectiveVisibility(Visibility.VISIBLE)))
//        onView(withText("Content")).check(matches(
//                withEffectiveVisibility(Visibility.VISIBLE)))
//        if(contentList.isNotEmpty()) {
//            onView(withText(contentList[0].title)).check(matches(
//                    withEffectiveVisibility(Visibility.VISIBLE)))
//            onView(withText(contentList[1].title)).check(matches(
//                    withEffectiveVisibility(Visibility.VISIBLE)))
//        }

    }

    private fun checkQuizQuestionsDisplayOk(
            clazzWorkQuizStuff : TestClazzWorkWithQuestionAndOptionsAndResponse?){

//        //Scroll to Submission
//            onView(withText("Submission")).check(doesNotExist())
//            onView(withId(R.id.item_simpl_button_button_tv)).check(doesNotExist())
//            onView(withId(R.id.item_clazzwork_submission_text_entry_et)).check(doesNotExist())
//
//
//        val q1uid = clazzWorkQuizStuff?.questionsAndOptions?.get(0)?.clazzWorkQuestion
//                ?.clazzWorkQuestionUid
//
//        onView(withId(R.id.fragment_clazz_work_with_submission_detail_rv)).perform(
//                scrollToHolder(withTagInQuestion(q1uid!!)))
//
//        onView(allOf(withText("Question 1"),
//            withId(R.id.item_clazzworkquestionandoptionswithresponse_title_tv))).check(
//                matches(isDisplayed()))
//
//        val q2uid = clazzWorkQuizStuff?.questionsAndOptions?.get(1)?.clazzWorkQuestion
//                ?.clazzWorkQuestionUid


    }

    private fun fillFields(newClazzWork: ClazzWork?, newContent: List<ContentEntry>,
                            questionAndResponses: List<ClazzWorkQuestionAndOptions>){
        //TODO:
    }

    @AdbScreenRecord("ClazzWorkEditFragment: Check When given no ClazzWork given " +
            "show edit a new ClazzWork ")
    @Test
    fun givenNoClazzWorkUid_whenLoadedThenFilledAndSaved_thenShouldShowAndInsert() {
        IdlingRegistry.getInstance().register(contentRVIdlingResource)
        IdlingRegistry.getInstance().register(questionsRVIdlingResource)

        reloadFragment(null)

        //Check overview page:
        checkClazzWorkBasicEditDisplayOk(null, listOf())


        //Fill fields
        fillFields(null, listOf(), listOf())

        //Hit submit
        //TODO

        //Assert inserted in database

        IdlingRegistry.getInstance().unregister(contentRVIdlingResource)
        IdlingRegistry.getInstance().unregister(questionsRVIdlingResource)
    }



    @AdbScreenRecord("ClazzWorkEditFragment: Check When given a saved ClazzWork should " +
            "show edit OK in all cases")
    @Test
    fun givenValidClazzWorkUid_whenLoadedAndUpdatedAndSubmitted_thenShouldShowAndUpdateAndPersist() {
        IdlingRegistry.getInstance().register(contentRVIdlingResource)
        IdlingRegistry.getInstance().register(questionsRVIdlingResource)

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


        reloadFragment(testClazzWork.clazzWork)

        //Check overview page:
        checkClazzWorkBasicEditDisplayOk(clazzWork, contentList)


        //Change type:
        clazzWork.clazzWorkSubmissionType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
        runBlocking{
            dbRule.db.clazzWorkDao.updateAsync(clazzWork)
        }

        reloadFragment(testClazzWork.clazzWork)

        //Check overview page
        checkClazzWorkBasicEditDisplayOk(clazzWork, contentList)

        //Change type to quiz
        var clazzWorkQuizStuff : TestClazzWorkWithQuestionAndOptionsAndResponse? = null
        runBlocking{
            clazzWorkQuizStuff = dbRule.db.insertQuizQuestionsAndOptions(clazzWork, false, 0,
                    0, 0, true)
            clazzWork = clazzWorkQuizStuff?.clazzWork!!
        }

        reloadFragment(clazzWorkQuizStuff!!.clazzWork)

        //Check overview page
        checkClazzWorkBasicEditDisplayOk(clazzWork, contentList)

        checkQuizQuestionsDisplayOk(clazzWorkQuizStuff)


        //Fill fields
        fillFields(null, listOf(), listOf())

        //Hit submit
        //TODO

        //Assert update in database
        IdlingRegistry.getInstance().unregister(contentRVIdlingResource)
        IdlingRegistry.getInstance().unregister(questionsRVIdlingResource)
    }


    private fun withTagInQuestion(quid: Long): Matcher<RecyclerView.ViewHolder?>? {
        return object : BoundedMatcher<RecyclerView.ViewHolder?,
                ClazzWorkQuestionAndOptionsWithResponseRA.ClazzWorkQuestionViewHolder>(
                ClazzWorkQuestionAndOptionsWithResponseRA.
                ClazzWorkQuestionViewHolder::class.java) {
            override fun matchesSafely(
                    item: ClazzWorkQuestionAndOptionsWithResponseRA
                    .ClazzWorkQuestionViewHolder): Boolean {
                return item.itemView.tag.equals(quid)
            }

            override fun describeTo(description: Description) {
                description.appendText("view holder with title: $quid")
            }
        }
    }


}
