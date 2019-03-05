package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryWithFileJoinStatus;
import com.ustadmobile.port.sharedse.container.ContainerManager;
import com.ustadmobile.port.sharedse.util.UmZipUtils;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EPUB_EXT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ZIP_EXT;

public class DeDupeUtil {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: <container destination><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }

        UMLogUtil.setLevel(args.length == 2 ? args[1] : "");
        UMLogUtil.logInfo(args[0]);

        new DeDupeUtil().dedup(new File(args[0]));

    }


    public void dedup(File containerDirectory) {

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");

        ContainerDao containerDao = repository.getContainerDao();
        ContentEntryContentEntryFileJoinDao fileJoinDao = repository.getContentEntryContentEntryFileJoinDao();

        List<ContentEntryWithFileJoinStatus> listOfEntries = fileJoinDao.findAllFiles();
        containerDirectory.mkdirs();
        for (ContentEntryWithFileJoinStatus entry : listOfEntries) {

            try {

                String originalFilePath = entry.getContentEntryFileStatus().getFilePath();
                File file = new File(originalFilePath);
                File directory = null;
                if (file.getPath().endsWith(ZIP_EXT) || file.getPath().endsWith(EPUB_EXT)) {
                    directory = new File(UMFileUtil.stripExtensionIfPresent(originalFilePath));
                    if (!directory.isDirectory()) {
                        directory.mkdirs();
                        UMLogUtil.logDebug("Zip file did not have directory " + file.getPath());
                        UmZipUtils.unzip(file, directory);
                    }
                }
                Map<File, String> filemap = new HashMap<>();
                if (directory != null) {
                    ContentScraperUtil.createContainerFromDirectory(directory, filemap);
                } else {
                    filemap.put(file, file.getName());
                }

                ContentEntryFile fileEntry = entry.getContentEntryFile();
                Container container = new Container();
                container.setLastModified(fileEntry.getLastModified());
                container.setFileSize(fileEntry.getFileSize());
                container.setMimeType(fileEntry.getMimeType());
                container.setRemarks(fileEntry.getRemarks());
                container.setMobileOptimized(fileEntry.isMobileOptimized());
                container.setContainerContentEntryUid(entry.getCecefjContentEntryUid());
                container.setContainerUid(containerDao.insert(container));

                ContainerManager manager = new ContainerManager(container, db,
                        repository, containerDirectory.getAbsolutePath());

                manager.addEntries(filemap, true);

                fileJoinDao.updateContainerId(container.getContainerUid(), entry.getCecefjUid());

                if (file.getPath().endsWith(ZIP_EXT) || file.getPath().endsWith(EPUB_EXT)) {

                    ContentScraperUtil.deleteFile(file);

                }
            } catch (Exception e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Failed to dedup for contentry " + entry.getCecefjContentEntryUid() +
                        " and file " + entry.getCecefjContentEntryFileUid());
            }

        }

    }

}
