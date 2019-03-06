package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContainerEntryFile;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

import java.util.List;

@UmDao
public abstract class ContainerEntryFileDao implements BaseDao<ContainerEntryFile> {

    //TODO: split this to handle very large queries
    @UmQuery("SELECT ContainerEntryFile.* FROM ContainerEntryFile WHERE cefMd5 IN (:md5Sums)")
    public abstract List<ContainerEntryFile> findEntriesByMd5Sums(List<String> md5Sums);

    @UmQuery("UPDATE ContainerEntryFile SET cefPath = :path WHERE cefUid = :cefUid")
    public abstract void updateFilePath(long cefUid, String path);

    @UmQuery("SELECT ContainerEntryFile.* FROM ContainerEntryFile " +
            "LEFT JOIN ContainerEntry ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid " +
            "LEFT JOIN Container ON Container.containerUid = ContainerEntry.ceContainerUid " +
            "LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = Container.containerContentEntryUid " +
            "WHERE ContentEntry.publik")
    public abstract List<ContainerEntryFile> findAllPublikContainerEntryFiles();


}
