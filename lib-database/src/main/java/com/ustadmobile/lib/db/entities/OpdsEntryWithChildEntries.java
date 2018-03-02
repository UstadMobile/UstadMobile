package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.util.UmUuidUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by mike on 2/20/18.
 */

public class OpdsEntryWithChildEntries extends OpdsEntryWithRelations {

    private List<OpdsEntryWithRelations> childEntries;

    private OpdsItemLoadCallback loadCallback;

    private OpdsEntry.OpdsItemLoadCallback addChildItemLoadCallback = new OpdsItemLoadCallback() {
        @Override
        public void onDone(OpdsEntry item) {
            if(loadCallback != null)
                loadCallback.onDone(item);
        }

        @Override
        public void onEntryAdded(OpdsEntryWithRelations childEntry, OpdsEntry parentFeed, int position) {
            if(childEntry.getUuid() == null)
                childEntry.setUuid(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));

            childEntries.add(childEntry);
            if(loadCallback != null)
                loadCallback.onEntryAdded(childEntry, parentFeed, position);
        }


        @Override
        public void onLinkAdded(OpdsLink link, OpdsEntry parentItem, int position) {
            if(loadCallback != null)
                loadCallback.onLinkAdded(link, parentItem, position);
        }

        @Override
        public void onError(OpdsEntry item, Throwable cause) {
            if(loadCallback != null)
                loadCallback.onError(item, cause);
        }
    };

    public List<OpdsEntryWithRelations> getChildEntries() {
        return childEntries;
    }

    public void setChildEntries(List<OpdsEntryWithRelations> childEntries) {
        this.childEntries = childEntries;
    }

    public OpdsEntry getChildEntryByEntryId(String entryId) {
        if(childEntries == null)
            return null;

        for(OpdsEntryWithRelations entry : childEntries) {
            if(entry == null)
                continue;

            if(entry.getEntryId() != null && entry.getEntryId().equals(entryId))
                return entry;
        }

        return null;
    }

    @Override
    public synchronized void load(XmlPullParser xpp, OpdsItemLoadCallback callback) throws IOException, XmlPullParserException {
        childEntries = new ArrayList<>();
        this.loadCallback = callback;
        super.load(xpp, addChildItemLoadCallback);
    }
}
