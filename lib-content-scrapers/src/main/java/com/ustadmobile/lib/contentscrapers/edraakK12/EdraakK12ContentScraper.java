package com.ustadmobile.lib.contentscrapers.edraakK12;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.ShrinkerUtil;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ARABIC_FONT_BOLD;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ARABIC_FONT_REGULAR;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ComponentType;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EDRAAK_CSS_FILENAME;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EDRAAK_JS_FILENAME;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.INDEX_HTML;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.JQUERY_JS;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.LAST_MODIFIED_TXT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MATERIAL_CSS;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MATERIAL_JS;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.QUESTIONS_JSON;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TINCAN_FILENAME;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.VIDEO_FILENAME_MP4;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.VIDEO_FILENAME_WEBM;


/**
 * Edraak Courses are identified by the component type ImportedComponent on the root json file
 * <p>
 * The course content is found in the object called target_component
 * The target_component have 2 types for component type: Test and Online
 * The Online component type has 2 children:- one with component type Video and the other is Exercise
 * <p>
 * Video Component Type will have a list of encoded videos that contain the url link and its size.
 * Exercise Component Type will have an object called question_set which has a list of questions with all its content
 * <p>
 * The Test component type is the same as Exercise component type
 */
public class EdraakK12ContentScraper implements Runnable {

