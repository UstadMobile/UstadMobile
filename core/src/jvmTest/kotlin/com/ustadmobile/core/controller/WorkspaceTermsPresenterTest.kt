package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.WorkspaceTermsDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.WorkspaceTermsView
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.WorkspaceTerms
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.scoped
import org.kodein.di.singleton

class WorkspaceTermsPresenterTest {

    private lateinit var mockView: WorkspaceTermsView

    private lateinit var di: DI

    private lateinit var mockImpl: UstadMobileSystemImpl

    private lateinit var mockDb: UmAppDatabase

    private lateinit var mockRepo: UmAppDatabase

    private lateinit var mockDbTermsDao: WorkspaceTermsDao

    private lateinit var mockRepoTermsDao: WorkspaceTermsDao

    @Before
    fun setup() {
        val endpointScope = EndpointScope()

        mockView = mock {

        }

        mockImpl = mock {

        }

        mockDbTermsDao  = mock {

        }

        mockDb = mock {
            on { workspaceTermsDao }.thenReturn(mockDbTermsDao)
        }

        mockRepoTermsDao = mock {

        }

        mockRepo = mock(extraInterfaces = arrayOf(DoorDatabaseRepository::class)) {
            on { workspaceTermsDao }.thenReturn(mockRepoTermsDao)
        }

        (mockRepo as DoorDatabaseRepository).stub {
            on { db }.thenReturn(mockDb)
        }

        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { mockImpl }
            bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton() { mockDb }
            bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton { mockRepo }
        }
    }

    @Test
    fun givenTermsExistForLang_whenOnCreateCalled_thenShouldShowTermsInSpecifiedLang() {
        mockImpl.stub {
            on { getDisplayedLocale(any()) }.thenReturn("fa")
        }
        listOf(mockDbTermsDao, mockRepoTermsDao).forEach {dao ->
            dao.stub {
                onBlocking { findWorkspaceTerms("fa") }.thenReturn(WorkspaceTerms().apply {
                    termsHtml = "Salam"
                })
            }
        }

        val presenter = WorkspaceTermsPresenter(Any(),
                mapOf(UstadView.ARG_SERVER_URL to "http://localhost/"), mockView, di)
        presenter.onCreate(null)

        verify(mockView, timeout(1000).atLeastOnce()).termsHtml = "Salam"
        verifyBlocking(mockDbTermsDao, timeout(1000)) {
            findWorkspaceTerms("fa")
        }
    }

}