package com.ustadmobile.port.android.view

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.agoda.kakao.common.views.KView
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.toughra.ustadmobile.R
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.port.android.screen.*
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.test.port.android.util.clickOptionMenu
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class LearnerGroupEndToEndTest : TestCase() {


    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

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

        val container = Container().apply {
            containerContentEntryUid = 1
            mimeType = "application/tincan+zip"
            containerUid = dbRule.db.containerDao.insert(this)
        }
        val containerTmpDir = UmFileUtilSe.makeTempDir("xapicontent", "${System.currentTimeMillis()}")
        val testFile = File.createTempFile("xapicontent", "xapifile", containerTmpDir)
        val input = javaClass.getResourceAsStream("/com/ustadmobile/app/android/XapiPackage-JsTetris_TCAPI.zip")
        testFile.outputStream().use { input?.copyTo(it) }

        val containerManager = ContainerManager(container, dbRule.db, dbRule.repo, containerTmpDir.absolutePath)
        addEntriesFromZipToContainer(testFile.absolutePath, containerManager)

        DownloadJobItem().apply {
            djiContentEntryUid = 1
            djiContainerUid = container.containerUid
            djiStatus = JobStatus.COMPLETE
            djiUid = dbRule.db.downloadJobItemDao.insert(this).toInt()
        }

    }

    @Test
    fun givenLearnerGroup_whenAddingNewMember_showInList() {
        var activityScenario: ActivityScenario<MainActivity>? = null
        init {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val launchIntent = Intent(context, MainActivity::class.java).also {
                it.putExtra(UstadView.ARG_NEXT,
                        "${ContentEntry2DetailView.VIEW_NAME}?${UstadView.ARG_ENTITY_UID}=1")
            }
            activityScenario = launchActivity(intent = launchIntent)
        }.run {

            ContentEntryDetailScreen {
                groupActivityButton {
                    click()
                }
            }

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

            PersonListScreen {
                recycler {
                    childWith<PersonListScreen.Person> {
                        withDescendant { withText("New Student") }
                    } perform {
                        click()
                    }
                }
            }
            LearnerGroupMemberListScreen {
                recycler {
                    hasSize(2)
                    childAt<LearnerGroupMemberListScreen.LearnerGroupMember>(0) {
                        memberName {
                            isDisplayed()
                            hasText("Test Teacher")
                        }
                        memberRole {
                            hasText("Primary user")
                        }
                    }
                    childAt<LearnerGroupMemberListScreen.LearnerGroupMember>(1) {
                        memberName {
                            isDisplayed()
                            hasText("New Student")
                        }
                        memberRole {
                            hasText("Participant")
                        }
                    }
                }
                activityScenario?.clickOptionMenu(R.id.action_selection_done)
            }
            XapiContentScreen {
                webView{

                }
            }

        }

    }


}