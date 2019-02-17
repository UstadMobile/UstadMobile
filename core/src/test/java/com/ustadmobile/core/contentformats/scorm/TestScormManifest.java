package com.ustadmobile.core.contentformats.scorm;

import com.ustadmobile.test.core.ResourcesHttpdTestServer;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by mike on 1/6/18.
 */

public class TestScormManifest {

    static final String SCORM_MANIFEST_RESOURCE = "/com/ustadmobile/test/core/scorm12manifest.xml";

    @BeforeClass
    public static void startHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.startServer();
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.stopServer();
    }

    @Test
    public void testParseScormManifest() throws IOException, XmlPullParserException{
        ScormManifest scormManifest = new ScormManifest();
        scormManifest.loadFromInputStream(getClass().getResourceAsStream(SCORM_MANIFEST_RESOURCE));
        Assert.assertEquals("Loaded identifier", "com.scorm.manifesttemplates.scorm12",
                scormManifest.getIdentifier());
        Assert.assertEquals("Got default organization", "B0",
                scormManifest.getDefaultOrganizationIdentifier());
        ScormManifest.Organization organization = scormManifest.getOrganizationByIdentifier(
                scormManifest.getDefaultOrganizationIdentifier());
        Assert.assertNotNull("Default organization not null", organization);
        Assert.assertEquals("Got organization title", "Title", organization.getTitle());
        ScormManifest.OrganizationItem orgItem = organization.getItems().get(0);
        Assert.assertEquals("Org item has identifier", "i1", orgItem.getIdentifier());
        Assert.assertEquals("Org item has identifierref", "r1", orgItem.getIdentifierRef());
        Assert.assertEquals("Org item has title", "Title", orgItem.getTitle());

        ScormManifest.Resource mainResource = scormManifest.getResourceByIdentifier(orgItem.getIdentifierRef());
        Assert.assertEquals("Main resource href is as expected", "index.html",
                mainResource.getHref());
    }
}
