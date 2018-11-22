package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.lib.contentscrapers.edraakK12.EdraakK12ContentScraper;
import com.ustadmobile.lib.contentscrapers.edraakK12.IndexEdraakK12Content;

import org.junit.Test;

import java.io.IOException;

public class TmpMainDebug {

    @Test
    public void testMain()  {
        if(System.getProperty("convertUrl") != null && System.getProperty("convertDir") != null){
            EdraakK12ContentScraper.main(new String[]{System.getProperty("convertUrl"), System.getProperty("convertDir")});
        }
    }

    @Test
    public void testIndexMain() throws IOException {

        if (System.getProperty("findUrl") != null && System.getProperty("findDir") != null) {
            IndexEdraakK12Content.main(new String[]{System.getProperty("findUrl"), System.getProperty("findDir")});
        }
    }

}
