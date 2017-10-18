package com.ustadmobile.ustadmobile.port.sharedse.utillib;

import com.ustadmobile.core.catalog.DirectoryScanner;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mike on 10/17/17.
 */

public class DirectoryOpdsGenerator {


    public static void main(String args[]) {
        String inDir = args[0];
        String outOpds = args[1];

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        UstadJSOPDSFeed feed = new UstadJSOPDSFeed();

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.scanDirectory(args[0], null, "scan", "scan", CatalogPresenter.SHARED_RESOURCE,
                null, feed, new Object());

        File outFile = new File(outOpds);
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(outFile);
        }catch(IOException e) {

        }
    }
}
