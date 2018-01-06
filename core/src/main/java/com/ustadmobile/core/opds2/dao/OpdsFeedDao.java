package com.ustadmobile.core.opds2.dao;

import com.ustadmobile.core.impl.UmLiveData;
import com.ustadmobile.core.opds2.entities.OpdsFeed;

/**
 * Created by mike on 1/6/18.
 */

public interface OpdsFeedDao {


    UmLiveData<OpdsFeed> getFeed(String href);



}
