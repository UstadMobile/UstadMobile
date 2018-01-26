package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmIndex;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Created by mike on 1/25/18.
 */
@UmPrimaryKey
public class ContainerFile {

    @UmPrimaryKey
    private int id;

    @UmIndexField
    private String normalizedPath;

    private int downloadStatus;

}
