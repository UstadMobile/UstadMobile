package com.ustadmobile.test.http.junitxml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("testsuites")
class TestSuites(
    val testSuites: List<TestSuite>
) {


}