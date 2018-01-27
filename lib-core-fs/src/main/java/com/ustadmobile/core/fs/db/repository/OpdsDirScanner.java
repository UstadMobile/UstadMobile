package com.ustadmobile.core.fs.db.repository;

import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.fs.contenttype.ContentTypePluginFs;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.ContainerFileEntry;
import com.ustadmobile.lib.db.entities.ContainerFileWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntry;

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


    @Override
    public void run() {
        File dirFile = new File(dirName);
        String fileExtension;
        for(File file : dirFile.listFiles()) {
            fileExtension = UMFileUtil.getExtension(file.getName());
            ContainerFileWithRelations containerFile = dbManager.getContainerFileDao()
                    .findContainerFileByDirPath(file.getAbsolutePath());
            ArrayList<ContainerFileEntry> containerFileEntries = new ArrayList<>();

            if(containerFile == null){
                containerFile = new ContainerFileWithRelations();
                containerFile.setNormalizedPath(file.getAbsolutePath());
                containerFile.setDirPath(file.getParentFile().getAbsolutePath());
                long containerFileId = dbManager.getContainerFileDao().insert(containerFile);
                containerFile.setId((int)containerFileId);
            }



            for(ContentTypePlugin plugin : UstadMobileSystemImpl.getInstance().getSupportedContentTypePlugins()) {
                if(!plugin.getFileExtensions().contains(fileExtension))
                    continue;

                if(!(plugin instanceof ContentTypePluginFs))
                    continue;

                List<? extends OpdsEntry> entriesInFile = ((ContentTypePluginFs)plugin)
                        .getEntries(file.getAbsolutePath(), dbManager.getContext());

                if(entriesInFile == null)
                    continue;


                for(OpdsEntry entry : entriesInFile) {
                    ContainerFileEntry fileEntry = new ContainerFileEntry();
                    fileEntry.setOpdsEntryUuid(entry.getId());
                    fileEntry.setContainerFileId(containerFile.getId());
                    fileEntry.setContainerEntryId(entry.getItemId());
                    containerFileEntries.add(fileEntry);
                }

                //now persist everything for this file
                dbManager.getContainerFileEntryDao().insert(containerFileEntries);
                dbManager.getOpdsEntryDao().insertList(new ArrayList<>(entriesInFile));
                break;
            }



        }



    }
}
