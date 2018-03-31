package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryAncestor;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
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

    @UmQuery("SELECT uuid From OpdsEntry WHERE entryId = :entryId")
    public abstract List<String> getUuidsForEntryId(String entryId);


    public static final String GET_DOWNLOAD_STATUS_SQL = "WITH RECURSIVE OpdsEntry_recursive(entryId) AS (         \n" +
            "\tVALUES(:entryId)         \n" +
            "\tUNION         \n" +
            "\tSELECT OpdsChildEntry.entryId FROM          \n" +
            "\tOpdsEntryParentToChildJoin          \n" +
            "\t\tJOIN OpdsEntry OpdsChildEntry ON OpdsEntryParentToChildJoin.childEntry = OpdsChildEntry.uuid         \n" +
            "\t\tJOIN OpdsEntry OpdsParentEntry ON OpdsEntryParentToChildJoin.parentEntry = OpdsParentEntry.uuid,         \n" +
            "\tOpdsEntry_recursive         \n" +
            "\tWHERE \n" +
            "\tOpdsParentEntry.entryId = OpdsEntry_recursive.entryId         \n" +
            ")         \n" +
            "                     \n" +
            "SELECT \n" +
            "SUM(totalBytesDownloaded) AS totalBytesDownloaded,          \n" +
            "SUM(linkLength) as totalSize,          \n" +
            "SUM(numContainers) AS entriesWithContainer,         \n" +
            "SUM(numContainersDownloaded) AS containersDownloaded,         \n" +
            "SUM(numContainersDownloadPending) AS containersDownloadPending         \n" +
            "FROM (         \n" +
            "\tSELECT DistinctEntries.entryId As distinctEntryId, ContainerFile.fileSize, DownloadJobItem.downloadedSoFar as downloadedSoFar,         \n" +
            "\tCASE         \n" +
            "\t\tWHEN ContainerFileEntry.containerEntryId IS NOT NULL THEN 1         \n" +
            "        WHEN DownloadJobItem.entryId IS NOT NULL THEN 1         \n" +
            "        WHEN OpdsLink.id IS NOT NULL THEN 1         \n" +
            "        ELSE 0         \n" +
            "\tEND AS numContainers,         \n" +
            "    \n" +
            "\tCASE          \n" +
            "\t\tWHEN ContainerFileEntry.containerEntryId IS NOT NULL THEN 1         \n" +
            "        ELSE 0         \n" +
            "    END AS numContainersDownloaded,         \n" +
            "\tCASE         \n" +
            "\t\tWHEN ContainerFileEntry.containerEntryId IS NULL AND DownloadJobItem.entryId IS NOT NULL THEN 1         \n" +
            "\t\tELSE 0         \n" +
            "\tEND AS numContainersDownloadPending,         \n" +
            "\tCASE           \n" +
            "\t\tWHEN ContainerFile.id IS NOT NULL THEN ContainerFile.fileSize         \n" +
            "\t\tWHEN DownloadJobItem.downloadedSoFar > 0 THEN DownloadJobItem.downloadedSoFar         \n" +
            "\t\tELSE 0         \n" +
            "\tEND AS totalBytesDownloaded,         \n" +
            "                     \n" +
            "\tCASE         \n" +
            "\t\tWHEN ContainerFile.id IS NOT NULL THEN ContainerFile.fileSize         \n" +
            "\t\tWHEN DownloadJobItem.entryId IS NOT NULL THEN DownloadJobItem.downloadLength         \n" +
            "\t\tWHEN OpdsLink.id IS NOT NULL THEN OpdsLink.length         \n" +
            "\t\tELSE 0         \n" +
            "\tEND AS linkLength         \n" +
            "                         \n" +
            "\tFROM OpdsEntry_recursive AS DistinctEntries         \n" +
            "\t\tLEFT JOIN ContainerFileEntry on ContainerFileEntry.containerFileEntryId = (SELECT containerFileEntryId FROM ContainerFileEntry WHERE ContainerFileEntry.containerEntryId = DistinctEntries.entryId LIMIT 1)         \n" +
            "\t\tLEFT JOIN ContainerFile on ContainerFileEntry.containerFileId = ContainerFile.id         \n" +
            "\t\tLEFT JOIN DownloadJobItem on DownloadJobItem.id = (SELECT DownloadJobItem.id FROM DownloadJobItem LEFT JOIN DownloadJob ON DownloadJobItem.downloadJobId = DownloadJob.id WHERE DownloadJobItem.entryId = DistinctEntries.entryId AND DownloadJob.status > 0 AND DownloadJob.status < 20)         \n" +
            "\t\tLEFT JOIN OpdsEntry on OpdsEntry.uuid = (         \n" +
            "\t\t\tSELECT OpdsEntry.uuid FROM OpdsEntry WHERE OpdsEntry.entryId = DistinctEntries.entryId ORDER BY OpdsEntry.updated LIMIT 1          \n" +
            "\t\t)         \n" +
            "\t\tLEFT JOIN OpdsLink On OpdsLink.id = (\n" +
            "\t\t\tSELECT OpdsLink.id FROM OpdsLink WHERE OpdsLink.entryUuid = OpdsEntry.uuid AND OpdsLink.rel LIKE \"http://opds-spec.org/acquisition%\" LIMIT 1\n" +
            "\t\t)         \n" +
            ")";
//    @UmQuery(GET_DOWNLOAD_STATUS_SQL)
//    public abstract OpdsEntryStatusCache getEntryDownloadStatus(String entryId);

//    @UmQuery(GET_DOWNLOAD_STATUS_SQL)
//    public abstract UmLiveData<OpdsEntryStatusCache> getEntryDownloadStatusLive(String entryId);


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

    public List<OpdsEntryAncestor> getAncestors(List<String> entryIds) {
        return getAncestors_RecursiveQuery(entryIds);
    }

    protected static final String GET_ANCESTOR_ENTRIES_RECURSIVE_SQL = "WITH RECURSIVE OpdsEntry_recursive(entryId, descendantId) AS (\n" +
            "\tSELECT OpdsEntry.entryId as entryId, OpdsEntry.entryId as descendantId FROM OpdsEntry WHERE OpdsEntry.entryId IN (:entryIds)\n" +
            "\tUNION\n" +
            "\tSELECT OpdsParentEntry.entryId AS entryId, OpdsEntry_recursive.entryId AS descendantId FROM \n" +
            "\tOpdsEntryParentToChildJoin \n" +
            "\t\tJOIN OpdsEntry OpdsChildEntry ON OpdsEntryParentToChildJoin.childEntry = OpdsChildEntry.uuid\n" +
            "\t\tJOIN OpdsEntry OpdsParentEntry ON OpdsEntryParentToChildJoin.parentEntry = OpdsParentEntry.uuid,\n" +
            "\tOpdsEntry_recursive\n" +
            "\tWHERE\n" +
            "\t\tOpdsChildEntry.entryId = OpdsEntry_recursive.entryId\n" +
            ")\n" +
            "SELECT * FROM OpdsEntry_recursive ";
    @UmQuery(GET_ANCESTOR_ENTRIES_RECURSIVE_SQL)
    public abstract List<OpdsEntryAncestor> getAncestors_RecursiveQuery(List<String> entryIds);


}
