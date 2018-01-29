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

    private String mimeType;

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

    /**
     * Get the mime type for this file. This is important as some different file types share the same
     * extension (e.g. .zip for Xapi Package and SCORM files). The mime type is set by the
     * OpdsDirScanner
     *
     * @return The mime type of this file, as specified by the ContentTypePlugin that generated
     * the entry for it.
     */
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
