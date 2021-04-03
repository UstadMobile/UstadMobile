package com.ustadmobile.core.notification

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.ext.toUmAccount
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class TakeAttendanceNotificationCheckerTest {

    @JvmField
    @Rule
    val ustadTestRule = UstadTestRule()

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var di: DI

    private lateinit var accountManager: UstadAccountManager

    private lateinit var activePerson: Person

    private lateinit var testClazz: Clazz

    private lateinit var testNotificationSetting: NotificationSetting

    private lateinit var testClazzLog: ClazzLog

    @Before
    fun setup() {
        di = DI {
            import (ustadTestRule.diModule)
        }

        accountManager = di.direct.instance()
        db = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_DB)
        repo = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)

        activePerson = Person().apply {
            firstNames = "Notification"
            lastName = "Test user"
            username = "test"
            runBlocking { repo.insertPersonAndGroup(this@apply) }
        }

        accountManager.activeAccount = activePerson.toUmAccount(accountManager.activeAccount.endpointUrl)

        testClazz = Clazz().apply {
            clazzName = "Test Clazz"
            clazzUid = repo.clazzDao.insert(this)
        }

        runBlocking {
            repo.enrolPersonIntoClazzAtLocalTimezone(activePerson, testClazz.clazzUid,
                ClazzEnrolment.ROLE_TEACHER)
        }

        testNotificationSetting = NotificationSetting().apply {
            nsType = NotificationSetting.TYPE_TAKE_ATTENDANCE_REMINDER
            nsPersonUid = activePerson.personUid
            nsUid = repo.notificationSettingDao.insert(this)
        }

        testClazzLog = ClazzLog().apply() {
            clazzLogClazzUid = testClazz.clazzUid
            logDate = systemTimeInMillis()
            clazzLogUid = repo.clazzLogDao.insert(this)
        }

    }

    @Test
    fun givenClazzScheduledWhenUserIsTeacher_whenCheckNotificationCalled_thenShouldCreateFeedEntry() {

        val notificationChecker = TakeAttendanceNotificationChecker(di,
            Endpoint(accountManager.activeAccount.endpointUrl))

        runBlocking {
            notificationChecker.checkNotification(testNotificationSetting.nsUid)
        }

        val personFeedEntries = runBlocking {
            db.feedEntryDao.findByPersonAsList(activePerson.personUid)
        }

        Assert.assertEquals("Found one feed entry notification created", 1,
            personFeedEntries.count {
                it.fePersonUid == activePerson.personUid && it.feEntityUid == testClazzLog.clazzLogUid
            })
    }

    @Test
    fun givenClazzScheduledWhenUserIsTeacher_whenCheckNotificationCalledTwice_thenShouldCreateOneFeedEntry() {

        val notificationChecker = TakeAttendanceNotificationChecker(di,
            Endpoint(accountManager.activeAccount.endpointUrl))

        runBlocking {
            notificationChecker.checkNotification(testNotificationSetting.nsUid)
        }

        val personFeedEntries = runBlocking {
            db.feedEntryDao.findByPersonAsList(activePerson.personUid)

            db.feedEntryDao.findByPersonAsList(activePerson.personUid)
        }

        Assert.assertEquals("Found one feed entry notification created", 1,
            personFeedEntries.count {
                it.fePersonUid == activePerson.personUid && it.feEntityUid == testClazzLog.clazzLogUid
            })
    }

}