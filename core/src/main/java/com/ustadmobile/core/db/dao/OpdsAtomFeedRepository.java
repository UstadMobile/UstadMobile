package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntryWithStatusCache;

import java.util.List;

public interface OpdsAtomFeedRepository {

    UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String entryId,
                                                            OpdsEntry.OpdsItemLoadCallback callback);

    UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url);

    UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String uuid);

    OpdsEntryWithRelations getEntryByUrlStatic(String url);

    UmLiveData<List<OpdsEntryWithStatusCache>> findEntriesByContainerFileDirectoryAsList(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback);

    UmProvider<OpdsEntryWithStatusCache> findEntriesByContainerFileDirectoryAsProvider(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback);

    List<OpdsEntryWithRelations> findEntriesByContainerFileNormalizedPath(String containerFilePath);


}
