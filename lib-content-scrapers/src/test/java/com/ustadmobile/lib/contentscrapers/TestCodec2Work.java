package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ContainerEntryDao;
import com.ustadmobile.core.db.dao.ContainerEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntry;
import com.ustadmobile.lib.db.entities.ContainerEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntry;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestCodec2Work {


    private void initDb() {


        UmAppDatabase db = UmAppDatabase.getInstance(null);
        db.clearAllTables();
        UmAppDatabase repo = db.getRepository("https://localhost", "");

        ContainerDao containerDoa = repo.getContainerDao();
        ContentEntryDao contentEntryDao = repo.getContentEntryDao();
        ContainerEntryDao containerEntryDao = db.getContainerEntryDao();
        ContainerEntryFileDao containerEntryFileDao = db.getContainerEntryFileDao();

        ContentEntry entry = new ContentEntry();
        entry.setSourceUrl("khan-id://1233");
        entry.setEntryId("abc");
        entry.setPublisher("Khan Academy");
        entry.setContentEntryUid(10);
        contentEntryDao.insert(entry);

        Container container = new Container();
        container.setMimeType("video/mp4");
        container.setContainerUid(5);
        container.setContainerContentEntryUid(10);
        containerDoa.insert(container);

        ContainerEntry containerEntry = new ContainerEntry();
        containerEntry.setCeContainerUid(5);
        containerEntry.setCePath("images/abc.mp4");
        containerEntry.setCeCefUid(6);
        containerEntry.setCeUid(7);
        containerEntryDao.insert(containerEntry);

        ContainerEntryFile containerEntryFile = new ContainerEntryFile();
        containerEntryFile.setCefUid(11);
        containerEntryFile.setCefPath("1233");
        containerEntryFileDao.insert(containerEntryFile);



    }

    @Test
    public void test() {

        initDb();

        File khanfolder = new File("D:\\content\\test-khan\\en\\");
        File containerFolder = new File("D:\\content\\test-container\\");

        Codec2KhanWork.main(new String[]{khanfolder.getPath(), containerFolder.getPath()});

        
    }

    @Test
    public void test2(){

        Path path = Paths.get("container", "container.xml");


    }




}
