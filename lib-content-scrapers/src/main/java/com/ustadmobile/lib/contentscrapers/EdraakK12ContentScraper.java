package com.ustadmobile.lib.contentscrapers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.*;


public class EdraakK12ContentScraper implements ContentScraper{

    public static void main(String[] args) {
        if(args.length != 2) {
            System.err.println("Usage: <edraak k12 json url> <file destination>");
            System.exit(1);
        }

        System.out.println(args[0]);
        System.out.println(args[1]);
        try {
            new EdraakK12ContentScraper().convert(args[0], new File(args[1]));
        }catch(IOException e) {
            System.err.println("Exception running convert");
            e.printStackTrace();
        }

    }

    public void convert(String contentId, int programId, String baseUrl, File destinationDir) throws IOException {
        convert(baseUrl + "component/" +  contentId + "/?states_program_id=" + programId, destinationDir);
    }
    /**
     *
     *  Given a url and a directory, download all its content and save it in a directory
     * @param urlString
     * @param destinationDir destination directory
     * @throws IOException
     */
    @Override
    public void convert(String urlString, File destinationDir) throws IOException {

        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed url", e);
        }

        destinationDir.mkdirs();

        ContentResponse response;
        try {
            response = ContentScraperUtil.parseJson(url);
        } catch (IOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("JSON INVALID", e.getCause());
        }

      //  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
      //  LocalDateTime dateTime = LocalDateTime.parse(response.updated, formatter);


        if(!ContentScraperUtil.isImportedComponent(response.component_type))
            throw new IllegalArgumentException("Not an imported content type!");

        if(response.target_component == null || response.target_component.children == null)
            throw new IllegalArgumentException("Null target component, or target component children are null");

        if(ComponentType.ONLINE.getType().equalsIgnoreCase(response.target_component.component_type)){

            // Contains children which have video and question set list
            for(ContentResponse children: response.target_component.children){

                if(ScraperConstants.ComponentType.VIDEO.getType().equalsIgnoreCase(children.component_type)){

                    if (children.video_info == null || children.video_info.encoded_videos == null || children.video_info.encoded_videos.isEmpty())
                        throw new IllegalArgumentException("Component Type was Video but no video found");

                    ContentResponse.Encoded_videos videoHref = selectVideo(children.video_info.encoded_videos);
                    URL videoUrl;
                    try {
                        videoUrl = new URL(url, videoHref.url);
                    } catch (MalformedURLException e) {
                        throw new IllegalArgumentException("Malformed url", e);
                    }


                    try {
                        ContentScraperUtil.downloadContent(videoUrl, destinationDir, VIDEO_MP4);
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Malformed url", e);
                    }

                } else if(ScraperConstants.QUESTION_SET_HOLDER_TYPES.contains(children.component_type)) {

                    List<ContentResponse> questionsList = children.question_set.children;
                    findAllExerciseImages(questionsList, destinationDir, url);

                }

            }

        }else if(ComponentType.TEST.getType().equalsIgnoreCase(response.target_component.component_type)){

            // list of questions sets
            List<ContentResponse> questionsList = response.target_component.question_set.children;
            findAllExerciseImages(questionsList, destinationDir, url);
        }


        // store the json in a file after modifying image links
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        File file = new File(destinationDir, ScraperConstants.CONTENT_JSON);

        FileWriter fileWriter = new FileWriter(file);
        String jsonString =  gson.toJson(response);
        fileWriter.write(jsonString);
        UMIOUtils.closeQuietly(fileWriter);

        writeFileToDirectory(ScraperConstants.JS_HTML_TAG, new File(destinationDir, INDEX_HTML));
        writeFileToDirectory(ScraperConstants.JS_TAG, new File(destinationDir, JQUERY_JS));
        writeFileToDirectory(ScraperConstants.MATERIAL_CSS_LINK, new File(destinationDir, ScraperConstants.MATERIAL_CSS));
        writeFileToDirectory(ScraperConstants.MATERIAL_JS_LINK, new File(destinationDir, ScraperConstants.MATERIAL_JS));
        writeFileToDirectory(ScraperConstants.REGULAR_ARABIC_FONT_LINK, new File(destinationDir, ScraperConstants.ARABIC_FONT_REGULAR));
        writeFileToDirectory(ScraperConstants.BOLD_ARABIC_FONT_LINK, new File(destinationDir, ScraperConstants.ARABIC_FONT_BOLD));


