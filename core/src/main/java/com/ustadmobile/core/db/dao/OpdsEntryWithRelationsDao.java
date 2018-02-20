package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelationsAndContainerMimeType;

import java.util.List;

/**
 * Created by mike on 1/15/18.
 */

public abstract class OpdsEntryWithRelationsDao {

    @UmQuery("SELECT * from OpdsEntry WHERE url = :url")
    public abstract UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String entryUuid,
                                                             OpdsEntry.OpdsItemLoadCallback callback);

    public UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String entryUuid) {
        return getEntryByUrl(url, entryUuid, null);
    }

    public UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url) {
        return getEntryByUrl(url, null, null);
    }

    public abstract OpdsEntryWithRelations getEntryByUrlStatic(String url);

    @UmQuery("SELECT * from OpdsEntry INNER JOIN OpdsEntryToParentOpdsEntry on OpdsEntry.uuid = OpdsEntry.uuid WHERE OpdsEntryToParentOpdsEntry.parentEntry = :parentId")
    public abstract UmProvider<OpdsEntryWithRelations> getEntriesByParent(String parentId);

    @UmQuery("SELECT * from OpdsEntry INNER JOIN OpdsEntryToParentOpdsEntry on OpdsEntry.uuid = OpdsEntry.uuid WHERE OpdsEntryToParentOpdsEntry.parentEntry = :parentId")
    public abstract UmLiveData<List<OpdsEntryWithRelations>> getEntriesByParentAsList(String parentId);

    @UmQuery("SELECT * from OpdsEntry INNER JOIN OpdsEntryToParentOpdsEntry on OpdsEntry.uuid = OpdsEntry.uuid WHERE OpdsEntryToParentOpdsEntry.parentEntry = :parentId")
    public abstract List<OpdsEntryWithRelations> getEntriesByParentAsListStatic(String parentId);

    @UmQuery("SELECT * from OpdsEntry where uuid = :uuid")
    public abstract UmLiveData<OpdsEntryWithRelations> getEntryByUuid(String uuid);

    @UmQuery("SELECT * from OpdsEntry where uuid = :uuid")
    public abstract OpdsEntryWithRelations getEntryByUuidStatic(String uuid);


    @UmQuery("SELECT uuid FROM OpdsEntry WHERE url = :url")
    public abstract String getUuidForEntryUrl(String url);

    protected static final String findEntriesByContainerFileDirectorySql ="SELECT * FROM OpdsEntry " +
            "LEFT JOIN ContainerFileEntry on OpdsEntry.uuid = ContainerFileEntry.opdsEntryUuid " +
            "LEFT JOIN ContainerFile on ContainerFileEntry.containerFileId = ContainerFile.id " +
            "WHERE ContainerFile.dirPath IN (:dirList)";

    @UmQuery(findEntriesByContainerFileDirectorySql)
    public abstract UmLiveData<List<OpdsEntryWithRelations>> findEntriesByContainerFileDirectoryAsList(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback);

    @UmQuery(findEntriesByContainerFileDirectorySql)
    public abstract UmProvider<OpdsEntryWithRelations> findEntriesByContainerFileDirectoryAsProvider(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback);

    protected static final String findEntriesByContainerFileSql = "SELECT * FROM OpdsEntry " +
            "LEFT JOIN ContainerFileEntry on OpdsEntry.uuid = ContainerFileEntry.opdsEntryUuid " +
            "LEFT JOIN ContainerFile on ContainerFileEntry.containerFileId = ContainerFile.id " +
            "WHERE ContainerFile.normalizedPath = :normalizedPath";

    @UmQuery(findEntriesByContainerFileSql)
    public abstract List<OpdsEntryWithRelations> findEntriesByContainerFileNormalizedPath(String normalizedPath);


    /**
     * Convenience method used by the download task when it needs to resolve a relative link. When
     * The OpdsEntry is a child entry loads from a feed it will not itself have a URL. We thus need
     * the url of the parent.
     *
     * Note : Technically an entry can have multiple parents. Entries loaded from an Opds feed will
     * not have that. Thus the first parent in the list will be the feed for the entry from which it
     * was loaded.
     *
     * @param childUuid Uuid of the child entry, for which we want to find the parent's url
     *
     * @return The url of the (first) parent entry.
     */
    @UmQuery("SELECT OpdsEntry.url FROM OpdsEntryParentToChildJoin " +
            " LEFT JOIN OpdsEntry ON OpdsEntryParentToChildJoin.parentEntry = OpdsEntry.uuid " +
            " WHERE OpdsEntryParentToChildJoin.childEntry = :childUuid")
    public abstract String findParentUrlByChildUuid(String childUuid);

    /**
     * Method to delete a given list of entries and any links that are associated with them
     */
    public long deleteEntriesWithRelationsByUuids(List<String> entryUuids){
        deleteLinksByOpdsEntryUuids(entryUuids);
        return deleteOpdsEntriesByUuids(entryUuids);
    }

    @UmQuery("DELETE FROM OpdsEntry WHERE uuid in (:entryUuids)")
    public abstract int deleteOpdsEntriesByUuids(List<String> entryUuids);


    @UmQuery("DELETE FROM OpdsLink WHERE entryUuid in (:entryUuids)")
    public abstract int deleteLinksByOpdsEntryUuids(List<String> entryUuids);

    @UmQuery("SELECT * FROM OpdsEntryWithRelations WHERE uuid in (:uuids)")
    public abstract List<OpdsEntryWithRelationsAndContainerMimeType> findByUuidsWithContainerMimeType(List<String> uuids);


}
