package com.ustadmobile.core.fs.db.repository;

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.fs.contenttype.ContentTypePluginFs;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.ContainerFileEntry;
import com.ustadmobile.lib.db.entities.ContainerFileWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 1/26/18.
 */

public class OpdsDirScanner implements Runnable{

    private DbManager dbManager;

    private List<String> dirNames;

    private OpdsEntry.OpdsItemLoadCallback callback;

    public OpdsDirScanner(DbManager dbManager, List<String> dirNames,
                          OpdsEntry.OpdsItemLoadCallback callback) {
        this.dbManager = dbManager;
        this.dirNames = dirNames;
        this.callback = callback;
    }

    public OpdsDirScanner(DbManager dbManager, List<String> dirNames) {
        this(dbManager, dirNames, null);
    }

    public OpdsDirScanner(DbManager dbManager) {
        this.dbManager = dbManager;
    }


    @Override
    public void run() {
        for(String dirName : dirNames) {
            File dirFile = new File(dirName);

            //make sure entries that are in the database in this directory are actually still there
            List<ContainerFile> containerFilesInDir = DbManager.getInstance(dbManager.getContext())
                    .getContainerFileDao().findFilesByDirectory(dirFile.getAbsolutePath());

            File file = null;
            for(ContainerFile containerFile : containerFilesInDir) {
                file = new File(containerFile.getNormalizedPath());
                if(!file.exists()){
                    dbManager.getContainerFileDao().deleteContainerFileAndRelations(dbManager.getContext(),
                            containerFile);
                }
            }

            File[] dirFilesList = dirFile.listFiles();
            if(dirFilesList == null)
                continue;//dir does not actually exist

            for(File scanfile : dirFilesList) {
                scanFile(scanfile);
            }
        }

        if(callback != null)
            callback.onDone(null);

    }

    public ContainerFileWithRelations scanFile(File file) {
        String fileExtension = UMFileUtil.getExtension(file.getName());
        ContainerFileWithRelations containerFile = dbManager.getContainerFileDao()
                .findContainerFileByPath(file.getAbsolutePath());
        ArrayList<ContainerFileEntry> containerFileEntries = new ArrayList<>();

        if(containerFile == null){
            containerFile = new ContainerFileWithRelations();
            containerFile.setNormalizedPath(file.getAbsolutePath());
            containerFile.setDirPath(file.getParentFile().getAbsolutePath());
        }else if(containerFile.getLastUpdated() > file.lastModified()) {
            return containerFile;
        }



        for(ContentTypePlugin plugin : UstadMobileSystemImpl.getInstance().getSupportedContentTypePlugins()) {
            if(!plugin.getFileExtensions().contains(fileExtension))
                continue;

            if(!(plugin instanceof ContentTypePluginFs))
                continue;

            List<OpdsEntryWithRelations> entriesInFile = ((ContentTypePluginFs)plugin)
                    .getEntries(file, dbManager.getContext());

            if(entriesInFile == null)
                continue;

            containerFile.setMimeType(plugin.getMimeTypes().get(0));
            containerFile.setFileSize(file.length());

            //insert the row into the database only once we see we can understand it
            if(containerFile.getId() == null) {
                long containerFileId = dbManager.getContainerFileDao().insert(containerFile);
                containerFile.setId((int)containerFileId);
            }

            for(OpdsEntry entry : entriesInFile) {
                ContainerFileEntry fileEntry = new ContainerFileEntry();
                fileEntry.setOpdsEntryUuid(entry.getUuid());
                fileEntry.setContainerFileId(containerFile.getId());
                fileEntry.setContainerEntryId(entry.getEntryId());
                containerFileEntries.add(fileEntry);
            }

            containerFile.setEntries(containerFileEntries);

            //delete old entry info on this file
            dbManager.getContainerFileEntryDao()
                    .deleteOpdsAndContainerFileEntriesByContainerFile(containerFile.getId());


            //now persist everything for this file
            dbManager.getContainerFileEntryDao().insert(containerFileEntries);

            for(OpdsEntryWithRelations entry : entriesInFile) {
                if(entry.getLinks() != null)
                    dbManager.getOpdsLinkDao().insert(entry.getLinks());
            }

            dbManager.getOpdsEntryDao().insertList(new ArrayList<>(entriesInFile));

            dbManager.getContainerFileDao().updateLastUpdatedById(containerFile.getId(),
                    System.currentTimeMillis());
            break;
        }

        return containerFile;
    }
}
