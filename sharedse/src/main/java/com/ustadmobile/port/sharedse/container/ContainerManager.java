package com.ustadmobile.port.sharedse.container;


import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntry;
import com.ustadmobile.lib.db.entities.ContainerEntryFile;
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile;
import com.ustadmobile.lib.util.Base64Coder;
import com.ustadmobile.port.sharedse.util.UmFileUtilSe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public class ContainerManager {

    private UmAppDatabase db;

    private UmAppDatabase dbRepo;

    private Container container;

    private Hashtable<String, ContainerEntryWithContainerEntryFile> pathToEntryMap = new Hashtable<>();

    private File newFileDir;

    public ContainerManager(Container container, UmAppDatabase db, UmAppDatabase dbRepo,
                            String newFileStorageDir) {
        this.container = container;
        this.db = db;
        this.dbRepo = dbRepo;
        this.newFileDir = new File(newFileStorageDir);
        loadFromDb();
    }

    private void loadFromDb() {
        List<ContainerEntryWithContainerEntryFile> entryList = db.getContainerEntryDao()
                .findByContainer(container.getContainerUid());
        for(ContainerEntryWithContainerEntryFile entry : entryList){
            pathToEntryMap.put(entry.getCePath(), entry);
        }
    }


    public void addEntries(Map<File, String> fileToPathInContainerMap, boolean copy) throws IOException{
        Map<File, String> fileToMd5Map = new HashMap<>();

        byte[] buf = new byte[8 * 1024];

        for(File inFile : fileToPathInContainerMap.keySet()) {
            fileToMd5Map.put(inFile, new String(
                    Base64Coder.encode(UmFileUtilSe.getMd5Sum(inFile, buf))));
        }

        //now see if we already have these files
        List<ContainerEntryFile> existingFiles = db.getContainerEntryFileDao()
                .findEntriesByMd5Sums(new ArrayList<>(fileToMd5Map.values()));
        Map<String, ContainerEntryFile> md5ToExistingFileMap = new HashMap<>();
        for(ContainerEntryFile entryFile : existingFiles){
            md5ToExistingFileMap.put(entryFile.getCefMd5(), entryFile);
        }

        List<ContainerEntryWithContainerEntryFile> newContainerEntries = new ArrayList<>();
        for(Map.Entry<File, String> entry : fileToMd5Map.entrySet()) {
            String fileMd5 = entry.getValue();

            ContainerEntryFile containerEntryFile = md5ToExistingFileMap.get(fileMd5);
            if(containerEntryFile == null) {
                //this is not a duplicate - we need to add it
                File srcFile = entry.getKey();
                containerEntryFile = new ContainerEntryFile(fileMd5, srcFile.length(),
                        srcFile.length(), ContainerEntryFile.COMPRESSION_NONE);
                if(!copy) {
                    containerEntryFile.setCefPath(srcFile.getPath());
                }

                containerEntryFile.setCefUid(db.getContainerEntryFileDao().insert(containerEntryFile));

                if(copy) {
                    File dstFile = new File(newFileDir, String.valueOf(containerEntryFile.getCefUid()));
                    UmFileUtilSe.copyFile(srcFile, dstFile);
                    containerEntryFile.setCefPath(dstFile.getAbsolutePath());
                    db.getContainerEntryFileDao().updateFilePath(containerEntryFile.getCefUid(),
                            containerEntryFile.getCefPath());

                }
            }

            ContainerEntryWithContainerEntryFile containerEntry =
                    new ContainerEntryWithContainerEntryFile(
                        fileToPathInContainerMap.get(entry.getKey()),
                        containerEntryFile);
            newContainerEntries.add(containerEntry);
        }


        db.getContainerEntryDao().insertList(new ArrayList<>(newContainerEntries));
        for(ContainerEntryWithContainerEntryFile file : newContainerEntries) {
            pathToEntryMap.put(file.getCePath(), file);
        }
    }

    public ContainerEntryWithContainerEntryFile getEntry(String pathInContainer) {
        return pathToEntryMap.get(pathInContainer);
    }

    public InputStream getInputStream(ContainerEntry containerEntry) throws IOException {
        ContainerEntryWithContainerEntryFile entryWithFile = pathToEntryMap.get(containerEntry.getCePath());
        if(entryWithFile == null)
            throw new FileNotFoundException("Container UID #" + container.getContainerUid() +
                    " has no entry with path " + containerEntry.getCePath());

        return new FileInputStream(entryWithFile.getContainerEntryFile().getCefPath());
    }
}
