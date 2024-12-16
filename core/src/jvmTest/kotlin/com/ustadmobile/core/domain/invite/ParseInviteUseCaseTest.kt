package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class ParseInviteUseCaseTest {

    private lateinit var parseInviteUseCase: ParseInviteUseCase
    private lateinit var mockEmailUseCase: ValidateEmailUseCase


    private lateinit var mockValidatePhoneUseCase: PhoneNumValidatorUseCase

    @BeforeTest
    fun setUp() {

        mockEmailUseCase = mock()
        mockValidatePhoneUseCase = mock {
            on { isValid(any()) }.thenReturn(false)
        }
        parseInviteUseCase = ParseInviteUseCase(mockValidatePhoneUseCase,mockEmailUseCase)

    }


    @Test
    fun givenValidEmail_whenInvoke_thenReturnTrue() = runBlocking {

        val result = parseInviteUseCase.invoke("valid.email@example.com")
        assertEquals(1, result.size)
        assertEquals(true, result[0].isValid)
    }

    @Test
    fun givenValidUsername_whenInvoke_thenReturnTrue() = runBlocking {

        val result = parseInviteUseCase.invoke("nik@1")
        assertEquals(1, result.size)
        assertEquals(true, result[0].isValid)
    }

    @Test
    fun givenMixedInput_whenInvoke_thenReturnMixedResults() = runBlocking {

        val text = "valid.email@example.com,  nik"
        val result = parseInviteUseCase.invoke(text)

        assertEquals(2, result.size)
        assertEquals(true, result[0].isValid)
        assertEquals(false, result[1].isValid)
    }
}