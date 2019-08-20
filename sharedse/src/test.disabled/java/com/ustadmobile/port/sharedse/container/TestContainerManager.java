package com.ustadmobile.port.sharedse.container;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.database.jdbc.DriverConnectionPoolInitializer;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile;
import com.ustadmobile.port.sharedse.util.UmFileUtilSe;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.sharedse.SharedSeTestConfig;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestContainerManager {

    private UmAppDatabase db;

    private UmAppDatabase dbRepo;

    private List<String> testFileNames = Arrays.asList("testfile1.png", "testfile2.png");

    private List<File> testFiles = new ArrayList<>();

    private File tmpDir;

    @Before
    public void setup() throws IOException {
        DriverConnectionPoolInitializer.bindDataSource("UmAppDatabase",
                SharedSeTestConfig.TESTDB_JDBCURL_UMMAPPDATABASE, true);
        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        dbRepo = db.getRepository("http://localhost/dummy/", "");
        db.clearAllTables();

        tmpDir = UmFileUtilSe.makeTempDir("TestContainerManager", "tmpdir");


        for(String testFileName : testFileNames) {
            File testFile = new File(tmpDir, testFileName);
            testFiles.add(testFile);

            try (
                InputStream resourceIn = getClass().getResourceAsStream(
                        "/com/ustadmobile/port/sharedse/container/" + testFileName);
                FileOutputStream fout = new FileOutputStream(testFile);
            ) {
                UMIOUtils.readFully(resourceIn, fout);
            }
        }
    }

    @After
    public void tearDown() {
        UmFileUtilSe.deleteRecursively(tmpDir);
    }


    @Test
    public void givenFileAdded_whenGetEntryCalled_shouldReturnInputStream() throws IOException{
        Container container = new Container();
        container.setContainerUid(dbRepo.getContainerDao().insert(container));
        ContainerManager manager = new ContainerManager(container, db, dbRepo,
                tmpDir.getAbsolutePath());
        Map<File, String> filesToAdd = new HashMap<>();
        filesToAdd.put(testFiles.get(0), "testfile1.png");
        manager.addEntries(filesToAdd, true);

        ContainerEntryWithContainerEntryFile entry = manager.getEntry("testfile1.png");
        InputStream fin = manager.getInputStream(entry);

        Assert.assertTrue("File retrieved through input stream has same md5",
                Arrays.equals(UmFileUtilSe.getMd5Sum(testFiles.get(0)), UmFileUtilSe.getMd5Sum(fin)));
        Assert.assertEquals("File size is recorded correctly", testFiles.get(0).length(),
                entry.getContainerEntryFile().getCeTotalSize());

        Container updatedContainer = dbRepo.getContainerDao().findByUid(container.getContainerUid());
        Assert.assertEquals("Container has 1 file", 1,
                updatedContainer.getCntNumEntries());
        Assert.assertEquals("Container 1 totalSize = size of file added",
                testFiles.get(0).length(), updatedContainer.getFileSize());
    }

    @Test
    public void givenSameFileAddedToMultipleContainers_whenGetFileCalled_thenShouldReturnSameContainerEntryFile() throws IOException{
        Container container1 = new Container();
        container1.setContainerUid(dbRepo.getContainerDao().insert(container1));
        ContainerManager manager1 = new ContainerManager(container1, db, dbRepo,
                tmpDir.getAbsolutePath());
        Map<File, String> filesToAdd1 = new HashMap<>();
        filesToAdd1.put(testFiles.get(0), "testfile1.png");
        manager1.addEntries(filesToAdd1, true);

        Container container2 = new Container();
        container2.setContainerUid(dbRepo.getContainerDao().insert(container2));
        ContainerManager manager2 = new ContainerManager(container2, db, dbRepo,
                tmpDir.getAbsolutePath());

        Map<File, String> filesToAdd2 = new HashMap<>();
        filesToAdd2.put(testFiles.get(0), "testfileothername.png");
        filesToAdd2.put(testFiles.get(1), "anotherimage.png");
        manager2.addEntries(filesToAdd2, true);

        Assert.assertEquals("When two identical files are added, the same content entry file is used",
                manager1.getEntry("testfile1.png").getCeCefUid(),
                manager2.getEntry("testfileothername.png").getCeCefUid());

        Assert.assertNull("Manager2 does not return a container entry if given a name from manager1",
                manager2.getEntry("testfile1.png"));

        Assert.assertEquals("Cotnainer2 num entries = 2", 2,
                dbRepo.getContainerDao().findByUid(container2.getContainerUid()).getCntNumEntries());
    }

    @Test
    public void givenExistingContainer_whenCopyToNewContainerCalled_thenShouldHaveSameContents() throws IOException{
        Container container = new Container();
        container.setContainerUid(dbRepo.getContainerDao().insert(container));
        ContainerManager manager = new ContainerManager(container, db, dbRepo,
                tmpDir.getAbsolutePath());

        Map<File, String> filesToAdd2 = new HashMap<>();
        filesToAdd2.put(testFiles.get(0), "testfileothername.png");
        filesToAdd2.put(testFiles.get(1), "anotherimage.png");
        manager.addEntries(filesToAdd2, true);

        ContainerManager copy = manager.copyToNewContainer();
        Assert.assertArrayEquals(UMIOUtils.readStreamToByteArray(manager.getInputStream(
                manager.getEntry("testfileothername.png"))),
                UMIOUtils.readStreamToByteArray(copy.getInputStream(
                        copy.getEntry("testfileothername.png"))));
        Assert.assertArrayEquals(UMIOUtils.readStreamToByteArray(manager.getInputStream(
                manager.getEntry("anotherimage.png"))),
                UMIOUtils.readStreamToByteArray(copy.getInputStream(
                        copy.getEntry("anotherimage.png"))));
    }

    @Test
    public void givenExistingContainer_whenFileAddedWithSamePath_thenFileShouldBeOverwritten() throws IOException {
        Container container = new Container();
        container.setContainerUid(dbRepo.getContainerDao().insert(container));
        ContainerManager manager = new ContainerManager(container, db, dbRepo, tmpDir.getAbsolutePath());

        String version1Content = "Version-1";
        String version2Content = "Version-2";

        File entryv1 = File.createTempFile("tmp", "testv1");
        FileOutputStream foutV1 = new FileOutputStream(entryv1);
        UMIOUtils.readFully(new ByteArrayInputStream(version1Content.getBytes()), foutV1);
        foutV1.close();

        File entryV2 = File.createTempFile("tmp", "testv2");
        FileOutputStream foutV2 = new FileOutputStream(entryV2);
        UMIOUtils.readFully(new ByteArrayInputStream(version2Content.getBytes()), foutV2);
        foutV2.close();

        manager.addEntry(entryv1, "test.txt",
                ContainerManager.OPTION_COPY | ContainerManager.OPTION_UPDATE_TOTALS);

        byte[] v1ContentFromContainer = UMIOUtils.readStreamToByteArray(manager.getInputStream(
                manager.getEntry("test.txt")));

        manager.addEntry(entryV2, "test.txt",
                ContainerManager.OPTION_COPY | ContainerManager.OPTION_UPDATE_TOTALS);
        byte[] v2ContentFRomContainer = UMIOUtils.readStreamToByteArray(manager.getInputStream(
                manager.getEntry("test.txt")));


        Assert.assertArrayEquals("After adding first version, got version 1 content",
                version1Content.getBytes(), v1ContentFromContainer);

        Assert.assertArrayEquals("After adding second version, got version 2 content",
                version2Content.getBytes(), v2ContentFRomContainer);
        Assert.assertEquals("After adding an entry with the same name, there is still only one entry",
                1, db.getContainerEntryDao().findByContainer(container.getContainerUid()).size());



    }


}
