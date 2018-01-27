package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.ContainerFileWithRelations;

/**
 * Created by mike on 1/27/18.
 */

public abstract class ContainerFileDao {

    public abstract ContainerFileWithRelations findContainerFileByDirPath(String dirPath);

    public abstract long insert(ContainerFile containerFile);


}
