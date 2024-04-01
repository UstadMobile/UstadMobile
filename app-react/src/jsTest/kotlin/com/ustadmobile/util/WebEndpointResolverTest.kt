package com.ustadmobile.util

import js.array.tupleOf
import web.url.URLSearchParams
import kotlin.test.Test
import kotlin.test.assertEquals

class WebEndpointResolverTest {

    @Test
    fun givenApiUrlInSearchParams_whenEndpointResolved_thenShouldReturnSearchParam() {
        val apiUrl = resolveEndpoint("http://localhost:8087/",
            URLSearchParams(arrayOf(tupleOf(SEARCH_PARAM_KEY_API_URL,
                "http://endpoint.ustadmobile.app/"))))
        assertEquals("http://endpoint.ustadmobile.app/", apiUrl)
    }

    @Test
    fun givenRootHref_whenEndpointIsResolved_thenShouldReturnHref() {
        val apiUrl = resolveEndpoint("http://endpoint.ustadmobile.app/",
            URLSearchParams(emptyArray())
        )
        assertEquals("http://endpoint.ustadmobile.app/", apiUrl)
    }

    @Test
    fun givenHrefWithDevelopmentLink_whenEndpointIsResolved_thenShouldReturnHref() {
        val apiUrl = resolveEndpoint("http://endpoint.ustadmobile.app/#/PageName",
            URLSearchParams(emptyArray())
        )
        assertEquals("http://endpoint.ustadmobile.app/", apiUrl)
    }

    @Test
    fun givenHrefWithProductionLink_whenEndpointIsResolved_thenShouldReturnHref() {
        val apiUrl = resolveEndpoint("http://endpoint.ustadmobile.app/umapp/#/PageName",
            URLSearchParams(emptyArray())
        )
        assertEquals("http://endpoint.ustadmobile.app/", apiUrl)
    }

}