package com.ustadmobile.lib.contentscrapers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.util.UMIOUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;


public class ContentScraperUtil {


    /**
     *  Checks if the html string has an attribute value within, if found download its content
     * @param htmlString html String
     * @param attribute Attribute to look for
     * @param destinationDir directory it will be saved if attribute found
     * @param fileName - name of the file once saved
     * @param url - base url
     * @return
     * @throws IOException
     */
    public static String checkIfJsonObjectHasAttribute(String htmlString, String attribute, File destinationDir, String fileName, URL url) throws IOException{
        if (htmlString != null && htmlString.contains(attribute))
            return ContentScraperUtil.downloadAllResources(htmlString, destinationDir, fileName, url);
        return htmlString;
    }


    /**
     *
     * Is the given componentType "Imported Component"
     * @param component_type
     * @return
     */
    public static Boolean isImportedComponent(String component_type) {
        return ScraperConstants.ComponentType.IMPORTED.getType().equalsIgnoreCase(component_type);
    }


    /**
     * Given an html String search for all images that have sources to download from
     * @param html
     * @param destinationDir
     * @param fileName
     * @param baseUrl
     * @return
     * @throws IOException
     */
    public static String downloadAllResources(String html, File destinationDir, String fileName, URL baseUrl) throws IOException {

        Document doc = Jsoup.parse(html);

        int imageCountInTag = 0;
        Elements images =  doc.select("img[src]");
        for(Element image: images){

            String url = image.attr("src");
            if(url.contains("data:image") && url.contains("base64")){
                continue;
            }
            URL imageUrl = new URL(baseUrl, url);

            downloadContent(imageUrl, destinationDir, imageCountInTag + fileName);

            image.attr("src",  destinationDir.getName() + "/" + imageCountInTag + fileName);

            imageCountInTag++;
        }

        String htmlBody = doc.body().html();
        return htmlBody;
    }


    /**
     *
     * Given a url, download its content and save in the destination directory
     * @param url
     * @param destinationDir
     * @param fileName
     * @throws IOException
     */
    public static void downloadContent(URL url, File destinationDir, String fileName) throws IOException {

        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            if(responseCode != HttpURLConnection.HTTP_OK)
                throw new IOException("HTTP Response code not 200: got " + responseCode);

            File savedFile = new File(destinationDir, fileName);

            inputStream = httpConn.getInputStream();
            outputStream = new FileOutputStream(savedFile);
            int bytesRead;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

        } finally {
            UMIOUtils.closeQuietly(inputStream);
            UMIOUtils.closeQuietly(outputStream);
        }

    }


    /**
     *
     * Given a url, parse the JSON from the http url response and return the object
     * @param url
     * @return
     * @throws IOException
     */
    public static ContentResponse parseJson(URL url) throws IOException {

        Reader reader = null;
        InputStream inputStream;
        URLConnection connection;
        try{
            connection = url.openConnection();
            connection.setRequestProperty("Accept","application/json, text/javascript, */*; q=0.01");
            inputStream = connection.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            UMIOUtils.readFully(inputStream, bout);

            String strVal = bout.toString("UTF-8");
            System.out.println("Read: " + strVal);

            reader = new InputStreamReader(new ByteArrayInputStream(bout.toByteArray()));
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(reader, ContentResponse.class);
        } finally {
            UMIOUtils.closeQuietly(reader);
        }
    }


}
