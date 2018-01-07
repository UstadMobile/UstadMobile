package com.ustadmobile.test.core;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opf.UstadJSOPF;

import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mike on 12/13/17.
 */

public class TestUstadJSOPDSEntry {




    @Test
    public void testNewEntryFromOpf() throws XmlPullParserException, IOException {
        InputStream opfIn = getClass().getResourceAsStream("/com/ustadmobile/test/core/package.opf");
        XmlPullParser parser = UstadMobileSystemImpl.getInstance().newPullParser();
        parser.setInput(opfIn, "UTF-8");
        UstadJSOPF opf = new UstadJSOPF();
        opf.loadFromOPF(parser);

        UstadJSOPDSEntry entry = new UstadJSOPDSEntry(null, opf, "application/epub+zip", null);
        Assert.assertEquals("Entry has expected ID", "202b10fe-b028-4b84-9b84-852aa766607d",
                entry.getItemId());
        Assert.assertEquals("Entry has 2 authors", 2, entry.getNumAuthors());
        Assert.assertEquals("Entry author 1 as expected", "Benita Rowe",
                entry.getAuthor(0).getName());
        Assert.assertEquals("Entry author 2 as expected", "Mike Dawson",
                entry.getAuthor(1).getName());


    }

}
