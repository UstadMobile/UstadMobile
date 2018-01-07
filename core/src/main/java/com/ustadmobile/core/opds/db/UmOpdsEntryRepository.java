package com.ustadmobile.core.opds.db;

import com.ustadmobile.core.impl.UmLiveData;
import com.ustadmobile.core.opds2.entities.OpdsEntry;

import java.util.List;

/**
 * Created by mike on 1/6/18.
 */

public interface UmOpdsEntryRepository {

    UmLiveData<List<OpdsEntry>> findEntriesInFeed(int feedId);

}
