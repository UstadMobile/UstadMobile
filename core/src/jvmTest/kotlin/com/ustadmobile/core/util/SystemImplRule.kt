package com.ustadmobile.core.util

import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * This test rule creates a fresh spy wrapper around UstadMobileSystemImpl for each test run. It can
 * then be used to verify calls to systemImpl.go etc.
 */
class SystemImplRule : TestWatcher(){

    private var systemImplSpy: UstadMobileSystemImpl? = null

    val systemImpl: UstadMobileSystemImpl
        get() = systemImplSpy!!


    override fun starting(description: Description?) {
        systemImplSpy = spy(UstadMobileSystemImpl.instance)
    }

    override fun finished(description: Description?) {
        systemImplSpy = null
    }

}