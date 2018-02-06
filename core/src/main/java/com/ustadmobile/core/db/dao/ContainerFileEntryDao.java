package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContainerFileEntry;
import com.ustadmobile.lib.db.entities.ContainerFileEntryWithContainerFile;

import java.util.List;

/**
 * Created by mike on 1/27/18.
 */

public abstract class ContainerFileEntryDao {

    @UmInsert
    public abstract void insert(List<ContainerFileEntry> fileEntries);

    @UmQuery("DELETE From OpdsEntry WHERE uuid in (Select opdsEntryUuid FROM ContainerFileEntry WHERE containerFileId = :containerFileId)")
    public abstract void deleteOpdsEntriesByContainerFile(int containerFileId);

    @UmQuery("DELETE FROM ContainerFileEntry WHERE containerFileId = :containerFileId")
    public abstract void deleteContainerFileEntriesByContainerFile(int containerFileId);

    public void deleteOpdsAndContainerFileEntriesByContainerFile(int containerFileId) {
        deleteOpdsEntriesByContainerFile(containerFileId);
        deleteContainerFileEntriesByContainerFile(containerFileId);
    }

    @UmQuery("SELECT containerEntryId, containerEntryUpdated FROM ContainerFileEntry WHERE containerEntryId IN (:entryIds)")
    public abstract List<ContainerFileEntry> findContainerFileEntriesByEntryIds(String[] entryIds);

    /**
     * Find the first container file entry that matches. Normally there should only be one copy of
     * a given entry on the disk.
     *
     * @param entryId
     * @return
     */
    @UmQuery("")
    public abstract ContainerFileEntryWithContainerFile findContainerFileEntryWithContainerFileByEntryId(String entryId);

    /**
     * Find all ContainerFiles that contain the given entryid
     *
     * @param entryId
     * @return
     */
    public abstract List<ContainerFileEntryWithContainerFile> findContainerFileEntriesWithContainerFileByEntryId(String entryId);

    @UmQuery("SELECT opdsEntryUuid FROM ContainerFileEntry WHERE containerFileId = :containerFileId")
    protected abstract List<String> findOpdsEntryUuidsByContainerFileId(int containerFileId);

    @UmQuery("DELETE from ContainerFileEntry Where containerFileId = :containerFileId")
    protected abstract void deleteByContainerFileId(int containerFileId);


}