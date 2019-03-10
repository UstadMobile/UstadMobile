package com.ustadmobile.lib.contentscrapers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ContainerEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.contentscrapers.util.SrtFormat;
import com.ustadmobile.lib.contentscrapers.util.VideoApi;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntry;
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile;
import com.ustadmobile.lib.db.entities.ContainerWithContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithContentEntryFileStatusAndContentEntryId;
import com.ustadmobile.port.sharedse.container.ContainerManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_KHAN;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.SUBTITLE_FILENAME;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.WEBM_EXT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ZIP_EXT;

public class Codec2KhanWork {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: <container file destination><khan folder destination><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }

        UMLogUtil.setLevel(args.length == 3 ? args[2] : "");

        new Codec2KhanWork(new File(args[0]), new File(args[1]));

    }

    public Codec2KhanWork(File containerFolder, File khanFolder) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type type = new TypeToken<List<SrtFormat>>() {
        }.getType();

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        ContainerDao containerDao = repository.getContainerDao();
        List<ContainerWithContentEntry> khanContainerList = containerDao.findKhanContainers();
        UMLogUtil.logTrace("Number of khan containers left to convert " + khanContainerList.size());
        ContainerEntryDao containerEntryDao = db.getContainerEntryDao();

        for (ContainerWithContentEntry khanFile : khanContainerList) {
            try {

                List<ContainerEntryWithContainerEntryFile> khanfileList = containerEntryDao.findByContainer(khanFile.getContainerUid());
                UMLogUtil.logTrace("Number of files in khanfileList " + khanfileList.size());
                if (khanFile.getFileSize() > 440401920) {

                    UMLogUtil.logTrace("Found a filesize of 420m for container" + khanFile.getSourceUrl());
                    containerEntryDao.deleteByContainerUid(khanFile.getContainerUid());
                    containerDao.deleteByUid(khanFile.getContainerUid());
                    continue;

                }

                String khanId = khanFile.getSourceUrl();
                khanId = khanId.substring(khanId.lastIndexOf("/") + 1);

                UMLogUtil.logTrace("Got the khanId from sourceUrl " + khanId);

                File mp4VideoFile = null;
                File contentFolder = null;
                long containerEntryUidToDelete = 0L;
                for (ContainerEntryWithContainerEntryFile file : khanfileList) {

                    String nameOfFile = file.getCePath();
                    if (nameOfFile.endsWith(".mp4")) {
                        containerEntryUidToDelete = file.getCeUid();
                        nameOfFile = nameOfFile.contains("/") ? nameOfFile.substring(nameOfFile.lastIndexOf("/") + 1) : nameOfFile;
                        contentFolder = Paths.get(khanFolder.getAbsolutePath(), khanId, khanId).toFile();
                        mp4VideoFile = new File(contentFolder, nameOfFile);
                    }
                }

                if (contentFolder == null) {
                    UMLogUtil.logError("Did not find the folder" + khanFile.getSourceUrl());
                    continue;
                }

                UMLogUtil.logTrace("Got the contentFolder  at " + contentFolder.getPath());
                UMLogUtil.logTrace("Got the mp4  at " + mp4VideoFile.getPath());

                String entryId = khanFile.getEntryId();
                File content = new File(mp4VideoFile.getPath());

                URL videoApiUrl = new URL("http://www.khanacademy.org/api/v1/videos/" + entryId);
                VideoApi videoApi = gson.fromJson(IOUtils.toString(videoApiUrl, UTF_ENCODING), VideoApi.class);
                String youtubeId = "";
                if (videoApi != null) {
                    youtubeId = videoApi.youtube_id;
                    if (videoApi.download_urls != null) {

                        String videoUrl = videoApi.download_urls.mp4;
                        if (videoUrl == null || videoUrl.isEmpty()) {
                            videoUrl = videoApi.download_urls.mp4Low;
                            if (videoUrl == null) {
                                UMLogUtil.logError("Video was not available in any format for url: " + khanFile.getSourceUrl());
                            }
                        }
                        if (videoUrl != null) {
                            content = new File(contentFolder, FilenameUtils.getName(videoUrl));
                            FileUtils.copyURLToFile(new URL(videoUrl), content);
                            UMLogUtil.logTrace("Got the video mp4");
                        } else {
                            UMLogUtil.logError("Did not get the video mp4 for " + khanFile.getSourceUrl());
                        }
                    }
                }


                try {
                    URL url = new URL("http://www.khanacademy.org/api/internal/videos/" + youtubeId + "/transcript");
                    String subtitleScript = IOUtils.toString(url, UTF_ENCODING);
                    List<SrtFormat> subTitleList = gson.fromJson(subtitleScript, type);
                    File srtFile = new File(contentFolder, SUBTITLE_FILENAME);
                    ContentScraperUtil.createSrtFile(subTitleList, srtFile);
                    UMLogUtil.logTrace("Created the subtitle file");

                } catch (Exception e) {
                    UMLogUtil.logInfo(ExceptionUtils.getStackTrace(e));
                    UMLogUtil.logInfo("No subtitle for youtube link " + youtubeId + " and fileUid " + khanFile.getContainerUid());
                }

                File webMFile = new File(contentFolder, UMFileUtil.stripExtensionIfPresent(content.getName()) + WEBM_EXT);
                ShrinkerUtil.convertKhanVideoToWebMAndCodec2(content, webMFile);

                UMLogUtil.logTrace("Converted Coddec2");

                ContentScraperUtil.deleteFile(content);
                if (!content.getPath().equals(mp4VideoFile.getPath())) {
                    ContentScraperUtil.deleteFile(mp4VideoFile);
                }


                ContainerManager containerManager = new ContainerManager(khanFile, db,
                        repository, containerFolder.getAbsolutePath());
                HashMap<File, String> fileMap = new HashMap<>();
                ContentScraperUtil.createContainerFromDirectory(contentFolder, fileMap);
                containerManager.addEntries(fileMap, true);
                containerDao.updateMimeType(MIMETYPE_KHAN, khanFile.getContainerUid());
                containerEntryDao.deleteByContainerEntryUid(containerEntryUidToDelete);

                UMLogUtil.logDebug("Completed conversion of " + khanFile.getSourceUrl());

            } catch (Exception e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Error converting for video " + khanFile.getSourceUrl());
            }


        }


    }


}
