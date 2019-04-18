package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithContentEntryFileStatusAndContentEntryId;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithFilePath;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipFile;

public class KhanWorkJob {

    public static void main(String[] args) {

        new KhanWorkJob(new File(args[0]));

    }

    public KhanWorkJob(File parentFolder) {

        int countmp4 = 0;
        if (parentFolder.isDirectory()) {

            File[] childFolders = parentFolder.listFiles();

            if (childFolders != null && childFolders.length > 0) {

                for (File contentFolder : childFolders) {

                    File[] contentFiles = contentFolder.listFiles();

                    if (contentFiles != null && contentFiles.length == 2) {

                        // 1 folder and 1 zip
                        for (File folders : contentFiles) {

                            String ext = FilenameUtils.getExtension(folders.getName());
                            if (ext.equals("zip")) {
                                ZipFile zipFile;
                                try {
                                    zipFile = new ZipFile(folders.getAbsolutePath());
                                    if (zipFile.size() == 2) {
                                        zipFile.close();
                                        boolean isAdded = folders.delete();
                                        if (isAdded) {
                                            countmp4++;
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }


                    }

                }

            }


        }

        System.out.println("Changed " + countmp4 + " filePath");


        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        ContentEntryFileDao contentEntryFileDao = repository.getContentEntryFileDao();
        List<ContentEntryFileWithContentEntryFileStatusAndContentEntryId> khanFileList = contentEntryFileDao.findKhanFiles();

        int countzip = 0;
        int countvideo = 0;
        for (ContentEntryFileWithContentEntryFileStatusAndContentEntryId khanFile : khanFileList) {

            if (khanFile.getFilePath().contains(".zip")) {

                countzip++;
                contentEntryFileDao.updateMimeType(ScraperConstants.MIMETYPE_WEB_CHUNK, khanFile.getContentEntryFileUid());

            } else if (khanFile.getFilePath().contains(".mp4")) {

                try {
                    File file = new File(khanFile.getFilePath());
                    System.out.println(Files.probeContentType(file.toPath()));

                    ContentEntryFile filEntry = new ContentEntryFile();
                    filEntry.setFileSize(file.length());
                    filEntry.setMd5sum(ContentScraperUtil.getMd5(file));
                    filEntry.setMimeType(ScraperConstants.MIMETYPE_MP4);
                    filEntry.setContentEntryFileUid(khanFile.getContentEntryFileUid());
                    contentEntryFileDao.updateFiles(file.length(),
                            ContentScraperUtil.getMd5(file),
                            ScraperConstants.MIMETYPE_MP4,
                            khanFile.getContentEntryFileUid());

                    countvideo++;

                } catch (IOException e) {
                    System.out.println("Crashed for file " + khanFile.getFilePath());
                }

            } else {
                System.out.println("File has a different mimetype " + khanFile.getFilePath());
            }

        }
        System.out.println("updated zip " + countzip + " filePath");
        System.out.println("updated video " + countvideo + " filePath");

    /*    for (ContentEntryFileStatus khan : khanList) {

            String path = khan.getFilePath();
            File zipFile = new File(path);

            File parentFolder = zipFile.getParentFile();
            File childFolder = new File(parentFolder, parentFolder.getName());

            if (childFolder.isDirectory()) {
                File[] list = childFolder.listFiles();

                if (list != null && list.length == 2) {
                    // this is the mp4 folder
                    for (File file : list) {
                        String ext = FilenameUtils.getExtension(file.getPath());
                        if (ext.endsWith("mp4")) {
                            boolean deleted = zipFile.deleteByDownloadSetUid();
                            if (deleted) {
                                countmp4++;
                            }
                        } else if (!ext.endsWith("txt")) {
                            System.out.println("Got new ext = " + ext);
                        }
                    }

                } else {
                    size += parentFolder.getTotalSpace();
                    count++;
                }

            }

        } */


    }

}
