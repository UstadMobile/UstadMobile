package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ContainerEntryFile {

    public static final int COMPRESSION_NONE = 0;

    public static final int COMPRESSION_GZIP = 1;

    @UmPrimaryKey(autoIncrement = true)
    private long cefUid;

    private String cefMd5;

    private String cefPath;

    private long ceTotalSize;

    private long ceCompressedSize;

    private int compression;

    public ContainerEntryFile() {

    }

    public ContainerEntryFile(String md5, long totalSize, long compressedSize, int compression) {
        this.cefMd5 = md5;
        this.ceTotalSize = totalSize;
        this.ceCompressedSize = compressedSize;
        this.compression = compression;
    }


    public long getCefUid() {
        return cefUid;
    }

    public void setCefUid(long cefUid) {
        this.cefUid = cefUid;
    }

    public String getCefMd5() {
        return cefMd5;
    }

    public void setCefMd5(String cefMd5) {
        this.cefMd5 = cefMd5;
    }

    public String getCefPath() {
        return cefPath;
    }

    public void setCefPath(String cefPath) {
        this.cefPath = cefPath;
    }

    public long getCeTotalSize() {
        return ceTotalSize;
    }

    public void setCeTotalSize(long ceTotalSize) {
        this.ceTotalSize = ceTotalSize;
    }

    public long getCeCompressedSize() {
        return ceCompressedSize;
    }

    public void setCeCompressedSize(long ceCompressedSize) {
        this.ceCompressedSize = ceCompressedSize;
    }

    public int getCompression() {
        return compression;
    }

    public void setCompression(int compression) {
        this.compression = compression;
    }
}
