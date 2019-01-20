package com.ustadmobile.lib.database;

import com.ustadmobile.lib.db.AbstractDoorwayDbBuilder;

import java.lang.reflect.Constructor;

public class UmDbBuilder {

    public static class JdbcDbBuilder<T> extends AbstractDoorwayDbBuilder<T> {

        private Object context;

        private String name;

        public JdbcDbBuilder(Class<T> dbClass, Object context, String name) {
            super(dbClass);
            this.context = context;
            this.name = name;
        }


        @Override
        public T build() {
            try {
                Class jdbcClass = Class.forName(dbClass.getName() + "_Jdbc");

                @SuppressWarnings("unchecked")
                Constructor constructor = jdbcClass.getConstructor(Object.class, String.class);

                @SuppressWarnings("unchecked")
                T dbResult = (T)constructor.newInstance(context, name);
                return dbResult;
            }catch(Exception e) {
                System.err.print("Exception creating database: " + e);
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> AbstractDoorwayDbBuilder<T> builder(Class<T> dbClass, Object context) {
        return new JdbcDbBuilder<>(dbClass, context, dbClass.getSimpleName());
    }

    public static <T> AbstractDoorwayDbBuilder<T> builder(Class<T> dbClass, Object context, String dbName) {
        return new JdbcDbBuilder<>(dbClass, context, dbName);
    }
}
