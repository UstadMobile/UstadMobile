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

    private String dirName;

    public OpdsDirScanner(DbManager dbManager, String dirName) {
        this.dbManager = dbManager;
        this.dirName = dirName;
    }

    public OpdsDirScanner(DbManager dbManager) {
        this.dbManager = dbManager;
    }


    @Override
    public void run() {
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

        for(File scanfile : dirFile.listFiles()) {
            scanFile(scanfile );
        }
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

            //insert the row into the database only once we see we can understand it
            containerFile.setMimeType(plugin.getMimeTypes().get(0));
            long containerFileId = dbManager.getContainerFileDao().insert(containerFile);
            containerFile.setId((int)containerFileId);


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
