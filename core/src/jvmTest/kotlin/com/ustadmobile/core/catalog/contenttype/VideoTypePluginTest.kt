package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.util.ShrinkUtils
import org.junit.Assert
import org.junit.Test

class VideoTypePluginTest {

    @Test
    fun givenInvalidVideoRatio_whenValidateCalled_thenShouldReturnNull() {
        Assert.assertNull("Invalid ratio run through validate ratio function returns null",
            ShrinkUtils.validateRatio("N/A"))
    }

    @Test
    fun givenValidVideoRatio_whenValidateCalled_thenShouldReturnRatio() {
        Assert.assertEquals("Valid ratio returns non-null string", "1:2",
                ShrinkUtils.validateRatio("1:2"))
    }

}