package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ErrorReportDao
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.view.ErrorReportView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ErrorReport
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.mockito.kotlin.*

class ErrorReportPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    @Rule
    @JvmField
    val tmpFileRule = TemporaryFolder()

    private lateinit var di: DI

    private lateinit var navController: UstadNavController

    private lateinit var mockView: ErrorReportView

    private lateinit var dbErrorReportDaoSpy: ErrorReportDao

    private lateinit var repoErrorReportDaoSpy: ErrorReportDao

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        navController = di.direct.instance()
        mockView = mock { }


        val db: UmAppDatabase by di.activeDbInstance()
        dbErrorReportDaoSpy = spy(db.errorReportDao)
        db.stub {
            on { errorReportDao }.thenReturn(dbErrorReportDaoSpy)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()
        repoErrorReportDaoSpy = spy(repo.errorReportDao)
        repo.stub {
            on { errorReportDao}.thenReturn(repoErrorReportDaoSpy)
        }
    }

    //@Test
    fun givenNewErrorReport_whenCreated_thenShouldSaveToDb() {
        val presenterArgs = mapOf(ErrorReportView.ARG_ERR_CODE to "42",
            ErrorReportView.ARG_MESSAGE to "The meaning of life")
        navController.navigate(ErrorReportView.VIEW_NAME, presenterArgs)

        val presenter = ErrorReportPresenter(Any(), presenterArgs, mockView, di)
        presenter.onCreate(mapOf())


        verifyBlocking(repoErrorReportDaoSpy, timeout(5000)) {
            insertAsync(argWhere {
                it.errorCode == 42 && it.message == "The meaning of life"
            })
        }

    }

    //@Test
    fun givenExistingErrorReport_whenCreated_shouldLoadFromDb() {
        runBlocking {
            repoErrorReportDaoSpy.insertAsync(ErrorReport().also {
                it.errorCode = 427
                it.message = "The meaning of life is 6x7"
                it.errUid = 42
            })
        }

        val presenterArgs = mapOf(UstadView.ARG_ENTITY_UID to "42")
        navController.navigate(ErrorReportView.VIEW_NAME, presenterArgs)

        val presenter = ErrorReportPresenter(Any(), presenterArgs, mockView, di)
        presenter.onCreate(mapOf())

        verifyBlocking(dbErrorReportDaoSpy, timeout(5000)) {
            findByUidAsync(eq(42L))
        }
    }

}