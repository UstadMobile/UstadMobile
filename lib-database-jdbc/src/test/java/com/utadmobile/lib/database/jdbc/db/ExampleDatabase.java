package com.utadmobile.lib.database.jdbc.db;

import com.ustadmobile.lib.database.annotation.UmDatabase;

@UmDatabase(version = 1, entities = {ExampleEntity.class})
public abstract class ExampleDatabase {

    private static volatile ExampleDatabase instance;

    public static synchronized ExampleDatabase getInstance(Object context) {
        if(instance == null){
            instance = com.utadmobile.lib.database.jdbc.db.ExampleDatabase_Factory.makeExampleDatabase(context);
        }

        return instance;
    }

    public static synchronized ExampleDatabase getInstance(Object context, String dbName) {
        return ExampleDatabase_Factory.makeExampleDatabase(context, dbName);
    }


    public abstract ExampleDao getExampleDao();

}
