package com.ustadmobile.core.fs.db.repository;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImplFs;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by mike on 1/13/18.
 */
public class OpdsItemLoader implements Runnable, OpdsEntry.OpdsItemLoadCallback {

    private UmAppDatabase dbManager;

    private OpdsEntryWithRelations itemToLoad;

    private String url;

    private Object context;

    private OpdsEntry.OpdsItemLoadCallback callback;

    long feedId = -1;

    //The OpdsEntry parent of this feed, as it was before we loaded this entry.
    private OpdsEntryWithRelations existingEntry;

    private boolean itemToLoadInserted = false;


    public OpdsItemLoader(Object context, UmAppDatabase dbManager, OpdsEntryWithRelations itemToLoad, String url,
                          OpdsEntry.OpdsItemLoadCallback callback) {
        this.dbManager = dbManager;
        this.itemToLoad = itemToLoad;
        this.url = url;
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void run() {
        InputStream requestIn;
        UmHttpRequest request = new UmHttpRequest(context, url);
        UmHttpResponse response = null;
        try {
            existingEntry = dbManager.getOpdsEntryWithRelationsDao().getEntryByUrlStatic(url);

            if(request.getUrl().startsWith("asset:///")) {
                UstadMobileSystemImplFs implFs = (UstadMobileSystemImplFs)UstadMobileSystemImpl.getInstance();
                requestIn = implFs.getAssetSync(context, url.substring("asset:///".length()));
            }else {
                response = UstadMobileSystemImpl.getInstance().makeRequestSync(request);
                requestIn = response.getResponseAsStream();
            }

            XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(requestIn ,"UTF-8");
            itemToLoad.setUrl(url);
            if(itemToLoad.getUuid() == null) {
                itemToLoad.setUuid((existingEntry != null && existingEntry.getUuid() != null) ?
                        existingEntry.getUuid() : UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));
            }

            itemToLoad.load(xpp, this);
        }catch(IOException e) {
            e.printStackTrace();
        }catch(XmlPullParserException x) {
            x.printStackTrace();
        }
    }

    @Override
    public void onDone(OpdsEntry item) {
        //commit the item itself to the database
        dbManager.getOpdsEntryDao().insert(itemToLoad);
        persistFeedLinks();
        if(callback != null) {
            callback.onDone(item);
        }
    }

    @Override
    public void onError(OpdsEntry item, Throwable cause) {
        if(callback != null)
            callback.onError(item, cause);
    }

    @Override
    public void onEntryAdded(OpdsEntryWithRelations entry, OpdsEntry parentFeed, int position) {
        if(!itemToLoadInserted)
            insertItemToLoad();


        String previousEntryUuid = dbManager.getOpdsEntryWithRelationsDao()
                .findUuidByEntryIdAndParentUrl(entry.getEntryId(), url);

        entry.setUuid(previousEntryUuid != null ? previousEntryUuid:
                UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));

        if(entry.getLinks() != null) {
            for(OpdsLink link : entry.getLinks()) {
                link.setEntryUuid(entry.getUuid());
            }
            dbManager.getOpdsLinkDao().insert(entry.getLinks());
        }
        OpdsEntryParentToChildJoin parentToChild = new OpdsEntryParentToChildJoin(parentFeed.getUuid(),
                entry.getUuid(), position);
        dbManager.getOpdsEntryParentToChildJoinDao().insert(parentToChild);
        dbManager.getOpdsEntryDao().insert(entry);

        dbManager.getOpdsEntryStatusCacheDao().handleOpdsEntriesLoaded(dbManager,
                Arrays.asList(entry));

        if(callback != null)
            callback.onEntryAdded(entry, parentFeed, position);
    }

    @Override
    public void onLinkAdded(OpdsLink link, OpdsEntry parentItem, int position) {
        if(callback != null)
            callback.onLinkAdded(link, parentItem, position);
    }

    private void insertItemToLoad() {
        dbManager.getOpdsEntryDao().insert(itemToLoad);
        dbManager.getOpdsEntryStatusCacheDao().handleOpdsEntriesLoaded(dbManager,
                Arrays.asList(itemToLoad));
        itemToLoadInserted = true;
    }

    private void persistFeedLinks() {
        OpdsEntryWithRelations itemWithLinks = itemToLoad;
        if(itemWithLinks.getLinks() == null)
            return;

        for(OpdsLink link : itemWithLinks.getLinks()) {
            link.setEntryUuid(itemWithLinks.getUuid());
        }

        dbManager.getOpdsLinkDao().insert(itemWithLinks.getLinks());
    }

}
