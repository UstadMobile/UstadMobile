package com.ustadmobile.core.impl.locale

import org.junit.Assert
import org.junit.Test
import org.xmlpull.v1.XmlPullParserFactory

class StringsXmlTest {

    private fun loadStringsFromResource(resName: String, idMap: Map<String, Int>,
                                        fallback: StringsXml? = null) : StringsXml{
        return this::class.java.getResourceAsStream(resName).use { stringsXmlIn ->
            val xpp = XmlPullParserFactory.newInstance().also {
                it.isNamespaceAware = false
                it.isValidating = false
            }.newPullParser()
            xpp.setInput(stringsXmlIn, "UTF-8")

            StringsXml(xpp, idMap, "test", fallback = fallback)
        }
    }

    @Test
    fun givenValidStringId_whenGetMessageCalled_thenShouldReturnString() {
        val stringsXml = loadStringsFromResource("strings.xml",
            mapOf("app_name" to 42))

        Assert.assertEquals("Got app name", "Ustad Mobile",  stringsXml[42])
    }

    @Test(expected = IllegalArgumentException::class)
    fun givenValidStringsXml_whenInvalidGetMessageCalled_thenShouldThrowException() {
        val stringsXml = loadStringsFromResource("strings.xml", mapOf("app_name" to 42))
        stringsXml[-1]
    }

    @Test(expected = IllegalArgumentException::class)
    fun givenInvalidStringsXml_whenLoaded_thenShouldThrowException() {
        loadStringsFromResource("strings_invalid.xml",
            mapOf("app_name" to 42))
    }

    @Test
    fun givenValidStringsXmlWithFallback_whenMessageIdNotInStringsButInFallacbk_thenShouldReturnFallbackValue() {
        val idMap = mapOf("app_name" to 42, "other" to 43)

        val stringsXmlEn = loadStringsFromResource("strings.xml", idMap)
        val stringsXmlForeign = loadStringsFromResource("strings_foreign.xml", idMap,
            fallback = stringsXmlEn)

        Assert.assertEquals("Foreign value comes from foreign string",
            stringsXmlForeign[43], "something else")
        Assert.assertEquals("Foreign value missing comes from fallback",
            stringsXmlForeign[42], "Ustad Mobile")
    }
}