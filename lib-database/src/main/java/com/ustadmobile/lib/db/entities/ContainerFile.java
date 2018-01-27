package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Created by mike on 1/25/18.
 */
@UmEntity
public class ContainerFile {

    @UmPrimaryKey(autoIncrement = true)
    private int id;

    @UmIndexField
    private String normalizedPath;

    private int downloadStatus;

    private String dirPath;

    private long lastUpdated;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNormalizedPath() {
        return normalizedPath;
    }

    public void setNormalizedPath(String normalizedPath) {
        this.normalizedPath = normalizedPath;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    /**
     * The time that this database entity was last updated with information from the file itself.
     *  E.g. if the last modified time of the file is greater than this timestamp, the file must be
     *  examined.
     *
     * @return
     */
    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
