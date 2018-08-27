package com.ustadmobile.lib.contentscrapers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.util.UMIOUtils;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Attr;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
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

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;


public class ContentScraperUtil {

    /**
     * Is the given componentType "Imported Component"
     *
     * @param component_type enum type
     * @return
     */
    public static boolean isImportedComponent(String component_type) {
        return ScraperConstants.ComponentType.IMPORTED.getType().equalsIgnoreCase(component_type);
    }


    /**
     * Given an html String, search for all tags that have src attribute to download from
     *
     * @param html           html string that might have src attributes
     * @param destinationDir location the src file will be stored
     * @param baseUrl        is needed for when the src is a path for the url
     * @returns the html with modified src pointing to its new location
     */
    public static String downloadAllResources(String html, File destinationDir, URL baseUrl) {

        if (html == null || html.isEmpty()) {
            // no string to parse
            return html;
        }

        Document doc = Jsoup.parse(html);

        Elements contentList = doc.select("[src]");
        for (Element content : contentList) {

            String url = content.attr("src");
            if ((url.contains("data:image") && url.contains("base64")) || url.contains("file://")) {
                continue;
                continue;
            }
            try {
                URL contentUrl = new URL(baseUrl, url);
                String fileName = getFileNameFromUrl(url);
                File contentFile = new File(destinationDir, fileName);

                downloadContent(contentUrl, contentFile);

                content.attr("src", destinationDir.getName() + "/" + fileName);
            } catch (IOException e) {
                continue;
            }

        }

        return doc.body().html();
    }


    /**
     * Given a url link, find the file name
     *
     * @param url
     * @return the extracted file name from url link
     */
    public static String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.length()).replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }


    /**
     * Given the list, save it as json file
     *
     * @param destinationDir directory it will be saved
     * @param list           ArrayList of Objects to be parsed to a string
     * @throws IOException
     */
    public static void saveListAsJson(File destinationDir, List<?> list, String fileName) throws IOException {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String savedQuestionsJson = gson.toJson(list, ArrayList.class);
        File savedQuestionsFile = new File(destinationDir, fileName);

        FileUtils.writeStringToFile(savedQuestionsFile, savedQuestionsJson, UTF_ENCODING);
    }


    /**
     * Given a url and file, download its content and write into the file
     *
     * @param url  url that contains contain to download from
     * @param file file it will write the content to
     * @param
     * @throws IOException
     */
    public static void downloadContent(URL url, File file) throws IOException {

        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK)
                throw new IOException("HTTP Response code not 200: got " + responseCode + url);

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
     * Given the last modified time from server, check if the file that is saved is up to date with server
     *
     * @param modifiedTime the last time file was modified from server
     * @param file         the current file in our directory
     * @return
     */
    public static boolean isContentUpdated(long modifiedTime, File file) {

        if (file.exists()) {
            return modifiedTime >= file.lastModified();
        }
        return true;
    }


    /**
     * Given a file, check it exists and has content by checking its size
     *
     * @param file that contains content
     * @return
     */
    public static boolean fileHasContent(File file) {
        return file.exists() && file.length() > 0;
    }


    /**
     * Given an EdraakK12Date, return it as a long
     *
     * @param date
     * @return
     */
    public static long parseEdraakK12Date(String date) {
        return LocalDateTime.parse(date, ScraperConstants.formatter).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }


    /**
     * Given a url, parse the JSON from the http url response and return the object
     *
     * @param url   url its downloading the json from
     * @param clazz the class of the object its returning
     * @return
     * @throws IOException
     */
    public static <T> T parseJson(URL url, Class<? extends T> clazz) throws IOException {

        Reader reader = null;
        InputStream inputStream;
        URLConnection connection;
        try {
            connection = url.openConnection();
            connection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            inputStream = connection.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            UMIOUtils.readFully(inputStream, bout);

            reader = new InputStreamReader(new ByteArrayInputStream(bout.toByteArray()));
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(reader, clazz);
        } finally {
            UMIOUtils.closeQuietly(reader);
        }
    }


    /**
     * Given a directory, save it using the filename, download its content and save in the given directory
     *
     * @param directoryToZip location of the folder that will be zipped
     * @param filename       name of the zip
     * @param locationToSave location where the zipped folder will be placed
     * @throws IOException
     */

    public static void zipDirectory(File directoryToZip, String filename, File locationToSave) throws IOException {

        File zippedFile = new File(locationToSave, filename + ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zippedFile.toPath()), StandardCharsets.UTF_8)) {
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
     * Generate tincan xml file
     *
     * @param destinationDirectory directory it will be saved
     * @param activityName         name of course/simulation
     * @param langCode             language of the course/simulation
     * @param fileName             name of file tincan will launch
     * @param typeText             type of tincan file - get from https://registry.tincanapi.com/
     * @param entityId             id of activity should match entry id of opds link
     * @param description          description of course/simulation
     * @param descLang             lang of description
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
