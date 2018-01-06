package com.ustadmobile.core.opds2.repository;

import com.ustadmobile.core.impl.UmLiveData;
import com.ustadmobile.core.opds2.dao.OpdsFeedDao;
import com.ustadmobile.core.opds2.entities.OpdsFeed;

/**
 * Created by mike on 1/6/18.
 */

public class OpdsFeedRepository implements OpdsFeedDao{

    @Override
    public UmLiveData<OpdsFeed> getFeed(String href) {
        return null;
    }


    protected UmLiveData<OpdsFeed> loadFeed(String href) {
        return null;
    }
}
