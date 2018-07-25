package com.ustadmobile.lib.contentscrapers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.ustadmobile.core.util.UMIOUtils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import sun.misc.JavaUtilZipFileAccess;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.*;


public class EdraakContentScraper {

    public void convert(String contentId, int programId, String baseUrl, File destinationDir) throws IOException {

        URL url;
        try {
            url = new URL(baseUrl + "component/" +  contentId + "/?states_program_id=" + programId);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed url", e);
        }

        ContentResponse response;
        try {
            response = parseJson(url);
        } catch (IOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("JSON INVALID", e.getCause());
        }

        if(!isImportedComponent(response.component_type))
            throw new IllegalArgumentException("Not an imported content type!");

        if(response.target_component == null || response.target_component.children == null)
            throw new IllegalArgumentException("Null target component, or target component children are null");

        for(ContentResponse children: response.target_component.children){

            if(ScraperConstants.ComponentType.VIDEO.getType().equalsIgnoreCase(children.component_type)){

                if (children.video_info == null || children.video_info.encoded_videos == null || children.video_info.encoded_videos.isEmpty())
                    throw new IllegalArgumentException("Component Type was Video but no video found");

                String videoHref = selectVideo(children.video_info.encoded_videos);
                URL videoUrl;
                try {
                    videoUrl = new URL(url, videoHref);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("Malformed url", e);
                }

                    try {
                        ContentScraperUtil.downloadContent(videoUrl, destinationDir, VIDEO_MP4);
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Malformed url", e);
                    }

                } else if(ScraperConstants.QUESTION_SET_HOLDER_TYPES.contains(children.component_type)){

                        List<ContentResponse> questionsList = children.question_set.children;

                    if(questionsList.isEmpty())
                        throw new IllegalArgumentException("No Questions were found in the question set");

                    for(ContentResponse exercise : questionsList) {
                        File exerciseDirectory = new File(destinationDir, exercise.id);
                        exerciseDirectory.mkdirs();

                        exercise.full_description = ContentScraperUtil.checkIfJsonObjectHasAttribute(exercise.full_description, IMG_TAG, exerciseDirectory, HtmlName.FULL_DESC.getName() + ScraperConstants.PNG_EXT);
                        exercise.explanation = ContentScraperUtil.checkIfJsonObjectHasAttribute(exercise.explanation, IMG_TAG, exerciseDirectory, HtmlName.EXPLAIN.getName() + ScraperConstants.PNG_EXT);
                        exercise.description = ContentScraperUtil.checkIfJsonObjectHasAttribute(exercise.description, IMG_TAG, exerciseDirectory, HtmlName.DESC + PNG_EXT);

                        if(ComponentType.MULTICHOICE.getType().equalsIgnoreCase(exercise.component_type)){
                            for(ContentResponse.Choice choice: exercise.choices){
                                choice.description = ContentScraperUtil.checkIfJsonObjectHasAttribute(choice.description, IMG_TAG, exerciseDirectory, choice.item_id + ScraperConstants.PNG_EXT);
                            }
                        }

                        for(ContentResponse.Hint hint : exercise.hints){
                            hint.description = ContentScraperUtil.checkIfJsonObjectHasAttribute(hint.description, IMG_TAG, exerciseDirectory, hint.item_id + ScraperConstants.PNG_EXT);
                        }

                    }

                    try {
                        saveQuestionsAsJson(destinationDir, questionsList);
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Invalid Questions Json");
                    }
            }
        }

        // store the json in a file after modifying image links
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        File file = new File(destinationDir, ScraperConstants.CONTENT_JSON);
        FileWriter fileWriter = new FileWriter(file);
        String jsonString =  gson.toJson(response);
        fileWriter.write(jsonString);
        UMIOUtils.closeQuietly(fileWriter);


        // zip it all
        File zippedFile = new File(destinationDir.getParent(), response.id +".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zippedFile.toPath()))){
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


    private String selectVideo(List<ContentResponse.Encoded_videos> encoded_videos) {

        String videoUrl = "";
        int videoSize= Integer.MAX_VALUE;

        for(ContentResponse.Encoded_videos videos: encoded_videos){
            if(videos.file_size > 0 && videos.file_size < videoSize){
                videoUrl = videos.url;
            }
        }
        return videoUrl;
    }

    private Boolean isImportedComponent(String component_type) {
        return ScraperConstants.ComponentType.IMPORTED.getType().equalsIgnoreCase(component_type);
    }

    private ContentResponse parseJson(URL url) throws IOException {
        //check closing these readers
        Reader reader = null;
        try{
            reader = new InputStreamReader(url.openStream()); //Read the json output
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(reader, ContentResponse.class);
        } finally {
            UMIOUtils.closeQuietly(reader);
        }
    }


}
