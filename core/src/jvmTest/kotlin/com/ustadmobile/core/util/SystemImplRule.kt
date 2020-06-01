package com.ustadmobile.core.util

import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import org.junit.rules.TestWatcher
import org.junit.runner.Description

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