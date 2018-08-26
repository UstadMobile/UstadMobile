package com.utadmobile.lib.database.jdbc.db;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;

@UmDao
public abstract class ExampleDao {

    @UmInsert
    public abstract long insert(ExampleEntity entity);

}
