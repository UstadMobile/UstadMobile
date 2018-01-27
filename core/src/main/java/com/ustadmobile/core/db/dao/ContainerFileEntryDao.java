package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.db.entities.ContainerFileEntry;

import java.util.List;

/**
 * Created by mike on 1/27/18.
 */

public abstract class ContainerFileEntryDao {

    @UmInsert
    public abstract void insert(List<ContainerFileEntry> fileEntries);

}
