package com.ustadmobile.test.core.impl;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opf.UstadJSOPF;

import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mike on 10/17/17.
 */

public class TestUstadJSOPF {

    @Test
    public void testOpfParse() throws IOException, XmlPullParserException{
        InputStream opfIn = getClass().getResourceAsStream("/com/ustadmobile/test/core/package.opf");
        XmlPullParser parser = UstadMobileSystemImpl.getInstance().newPullParser();
        parser.setInput(opfIn, "UTF-8");
        UstadJSOPF opf = new UstadJSOPF();
        opf.loadFromOPF(parser);
        Assert.assertEquals("Title as expected", "The Little Chicks", opf.title);
        Assert.assertEquals("Id as expected", "202b10fe-b028-4b84-9b84-852aa766607d", opf.id);
        Assert.assertTrue("Spine loaded", opf.spine.length > 0);
        Assert.assertEquals("Language loaded", "en-US", opf.getLanguages().elementAt(0));
        Assert.assertEquals("Cover image as expected", "cover.png", opf.getCoverImage(null).href);
        Assert.assertEquals("Loaded author 1 as expected", "Benita Rowe",
                opf.getCreator(0).getCreator());
        Assert.assertEquals("Loaded author 1 as expected -id", "author1",
                opf.getCreator(0).getId());
        Assert.assertEquals("Loaded author 2 as expected", "Mike Dawson",
                opf.getCreator(1).getCreator());
    }

}
