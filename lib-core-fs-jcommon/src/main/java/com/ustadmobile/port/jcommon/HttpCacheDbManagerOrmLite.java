package com.ustadmobile.port.jcommon;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ustadmobile.core.fs.db.HttpCacheDbEntry;
import com.ustadmobile.core.fs.db.HttpCacheDbManager;
import com.ustadmobile.port.jcommon.fs.db.HttpCacheDbEntryEntity;

import java.sql.SQLException;

/**
 * ORMLite implementation of HttpCacheDbManager
 *
 * Created by mike on 12/30/17.
 */

public abstract class HttpCacheDbManagerOrmLite extends HttpCacheDbManager {

    @Override
    public HttpCacheDbEntry getEntryByUrl(Object context, String url) {
        Dao<HttpCacheDbEntryEntity, Integer> dao = getDao(context);
        try {
            QueryBuilder<HttpCacheDbEntryEntity, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(HttpCacheDbEntryEntity.COLNAME_URL, url);
            return dao.queryForFirst(queryBuilder.prepare());
        }catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public HttpCacheDbEntry makeNewEntry(Object context) {
        return new HttpCacheDbEntryEntity();
    }

    @Override
    public void persist(Object context, HttpCacheDbEntry entry) {
        Dao<HttpCacheDbEntryEntity, Integer> dao = getDao(context);
        try {
            dao.createOrUpdate((HttpCacheDbEntryEntity)entry);
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Object context, HttpCacheDbEntry entry) {
        try {
            getDao(context).delete((HttpCacheDbEntryEntity)entry);
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method varies between Android and Jdbc implementations. The implementation class only needs
     * to implement this method
     *
     * @param context System context object
     * @return OrmLite Dao
     */
    public abstract Dao<HttpCacheDbEntryEntity, Integer> getDao(Object context);
}
