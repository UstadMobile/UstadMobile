package com.ustadmobile.core.contentformats.epub.opf;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mike on 10/17/17.
 */

public class TestOpfDocument {

    @Test
    public void givenValidOpf_whenLoaded_thenShouldHavePropertiesFromOpfFile() throws IOException, XmlPullParserException{
        InputStream opfIn = getClass().getResourceAsStream("TestOpfDocument-valid.opf");
        XmlPullParser parser = UstadMobileSystemImpl.getInstance().newPullParser();
        parser.setInput(opfIn, "UTF-8");
        OpfDocument opf = new OpfDocument();
        opf.loadFromOPF(parser);
        Assert.assertEquals("Title as expected", "The Little Chicks", opf.title);
        Assert.assertEquals("Id as expected", "202b10fe-b028-4b84-9b84-852aa766607d", opf.id);
        Assert.assertTrue("Spine loaded", opf.getSpine().size() > 0);
        Assert.assertEquals("Language loaded", "en-US", opf.getLanguages().get(0));
        Assert.assertEquals("Cover image as expected", "cover.png", opf.getCoverImage(null).href);
        Assert.assertEquals("Loaded author 1 as expected", "Benita Rowe",
                opf.getCreator(0).getCreator());
        Assert.assertEquals("Loaded author 1 as expected -id", "author1",
                opf.getCreator(0).getId());
        Assert.assertEquals("Loaded author 2 as expected", "Mike Dawson",
                opf.getCreator(1).getCreator());
        Assert.assertEquals("Loaded mime type as expected for page", "application/xhtml+xml",
                opf.getMimeType("Page_1.xhtml"));
    }

    @Test
    public void givenValidOpf_whenLoaded_thenCanSerialize() throws IOException, XmlPullParserException {
        InputStream opfIn = getClass().getResourceAsStream("TestOpfDocument-valid.opf");
        XmlPullParser parser = UstadMobileSystemImpl.getInstance().newPullParser();
        parser.setInput(opfIn, "UTF-8");
        OpfDocument opf = new OpfDocument();
        opf.loadFromOPF(parser);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
        serializer.setOutput(bout, "UTF-8");
        opf.serialize(serializer);
        bout.flush();
        String xmlStr = new String(bout.toByteArray(), "UTF-8");
        Assert.assertNotNull(xmlStr);
    }

}
