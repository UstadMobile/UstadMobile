package com.ustadmobile.port.android.impl;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;

import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.AcquisitionStatusListener;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.util.UMFileUtil;

import java.io.File;
import java.util.Vector;

/**
 * Created by mike on 4/19/17.
 */

public class AcquisitionManagerAndroid extends AcquisitionManager {

    @Override
    public void acquireCatalogEntries(UstadJSOPDSFeed acquireFeed, Object context) {
        Vector downloadDestVector = acquireFeed.getLinks(AcquisitionManager.LINK_REL_DOWNLOAD_DESTINATION,
                null);
        if(downloadDestVector.isEmpty()) {
            throw new IllegalArgumentException("No download destination in acquisition feed for acquireCatalogEntries");
        }
        File downloadDestDir = new File(((String[])downloadDestVector.get(0))[UstadJSOPDSEntry.LINK_HREF]);


        String[] selfLink = acquireFeed.getSelfLink();
        if(selfLink == null)
            throw new IllegalArgumentException("No self link on feed - required to resolve links");

        String feedHref = selfLink[UstadJSOPDSEntry.LINK_HREF];

        Context aContext = (Context)context;
        DownloadManager manager = (DownloadManager)aContext.getSystemService(Context.DOWNLOAD_SERVICE);

        for(int i = 0; i < acquireFeed.entries.length; i++) {
            String downloadUrl = UMFileUtil.resolveLink(feedHref,
                acquireFeed.entries[i].getFirstAcquisitionLink(null)[UstadJSOPDSEntry.LINK_HREF]);
            String fileName = UMFileUtil.getFilename(downloadUrl);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            File destFile = new File(downloadDestDir, fileName);
            request.setDestinationUri(Uri.fromFile(destFile));
            manager.enqueue(request);
        }
    }

    @Override
    public int[] getEntryStatusById(String entryId, Object context) {
        return new int[0];
    }

    @Override
    public void registerEntryAquisitionStatusListener(AcquisitionStatusListener listener) {

    }

    @Override
    public void unregisterEntryAquisitionStatusListener(AcquisitionStatusListener listener) {

    }
}
