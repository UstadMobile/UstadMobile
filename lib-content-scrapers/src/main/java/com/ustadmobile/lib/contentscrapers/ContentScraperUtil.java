package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.util.UMIOUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class ContentScraperUtil {

    public static String checkIfJsonObjectHasAttribute(String json, String attribute, File destinationDir, String fileName) throws IOException{
        if (json != null && json.contains(attribute))
            return ContentScraperUtil.downloadAllResources(json, destinationDir, fileName);
        return json;
    }


    public static String downloadAllResources(String html, File destinationDir, String fileName) throws IOException {

        Document doc = Jsoup.parse(html);

        int imageCountInTag = 0;
        Elements images =  doc.select("img[src]");
        for(Element image: images){

            String url = image.attr("src");
            if(url.contains("data:image") && url.contains("base64")){
                continue;
            }
            URL imageUrl = new URL(url);

            downloadContent(imageUrl, destinationDir, imageCountInTag + fileName);

            image.attr("src",  destinationDir.getName() + "/" + imageCountInTag + fileName);

            imageCountInTag++;
        }

        String htmlBody = doc.body().html();
        return htmlBody;
    }

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


}
