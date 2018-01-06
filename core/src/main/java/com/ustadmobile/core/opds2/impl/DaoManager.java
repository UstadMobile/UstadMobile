package com.ustadmobile.core.opds2.impl;

/**
 * Created by mike on 1/6/18.
 */

public abstract class DaoManager {

    private static DaoManager instance;

    public static final DaoManager getInstance() {
        return instance;
    }

    public abstract <C> C getDao(Object context, Class<C> daoClass);

    public abstract <C> C getRepository(Object context, Class<C> daoClass);


}
