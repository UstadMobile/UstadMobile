package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithContentEntryFileStatusAndContentEntryId;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithFilePath;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithStatus;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

/**
 * Deprecated: this is being replaced with Container which support de-duplicating entries
 */
@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
@Deprecated
public abstract class ContentEntryFileDao implements SyncableDao<ContentEntryFile, ContentEntryFileDao> {

    @UmInsert
    public abstract Long[] insert(List<ContentEntryFile> files);

    @UmQuery("Select ContentEntryFile.* FROM ContentEntryFile LEFT JOIN ContentEntryContentEntryFileJoin " +
            "ON ContentEntryFile.contentEntryFileUid = ContentEntryContentEntryFileJoin.cecefjContentEntryFileUid " +
            "WHERE ContentEntryContentEntryFileJoin.cecefjContentEntryUid = :contentEntryUid " +
            "ORDER BY lastModified DESC")
    public abstract void findFilesByContentEntryUid(long contentEntryUid, UmCallback<List<ContentEntryFile>> callback);

    @UmQuery("SELECT ContentEntryFile.* FROM ContentEntryFile LEFT JOIN ContentEntryContentEntryFileJoin " +
            "ON ContentEntryFile.contentEntryFileUid = ContentEntryContentEntryFileJoin.cecefjContentEntryFileUid " +
            "WHERE ContentEntryContentEntryFileJoin.cecefjContentEntryUid = :contentEntryUid")
    public abstract List<ContentEntryFile> findFilesByContentEntryUid(long contentEntryUid);

    @UmQuery("SELECT ContentEntryFile.*, ContentEntryFileStatus.* FROM ContentEntryFile " +
            "LEFT JOIN ContentEntryFileStatus ON ContentEntryFile.contentEntryFileUid = ContentEntryFileStatus.cefsContentEntryFileUid " +
            "WHERE ContentEntryFile.contentEntryFileUid = :contentEntryFileUid")
    public abstract ContentEntryFileWithStatus findByUidWithStatus(long contentEntryFileUid);

    @UmQuery("SELECT * FROM ContentEntryFile")
    public abstract List<ContentEntryFile> findAll();

    @UmQuery("SELECT ContentEntryFile.* FROM ContentEntryFile " +
            "LEFT JOIN ContentEntryContentEntryFileJoin ON ContentEntryContentEntryFileJoin.cecefjContentEntryFileUid = ContentEntryFile.contentEntryFileUid " +
            "LEFT JOIN ContentEntry ON ContentEntryContentEntryFileJoin.cecefjContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.publik")
    public abstract List<ContentEntryFile> getPublicContentEntryFiles();

    @UmQuery("SELECT ContentEntryFile.*, ContentEntryFileStatus.filePath, ContentEntryFileStatus.cefsUid, " +
            "ContentEntry.entryid, ContentEntry.contentEntryUid from ContentEntryFile " +
            "LEFT JOIN ContentEntryFileStatus ON ContentEntryFile.contentEntryFileUid = ContentEntryFileStatus.cefsContentEntryFileUid " +
            "LEFT JOIN ContentEntryContentEntryFileJoin ON ContentEntryFile.contentEntryFileUid = ContentEntryContentEntryFileJoin.cecefjContentEntryFileUid " +
            "LEFT JOIN ContentEntry ON ContentEntryContentEntryFileJoin.cecefjContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryFileStatus.filePath LIKE '%/khan/en/%' AND (ContentEntryFile.mimeType = 'video/mp4' OR ContentEntryFile.mimeType = 'application/khan-video+zip')")
    public abstract List<ContentEntryFileWithContentEntryFileStatusAndContentEntryId> findKhanFiles();

    @UmQuery("SELECT ContentEntryFile.*, ContentEntryFileStatus.filePath from ContentEntryFile " +
            "LEFT JOIN ContentEntryFileStatus ON ContentEntryFile.contentEntryFileUid = ContentEntryFileStatus.cefsContentEntryFileUid " +
            "WHERE ContentEntryFile.mimetype = 'application/epub+zip'")
    public abstract List<ContentEntryFileWithFilePath> findEpubsFiles();

    @UmQuery("UPDATE ContentEntryFile SET fileSize = :length, md5sum = :md5 WHERE contentEntryFileUid = :cefsUid")
    public abstract void updateEpubFiles(long length, String md5, long cefsUid);

    @UmQuery("UPDATE ContentEntryFile SET fileSize = :length, md5sum = :md5, mimeType = :mimeType  WHERE contentEntryFileUid = :cefsUid")
    public abstract void updateFiles(long length, String md5, String mimeType, long cefsUid);

    @UmQuery("UPDATE ContentEntryFile SET mimeType = :mimeType  WHERE contentEntryFileUid = :cefsUid")
    public abstract void updateMimeType(String mimeType, long cefsUid);

    @UmQuery("SELECT ContentEntryFile.*, ContentEntryFileStatus.* " +
            "FROM ContentEntryFile " +
            "LEFT JOIN ContentEntryContentEntryFileJoin ON ContentEntryFile.contentEntryFileUid = ContentEntryContentEntryFileJoin.cecefjContentEntryFileUid " +
            "LEFT JOIN ContentEntryFileStatus ON ContentEntryFile.contentEntryFileUid = ContentEntryFileStatus.cefsContentEntryFileUid " +
            "WHERE ContentEntryContentEntryFileJoin.cecefjContentEntryUid = :contentEntryUid " +
            "AND ContentEntryFileStatus.filePath IS NOT NULL " +
            "ORDER BY lastModified DESC LIMIT 1")
    public abstract void findLatestCompletedFileForEntry(long contentEntryUid,
                                                         UmCallback<ContentEntryFileWithStatus> callback);

    @UmQuery("DELETE FROM ContentEntryFile WHERE contentEntryFileUid = :statusUid")
    public abstract void deleteByUid(long statusUid);

}
