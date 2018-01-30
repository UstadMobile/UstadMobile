package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents the response from a network node to whether or not a given entry is available locally
 */
public class EntryStatusResponse {

    @UmPrimaryKey(autoIncrement = true)
    private int id;

    private String entryId;

    private long updated;

    private int nodeId;

}