        // zip it all
        File zippedFile = new File(destinationDir.getParent(), response.id +".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zippedFile.toPath()), StandardCharsets.UTF_8)){
            Path sourceDirPath = Paths.get(destinationDir.toURI());
            Files.walk(sourceDirPath).filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                        try {
                            out.putNextEntry(zipEntry);
                            out.write(Files.readAllBytes(path));
                            out.closeEntry();
                        } catch (Exception e) {
                            System.err.println(e.getCause());
                        }
                    });
        }

    }

    private void writeFileToDirectory(String input, File file) {
        InputStream htmlIns = getClass().getResourceAsStream(input);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            int bytesRead;
            byte[] buffer = new byte[4096];
            while ((bytesRead = htmlIns.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            UMIOUtils.closeQuietly(htmlIns);
            UMIOUtils.closeQuietly(outputStream);
        }
    }


    /**
     *  Given an array of questions, find the questions that have image tags in their html and save the image within the directory
     *  Finally write the list into a file
     *
     * @param questionsList
     * @param destinationDir
     * @throws IOException
     */
    private void findAllExerciseImages(List<ContentResponse> questionsList, File destinationDir, URL url) throws IOException {

            if (questionsList.isEmpty())
                throw new IllegalArgumentException("No Questions were found in the question set");

            for (ContentResponse exercise : questionsList) {
                File exerciseDirectory = new File(destinationDir, exercise.id);
                exerciseDirectory.mkdirs();

                exercise.full_description = ContentScraperUtil.checkIfJsonObjectHasAttribute(exercise.full_description, IMG_TAG, exerciseDirectory, HtmlName.FULL_DESC.getName() + ScraperConstants.PNG_EXT, url);
                exercise.explanation = ContentScraperUtil.checkIfJsonObjectHasAttribute(exercise.explanation, IMG_TAG, exerciseDirectory, HtmlName.EXPLAIN.getName() + ScraperConstants.PNG_EXT, url);
                exercise.description = ContentScraperUtil.checkIfJsonObjectHasAttribute(exercise.description, IMG_TAG, exerciseDirectory, HtmlName.DESC + PNG_EXT, url);

                if (ComponentType.MULTICHOICE.getType().equalsIgnoreCase(exercise.component_type)) {
                    for (ContentResponse.Choice choice : exercise.choices) {
                        choice.description = ContentScraperUtil.checkIfJsonObjectHasAttribute(choice.description, IMG_TAG, exerciseDirectory, choice.item_id + ScraperConstants.PNG_EXT, url);
                    }
                }

                for (ContentResponse.Hint hint : exercise.hints) {
                    hint.description = ContentScraperUtil.checkIfJsonObjectHasAttribute(hint.description, IMG_TAG, exerciseDirectory, hint.item_id + ScraperConstants.PNG_EXT, url);
                }

            }

            try {
                saveQuestionsAsJson(destinationDir, questionsList);
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid Questions Json");
            }
    }


    /**
     *
     * Given the list of questions, save it as json file
     * @param destinationDir
     * @param questionsList
     * @throws IOException
     */
    private void saveQuestionsAsJson(File destinationDir, List<ContentResponse> questionsList) throws IOException{
        FileWriter fileWriter = null;
        try {

            Gson gson = new GsonBuilder().create();
            String savedQuestionsJson = gson.toJson(questionsList, ArrayList.class);
            File savedQuestionsFile = new File(destinationDir, QUESTIONS_JSON);

            fileWriter = new FileWriter(savedQuestionsFile);
            fileWriter.write(savedQuestionsJson);

        } finally {
            UMIOUtils.closeQuietly(fileWriter);
        }
    }


    /**
     *
     * Given a list of videos, find the one with the smallest size
     * @param encoded_videos
     * @return chosen video url
     */
    private ContentResponse.Encoded_videos selectVideo(List<ContentResponse.Encoded_videos> encoded_videos) {

        ContentResponse.Encoded_videos selectedVideo = null;
        int videoSize= Integer.MAX_VALUE;

        for(ContentResponse.Encoded_videos videos: encoded_videos){
            if(videos.file_size > 0 && videos.file_size < videoSize){
                selectedVideo = videos;
            }
        }
        return selectedVideo;
    }


}
