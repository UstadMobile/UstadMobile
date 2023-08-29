package com.ustadmobile.core.impl.locale

import com.ustadmobile.core.generated.locale.MessageIdMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.APPCONFIG_PROPERTIES_PATH
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import org.junit.Assert
import org.junit.Test
import org.xmlpull.v1.XmlPullParserFactory
import java.util.*

class StringsXmlTest {

    private fun loadStringsFromResource(resName: String, idMap: Map<String, Int>,
                                        fallback: StringsXml? = null) : StringsXml{
        val xppFactory = XmlPullParserFactory.newInstance().also {
            it.isNamespaceAware = true
            it.isValidating = false
        }

        return this::class.java.getStringsXmlResource(resName, xppFactory, idMap,
            fallback)
    }

    private val messageIdMapFlipped: Map<String, Int> by lazy {
        MessageIdMap.idMap.entries.associate { (k, v) -> v to k }
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

    /**
     * This is included to ensure that all strings xml files used are valid and will load
     */
    @Test
    fun loadRealStringsXml() {
        val map = messageIdMapFlipped
        val defaultStringsXml = loadStringsFromResource("/values/strings_ui.xml",
            map)
        Assert.assertNotNull(defaultStringsXml)
        val supportedLanguagesConfig = SupportedLanguagesConfig()

        val locales = supportedLanguagesConfig.availableLanguagesConfig
        locales.split(",").filter { it != "en" }.forEach { locale ->
            try {
                val stringsXml = loadStringsFromResource("/values-$locale/strings.xml", map,
                    defaultStringsXml)
                Assert.assertNotNull(stringsXml)
            }catch(e: Exception) {
                throw IllegalStateException("Exception loading locale $locale", e)
            }
        }

    }
}