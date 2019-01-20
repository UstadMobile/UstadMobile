package com.ustadmobile.lib.db;

import java.util.List;

public abstract class AbstractDoorwayDbBuilder<T> {

    protected Class dbClass;

    protected List<DbCallback> callbackList;

    public AbstractDoorwayDbBuilder(Class<T> dbClass) {
        this.dbClass = dbClass;
    }

    public AbstractDoorwayDbBuilder addCallback(DbCallback callback) {
        callbackList.add(callback);
        return this;
    }

    public abstract T build();

}
