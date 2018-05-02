package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryRelative;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelationsAndContainerMimeType;
import com.ustadmobile.lib.db.entities.OpdsEntryWithStatusCache;

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

    /**
     * Find OpdsEntryWithRelations by it's primary key (uuid)
     *
     * @param uuid The UUID to find
     *
     * @return The OpdsEntryWithRelations object representing this in the database, or null if it doesn't exist
     */
    public abstract OpdsEntryWithRelations findByUuid(String uuid);


    @UmQuery("SELECT * from OpdsEntry INNER JOIN OpdsEntryToParentOpdsEntry on OpdsEntry.uuid = OpdsEntry.uuid WHERE OpdsEntryToParentOpdsEntry.parentEntry = :parentId")
    public abstract UmProvider<OpdsEntryWithRelations> getEntriesByParent(String parentId);

    @UmQuery("SELECT OpdsEntry.* FROM OpdsEntry INNER JOIN OpdsEntryParentToChildJoin on OpdsEntry.uuid = OpdsEntryParentToChildJoin.childEntry WHERE OpdsEntryParentToChildJoin.parentEntry = :parentId ORDER BY childIndex")
    public abstract UmProvider<OpdsEntryWithStatusCache> getEntriesWithStatusCacheByParent(String parentId);




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

    @UmQuery("SELECT * FROM OpdsEntry WHERE entryId = :entryId LIMIT 1")
    public abstract OpdsEntryWithRelations findFirstByEntryIdStatic(String entryId);

    @UmQuery("SELECT uuid FROM OpdsEntry " +
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
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback);

    @UmQuery(findEntriesByContainerFileDirectorySql)
    public abstract UmProvider<OpdsEntryWithStatusCache> findEntriesByContainerFileDirectoryAsProvider(
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

    @UmQuery("SELECT uuid From OpdsEntry WHERE entryId = :entryId")
    public abstract List<String> getUuidsForEntryId(String entryId);

    protected static final String GET_CHILD_ENTRIES_RECURSIVE_SQL = "WITH RECURSIVE OpdsEntry_recursive(entryId) AS (\n" +
            "\tVALUES(:entryId)\n" +
            "\tUNION\n" +
            "\tSELECT OpdsChildEntry.entryId FROM \n" +
            "\tOpdsEntryParentToChildJoin \n" +
            "\t\tJOIN OpdsEntry OpdsChildEntry ON OpdsEntryParentToChildJoin.childEntry = OpdsChildEntry.uuid\n" +
            "\t\tJOIN OpdsEntry OpdsParentEntry ON OpdsEntryParentToChildJoin.parentEntry = OpdsParentEntry.uuid,\n" +
            "\tOpdsEntry_recursive\n" +
            "\tWHERE\n" +
            "\t\tOpdsParentEntry.entryId = OpdsEntry_recursive.entryId\n" +
            ")\n" +
            "\n" +
            "SELECT entryId FROM OpdsEntry_recursive";
    @UmQuery(GET_CHILD_ENTRIES_RECURSIVE_SQL)
    public abstract List<String> findAllChildEntryIdsRecursive(String entryId);

    public List<OpdsEntryRelative> getAncestors(List<String> entryIds) {
        return getAncestors_RecursiveQuery(entryIds);
    }

    protected static final String GET_ANCESTOR_ENTRIES_RECURSIVE_SQL =
            "WITH RECURSIVE OpdsEntry_recursive(entryId, relativeEntryId, distance) AS ( " +
            "SELECT OpdsEntry.entryId as entryId, OpdsEntry.entryId as relativeEntryId, 0 FROM OpdsEntry WHERE OpdsEntry.entryId IN (:entryIds) " +
            "UNION " +
            "SELECT OpdsParentEntry.entryId AS entryId, OpdsEntry_recursive.relativeEntryId AS relativeEntryId, OpdsEntry_recursive.distance + 1 AS distance FROM " +
            "OpdsEntryParentToChildJoin " +
            "JOIN OpdsEntry OpdsChildEntry ON OpdsEntryParentToChildJoin.childEntry = OpdsChildEntry.uuid " +
            "JOIN OpdsEntry OpdsParentEntry ON OpdsEntryParentToChildJoin.parentEntry = OpdsParentEntry.uuid, " +
            "OpdsEntry_recursive " +
            "WHERE " +
            "OpdsChildEntry.entryId = OpdsEntry_recursive.entryId " +
            ")" +
            "SELECT * FROM OpdsEntry_recursive ";

    @UmQuery(GET_ANCESTOR_ENTRIES_RECURSIVE_SQL)
    public abstract List<OpdsEntryRelative> getAncestors_RecursiveQuery(List<String> entryIds);

    protected static final String GET_DESCENDANT_ENTRIES_RECURSIVE_SQL =
            "WITH RECURSIVE OpdsEntry_recursive(entryId, relativeEntryId, distance) AS ( " +
            "SELECT OpdsEntry.entryId as entryId, OpdsEntry.entryId as relativeEntryId, 0 FROM OpdsEntry WHERE OpdsEntry.entryId IN (:entryIds) "  +
            "UNION " +
            "SELECT " +
            "OpdsEntry_recursive.entryId AS entryId, " +
            "OpdsChildEntry.entryId as relativeEntryId, " +
            "OpdsEntry_recursive.distance + 1 AS distance " +
            "FROM " +
            "OpdsEntryParentToChildJoin " +
            "JOIN OpdsEntry OpdsChildEntry ON OpdsEntryParentToChildJoin.childEntry = OpdsChildEntry.uuid " +
            "JOIN OpdsEntry OpdsParentEntry ON OpdsEntryParentToChildJoin.parentEntry = OpdsParentEntry.uuid, " +
            "OpdsEntry_recursive " +
            "WHERE " +
            "OpdsParentEntry.entryId = OpdsEntry_recursive.relativeEntryId " +
            ") " +
            "SELECT * FROM OpdsEntry_recursive WHERE distance > 0";

    @UmQuery(GET_DESCENDANT_ENTRIES_RECURSIVE_SQL)
    public abstract List<OpdsEntryRelative> getDescendant_RecursiveQuery(List<String> entryIds);

    protected static final String GET_ENTRIES_WITH_DOWNLOADSET_SQL =
            "SELECT OpdsEntry.*, OpdsEntryStatusCache.* FROM OpdsEntry " +
            "JOIN OpdsEntryStatusCache ON OpdsEntry.entryId = OpdsEntryStatusCache.statusEntryId " +
            "JOIN DownloadSet ON OpdsEntry.uuid = DownloadSet.rootOpdsUuid";
    @UmQuery(GET_ENTRIES_WITH_DOWNLOADSET_SQL)
    public abstract UmProvider<OpdsEntryWithStatusCache> getEntriesWithDownloadSet();




}
