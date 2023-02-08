package com.ustadmobile.test.http.junitxml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("testsuite")
class TestSuite(
    val name: String,
    val device: String,
    val tests: Int,
    val failures: Int = 0,
    val testCases: List<TestCase>
) {

}