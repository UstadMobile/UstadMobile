package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerWithContentEntry;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao
@UmRepository
public abstract class ContainerDao implements SyncableDao<Container, ContainerDao> {

    @UmInsert
    public abstract Long[] insert(List<Container> containerList);

    @UmQuery("Select Container.* FROM Container " +
            "WHERE Container.containerContentEntryUid = :contentEntry " +
            "ORDER BY Container.lastModified DESC LIMIT 1")
    public abstract void getMostRecentContainerForContentEntryAsync(long contentEntry, UmCallback<Container> callback);

    @UmQuery("Select Container.* FROM Container " +
            "WHERE Container.containerContentEntryUid = :contentEntry " +
            "ORDER BY Container.lastModified DESC LIMIT 1")
    public abstract Container getMostRecentContainerForContentEntry(long contentEntry);


    @UmQuery("SELECT recent.* " +
            "FROM Container recent LEFT JOIN Container old " +
            "ON (recent.containerContentEntryUid = old.containerContentEntryUid " +
            "AND recent.lastModified < old.lastModified) " +
            "WHERE old.containerUid IS NULL " +
            "AND recent.containerContentEntryUid IN (:contentEntries)")
    public abstract void findRecentContainerToBeMonitoredWithEntriesUid(List<Long> contentEntries, UmCallback<List<Container>> callback);

    @UmQuery("Select Container.* FROM Container " +
            "WHERE Container.containerContentEntryUid = :contentEntryUid " +
            "ORDER BY Container.lastModified DESC")
    public abstract void findFilesByContentEntryUid(long contentEntryUid, UmCallback<List<Container>> callback);


    @UmQuery("SELECT Container.* FROM Container " +
            "LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = containerContentEntryUid " +
            "WHERE ContentEntry.publik")
    public abstract List<Container> findAllPublikContainers();

    @UmQuery("SELECT * From Container WHERE Container.containerUid = :containerUid")
    public abstract void findByUid(long containerUid, UmCallback<Container> containerUmCallback);

    @UmQuery("UPDATE Container " +
            "SET cntNumEntries = (SELECT COUNT(*) FROM ContainerEntry WHERE ceContainerUid = Container.containerUid)," +
            "fileSize = (SELECT SUM(ContainerEntryFile.ceCompressedSize) AS totalSize FROM ContainerEntry " +
            "JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid " +
            "WHERE ContainerEntry.ceContainerUid = Container.containerUid) " +
            "WHERE containerUid = :containerUid")
    public abstract void updateContainerSizeAndNumEntries(long containerUid);

    @UmQuery("SELECT Container.containerUid FROM Container " +
            "WHERE (SELECT COUNT(*) FROM ContainerEntry WHERE ceContainerUid = Container.containerUid) = Container.cntNumEntries " +
            "AND Container.containerUid = :containerUid")
    public abstract Long findLocalAvailabilityByUid(long containerUid);

    @UmQuery("SELECT Container.*, ContentEntry.entryId, ContentEntry.sourceUrl FROM Container " +
            "LEFT JOIN ContentEntry ON Container.containerContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.publisher LIKE '%Khan Academy%' AND Container.mimeType = 'video/mp4'")
    public abstract List<ContainerWithContentEntry> findKhanContainers();

    @UmQuery("DELETE FROM Container WHERE containerUid = :containerUid")
    public abstract void deleteByUid(long containerUid);

    @UmQuery("UPDATE Container SET mimeType = :mimeType WHERE Container.containerUid = :containerUid")
    public abstract void updateMimeType(String mimeType, long containerUid);

}
