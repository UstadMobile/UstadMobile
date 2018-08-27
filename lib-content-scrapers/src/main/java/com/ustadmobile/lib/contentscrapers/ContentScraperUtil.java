package com.ustadmobile.lib.contentscrapers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.contentscrapers.EdraakK12.ContentResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Attr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


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
            if((url.contains("data:image") && url.contains("base64")) || url.contains("file://")){
                continue;
            }
            URL imageUrl = new URL(baseUrl, url);
            File imageFile = new File(destinationDir, imageCountInTag + fileName);
            downloadContent(imageUrl, imageFile);

            image.attr("src",  destinationDir.getName() + "/" + imageCountInTag + fileName);

            imageCountInTag++;
        }

        return doc.body().html();
    }


    /**
     *
     * Given a url and file, download its content and write into the file
     * @param url
     * @param file
     * @param
     * @throws IOException
     */
    public static void downloadContent(URL url,File file) throws IOException {

        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            if(responseCode != HttpURLConnection.HTTP_OK)
                throw new IOException("HTTP Response code not 200: got " + responseCode);

            inputStream = httpConn.getInputStream();
            outputStream = new FileOutputStream(file);
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
     * Given text, write into a file
     * @param text
     * @param file
     */
    public static void writeStringToFile(String text, File file) {

        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            UMIOUtils.closeQuietly(writer);
        }

    }




    /**
     *
     * Given the last modified time and the file return if modified time is recent
     * @param modifiedTime
     * @param file
     * @return
     */
    public static boolean isContentUpdated(long modifiedTime, File file) {

        if(file.exists()){
            return modifiedTime >= file.lastModified();
        }
        return true;
    }


    public static boolean isFileCreated(File file){
        return file.exists() && file.length() > 0;
    }


    /**
     *
     * Given an EdraakK12Date, return it as a long
     * @param date
     * @return
     */
    public static long parseEdraakK12Date(String date){
        return LocalDateTime.parse(date, ScraperConstants.formatter).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
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

            reader = new InputStreamReader(new ByteArrayInputStream(bout.toByteArray()));
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(reader, ContentResponse.class);
        } finally {
            UMIOUtils.closeQuietly(reader);
        }
    }


    /**
     *
     * Given a directory, save it using the filename, download its content and save in the given directory
     * @param directoryToZip
     * @param filename
     * @param locationToSave
     * @throws IOException
     */

    public static void zipDirectory(File directoryToZip, String filename, File locationToSave) throws IOException {

        File zippedFile = new File(locationToSave, filename +".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zippedFile.toPath()), StandardCharsets.UTF_8)){
            Path sourceDirPath = Paths.get(directoryToZip.toURI());
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


    /**
     *
     * Generate tincan xml file
     * @param destinationDirectory directory it will be saved
     * @param activityName name of course/simulation
     * @param langCode langugage of the course/simulation
     * @param fileName name of file tincan will launch
     * @param typeText type of tincan file - get from https://registry.tincanapi.com/
     * @param entityId id of activity should match entry id of opds link
     * @param description description of course/simulation
     * @param descLang lang of description
     *
     * @return
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static void generateTinCanXMLFile(File destinationDirectory, String activityName, String langCode, String fileName, String typeText, String entityId, String description, String descLang) throws ParserConfigurationException, TransformerException {

        DocumentBuilderFactory dbFactory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = dBuilder.newDocument();

        org.w3c.dom.Element rootElement = doc.createElement("tincan");
        Attr xlms = doc.createAttribute("xmlns");
        xlms.setValue("http://projecttincan.com/tincan.xsd");
        rootElement.setAttributeNode(xlms);
        doc.appendChild(rootElement);

        org.w3c.dom.Element activities = doc.createElement("activities");
        rootElement.appendChild(activities);

        org.w3c.dom.Element activityNode = doc.createElement("activity");
        Attr id = doc.createAttribute("id");
        Attr type = doc.createAttribute("type");
        id.setValue(entityId);
        type.setValue(typeText);
        activityNode.setAttributeNode(id);
        activityNode.setAttributeNode(type);
        activities.appendChild(activityNode);

        org.w3c.dom.Element nameElement = doc.createElement("name");
        nameElement.appendChild(doc.createTextNode(activityName));
        activityNode.appendChild(nameElement);

        org.w3c.dom.Element descElement = doc.createElement("description");
        Attr lang = doc.createAttribute("lang");
        lang.setValue(descLang);
        descElement.setAttributeNode(lang);
        descElement.appendChild(doc.createTextNode(description));
        activityNode.appendChild(descElement);

        org.w3c.dom.Element launchElement = doc.createElement("launch");
        Attr langLaunch = doc.createAttribute("lang");
        langLaunch.setValue(langCode);
        launchElement.setAttributeNode(langLaunch);
        launchElement.appendChild(doc.createTextNode(fileName));
        activityNode.appendChild(launchElement);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(destinationDirectory, "tincan.xml"));
        transformer.transform(source, result);

    }


}
