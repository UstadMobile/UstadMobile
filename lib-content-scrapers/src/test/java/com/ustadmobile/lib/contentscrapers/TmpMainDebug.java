package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.lib.contentscrapers.edraakK12.EdraakK12ContentScraper;
import com.ustadmobile.lib.contentscrapers.edraakK12.IndexEdraakK12Content;

import org.junit.Test;

public class TmpMainDebug {

    @Test
    public void testMain()  {
        if(System.getProperty("convertUrl") != null && System.getProperty("convertDest") != null){
            EdraakK12ContentScraper.main(new String[]{System.getProperty("convertUrl"), System.getProperty("convertDest")});
        }
    }

    @Test
    public void testIndexMain() {

        if (System.getProperty("findUrl") != null && System.getProperty("findDest") != null) {
            IndexEdraakK12Content.main(new String[]{System.getProperty("findUrl"), System.getProperty("findDest")});
        }
    }

}
