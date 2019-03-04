package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestDeDupeUtil {

    public void init() throws IOException {

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        db.clearAllTables();
        UmAppDatabase repo = db.getRepository("https://localhost", "");

        ContentEntryContentEntryFileJoinDao fileJoinDao = repo.getContentEntryContentEntryFileJoinDao();
        ContentEntryFileDao fileDao = repo.getContentEntryFileDao();
        ContentEntryFileStatusDao fileStatusDao = db.getContentEntryFileStatusDao();
        ContentEntryContentEntryFileJoin join = new ContentEntryContentEntryFileJoin();
        join.setCecefjUid(1);
        join.setCecefjContentEntryUid(2);
        join.setCecefjContentEntryFileUid(3);
        fileJoinDao.insert(join);

        ContentEntryFile entryFile = new ContentEntryFile();
        entryFile.setContentEntryFileUid(3);
        fileDao.insert(entryFile);

        File tmpDir = Files.createTempDirectory("contentDirecory").toFile();

        File content = new File(tmpDir, "content.zip");
        FileUtils.copyToFile(getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/africanbooks/asb18187.epub"), content);

        ContentEntryFileStatus status = new ContentEntryFileStatus();
        status.setCefsContentEntryFileUid(3);
        status.setFilePath(content.getPath());
        status.setCefsUid(4);
        fileStatusDao.insert(status);


        ContentEntryContentEntryFileJoin join2 = new ContentEntryContentEntryFileJoin();
        join2.setCecefjUid(11);
        join2.setCecefjContentEntryUid(12);
        join2.setCecefjContentEntryFileUid(13);
        fileJoinDao.insert(join2);

        ContentEntryFile file2 = new ContentEntryFile();
        file2.setContentEntryFileUid(13);
        fileDao.insert(file2);

        File secondContent = new File(tmpDir, "secondcontent.zip");
        FileUtils.copyToFile(getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/shrinker/missing-image.epub"), secondContent);

        ContentEntryFileStatus secondStatus = new ContentEntryFileStatus();
        secondStatus.setCefsContentEntryFileUid(13);
        secondStatus.setFilePath(secondContent.getPath());
        secondStatus.setCefsUid(14);
        fileStatusDao.insert(secondStatus);



    }


    @Test
    public void test() throws IOException {

        init();

        File containerDir = Files.createTempDirectory("dedupemasterDirectory").toFile();

        DeDupeUtil.main(new String[]{containerDir.getPath()});


    }
}
