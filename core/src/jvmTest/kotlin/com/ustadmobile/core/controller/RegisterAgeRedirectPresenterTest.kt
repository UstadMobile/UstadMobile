package com.ustadmobile.core.controller

import org.mockito.kotlin.*
import com.soywiz.klock.DateTime
import com.soywiz.klock.years
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.RegisterAgeRedirectView
import com.ustadmobile.core.view.SiteTermsDetailView
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

class RegisterAgeRedirectPresenterTest {

    lateinit var di: DI

    lateinit var mockView: RegisterAgeRedirectView

    lateinit var mockSystemImpl: UstadMobileSystemImpl

    @Before
    fun setup() {
        mockSystemImpl = mock {
            on { getString(any<Int>(), any())}.thenAnswer {
                it.arguments[0].toString()
            }

        }
        mockView = mock { }
        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { mockSystemImpl }
        }
    }

    //@Test
    fun givenAgeIsMinor_whenHandleClickNextCalled_thenShouldGoToRegisterMinorPresenter() {
        val presenter = RegisterAgeRedirectPresenter(Any(), mapOf(), mockView, di)
        presenter.onCreate(null)

        val dateOfBirthMillis = (DateTime.now() - 5.years).unixMillisLong

        mockView.stub {
            on { dateOfBirth }.thenReturn(dateOfBirthMillis)
        }

        presenter.handleClickNext()
        verify(mockSystemImpl, timeout(5000))
                .go(eq(PersonEditView.VIEW_NAME_REGISTER),
                        argWhere { it[PersonEditView.ARG_DATE_OF_BIRTH]?.toLong() == dateOfBirthMillis },
                        any())
    }

    //@Test
    fun givenAgeIsAdult_whenHandleClickNextCalled_thenShouldGoToAcceptTerms() {
        val presenter = RegisterAgeRedirectPresenter(Any(), mapOf(), mockView, di)
        presenter.onCreate(null)

        val dateOfBirthMillis = (DateTime.now() - 18.years).unixMillisLong

        mockView.stub {
            on { dateOfBirth }.thenReturn(dateOfBirthMillis)
        }

        presenter.handleClickNext()

        verify(mockSystemImpl, timeout(5000))
                .go(eq(SiteTermsDetailView.VIEW_NAME_ACCEPT_TERMS),
                        argWhere { it[PersonEditView.ARG_DATE_OF_BIRTH]?.toLong() == dateOfBirthMillis },
                        any())
    }


}