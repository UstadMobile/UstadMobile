package com.ustadmobile.core.fs.db;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.ContainerFileEntryWithContainerFile;

import java.io.File;
import java.util.List;

/**
 * Created by mike on 2/6/18.
 */

public class ContainerFileHelper {

    private static ContainerFileHelper instance;

    public static ContainerFileHelper getInstance() {
        if(instance == null)
            instance = new ContainerFileHelper();

        return instance;
    }

    public boolean deleteContainerFile(Object context, ContainerFile containerFile){
        UmAppDatabase.getInstance(context).getContainerFileDao().deleteContainerFileAndRelations(
                context,containerFile);
        File file = new File(containerFile.getNormalizedPath());
        return file.delete();
    }

    public int deleteAllContainerFilesByEntryId(Object context, String entryId){
        List<ContainerFileEntryWithContainerFile> containerFileEntries = UmAppDatabase.getInstance(context)
                .getContainerFileEntryDao().findContainerFileEntriesWithContainerFileByEntryId(entryId);

        int deleteCount = 0;
        for(ContainerFileEntryWithContainerFile containerFileEntry : containerFileEntries) {
            if(deleteContainerFile(context, containerFileEntry.getContainerFile())) {
                deleteCount++;
            }
        }

        if(deleteCount > 0 && deleteCount == containerFileEntries.size()){//All entries have been deleted
            UmAppDatabase.getInstance(context).getOpdsEntryStatusCacheDao().handleContainerDeleted(entryId);
        }

        return deleteCount;
    }

}
