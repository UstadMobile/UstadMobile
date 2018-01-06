package com.ustadmobile.ustadmobile.port.sharedse.utillib;

import com.ustadmobile.core.catalog.DirectoryScanner;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mike on 10/17/17.
 */

public class DirectoryOpdsGenerator {


    public static void main(String args[]) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        String inDir = args[0];
        File inDirFile = new File(inDir);

        UstadJSOPDSFeed feed = new UstadJSOPDSFeed();

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.scanDirectory(inDir, inDir, "scan", "scan", CatalogPresenter.SHARED_RESOURCE,
                null, feed, new Object());


        feed.sortEntries(new UMUtil.Comparer() {
            @Override
            public int compare(Object o1, Object o2) {
                UstadJSOPDSEntry e1 = (UstadJSOPDSEntry)o1;
                UstadJSOPDSEntry e2 = (UstadJSOPDSEntry)o2;

                String e1AcquireLink = e1.getFirstAcquisitionLink(null).getHref();
                String e2AcquireLink = e2.getFirstAcquisitionLink(null).getHref();

                return e1AcquireLink.compareTo(e2AcquireLink);
            }
        });


        UstadJSOPDSEntry entry;
        for(int i = 0; i < feed.size(); i++) {
            entry = feed.getEntry(i);
            String feedAcquireLink = entry.getFirstAcquisitionLink(null).getHref();
            String baseName = UMFileUtil.getFilename(feedAcquireLink);
            String entryFilename = baseName + ".entry.opds";
            UstadJSOPDSEntry itemEntry = new UstadJSOPDSEntry(null, entry);

            FileOutputStream entryOut = null;
            try {
                entryOut = new FileOutputStream(new File(inDirFile, entryFilename));
                entryOut.write(itemEntry.serializeToString(false, true).getBytes("UTF-8"));
                entryOut.flush();
            }catch(IOException e) {
                e.printStackTrace();
            }finally {
                UMIOUtils.closeOutputStream(entryOut);
            }

        }

        File outFile = new File(inDirFile, "index.opds");
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(outFile);
            fout.write(feed.serializeToString(false, true).getBytes("UTF-8"));
            fout.flush();
        }catch(IOException e) {

        }finally {
            UMIOUtils.closeOutputStream(fout);
        }
    }
}
