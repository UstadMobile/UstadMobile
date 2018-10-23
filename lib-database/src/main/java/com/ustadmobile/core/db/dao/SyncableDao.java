package com.ustadmobile.core.db.dao;

import java.util.List;

public interface SyncableDao<T> {

    void syncWith(SyncableDao<T> otherDao);

    List<T> handlingIncomingSync(List<T> incomingChanges, long fromChangeSequenceNumber,
                                 long userId);




}
