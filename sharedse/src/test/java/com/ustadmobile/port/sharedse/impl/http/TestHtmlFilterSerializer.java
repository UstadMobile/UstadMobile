package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.port.sharedse.impl.http.EpubHtmlFilterSerializer;

import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mike on 11/4/17.
 */

public class TestHtmlFilterSerializer {

    @Test
    public void testSerializer() throws IOException, XmlPullParserException{
        EpubHtmlFilterSerializer serializer = new EpubHtmlFilterSerializer();
        InputStream in = getClass().getResourceAsStream("/com/ustadmobile/port/sharedse/epub-page.html");
        serializer.setIntput(in);
        serializer.setScriptSrcToAdd("/path/to/script");
        byte[] filteredInput = serializer.getOutput();
        String filteredStr = new String(filteredInput, "UTF-8");
        Assert.assertNotNull(filteredStr);
    }
}
