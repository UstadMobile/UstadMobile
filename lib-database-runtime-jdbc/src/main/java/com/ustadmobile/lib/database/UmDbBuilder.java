package com.ustadmobile.lib.database;

import java.lang.reflect.Constructor;

public class UmDbBuilder {

    public static <T> T makeDatabase(Class<T> dbClass, Object context) {
        return makeDatabase(dbClass, context, dbClass.getSimpleName());
    }

    public static <T> T makeDatabase(Class<T> dbClass, Object context, String dbName) {
        try {
            Class jdbcClass = Class.forName(dbClass.getName() + "_Jdbc");

            @SuppressWarnings("unchecked")
            Constructor constructor = jdbcClass.getConstructor(Object.class, String.class);

            @SuppressWarnings("unchecked")
            T dbResult = (T)constructor.newInstance(context, dbName);
            return dbResult;
        }catch(Exception e) {
            System.err.print("Exception creating database: " + e);
            throw new RuntimeException(e);
        }
    }
}