    private File containerDirectory;
    private int sqiUid;
    private ContentEntry parentEntry;
    private File destinationDirectory;
    boolean contentUpdated = false;
    private URL scrapUrl;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <edraak k12 json url> <file destination><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }
        UMLogUtil.setLevel(args.length == 3 ? args[2] : "");
        UMLogUtil.logInfo("main url for edraak = " + args[0]);
        UMLogUtil.logInfo("main file destination = " + args[1]);
        try {
            new EdraakK12ContentScraper(args[0], new File(args[1])).scrapeContent();
        } catch (IOException e) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logError("Exception running scrapeContent");
        }
    }

    public EdraakK12ContentScraper(String url, File destinationDir) throws MalformedURLException {
        this.scrapUrl = new URL(url);
        this.destinationDirectory = destinationDir;
    }

    public EdraakK12ContentScraper(URL scrapeUrl, File destinationDirectory, File containerDir, ContentEntry parent, int sqiUid) {
        this.destinationDirectory = destinationDirectory;
        this.containerDirectory = containerDir;
        this.scrapUrl = scrapeUrl;
        this.parentEntry = parent;
        this.sqiUid = sqiUid;
    }

    public static String generateUrl(String baseUrl, String contentId, int programId) {
        return baseUrl + "component/" + contentId + "/?states_program_id=" + programId;
    }

    @Override
    public void run() {
        System.gc();
        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        ContainerDao containerDao = repository.getContainerDao();
        ScrapeQueueItemDao queueDao = db.getScrapeQueueItemDao();


        long startTime = System.currentTimeMillis();
        UMLogUtil.logInfo("Started scraper url " + scrapUrl + " at start time: " + startTime);
        queueDao.setTimeStarted(sqiUid, startTime);

        boolean successful = false;
        try {
            scrapeContent();
            successful = true;
            if (hasContentUpdated()) {
                ContentScraperUtil.insertContainer(containerDao, parentEntry, true, ScraperConstants.MIMETYPE_ZIP,
                        destinationDirectory.lastModified(), destinationDirectory, db, repository, containerDirectory);

            }
        } catch (Exception e) {
            UMLogUtil.logError(ExceptionUtils.getMessage(e));
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
            File lastModified = new File(destinationDirectory.getParentFile(), destinationDirectory.getName() + LAST_MODIFIED_TXT);
            ContentScraperUtil.deleteFile(lastModified);
        }

        queueDao.updateSetStatusById(sqiUid, successful ? ScrapeQueueItemDao.STATUS_DONE : ScrapeQueueItemDao.STATUS_FAILED);
        queueDao.setTimeFinished(sqiUid, System.currentTimeMillis());
        long duration = System.currentTimeMillis() - startTime;
        UMLogUtil.logInfo("Ended scrape for url " + scrapUrl + " in duration: " + duration);

    }

    /**
     * Given a url and a directory, download all its content and save it in a directory
     *
     * @throws IOException
     */
    public void scrapeContent() throws IOException {

        destinationDirectory.mkdirs();

        ContentResponse response;
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) scrapUrl.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            response = new GsonBuilder().create().fromJson(IOUtils.toString(urlConnection.getInputStream(), UTF_ENCODING), ContentResponse.class);

            File lastModified = new File(destinationDirectory.getParentFile(), destinationDirectory.getName() + LAST_MODIFIED_TXT);
            contentUpdated = ContentScraperUtil.isFileContentsUpdated(lastModified, (response.updated != null && !response.updated.isEmpty()) ? response.updated :
                    (response.created != null && !response.created.isEmpty()) ? response.created :
                            String.valueOf(System.currentTimeMillis()));

            if (!contentUpdated) {
                return;
            }

            if (ContentScraperUtil.fileHasContent(destinationDirectory)) {
                FileUtils.deleteDirectory(destinationDirectory);
                destinationDirectory.mkdirs();
            }

        } catch (IOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("JSON INVALID for url " + scrapUrl.toString(), e.getCause());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }


        if (!ContentScraperUtil.isImportedComponent(response.component_type))
            throw new IllegalArgumentException("Not an imported content type! for id" + response.id);

        if (response.target_component == null || response.target_component.children == null)
            throw new IllegalArgumentException("Null target component, or target component children are null for id " + response.id);


        boolean hasVideo = false;
        boolean hasQuestions = false;
        String exceptionQuestion = "";
        String exceptionVideo = "";

        List<ContentResponse> questionsList = getQuestionSet(response);
        try {
            downloadQuestions(questionsList, destinationDirectory, scrapUrl);
            hasQuestions = true;
        } catch (IllegalArgumentException e) {
            exceptionQuestion = ExceptionUtils.getStackTrace(e);
            UMLogUtil.logDebug("The question set was not available for response id " + response.id);
        }

        if (ComponentType.ONLINE.getType().equalsIgnoreCase(response.target_component.component_type)) {

            // Contains children which have video
            for (ContentResponse children : response.target_component.children) {

                if (ScraperConstants.ComponentType.VIDEO.getType().equalsIgnoreCase(children.component_type)) {

                    try {
                        downloadVideo(children);
                        hasVideo = true;
                    } catch (IllegalArgumentException e) {
                        exceptionVideo = ExceptionUtils.getStackTrace(e);
                        UMLogUtil.logDebug("Video was unable to download or had no video for response id" + response.id);
                    }
                }

            }
        }

        if (!hasVideo && !hasQuestions) {
            throw new IllegalArgumentException(
                    exceptionQuestion + "\n" +
                            exceptionVideo +
                            "\nNo Video or Questions found in this id " + response.id);
        }


        File contentJsonFile = new File(destinationDirectory, ScraperConstants.CONTENT_JSON);
        if (!ContentScraperUtil.fileHasContent(contentJsonFile)) {
            // store the json in a file after modifying image links
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            String jsonString = gson.toJson(response);
            FileUtils.writeStringToFile(contentJsonFile, jsonString, ScraperConstants.UTF_ENCODING);
        }

        try {
            String index = UMIOUtils.readToString(getClass().getResourceAsStream(ScraperConstants.EDRAAK_INDEX_HTML_TAG), UTF_ENCODING);
            Document doc = Jsoup.parse(index, UTF_ENCODING);
            doc.head().selectFirst("title").text(response.title);
            FileUtils.writeStringToFile(new File(destinationDirectory, INDEX_HTML), doc.toString(), UTF_ENCODING);

            checkBeforeCopyToFile(ScraperConstants.JS_TAG, new File(destinationDirectory, JQUERY_JS));
            checkBeforeCopyToFile(ScraperConstants.MATERIAL_CSS_LINK, new File(destinationDirectory, MATERIAL_CSS));
            checkBeforeCopyToFile(ScraperConstants.MATERIAL_JS_LINK, new File(destinationDirectory, MATERIAL_JS));
            checkBeforeCopyToFile(ScraperConstants.REGULAR_ARABIC_FONT_LINK, new File(destinationDirectory, ARABIC_FONT_REGULAR));
            checkBeforeCopyToFile(ScraperConstants.BOLD_ARABIC_FONT_LINK, new File(destinationDirectory, ARABIC_FONT_BOLD));
            checkBeforeCopyToFile(ScraperConstants.EDRAAK_CSS_LINK, new File(destinationDirectory, EDRAAK_CSS_FILENAME));
            checkBeforeCopyToFile(ScraperConstants.EDRAAK_JS_LINK, new File(destinationDirectory, EDRAAK_JS_FILENAME));

            File tinCanFile = new File(destinationDirectory, TINCAN_FILENAME);
            if (!ContentScraperUtil.fileHasContent(tinCanFile)) {

                ContentScraperUtil.generateTinCanXMLFile(destinationDirectory, response.title, "ar",
                        ScraperConstants.INDEX_HTML, ScraperConstants.MODULE_TIN_CAN_FILE,
                        scrapUrl.toString().substring(0, scrapUrl.toString().indexOf("component/")) + response.id,
                        "", "en");
            }

        } catch (IOException | TransformerException | ParserConfigurationException e) {
            UMLogUtil.logError("Failed to download the necessary files for response id " + response.id);
            throw new IOException(ExceptionUtils.getCause(e));
        }
    }

    private boolean downloadVideo(ContentResponse children) {
        if (children.video_info == null || children.video_info.encoded_videos == null || children.video_info.encoded_videos.isEmpty())
            throw new IllegalArgumentException("Component Type was Video but no video found for response id");

        ContentResponse.Encoded_videos videoHref = selectVideo(children.video_info.encoded_videos);
        URL videoUrl;
        try {
            videoUrl = new URL(scrapUrl, videoHref.url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("video Malformed url for response");
        }


        File videoFile = new File(destinationDirectory, VIDEO_FILENAME_MP4);
        File webmFile = new File(destinationDirectory, VIDEO_FILENAME_WEBM);
        if (ContentScraperUtil.isContentUpdated(ContentScraperUtil.parseServerDate(videoHref.modified), webmFile)) {
            try {
                FileUtils.copyURLToFile(videoUrl, videoFile);
                ShrinkerUtil.convertVideoToWebM(videoFile, webmFile);
                ContentScraperUtil.deleteFile(videoFile);
                return true;
            } catch (IOException e) {
                throw new IllegalArgumentException("Download Video Malformed url for response");
            }
        }
        return false;
    }

    /**
     * Check if any new content has been updated after scraping
     *
     * @return Return true if content has been updated
     */
    public boolean hasContentUpdated() {
        return contentUpdated;
    }

    private void checkBeforeCopyToFile(String fileToDownload, File locationToSave) throws IOException {
        if (!ContentScraperUtil.fileHasContent(locationToSave)) {
            FileUtils.copyToFile(getClass().getResourceAsStream(fileToDownload), locationToSave);
        }
    }

    /**
     * Find and return question set for imported content
     *
     * @param response depending on type of course (lesson or test), the question set is in different locations
     * @return the question set if found
     */
    List<ContentResponse> getQuestionSet(ContentResponse response) {

        if (ComponentType.ONLINE.getType().equalsIgnoreCase(response.target_component.component_type)) {

            for (ContentResponse children : response.target_component.children) {
                if (ScraperConstants.QUESTION_SET_HOLDER_TYPES.contains(children.component_type)) {

                    return children.question_set.children;
                }
            }
        } else if (ComponentType.TEST.getType().equalsIgnoreCase(response.target_component.component_type)) {

            return response.target_component.question_set.children;

        }
        return null;
    }


    /**
     * Given an array of questions, find the questions that have image tags in their html and save the image within the directory
     * Finally write the list into a file
     *
     * @param questionsList  list of questions from json response
     * @param destinationDir directory where folder for each question will be saved (for images)
     * @return true if any content updated was updated based on server date and compared to last modified date of folder
     */
    private boolean downloadQuestions(List<ContentResponse> questionsList, File destinationDir, URL url) {

        if (questionsList == null || questionsList.isEmpty())
            throw new IllegalArgumentException("No Questions were found in the question set");

        int exerciseUpdatedCount = 0;
        for (ContentResponse exercise : questionsList) {

            File exerciseDirectory = new File(destinationDir, exercise.id);
            exerciseDirectory.mkdirs();

            exercise.full_description = ContentScraperUtil.downloadAllResources(exercise.full_description, exerciseDirectory, url);
            exercise.explanation = ContentScraperUtil.downloadAllResources(exercise.explanation, exerciseDirectory, url);
            exercise.description = ContentScraperUtil.downloadAllResources(exercise.description, exerciseDirectory, url);

            if (ComponentType.MULTICHOICE.getType().equalsIgnoreCase(exercise.component_type)) {
                for (ContentResponse.Choice choice : exercise.choices) {
                    choice.description = ContentScraperUtil.downloadAllResources(choice.description, exerciseDirectory, url);
                }
            }

            for (ContentResponse.Hint hint : exercise.hints) {
                hint.description = ContentScraperUtil.downloadAllResources(hint.description, exerciseDirectory, url);
            }

            try {
                if (exerciseDirectory.listFiles().length > 0) {
                    exerciseUpdatedCount++;
                }
            } catch (NullPointerException ignored) {

            }


        }

        try {
            if (exerciseUpdatedCount > 0) {
                ContentScraperUtil.saveListAsJson(destinationDir, questionsList, QUESTIONS_JSON);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid Questions Json");
        }

        return exerciseUpdatedCount > 0;
    }


    /**
     * Given a list of videos, find the one with the smallest size
     *
     * @param encoded_videos list of videos from json response
     * @return chosen video url based on lowest size
     */
    private ContentResponse.Encoded_videos selectVideo(List<ContentResponse.Encoded_videos> encoded_videos) {

        ContentResponse.Encoded_videos selectedVideo = null;
        int videoSize = Integer.MAX_VALUE;

        for (ContentResponse.Encoded_videos videos : encoded_videos) {
            if (videos.file_size > 0 && videos.file_size < videoSize) {
                selectedVideo = videos;
            }
        }
        return selectedVideo;
    }


}
