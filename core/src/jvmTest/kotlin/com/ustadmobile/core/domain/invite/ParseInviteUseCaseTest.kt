package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class ParseInviteUseCaseTest {

    private lateinit var parseInviteUseCase: ParseInviteUseCase


    private lateinit var mockValidatePhoneUseCase: PhoneNumValidatorUseCase

    @BeforeTest
    fun setUp() {


        mockValidatePhoneUseCase = mock {
            on { isValid(any()) }.thenReturn(false)
        }
        parseInviteUseCase = ParseInviteUseCase()

    }


    @Test
    fun givenValidEmail_whenInvoke_thenReturnTrue() = runBlocking {

        val result = parseInviteUseCase.invoke("valid.email@example.com", mockValidatePhoneUseCase)
        assertEquals(1, result.size)
        assertEquals(true, result[0].isValid)
    }

    @Test
    fun givenValidPhone_whenInvoke_thenReturnTrue() = runBlocking {

        val result = parseInviteUseCase.invoke("+911234567890", mockValidatePhoneUseCase)
        assertEquals(1, result.size)
        assertEquals(true, result[0].isValid)
    }

    @Test
    fun givenMixedInput_whenInvoke_thenReturnMixedResults() = runBlocking {

        val text = "valid.email@example.com,  +911234567890"
        val result = parseInviteUseCase.invoke(text, mockValidatePhoneUseCase)

        assertEquals(2, result.size)
        assertEquals(true, result[0].isValid)
        assertEquals(true, result[1].isValid)
    }
}