package com.ustadmobile.core.fs.db;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.ContainerFileEntryWithContainerFile;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        DbManager.getInstance(context).getContainerFileDao().deleteContainerFileAndRelations(
                context,containerFile);
        File file = new File(containerFile.getNormalizedPath());
        return file.delete();
    }

    public int deleteAllContainerFilesByEntryId(Object context, String entryId){
        List<ContainerFileEntryWithContainerFile> containerFileEntries = DbManager.getInstance(context)
                .getContainerFileEntryDao().findContainerFileEntriesWithContainerFileByEntryId(entryId);

        int deleteCount = 0;
        for(ContainerFileEntryWithContainerFile containerFileEntry : containerFileEntries) {
            if(deleteContainerFile(context, containerFileEntry.getContainerFile())) {
                deleteCount++;
            }
        }

        if(deleteCount > 0 && deleteCount == containerFileEntries.size()){//All entries have been deleted
            DbManager.getInstance(context).getOpdsEntryStatusCacheDao().handleContainerDeleted(entryId);
        }

        return deleteCount;
    }

}
