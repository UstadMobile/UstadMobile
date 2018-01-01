package com.ustadmobile.port.javase.impl;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ustadmobile.core.fs.db.HttpCacheDbEntry;
import com.ustadmobile.port.jcommon.fs.db.HttpCacheDbEntryEntity;

import java.sql.SQLException;

/**
 * Created by mike on 12/31/17.
 */

public class UmContextSe {

    private String cacheDbJdbcUrl;

    private ConnectionSource cacheDbConnectionSource;
    
    public ConnectionSource getCacheDbConnectionSource() {
        if(cacheDbConnectionSource == null) {
            try {
                cacheDbConnectionSource = new JdbcConnectionSource(cacheDbJdbcUrl);
                TableUtils.createTableIfNotExists(cacheDbConnectionSource, HttpCacheDbEntryEntity.class);
            }catch(SQLException e) {
                e.printStackTrace();
            }
        }

        return cacheDbConnectionSource;
    }

    public String getCacheDbJdbcUrl() {
        return cacheDbJdbcUrl;
    }

    public void setCacheDbJdbcUrl(String cacheDbJdbcUrl) {
        this.cacheDbJdbcUrl = cacheDbJdbcUrl;
    }
}
