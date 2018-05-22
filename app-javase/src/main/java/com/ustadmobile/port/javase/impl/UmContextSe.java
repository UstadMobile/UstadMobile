package com.ustadmobile.port.javase.impl;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

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
