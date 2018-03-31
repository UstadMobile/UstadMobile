package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCacheAncestor;

import java.util.List;

/**
 * DAO for OpdsEntryStatusCacheAncestor object
 *
 * @see OpdsEntryStatusCacheAncestor
 */

public abstract class OpdsEntryStatusCacheAncestorDao {

    /**
     * Insert the given list of OpdsEntryStatusCache ancestor objects
     *
     * @param ancestorList List of OpdsEntryStatusCache ancestor objects to insert
     */
    @UmInsert
    public abstract void insertAll(List<OpdsEntryStatusCacheAncestor> ancestorList);

}
