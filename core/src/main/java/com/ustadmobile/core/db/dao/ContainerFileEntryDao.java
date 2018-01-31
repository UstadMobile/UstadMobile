package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContainerFileEntry;

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


}