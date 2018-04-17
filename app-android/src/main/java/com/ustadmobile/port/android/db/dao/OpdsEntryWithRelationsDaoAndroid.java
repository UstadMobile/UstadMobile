package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryAncestor;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelationsAndContainerMimeType;
import com.ustadmobile.lib.db.entities.OpdsEntryWithStatusCache;

import java.util.List;

/**
 * Created by mike on 1/15/18.
 */
@Dao
public abstract class OpdsEntryWithRelationsDaoAndroid extends OpdsEntryWithRelationsDao {

    @Override
    public UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String entryId,
                                                            OpdsEntry.OpdsItemLoadCallback callback) {
        return new UmLiveDataAndroid<>(getEntryByUrlR(url));
    }

    @Query("SELECT * From OpdsEntry Where url = :url")
    public abstract LiveData<OpdsEntryWithRelations> getEntryByUrlR(String url);

    @Override
    @Query("SELECT * FROM OpdsEntry WHERE uuid = :uuid")
    public abstract OpdsEntryWithRelations findByUuid(String uuid);

    @Query("SELECT OpdsEntry.* from OpdsEntry INNER JOIN OpdsEntryParentToChildJoin on OpdsEntry.uuid = OpdsEntryParentToChildJoin.childEntry WHERE OpdsEntryParentToChildJoin.parentEntry = :parentId ORDER BY childIndex")
    public abstract DataSource.Factory<Integer, OpdsEntryWithRelations> findEntriesByParentR(String parentId);

    @Query("SELECT OpdsEntry.*, OpdsEntryStatusCache.* FROM OpdsEntry " +
            " INNER JOIN OpdsEntryParentToChildJoin on OpdsEntry.uuid = OpdsEntryParentToChildJoin.childEntry " +
            " LEFT JOIN OpdsEntryStatusCache ON OpdsEntry.entryId = OpdsEntryStatusCache.statusEntryId " +
            "WHERE OpdsEntryParentToChildJoin.parentEntry = :parentId ORDER BY childIndex")
    public abstract DataSource.Factory<Integer, OpdsEntryWithStatusCache> findEntriesWithStatusCacheByParent_Room(String parentId);

    @Override
    public UmProvider<OpdsEntryWithStatusCache> getEntriesWithStatusCacheByParent(String parentId) {
        return () -> findEntriesWithStatusCacheByParent_Room(parentId);
    }

    @Override
    @Query("Select * from OpdsEntry WHERE url = :url")
    public abstract OpdsEntryWithRelations getEntryByUrlStatic(String url);

    @Override
    public UmLiveData<List<OpdsEntryWithRelations>> getEntriesByParentAsList(String parentId){
        return new UmLiveDataAndroid<>(findEntriesByParentAsListR(parentId));
    }

    @Query("SELECT OpdsEntry.* from OpdsEntry INNER JOIN OpdsEntryParentToChildJoin on OpdsEntry.uuid = OpdsEntryParentToChildJoin.childEntry WHERE OpdsEntryParentToChildJoin.parentEntry = :parentId ORDER BY childIndex")
    public abstract LiveData<List<OpdsEntryWithRelations>> findEntriesByParentAsListR(String parentId);

    @Override
    public UmProvider<OpdsEntryWithRelations> getEntriesByParent(String parentId) {
        return () -> findEntriesByParentR(parentId);
    }

    @Override
    @Query("SELECT OpdsEntry.* from OpdsEntry INNER JOIN OpdsEntryParentToChildJoin on OpdsEntry.uuid = OpdsEntryParentToChildJoin.childEntry WHERE OpdsEntryParentToChildJoin.parentEntry = :parentId ORDER BY childIndex")
    public abstract List<OpdsEntryWithRelations> getEntriesByParentAsListStatic(String parentId);

    @Override
    public UmLiveData<OpdsEntryWithRelations> getEntryByUuid(String uuid) {
        return new UmLiveDataAndroid<>(getEntryByUuidR(uuid));
    }

    @Query("SELECT * from OpdsEntry where uuid = :uuid")
    public abstract LiveData<OpdsEntryWithRelations> getEntryByUuidR(String uuid);

    @Override
    @Query("SELECT uuid FROM OpdsEntry WHERE url = :url")
    public abstract String getUuidForEntryUrl(String url);

    @Query(findEntriesByContainerFileDirectorySql)
    public abstract LiveData<List<OpdsEntryWithStatusCache>> findEntriesByContainerFileDirectoryR(List<String> dirList);

    @Override
    public UmLiveData<List<OpdsEntryWithStatusCache>> findEntriesByContainerFileDirectoryAsList(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback) {
        return new UmLiveDataAndroid<>(findEntriesByContainerFileDirectoryR(dirList));
    }



    @Query(findEntriesByContainerFileDirectorySql)
    public abstract DataSource.Factory<Integer, OpdsEntryWithStatusCache> findEntriesByContainerFileDirectoryAsProviderR(List<String> dirList);

    @Override
    public UmProvider<OpdsEntryWithStatusCache> findEntriesByContainerFileDirectoryAsProvider(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback) {
        return () -> findEntriesByContainerFileDirectoryAsProviderR(dirList);
    }

    @Override
    @Query("SELECT OpdsEntry.url FROM OpdsEntryParentToChildJoin " +
            " LEFT JOIN OpdsEntry ON OpdsEntryParentToChildJoin.parentEntry = OpdsEntry.uuid " +
            " WHERE OpdsEntryParentToChildJoin.childEntry = :childUuid")
    public abstract String findParentUrlByChildUuid(String childUuid);

    @Override
    @Query("Select * From OpdsEntry WHERE uuid = :uuid")
    public abstract OpdsEntryWithRelations getEntryByUuidStatic(String uuid);

    @Override
    @Query("SELECT * FROM OpdsEntry WHERE entryId = :entryId LIMIT 1")
    public abstract OpdsEntryWithRelations findFirstByEntryIdStatic(String entryId);

    @Override
    @Query("SELECT OpdsEntry.uuid FROM OpdsEntry " +
            "LEFT JOIN OpdsEntryParentToChildJoin ON OpdsEntryParentToChildJoin.childEntry = OpdsEntry.uuid " +
            "LEFT JOIN OpdsEntry OpdsEntryParent ON OpdsEntryParentToChildJoin.parentEntry = OpdsEntryParent.uuid " +
            "WHERE OpdsEntry.entryId = :entryId AND OpdsEntryParent.url = :parentUrl ")
    public abstract String findUuidByEntryIdAndParentUrl(String entryId, String parentUrl);

    @Override
    @Query("DELETE FROM OpdsEntry WHERE uuid in (:entryUuids)")
    public abstract int deleteOpdsEntriesByUuids(List<String> entryUuids);

    @Override
    @Query("DELETE FROM OpdsLink WHERE entryUuid in (:entryUuids)")
    public abstract int deleteLinksByOpdsEntryUuids(List<String> entryUuids);

    @Override
    @Query(findEntriesByContainerFileSql)
    public abstract List<OpdsEntryWithRelations> findEntriesByContainerFileNormalizedPath(String normalizedPath);


    @Override
    @Query("SELECT OpdsEntry.*, ContainerFile.mimeType as containerMimeType FROM OpdsEntry " +
            "LEFT JOIN ContainerFileEntry on OpdsEntry.entryId = ContainerFileEntry.containerEntryId " +
            "LEFT JOIN ContainerFile on ContainerFileEntry.containerFileId = ContainerFile.id " +
            "WHERE OpdsEntry.uuid in (:uuids)")
    public abstract List<OpdsEntryWithRelationsAndContainerMimeType> findByUuidsWithContainerMimeType(List<String> uuids);

    @Override
    @Query("SELECT uuid From OpdsEntry WHERE entryId = :entryId")
    public abstract List<String> getUuidsForEntryId(String entryId);

    @Override
    @Query(GET_CHILD_ENTRIES_RECURSIVE_SQL)
    public abstract List<String> findAllChildEntryIdsRecursive(String entryId);

    @Override
    @Query(GET_ANCESTOR_ENTRIES_RECURSIVE_SQL)
    public abstract List<OpdsEntryAncestor> getAncestors_RecursiveQuery(List<String> entryIds);
}
