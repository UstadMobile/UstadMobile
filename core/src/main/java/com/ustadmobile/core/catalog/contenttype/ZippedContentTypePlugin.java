package com.ustadmobile.core.catalog.contenttype;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mike on 10/17/17.
 */

public abstract class ZippedContentTypePlugin extends ContentTypePlugin{

    protected static class ZippedEntryResult implements EntryResult {

        private UstadJSOPDSFeed feed;

        private String containerUri;

        private String thumbnailPathIinZip;

        private String thumbnailMimeType;

        protected ZippedEntryResult(UstadJSOPDSFeed feed, String containerUri, String thumbnailPathInZip,
                                String thumbnailMimeType) {
            this.feed = feed;
            this.containerUri = containerUri;
            this.thumbnailPathIinZip = thumbnailPathInZip;
            this.thumbnailMimeType = thumbnailMimeType;
        }

        @Override
        public UstadJSOPDSFeed getFeed() {
            return feed;
        }

        @Override
        public InputStream getThumbnail() {
            if(thumbnailPathIinZip == null)
                return null;

            InputStream in = null;
            try {
                in = UstadMobileSystemImpl.getInstance().openZip(containerUri).openInputStream(
                        thumbnailPathIinZip);
            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 687, containerUri + "!" + thumbnailPathIinZip,
                        e);
            }

            return in;
        }

        @Override
        public String getThumbnailMimeType() {
            return thumbnailMimeType;
        }
    }

}
