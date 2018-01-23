package com.ustadmobile.core.fs.db.repository;

import com.ustadmobile.core.db.DbManager;
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
import java.util.UUID;

/**
 * Created by mike on 1/13/18.
 */
public class OpdsItemLoader implements Runnable, OpdsEntry.OpdsItemLoadCallback {

    private DbManager dbManager;

    private OpdsEntry itemToLoad;

    private String url;

    private Object context;

    long feedId = -1;

    public OpdsItemLoader(Object context, DbManager dbManager, OpdsEntry itemToLoad, String url) {
        this.dbManager = dbManager;
        this.itemToLoad = itemToLoad;
        this.url = url;
        this.context = context;
    }

    @Override
    public void run() {
        InputStream requestIn;
        UmHttpRequest request = new UmHttpRequest(context, url);
        UmHttpResponse response = null;
        try {
            response = UstadMobileSystemImpl.getInstance().makeRequestSync(request);
            requestIn = response.getResponseAsStream();
            XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(requestIn ,"UTF-8");
            itemToLoad.setUrl(url);
            itemToLoad.setId(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));
            //persist to the database so that items are correctly linked
            dbManager.getOpdsEntryDao().insert((OpdsEntry) itemToLoad);

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

        dbManager.getOpdsEntryDao().insert((OpdsEntry)itemToLoad);
        persistFeedLinks();

    }

    @Override
    public void onError(OpdsEntry item, Throwable cause) {

    }

    @Override
    public void onEntryAdded(OpdsEntryWithRelations entry, OpdsEntry parentFeed, int position) {
        entry.setId(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()));

        if(entry.getLinks() != null) {
            for(OpdsLink link : entry.getLinks()) {
                link.setEntryId(entry.getId());
            }
            dbManager.getOpdsLinkDao().insert(entry.getLinks());
        }
        OpdsEntryParentToChildJoin parentToChild = new OpdsEntryParentToChildJoin(parentFeed.getId(),
                entry.getId(), position);
        dbManager.getOpdsEntryParentToChildJoinDao().insert(parentToChild);
        dbManager.getOpdsEntryDao().insert(entry);
    }

    @Override
    public void onLinkAdded(OpdsLink link, OpdsEntry parentItem, int position) {

    }

    private void persistFeedLinks() {
        OpdsEntryWithRelations itemWithLinks = (OpdsEntryWithRelations) itemToLoad;
        if(itemWithLinks.getLinks() == null)
            return;

        for(OpdsLink link : itemWithLinks.getLinks()) {
            link.setFeedId(itemWithLinks.getId());
        }

        dbManager.getOpdsLinkDao().insert(itemWithLinks.getLinks());
    }

}
