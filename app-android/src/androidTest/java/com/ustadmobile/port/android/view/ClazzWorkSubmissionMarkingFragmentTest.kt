package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.IdlingRegistry
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.DataBindingIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withDataBindingIdlingResource
import com.ustadmobile.util.test.ext.TestClazzWork
import com.ustadmobile.util.test.ext.createTestContentEntriesAndJoinToClazzWork
import com.ustadmobile.util.test.ext.insertTestClazzWorkAndQuestionsAndOptionsWithResponse
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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

        Thread.sleep(1000)

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

        Thread.sleep(1000)

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

        Thread.sleep(1000)

    }


    @Test
    fun givenNoClazzWorkSubmissionMarkingPresentYetForQuiz_whenFilledInAndReturnAndMarkNextClicked_thenShouldSaveToDatabaseAndLoadNextInQueue() {

        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        val testClazzWork = createQuizDbScenario()
        val clazzWorkUid: Long = testClazzWork.clazzWork.clazzWorkUid
        val clazzMemberUid: Long = testClazzWork.submissions!!.get(0).clazzWorkSubmissionClazzMemberUid

        val fragmentScenario = reloadFragment(clazzWorkUid, clazzMemberUid)

        fragmentScenario.onFragment {
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.fragmentClazzWorkSubmissionMarkingRv
        }

        Thread.sleep(1000)
        //TODO1. Go to

    }

}