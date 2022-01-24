package com.ustadmobile.core.impl.locale

import org.xmlpull.v1.XmlPullParserFactory

/**
 * Important: when running on JVM 9+, this MUST be called on a Class object representing a class
 * within the core module (such that it can access the given resource).
 */
fun Class<*>.getStringsXmlResource(resourceName: String,
                                   xppFactory: XmlPullParserFactory,
                                   messageIdMap: Map<String, Int>,
                                   fallback: StringsXml? = null,
                                   commentsEnabled: Boolean = false,
                                   trackOrder: Boolean = false
): StringsXml {
    val inputStream = getResourceAsStream(resourceName)
        ?: throw IllegalArgumentException("$resourceName not found for loading strings xml")

    return inputStream.use { resourceIn ->
        val xpp = xppFactory.newPullParser()
        xpp.setInput(resourceIn, "UTF-8")
        StringsXml(xpp, xppFactory, messageIdMap, resourceName, fallback, commentsEnabled, trackOrder)
    }
}
