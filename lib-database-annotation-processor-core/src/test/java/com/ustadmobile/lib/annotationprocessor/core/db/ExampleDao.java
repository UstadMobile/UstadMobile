package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;

@UmDao
public abstract class ExampleDao {

    @UmInsert
    public abstract void insertE(ExampleEntity entity);

}
