package com.ustadmobile.core.util

import org.junit.Test
import kotlin.test.assertEquals

class UstadUrlComponentsTest {

    @Test
    fun givenUrlWithPathPrefixWithNoArgs_whenParsed_thenShouldInterpretCorrectly() {
        val components = UstadUrlComponents.parse("http://server:8087/umapp/#/AViewName")
        assertEquals("http://server:8087/", components.endpoint)
        assertEquals("AViewName", components.viewName)
        assertEquals("", components.queryString)
    }

    @Test
    fun givenUrlWithNoPathPrefixWithNoArgs_whenParsed_thenShouldInterpretCorrectly() {
        val components = UstadUrlComponents.parse("http://server:8087/#/AViewName")
        assertEquals("http://server:8087/", components.endpoint)
        assertEquals("AViewName", components.viewName)
        assertEquals("", components.queryString)
    }

    @Test
    fun givenUrlWithNoPathPrefixWithEmptyArgs_whenParsed_thenShouldInterpretCorrectly() {
        val components = UstadUrlComponents.parse("http://server:8087/#/AViewName?")
        assertEquals("http://server:8087/", components.endpoint)
        assertEquals("AViewName", components.viewName)
        assertEquals("", components.queryString)
    }

    @Test
    fun givenUrlWithPathPrefixWithArgs_whenParsed_thenShouldInterpretCorrectly() {
        val components = UstadUrlComponents.parse("http://server:8087/umapp/#/AViewName?arg1=value1")
        assertEquals("http://server:8087/", components.endpoint)
        assertEquals("AViewName", components.viewName)
        assertEquals("arg1=value1", components.queryString)
    }

}