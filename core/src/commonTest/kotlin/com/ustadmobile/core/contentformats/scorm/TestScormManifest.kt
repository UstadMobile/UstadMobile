package com.ustadmobile.core.contentformats.scorm


import kotlinx.io.IOException
import org.junit.Assert
import org.junit.Test
import org.xmlpull.v1.XmlPullParserException

import java.io.IOException

/**
 * Created by mike on 1/6/18.
 */

class TestScormManifest {

    @Test
    @Throws(IOException::class, XmlPullParserException::class)
    fun testParseScormManifest() {
        val scormManifest = ScormManifest()
        scormManifest.loadFromInputStream(javaClass.getResourceAsStream(SCORM_MANIFEST_RESOURCE))
        Assert.assertEquals("Loaded identifier", "com.scorm.manifesttemplates.scorm12",
                scormManifest.identifier)
        Assert.assertEquals("Got default organization", "B0",
                scormManifest.defaultOrganizationIdentifier)
        val organization = scormManifest.getOrganizationByIdentifier(
                scormManifest.defaultOrganizationIdentifier)
        Assert.assertNotNull("Default organization not null", organization)
        Assert.assertEquals("Got organization title", "Title", organization.title)
        val orgItem = organization.getItems()[0]
        Assert.assertEquals("Org item has identifier", "i1", orgItem.identifier)
        Assert.assertEquals("Org item has identifierref", "r1", orgItem.identifierRef)
        Assert.assertEquals("Org item has title", "Title", orgItem.title)

        val mainResource = scormManifest.getResourceByIdentifier(orgItem.identifierRef!!)
        Assert.assertEquals("Main resource href is as expected", "index.html",
                mainResource.href)
    }

    companion object {

        internal val SCORM_MANIFEST_RESOURCE = "/com/ustadmobile/test/core/scorm12manifest.xml"

    }
}
