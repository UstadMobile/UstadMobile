package com.ustadmobile.test.http.junitxml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@SerialName("failure")
class Failure(
    val message: String? = null,
    val name: String? = null,
    @XmlValue(true)
    val content: String? = null,

) {


}