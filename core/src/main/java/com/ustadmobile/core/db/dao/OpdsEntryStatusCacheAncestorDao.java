package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCacheAncestor;

import java.util.List;

/**
 * DAO for OpdsEntryStatusCacheAncestor object
 *
 * @see OpdsEntryStatusCacheAncestor
 */
@UmDao
public abstract class OpdsEntryStatusCacheAncestorDao {

    /**
     * Insert the given list of OpdsEntryStatusCache ancestor objects
     *
     * @param ancestorList List of OpdsEntryStatusCache ancestor objects to insert
     */
    @UmInsert
    public abstract void insertAll(List<OpdsEntryStatusCacheAncestor> ancestorList);


    protected static final String GET_ANCESTOR_ENTRIES_RECURSIVE_SQL_TO_INSERT =
            "WITH RECURSIVE OpdsEntry_recursive(entryId, descendantId, ancestorOpdsEntryStatusCacheId, opdsEntryStatusCacheId, entryStatusCacheAncestorUid) AS ( " +
                    "SELECT " +
                    "OpdsEntry.entryId as entryId, OpdsEntry.entryId as descendantId, " +
                    "OpdsEntryStatusCache.statusCacheUid AS ancestorOpdsEntryStatusCacheId, OpdsEntryStatusCache.statusCacheUid AS opdsEntryStatusCacheId, " +
                    "EXISTS( " +
                    "SELECT pkId FROM OpdsEntryStatusCacheAncestor " +
                    "WHERE " +
                    "OpdsEntryStatusCacheAncestor.ancestorOpdsEntryStatusCacheId = (SELECT statusCacheUid From OpdsEntryStatusCache WHERE statusEntryId = OpdsEntry.entryId) " +
                    "AND " +
                    "OpdsEntryStatusCacheAncestor.opdsEntryStatusCacheId = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = OpdsEntry.entryId) " +
                    ")" +
                    "FROM OpdsEntry  " +
                    "LEFT JOIN OpdsEntryStatusCache ON OpdsEntryStatusCache.statusEntryId = OpdsEntry.entryId " +
                    "WHERE OpdsEntry.entryId IN (:entryIds) " +
                    "UNION  " +
                    "SELECT  " +
                    "OpdsParentEntry.entryId AS entryId, " +
                    "OpdsEntry_recursive.descendantId AS descendantId, " +
                    "OpdsEntryStatusCacheEntry.statusCacheUid AS ancestorOpdsEntryStatusCacheId, " +
                    "OpdsEntryStatusCacheDescendant.statusCacheUid AS opdsEntryStatusCacheId, " +
                    "OpdsEntryStatusCacheAncestor.pkId " +
                    "FROM  " +
                    "OpdsEntryParentToChildJoin  " +
                    "JOIN OpdsEntry OpdsChildEntry ON OpdsEntryParentToChildJoin.childEntry = OpdsChildEntry.uuid  " +
                    "JOIN OpdsEntry OpdsParentEntry ON OpdsEntryParentToChildJoin.parentEntry = OpdsParentEntry.uuid " +
                    "JOIN OpdsEntryStatusCache OpdsEntryStatusCacheEntry ON OpdsEntryStatusCacheEntry.statusEntryId = OpdsParentEntry.entryId " +
                    "JOIN OpdsEntryStatusCache OpdsEntryStatusCacheDescendant ON OpdsEntryStatusCacheDescendant.statusEntryId = OpdsEntry_recursive.descendantId " +
                    "LEFT JOIN OpdsEntryStatusCacheAncestor ON OpdsEntryStatusCacheAncestor.ancestorOpdsEntryStatusCacheId = OpdsEntryStatusCacheEntry.statusCacheUid AND OpdsEntryStatusCacheAncestor.opdsEntryStatusCacheId = OpdsEntryStatusCacheDescendant.statusCacheUid, " +
                    "OpdsEntry_recursive  " +
                    "WHERE  " +
                    "OpdsChildEntry.entryId = OpdsEntry_recursive.entryId " +
                    ") " +
                    "SELECT ancestorOpdsEntryStatusCacheId, opdsEntryStatusCacheId, 0 AS pkId FROM OpdsEntry_recursive WHERE entryStatusCacheAncestorUid = 0 OR entryStatusCacheAncestorUid IS NULL";

    /**
     * Make a list of all those entries that
     * @param entryIds
     * @return
     */
    @UmQuery(GET_ANCESTOR_ENTRIES_RECURSIVE_SQL_TO_INSERT)
    public abstract List<OpdsEntryStatusCacheAncestor> findAncestorsToAdd(List<String> entryIds);

}
