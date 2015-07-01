/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.tests;

import com.ustadmobile.app.controller.UstadMobileAppController;
import com.ustadmobile.controller.CatalogController;
import com.ustadmobile.model.CatalogModel;
import com.ustadmobile.opds.UstadJSOPDSFeed;
import j2meunit.framework.TestCase;
import java.io.IOException;
import java.io.InputStream;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author varuna
 */
public class TestCatalogView extends TestCase {
    public TestCatalogView(){
        setName("CatalogView Test");
    }
    
    public void runTest() throws Throwable{
        assertEquals("Simple Test OK", 2, 1+1);
        
        String fileURI = "/com/ustadmobile/app/tests/" + 
                TestUtils.testSettings.get("opdsxml").toString();
        InputStream bais = getClass().getResourceAsStream(
                fileURI);       
        KXmlParser parser = new KXmlParser();
        parser = (KXmlParser) UstadMobileAppController.parseXml(bais);
        UstadJSOPDSFeed feed = UstadJSOPDSFeed.loadFromXML(parser);
        
        CatalogModel feedModel = new CatalogModel(feed);
        CatalogController catalogController = new CatalogController(feedModel);
        
        com.sun.lwuit.Display.getInstance().callSeriallyAndWait(new Runnable() {
            public void run() {
                try {
                    String fileURI = "/com/ustadmobile/app/tests/" + 
                    TestUtils.testSettings.get("opdsxml").toString();
                    InputStream bais = getClass().getResourceAsStream(
                            fileURI);       
                    KXmlParser parser = new KXmlParser();
                    parser = (KXmlParser) UstadMobileAppController.parseXml(bais);
                    UstadJSOPDSFeed feed = UstadJSOPDSFeed.loadFromXML(parser);

                    CatalogModel feedModel = new CatalogModel(feed);
                    CatalogController catalogController = new CatalogController(feedModel);
                    catalogController.show();
                } catch (XmlPullParserException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        assertTrue("Completed first callSerially and wait", true);
        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}
         
    }
}
