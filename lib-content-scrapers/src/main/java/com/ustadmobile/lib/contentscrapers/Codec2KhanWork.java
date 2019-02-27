package com.ustadmobile.lib.contentscrapers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.contentscrapers.util.SrtFormat;
import com.ustadmobile.lib.contentscrapers.util.VideoApi;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithContentEntryFileStatusAndContentEntryId;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.SUBTITLE_FILENAME;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.WEBM_EXT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ZIP_EXT;

public class Codec2KhanWork {

    public static void main(String[] args) {

        UMLogUtil.setLevel(args.length == 1 ? args[0] : "");

        new Codec2KhanWork();

    }

    public Codec2KhanWork() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Type type = new TypeToken<List<SrtFormat>>() {
        }.getType();

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        ContentEntryFileDao contentEntryFileDao = repository.getContentEntryFileDao();
        ContentEntryFileStatusDao statusDao = db.getContentEntryFileStatusDao();
        ContentEntryContentEntryFileJoinDao fileJoinDao = repository.getContentEntryContentEntryFileJoinDao();
        List<ContentEntryFileWithContentEntryFileStatusAndContentEntryId> khanFileList = contentEntryFileDao.findKhanFiles();

        for (ContentEntryFileWithContentEntryFileStatusAndContentEntryId khanFile : khanFileList) {
            try {

                UMLogUtil.logTrace("Started with khan file at " + khanFile.getFilePath());
                File mp4VideoFile = null;
                File contentFolder;
                if (khanFile.getFilePath().endsWith(".mp4")) {
                    mp4VideoFile = new File(khanFile.getFilePath());
                    contentFolder = mp4VideoFile.getParentFile();
                } else if (khanFile.getFilePath().endsWith(".zip")) {
                    File zip = new File(khanFile.getFilePath());
                    contentFolder = new File(UMFileUtil.stripExtensionIfPresent(zip.getPath()));
                } else {
                    UMLogUtil.logError("Found a file path that was not zip or mp4");
                    continue;
                }
                File parentFolder = contentFolder.getParentFile();
                if (parentFolder.getPath().endsWith("/en")) {
                    UMLogUtil.logTrace("Got folder with parent en");
                    parentFolder = new File(parentFolder, UMFileUtil.stripExtensionIfPresent(contentFolder.getName()));
                    contentFolder = new File(parentFolder, parentFolder.getName());
                }
                UMLogUtil.logTrace("Got the parent folder " + parentFolder.getPath());


                // delete if greater than 420mb - go to next entry
                if (khanFile.getFileSize() > 440401920) {

                    statusDao.deleteByUid(khanFile.getCefsUid());
                    contentEntryFileDao.deleteByUid(khanFile.getContentEntryFileUid());
                    fileJoinDao.deleteByUid(khanFile.getContentEntryFileUid(), khanFile.getContentEntryUid());
                    UMLogUtil.logTrace("found a file that was larger than 420mb at  " + khanFile.getFilePath());
                    continue;
                }

                String entryId = khanFile.getEntryId();
                File content = null;
                if (mp4VideoFile != null) {
                    content = new File(mp4VideoFile.getPath());
                }
                URL videoApiUrl = new URL("http://www.khanacademy.org/api/v1/videos/" + entryId);
                VideoApi videoApi = gson.fromJson(IOUtils.toString(videoApiUrl, UTF_ENCODING), VideoApi.class);
                String youtubeId = videoApi.youtube_id;

                if (videoApi.download_urls != null) {

                    String videoUrl = videoApi.download_urls.mp4;
                    if (videoUrl == null || videoUrl.isEmpty()) {
                        videoUrl = videoApi.download_urls.mp4Low;
                        if (videoUrl == null) {
                            UMLogUtil.logError("Video was not available in any format for url: " + khanFile.getFilePath());
                        }
                    }
                    if (videoUrl != null) {
                        content = new File(contentFolder, FilenameUtils.getName(videoUrl));
                        FileUtils.copyURLToFile(new URL(videoUrl), content);
                        UMLogUtil.logTrace("Got the video mp4");
                    } else {
                        UMLogUtil.logError("Did not get the video mp4 for " + khanFile.getFilePath());
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
                    UMLogUtil.logInfo("No subtitle for youtube link " + youtubeId + " and fileUid " + khanFile.getContentEntryFileUid());
                }

                File webMFile = new File(contentFolder, UMFileUtil.stripExtensionIfPresent(content.getName()) + WEBM_EXT);
                ShrinkerUtil.convertKhanVideoToWebMAndCodec2(content, webMFile);

                UMLogUtil.logTrace("Converted Coddec2");

                ContentScraperUtil.deleteFile(content);
                ContentScraperUtil.deleteFile(mp4VideoFile);


                File zipFile = new File(parentFolder, contentFolder.getName() + ZIP_EXT);
                ContentScraperUtil.zipDirectory(contentFolder,
                        zipFile.getName(),
                        parentFolder);

                contentEntryFileDao.updateFiles(zipFile.length(),
                        ContentScraperUtil.getMd5(zipFile),
                        ScraperConstants.MIMETYPE_KHAN,
                        khanFile.getContentEntryFileUid());

                UMLogUtil.logTrace("Zipped");

                ContentEntryFileStatus fileStatus = new ContentEntryFileStatus();
                fileStatus.setCefsUid(khanFile.getCefsUid());
                fileStatus.setFilePath(zipFile.getPath());
                fileStatus.setCefsContentEntryFileUid(khanFile.getContentEntryFileUid());
                statusDao.update(fileStatus);

                UMLogUtil.logDebug("Completed conversion of " + khanFile.getFilePath());

            } catch (Exception e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Error converting for video " + khanFile.getFilePath());
            }


        }


    }


}
