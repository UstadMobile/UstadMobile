package com.ustadmobile.port.sharedse.container;


import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntry;
import com.ustadmobile.lib.db.entities.ContainerEntryFile;
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile;
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5;
import com.ustadmobile.lib.util.Base64Coder;
import com.ustadmobile.port.sharedse.util.UmFileUtilSe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class ContainerManager {

    private UmAppDatabase db;

    private UmAppDatabase dbRepo;

    private Container container;

    private Hashtable<String, ContainerEntryWithContainerEntryFile> pathToEntryMap = new Hashtable<>();

    private File newFileDir;


    public static final int OPTION_COPY = 1;

    public static final int OPTION_MOVE = 2;

    public static final int OPTION_UPDATE_TOTALS = 4;

    public ContainerManager(Container container, UmAppDatabase db, UmAppDatabase dbRepo,
                            String newFileStorageDir) {
        this.container = container;
        this.db = db;
        this.dbRepo = dbRepo;
        this.newFileDir = new File(newFileStorageDir);

        loadFromDb();
    }

    public ContainerManager(Container container, UmAppDatabase db, UmAppDatabase dbRepo) {
        this.container = container;
        this.db = db;
        this.dbRepo = dbRepo;
        loadFromDb();
    }

    private void loadFromDb() {
        List<ContainerEntryWithContainerEntryFile> entryList = db.getContainerEntryDao()
                .findByContainer(container.getContainerUid());
        for (ContainerEntryWithContainerEntryFile entry : entryList) {
            pathToEntryMap.put(entry.getCePath(), entry);
        }
    }


    public void addEntries(Map<File, String> fileToPathInContainerMap, int options) throws IOException {
        if(newFileDir == null)
            throw new IllegalStateException("ContainerManager in read-only mode: no directory for new files set");

        Map<File, String> fileToMd5Map = new HashMap<>();

        byte[] buf = new byte[8 * 1024];

        for (File inFile : fileToPathInContainerMap.keySet()) {
            fileToMd5Map.put(inFile, new String(
                    Base64Coder.encode(UmFileUtilSe.getMd5Sum(inFile, buf))));
        }

        //now see if we already have these files
        List<ContainerEntryFile> existingFiles = db.getContainerEntryFileDao()
                .findEntriesByMd5Sums(new ArrayList<>(fileToMd5Map.values()));
        Map<String, ContainerEntryFile> md5ToExistingFileMap = new HashMap<>();
        for (ContainerEntryFile entryFile : existingFiles) {
            md5ToExistingFileMap.put(entryFile.getCefMd5(), entryFile);
        }

        List<ContainerEntryWithContainerEntryFile> newContainerEntries = new ArrayList<>();
        for (Map.Entry<File, String> entry : fileToMd5Map.entrySet()) {
            String fileMd5 = entry.getValue();

            ContainerEntryFile containerEntryFile = md5ToExistingFileMap.get(fileMd5);
            if (containerEntryFile == null) {
                //this is not a duplicate - we need to add it
                File srcFile = entry.getKey();
                containerEntryFile = new ContainerEntryFile(fileMd5, srcFile.length(),
                        srcFile.length(), ContainerEntryFile.COMPRESSION_NONE);
                if (!((options & OPTION_COPY) == OPTION_COPY)) {
                    containerEntryFile.setCefPath(srcFile.getPath());
                }

                containerEntryFile.setCefUid(db.getContainerEntryFileDao().insert(containerEntryFile));

                if ((options & OPTION_COPY) == OPTION_COPY) {
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
                            container, containerEntryFile);
            newContainerEntries.add(containerEntry);
        }


        db.getContainerEntryDao().insertList(new ArrayList<>(newContainerEntries));
        for (ContainerEntryWithContainerEntryFile file : newContainerEntries) {
            pathToEntryMap.put(file.getCePath(), file);
        }

        if((options & OPTION_UPDATE_TOTALS) == OPTION_UPDATE_TOTALS){
            container.setCntNumEntries(pathToEntryMap.size());
            long sizeCount = 0;
            for(ContainerEntryWithContainerEntryFile entry : pathToEntryMap.values()) {
                if(entry.getContainerEntryFile() != null)
                    sizeCount += entry.getContainerEntryFile().getCeCompressedSize();
            }
            container.setFileSize(sizeCount);

            dbRepo.getContainerDao().updateContainerSizeAndNumEntries(container.getContainerUid());
        }
    }

    public void addEntries(Map<File, String> fileToPathInContainerMap, boolean copy) throws IOException {
        addEntries(fileToPathInContainerMap, (copy ? OPTION_COPY : 0) | OPTION_UPDATE_TOTALS);
    }

    public void addEntry(File file, String pathInContainer, boolean copy) throws IOException{
        Map<File, String> fileToPathMap = new HashMap<>();
        fileToPathMap.put(file, pathInContainer);
        addEntries(fileToPathMap, copy);
    }

    public void addEntry(File file, String pathInContainer, int options) throws IOException{
        Map<File, String> fileToPathMap = new HashMap<>();
        fileToPathMap.put(file, pathInContainer);
        addEntries(fileToPathMap, options);
    }


    /**
     * Add all the entries from the given zip
     * @param zipFile
     * @throws IOException
     */
    public void addEntriesFromZip(ZipFile zipFile, int flags) throws IOException{
        File tmpDir = null;
        try {
            tmpDir = File.createTempFile("container" + container.getContainerUid(), "uziptmp");
            if(!(tmpDir.delete() && tmpDir.mkdirs())){
                throw new IOException("Could not make temporary directory");
            }

            Map<File, String> filesToAddMap = new HashMap<>();
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                if(entry.isDirectory())
                    continue;

                File unzipTmpFile = new File(tmpDir, entry.getName());
                File parentDirFile = unzipTmpFile.getParentFile();
                if(!parentDirFile.isDirectory() && !parentDirFile.mkdirs())
                    throw new IOException("Could not make directory for: " + unzipTmpFile.getAbsolutePath());

                try (
                        InputStream zipIn = zipFile.getInputStream(entry);
                        OutputStream fileOut = new FileOutputStream(unzipTmpFile);
                ) {
                    UMIOUtils.readFully(zipIn, fileOut);
                }catch(IOException e) {
                    throw e;
                }

                filesToAddMap.put(unzipTmpFile, entry.getName());
            }

            addEntries(filesToAddMap, flags);
        }catch(IOException e) {
            throw e;
        }finally {
            if(tmpDir != null)
                UmFileUtilSe.deleteRecursively(tmpDir);
        }


    }


    /**
     * Go through the list of items to be added to the container. Any item that we already have
     * (searching by md5 sum) will be linked. A list of those items that are not yet in this
     * container will be returned
     *
     * @return
     */
    public Collection<ContainerEntryWithMd5> linkExistingItems(List<ContainerEntryWithMd5> newEntriesList) {
        Map<String, ContainerEntryWithMd5> newEntryPathToEntryMap = new HashMap<>();
        for(ContainerEntryWithMd5 item : newEntriesList) {
            newEntryPathToEntryMap.put(item.getCePath(), item);
        }

        //remove those already in our list
        for(ContainerEntryWithContainerEntryFile existingEntry : pathToEntryMap.values()){
            newEntryPathToEntryMap.remove(existingEntry.getCePath());
        }

        Map<String, String> remainingMd5ToPathMap = new HashMap<>();
        for(ContainerEntryWithMd5 item : newEntryPathToEntryMap.values()) {
            remainingMd5ToPathMap.put(item.getCefMd5(), item.getCePath());
        }

        //look for those items which are missing for which we have the md5
        List<ContainerEntryFile> existingFiles = db.getContainerEntryFileDao()
                .findEntriesByMd5Sums(new ArrayList<>(remainingMd5ToPathMap.keySet()));
        List<ContainerEntryWithContainerEntryFile> newEntries = new ArrayList<>();
        for(ContainerEntryFile existingFile : existingFiles) {
            String entryPath = remainingMd5ToPathMap.get(existingFile.getCefMd5());
            newEntries.add(new ContainerEntryWithContainerEntryFile(
                    entryPath, container, existingFile));
            newEntryPathToEntryMap.remove(entryPath);
        }
        db.getContainerEntryDao().insertList(new ArrayList<>(newEntries));

        return newEntryPathToEntryMap.values();
    }


    public ContainerEntryWithContainerEntryFile getEntry(String pathInContainer) {
        return pathToEntryMap.get(pathInContainer);
    }

    public List<ContainerEntryWithContainerEntryFile> getAllEntries() {
        return new ArrayList<>(pathToEntryMap.values());
    }

    public InputStream getInputStream(ContainerEntry containerEntry) throws IOException {
        ContainerEntryWithContainerEntryFile entryWithFile = pathToEntryMap.get(containerEntry.getCePath());
        if (entryWithFile == null)
            throw new FileNotFoundException("Container UID #" + container.getContainerUid() +
                    " has no entry with path " + containerEntry.getCePath());

        return new FileInputStream(entryWithFile.getContainerEntryFile().getCefPath());
    }
}
