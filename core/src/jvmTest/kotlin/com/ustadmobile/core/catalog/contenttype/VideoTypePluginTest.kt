package com.ustadmobile.core.catalog.contenttype

import org.junit.Assert
import org.junit.Test

class VideoTypePluginTest {

    @Test
    fun givenInvalidVideoRatio_whenValidateCalled_thenShouldReturnNull() {
        Assert.assertNull("Invalid ratio run through validate ratio function returns null",
            VideoTypePlugin.validateRatio("N/A"))
    }

    @Test
    fun givenValidVideoRatio_whenValidateCalled_thenShouldReturnRatio() {
        Assert.assertEquals("Valid ratio returns non-null string", "1:2",
            VideoTypePlugin.validateRatio("1:2"))
    }

}