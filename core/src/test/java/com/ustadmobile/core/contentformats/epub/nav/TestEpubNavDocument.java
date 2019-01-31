package com.ustadmobile.core.contentformats.epub.nav;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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


    @Test
    public void givenDocLoaded_whenSerializedAndReloaded_thenShouldBeTheSame() throws XmlPullParserException,
            IOException{
        EpubNavDocument navDoc = new EpubNavDocument();
        InputStream docIn = getClass().getResourceAsStream("TestEPUBNavDocument-valid.xhtml");
        navDoc.load(
                UstadMobileSystemImpl.getInstance().newPullParser(docIn, "UTF-8"));

        XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        serializer.setOutput(bout, "UTF-8");
        navDoc.serialize(serializer);
        bout.flush();


        EpubNavDocument loadedDoc = new EpubNavDocument();
        XmlPullParser loadedXpp = UstadMobileSystemImpl.getInstance().newPullParser(
                new ByteArrayInputStream(bout.toByteArray()), "UTF-8");
        loadedDoc.load(loadedXpp);

        Assert.assertEquals("Loaded and reserialized docs have same toc id",
                navDoc.getToc().getId(), loadedDoc.getToc().getId());
        Assert.assertEquals("Loaded and reserialized tocs have same number of child entries",
                navDoc.getToc().size(), loadedDoc.getToc().size());
    }

}
