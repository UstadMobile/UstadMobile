package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.NetworkTaskStatus;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryRelative;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelationsAndContainerMimeType;
import com.ustadmobile.lib.db.entities.OpdsEntryWithStatusCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 1/15/18.
 */
@UmDao
public abstract class OpdsEntryWithRelationsDao {


    //TODO: refactor this to remove unused parameters
    public UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String entryUuid,
                                                             OpdsEntry.OpdsItemLoadCallback callback) {
        return getEntryByUrl(url);
    }


    @UmQuery("SELECT * From OpdsEntry Where url = :url")
    public abstract UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url);


    @UmQuery("Select * from OpdsEntry WHERE url = :url")
    public abstract OpdsEntryWithRelations getEntryByUrlStatic(String url);




    @UmQuery("Select * From OpdsEntry WHERE uuid = :uuid")
    public abstract OpdsEntryWithRelations getEntryByUuidStatic(String uuid);



    @UmQuery("SELECT OpdsEntry.uuid FROM OpdsEntry " +
            "LEFT JOIN OpdsEntryParentToChildJoin ON OpdsEntryParentToChildJoin.childEntry = OpdsEntry.uuid " +
            "LEFT JOIN OpdsEntry OpdsEntryParent ON OpdsEntryParentToChildJoin.parentEntry = OpdsEntryParent.uuid " +
            "WHERE OpdsEntry.entryId = :entryId AND OpdsEntryParent.url = :parentUrl ")
    public abstract String findUuidByEntryIdAndParentUrl(String entryId, String parentUrl);


    protected static final String findEntriesByContainerFileDirectorySql ="SELECT * FROM OpdsEntry " +
            "LEFT JOIN ContainerFileEntry on OpdsEntry.uuid = ContainerFileEntry.opdsEntryUuid " +
            "LEFT JOIN ContainerFile on ContainerFileEntry.containerFileId = ContainerFile.id " +
            "LEFT JOIN OpdsEntryStatusCache on OpdsEntry.entryId = OpdsEntryStatusCache.statusEntryId " +
            "WHERE ContainerFile.dirPath IN (:dirList)";

    @UmQuery(findEntriesByContainerFileDirectorySql)
    public abstract UmLiveData<List<OpdsEntryWithStatusCache>> findEntriesByContainerFileDirectoryAsList(
            List<String> dirList);

    //TODO: remove unused parameter
    public UmLiveData<List<OpdsEntryWithStatusCache>> findEntriesByContainerFileDirectoryAsList(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback) {
        return findEntriesByContainerFileDirectoryAsList(dirList);
    }

    @UmQuery(findEntriesByContainerFileDirectorySql)
    public abstract UmProvider<OpdsEntryWithStatusCache> findEntriesByContainerFileDirectoryAsProvider(
            List<String> dirList);

    //TODO: this method should not be called as the callback is never really used
    @Deprecated
    public UmProvider<OpdsEntryWithStatusCache> findEntriesByContainerFileDirectoryAsProvider(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback) {
        return findEntriesByContainerFileDirectoryAsProvider(dirList);
    }

    protected static final String findEntriesByContainerFileSql = "SELECT * FROM OpdsEntry " +
            "LEFT JOIN ContainerFileEntry on OpdsEntry.uuid = ContainerFileEntry.opdsEntryUuid " +
            "LEFT JOIN ContainerFile on ContainerFileEntry.containerFileId = ContainerFile.id " +
            "WHERE ContainerFile.normalizedPath = :normalizedPath";

    @UmQuery(findEntriesByContainerFileSql)
    public abstract List<OpdsEntryWithRelations> findEntriesByContainerFileNormalizedPath(String normalizedPath);



    @UmQuery("SELECT OpdsEntry.*, ContainerFile.mimeType as containerMimeType FROM OpdsEntry " +
            "LEFT JOIN ContainerFileEntry on OpdsEntry.entryId = ContainerFileEntry.containerEntryId " +
            "LEFT JOIN ContainerFile on ContainerFileEntry.containerFileId = ContainerFile.id " +
            "WHERE OpdsEntry.uuid in (:uuids)")
    public abstract List<OpdsEntryWithRelationsAndContainerMimeType> findByUuidsWithContainerMimeType(List<String> uuids);

    @UmQuery("SELECT uuid From OpdsEntry WHERE entryId = :entryId")
    public abstract List<String> getUuidsForEntryId(String entryId);



}
