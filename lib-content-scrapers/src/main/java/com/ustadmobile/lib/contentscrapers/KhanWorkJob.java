package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

public class KhanWorkJob {

    public static void main(String[] args) {

        new KhanWorkJob();

    }

    public KhanWorkJob() {

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        ContentEntryFileStatusDao statusDao = db.getContentEntryFileStatusDao();

        List<ContentEntryFileStatus> khanList = statusDao.findKhan();
        System.out.println("khan list size = " + khanList.size());
        int countmp4 = 0;
        int count = 0;
        long size = 0;
        for (ContentEntryFileStatus khan : khanList) {

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
                            boolean deleted = zipFile.delete();
                            if (deleted) {
                                countmp4++;
                            }
                        } else if (!ext.endsWith("txt")) {
                            System.out.println("Got new ext = " + ext);
                        }
                    }

                }else{
                    size += parentFolder.getTotalSpace();
                    count++;
                }

            }

        }
        System.out.println("Changed " + countmp4 + " filePath");
        System.out.println("Average size of files that are not khan videos are " + (size/count));

    }

}
