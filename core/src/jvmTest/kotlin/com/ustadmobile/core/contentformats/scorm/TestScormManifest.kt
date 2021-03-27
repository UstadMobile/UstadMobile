package com.ustadmobile.core.contentformats.scorm


import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.xmlpullparserkmp.XmlPullParserConstants
import org.junit.Assert
import org.junit.Test
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Created by mike on 1/6/18.
 */

class TestScormManifest {

    @Test
    fun testParseScormManifest() {
        val scormManifest = ScormManifest()
        val xpp = XmlPullParserFactory.newInstance().newPullParser()
        val inStream = javaClass.getResourceAsStream(SCORM_MANIFEST_RESOURCE)
        xpp.setFeature(XmlPullParserConstants.FEATURE_PROCESS_NAMESPACES, true)
        xpp.setInput(inStream, "UTF-8")
        scormManifest.loadFromXpp(xpp)
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
