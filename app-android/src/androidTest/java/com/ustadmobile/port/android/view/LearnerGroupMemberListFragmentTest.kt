package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
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
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @Before
    fun setup() {

        dbRule.insertPersonAndStartSession(Person().apply {
            admin = true
            personUid = UmAppDatabaseAndroidClientRule.DEFAULT_ACTIVE_USER_PERSONUID
            firstNames = "Test"
            lastName = "Teacher"
        })

        Person().apply {
            firstNames = "New"
            lastName = "Student"
            personUid = dbRule.repo.personDao.insert(this)
        }

        ContentEntry().apply {
            contentEntryUid = 1
            dbRule.repo.contentEntryDao.insert(this)
        }


        LearnerGroup().apply {
            learnerGroupName = "Test"
            learnerGroupDescription = "New Group"
            learnerGroupUid = 1
            dbRule.repo.learnerGroupDao.insert(this)
        }

        LearnerGroupMember().apply {
            learnerGroupMemberRole = LearnerGroupMember.PRIMARY_ROLE
            learnerGroupMemberLgUid = 1
            learnerGroupMemberPersonUid = dbRule.account.personUid
            dbRule.repo.learnerGroupMemberDao.insert(this)
        }

        GroupLearningSession().apply {
            groupLearningSessionUid = 1
            groupLearningSessionContentUid = 1
            groupLearningSessionLearnerGroupUid = 1
            dbRule.repo.groupLearningSessionDao.insert(this)
        }


    }

    @AdbScreenRecord("Given learner group when loaded show list of members")
    @Test
    fun givenLearnerGroupWithEntry_whenLoaded_showListOfMembers() {

        init {
            val args = bundleOf(UstadView.ARG_CONTENT_ENTRY_UID to "1",
                    UstadView.ARG_LEARNER_GROUP_UID to "1")

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
