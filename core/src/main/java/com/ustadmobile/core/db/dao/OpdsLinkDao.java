package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.annotation.UmInsert;
import com.ustadmobile.lib.db.entities.OpdsLink;

import java.util.List;

/**
 * Created by mike on 1/16/18.
 */

public abstract class OpdsLinkDao {

    @UmInsert
    public abstract void insert(List<OpdsLink> links);


}
