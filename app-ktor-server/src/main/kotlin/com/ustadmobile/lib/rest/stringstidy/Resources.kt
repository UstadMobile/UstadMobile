package com.ustadmobile.lib.rest.resmodel

import kotlinx.serialization.SerialName
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@kotlinx.serialization.Serializable
@SerialName("resources")
class Resources(
    @XmlSerialName(value = "ignore", namespace = "http://schemas.android.com/tools", prefix = "tools")
    val ignore: String,
    val strings: List<ResString>,
    val stringArrays: List<StringArray>,
    val plurals: List<Plurals>,
) {
}