package com.ustadmobile.port.jcommon.fs.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.core.fs.db.HttpCacheDbEntry;

/**
 * Created by mike on 12/30/17.
 */
@DatabaseTable(tableName="cache")
public class HttpCacheDbEntryEntity implements HttpCacheDbEntry{

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int entryId;

    public static final String COLNAME_URL = "url";

    @DatabaseField(columnName = COLNAME_URL, index = true)
    private String url;

    @DatabaseField
    private String fileUri;

    @DatabaseField
    private long expiresTime = -1;

    @DatabaseField
    private String contentType;

    @DatabaseField
    private String eTag;

    @DatabaseField
    private long lastModified;

    @DatabaseField
    private String cacheControl;

    @DatabaseField
    private long contentLength;

    @DatabaseField
    private int statusCode;

    @DatabaseField
    private long lastChecked;

    @DatabaseField(index = true)
    private long lastAccessed;


    @Override
    public int getEntryId() {
        return entryId;
    }

    @Override
    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    @Override
    public long getExpiresTime() {
        return expiresTime;
    }

    @Override
    public void setExpiresTime(long expiresTime) {
        this.expiresTime = expiresTime;
    }

    @Override
    public String geteTag() {
        return eTag;
    }

    @Override
    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public String getCacheControl() {
        return cacheControl;
    }

    @Override
    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String getFileUri() {
        return fileUri;
    }

    @Override
    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    @Override
    public long getLastAccessed() {
        return lastAccessed;
    }

    @Override
    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    @Override
    public long getLastChecked() {
        return lastChecked;
    }

    @Override
    public void setLastChecked(long lastChecked) {
        this.lastChecked = lastChecked;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }
}
