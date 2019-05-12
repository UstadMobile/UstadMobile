package com.ustadmobile.lib.db;

public class UmDbType {

    public static final int TYPE_UNKNOWN = -1;

    public static final int TYPE_SQLITE = 1;

    public static final int TYPE_POSTGRES = 2;

    public static int typeByProductName(String jdbcProductName) {
        switch(jdbcProductName) {
            case "PostgreSQL":
                return TYPE_POSTGRES;
            case "SQLite":
                return TYPE_SQLITE;
            default:
                return TYPE_UNKNOWN;
        }
    }

}
