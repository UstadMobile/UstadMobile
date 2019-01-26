package com.ustadmobile.core.contentformats.epub.nav;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class TestEpubNavDocument {

    @Test
    public void givenValidDoc_whenParsed_thenPropertiesShouldMatchFile() throws
            XmlPullParserException, IOException {
        EpubNavDocument navDoc = new EpubNavDocument();
        InputStream docIn = getClass().getResourceAsStream("TestEPUBNavDocument-valid.xhtml");

        navDoc.load(
                UstadMobileSystemImpl.getInstance().newPullParser(docIn, "UTF-8"));

        Assert.assertNotNull("Navigation doc has found table of contents", navDoc.getToc());
        Assert.assertEquals("Navigation doc has 7 children", 7,
                navDoc.getToc().size());
    }

}
