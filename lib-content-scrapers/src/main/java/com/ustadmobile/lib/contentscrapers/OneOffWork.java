package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerEntryFileDao;
import com.ustadmobile.lib.db.entities.ContainerEntryFile;
import com.ustadmobile.port.sharedse.util.Base64Coder;
import com.ustadmobile.port.sharedse.util.UmFileUtilSe;

import java.io.File;
import java.io.IOException;

public class OneOffWork {

    public static void main(String[] args) throws IOException {
        new OneOffWork();

    }

    public OneOffWork() throws IOException {


        UmAppDatabase db = UmAppDatabase.getInstance(null);
        ContainerEntryFileDao entryFileDao = db.getContainerEntryFileDao();
        ContainerEntryFile containerEntryFile = entryFileDao.findByUid(500332);

        if(!containerEntryFile.getCefPath().contains("500332")){
            throw new IllegalArgumentException("not the correct file");
        }

        File file = new File(containerEntryFile.getCefPath());



        byte[] buf = new byte[8 * 1024];

        String md5 = Base64Coder.encodeToString(UmFileUtilSe.getMd5Sum(file, buf));

        containerEntryFile.setCefMd5(md5);
        containerEntryFile.setCeTotalSize(file.length());

        entryFileDao.update(containerEntryFile);


    }

}
