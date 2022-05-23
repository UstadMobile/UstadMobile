package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzEnrolmentDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.util.test.ext.startLocalTestSessionBlocking
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class JoinWithCodePresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: JoinWithCodeView

    private lateinit var di: DI

    private lateinit var clazzToEnrolInto: Clazz

    private lateinit var clazzEnrolmentRepoDaoSpy: ClazzEnrolmentDao

    private lateinit var accountManager: UstadAccountManager

    private var context: Any = Any()

    private var apiUrl: String? =null

    private lateinit var systemImpl: UstadMobileSystemImpl

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        mockView = mock { }

        accountManager = di.direct.instance()
        systemImpl = di.direct.instance()

        val currentEndpoint = accountManager.activeEndpoint.url
        val repo: UmAppDatabase by di.activeRepoInstance()

        val activeUser = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                firstNames = "Test"
                lastName = "User"
                username = "testuser"
            })
        }

        accountManager.startLocalTestSessionBlocking(activeUser, accountManager.activeEndpoint.url)


        clazzEnrolmentRepoDaoSpy = spy(repo.clazzEnrolmentDao)
        whenever(repo.clazzEnrolmentDao).thenReturn(clazzEnrolmentRepoDaoSpy)

        val systemImpl: UstadMobileSystemImpl by di.instance()

        apiUrl = currentEndpoint

        runBlocking {
            clazzToEnrolInto = Clazz("Test Class")
            repo.createNewClazzAndGroups(clazzToEnrolInto, systemImpl, mapOf(), context)
        }
    }

    @Test
    fun givenValidCode_whenHandleClickDoneCalled_thenShouldEnrollAsPendingStudent() {

        val presenter = JoinWithCodePresenter(context,
                mapOf(UstadView.ARG_CODE_TABLE to Clazz.TABLE_ID.toString(),
                UstadView.ARG_CODE to clazzToEnrolInto.clazzCode!!), mockView, di)
        presenter.onCreate(null)
        presenter.onStart()

        verifyBlocking(clazzEnrolmentRepoDaoSpy, timeout(5000 * 5000)) {
            insertAsync(argWhere {
                it.clazzEnrolmentPersonUid == accountManager.activeAccount.personUid &&
                        it.clazzEnrolmentRole == ClazzEnrolment.ROLE_STUDENT_PENDING
            })
        }

        val systemImpl: UstadMobileSystemImpl = di.direct.instance()
        verify(systemImpl, timeout(5000)).go(eq(ClazzList2View.VIEW_NAME), any(), any(), argWhere {
            it.popUpToInclusive == true && it.popUpToViewName == UstadView.CURRENT_DEST
        })
    }

    @Test
    fun givenValidCodeButStudentAlreadyEnroled_whenHandleClickDoneCalled_thenShouldShowError() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()
        val accountManager: UstadAccountManager by di.instance()

        runBlocking {
            val person = db.personDao.findByUid(accountManager.activeAccount.personUid)
                ?: throw IllegalStateException("no person!")
            repo.enrolPersonIntoClazzAtLocalTimezone(person, clazzToEnrolInto.clazzUid,
                ClazzEnrolment.ROLE_STUDENT)
        }


        val presenter = JoinWithCodePresenter(context,
            mapOf(UstadView.ARG_CODE_TABLE to Clazz.TABLE_ID.toString(),
                UstadView.ARG_CODE to clazzToEnrolInto.clazzCode!!), mockView, di)
        presenter.onCreate(null)
        presenter.onStart()

        verify(mockView, timeout(5000)).errorText = any()
    }

    @Test
    fun givenInvalidCode_whenHandleClickDoenCalled_thenShouldShowError() {
        val presenter = JoinWithCodePresenter(context,
                mapOf(UstadView.ARG_CODE_TABLE to Clazz.TABLE_ID.toString()), mockView, di)
        presenter.handleClickDone("wrong")

        verify(mockView, timeout(5000)).errorText = any()
    }

    @Test
    fun givenLoadedWithIncorrectCode_whenLoading_thenShouldShowError(){
        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_SERVER_URL to apiUrl.toString(),
                UstadView.ARG_CODE_TABLE to Clazz.TABLE_ID.toString()
        ), mockView, di)
        presenter.onCreate(null)
        presenter.onStart()
        presenter.handleClickDone("wrong")

        verify(mockView, timeout(10000)).errorText =
                systemImpl.getString(MessageID.invalid_register_code, context)
    }

    @Test
    fun givenLoadedWithNoTableCode_whenLoading_thenShouldShowError(){
        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_CODE to "any"
        ), mockView, di)
        presenter.onCreate(null)
        presenter.onStart()

        verify(mockView, timeout(5000)).errorText =
            systemImpl.getString(MessageID.invalid_register_code, context)
    }


}