package com.ustadmobile.test.http.junitxml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("testcase")
class TestCase(
    val id: String,
    val name: String,
    val classname: String? = null,

    val failure: List<Failure> = emptyList(),
) {


}