//package com.ustadmobile.test.core.impl
//
//
//import com.ustadmobile.port.javase.impl.UmContextSe
//
///**
// * TestUtil is designed to abstract away the differences between conducting testing on "smart"
// * devices where we can run NanoHTTPD and J2ME where we need to use an external server.
// */
//
//object PlatformTestUtil {
//
//    internal var testContext = UmContextSe()
//
//    @JvmStatic
//    val targetContext: Any
//        get() = testContext
//
//    @JvmStatic
//    fun getTestContext(): Any {
//        return Any()
//    }
//
//}
