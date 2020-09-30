package com.ustadmobile.port.android.view

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.core.view.LearnerGroupMemberListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.screen.LearnerGroupMemberListScreen
import com.ustadmobile.port.android.screen.MainActivityScreen
import com.ustadmobile.port.android.screen.PersonListScreen
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LearnerGroupEndToEndTest : TestCase() {


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
            learnerGroupMemberRole = LearnerGroupMember.PRIMARY_ROLE
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
    fun givenLearnerGroup_whenAddingNewMember_showInList() {

        init {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val launchIntent = Intent(context, MainActivity::class.java).also {
                it.putExtra(UstadView.ARG_NEXT,
                        "${LearnerGroupMemberListView.VIEW_NAME}?${UstadView.ARG_CONTENT_ENTRY_UID}=1&${UstadView.ARG_LEARNER_GROUP_UID}=1")
            }
            launchActivity<MainActivity>(intent = launchIntent)
        }.run {

            LearnerGroupMemberListScreen {
                recycler {
                    hasSize(1)
                    childAt<LearnerGroupMemberListScreen.LearnerGroupMember>(0) {
                        memberName {
                            isDisplayed()
                            hasText("Test Teacher")
                        }
                    }
                }
            }

            MainActivityScreen {
                fab {
                    click()
                }
            }

            PersonListScreen{
                recycler{
                    childWith<PersonListScreen.Person> {
                        withDescendant { withText("New Student") }
                    } perform {
                        click()
                    }
                }
            }

            LearnerGroupMemberListScreen{
                recycler{
                    hasSize(2)
                    childAt<LearnerGroupMemberListScreen.LearnerGroupMember>(0) {
                        memberName {
                            isDisplayed()
                            hasText("Test Teacher")
                            hasText("Primary user")
                        }
                    }
                    childAt<LearnerGroupMemberListScreen.LearnerGroupMember>(1) {
                        memberName {
                            isDisplayed()
                            hasText("New Student")
                        }
                        memberRole{
                            hasText("Participant")
                        }
                    }
                }
            }

        }

    }


}