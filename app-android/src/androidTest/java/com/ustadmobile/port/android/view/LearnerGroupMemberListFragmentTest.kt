package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.launchActivity
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.core.view.LearnerGroupMemberListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.screen.LearnerGroupMemberListScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("Learner Group Member List Tests")
class LearnerGroupMemberListFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()


    @Before
    fun setup() {

        dbRule.insertPersonForActiveUser(Person().apply {
            admin = true
            firstNames = "Test"
            lastName = "Teacher"
        })

        Person().apply {
            firstNames = "New"
            lastName = "Student"
            personUid = dbRule.db.personDao.insert(this)
        }

        ContentEntry().apply {
            contentEntryUid = 1
            dbRule.db.contentEntryDao.insert(this)
        }


        LearnerGroup().apply {
            learnerGroupName = "Test"
            learnerGroupDescription = "New Group"
            learnerGroupUid = 1
            dbRule.db.learnerGroupDao.insert(this)
        }

        LearnerGroupMember().apply {
            learnerGroupMemberRole = LearnerGroupMember.TEACHER_ROLE
            learnerGroupMemberLgUid = 1
            learnerGroupMemberPersonUid = dbRule.account.personUid
            dbRule.db.learnerGroupMemberDao.insert(this)
        }

        GroupLearningSession().apply {
            groupLearningSessionUid = 1
            groupLearningSessionContentUid = 1
            groupLearningSessionLearnerGroupUid = 1
            dbRule.db.groupLearningSessionDao.insert(this)
        }


    }

    @Test
    fun givenLearnerGroupWithEntry_whenLoaded_showListOfMembers() {

        init {
            val args = bundleOf(UstadView.ARG_CONTENT_ENTRY_UID to "1",
                    LearnerGroupMemberListView.ARG_LEARNER_GROUP_UID to "1")

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = args) {
                LearnerGroupMemberListFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }
        }.run {
            LearnerGroupMemberListScreen {
                recycler {
                    hasSize(1)
                    childAt<LearnerGroupMemberListScreen.LearnerGroupMember>(0) {
                        memberName {
                            isDisplayed()
                            hasText("Test Teacher")
                        }
                        memberRole{
                            isDisplayed()
                            hasText("Primary user")
                        }
                    }
                }
            }
        }

    }


}
