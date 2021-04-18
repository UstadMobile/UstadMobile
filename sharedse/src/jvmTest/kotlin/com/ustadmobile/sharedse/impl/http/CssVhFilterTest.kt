package com.ustadmobile.sharedse.impl.http

import org.mockito.kotlin.mock
import com.ustadmobile.port.sharedse.impl.http.CssVhFilter
import fi.iki.elonen.NanoHTTPD
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream

class CssVhFilterTest {


    private fun runCssFilter(cssText: String): String {
        val mockResponseIn = mock<NanoHTTPD.Response> {
            on { mimeType }.thenReturn("text/css")
            on { data }.thenReturn(ByteArrayInputStream(cssText.toByteArray()))
        }

        val vhFilter = CssVhFilter({2F})
        val filteredResponse = vhFilter.filterResponse(mockResponseIn, mock {}, mapOf(),
                mock {})

        return filteredResponse.data.bufferedReader().readText()
    }

    @Test
    fun givenCssWithVh_whenFiltered_thenShouldConvertToPx() {
        val filteredResponseText = runCssFilter("""
            .class {
                max-height: 70vh;
                border: 1px solid red;
            }
        """.trimIndent())

        Assert.assertTrue("CSS text reformatted replaced vh with px unit",
                filteredResponseText.contains("140.0px"))

    }

    @Test
    fun givenCssWithNoVh_whenFiltered_thenShouldNotChange() {
        val cssText = """
            .class {
                max-height: 2000px;
                border: 1px solid red;
            }
        """.trimIndent()
        val filteredResponseText = runCssFilter(cssText)

        Assert.assertEquals("CSS text with no vh will not be changed by filter", cssText,
            filteredResponseText)

    }

    @Test
    fun givenCssWithVhLiteral_whenFiltered_thenShouldNotChange() {
        val cssText = """
            .vh {
                max-height: 500px;
                background: url('somevh.png');
            }
        """.trimIndent()

        val filteredResponseText = runCssFilter(cssText)

        Assert.assertEquals("CSS text with no vh will not be changed by filter", cssText,
                filteredResponseText)
    }

}