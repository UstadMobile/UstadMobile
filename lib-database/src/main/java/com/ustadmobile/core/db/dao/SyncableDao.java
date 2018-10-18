package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmAppDatabase;

import java.util.List;

public interface SyncableDao<T> extends BaseDao<T> {

    void syncWith(SyncableDao<T> otherDao);

    List<T> handlingIncomingSync(List<T> incomingChanges, long fromChangeSequenceNumber,
                                 long userId, UmAppDatabase myDb);

    List<T> findLocalChanges(long fromChangeSequenceNumber, long userId);



}
