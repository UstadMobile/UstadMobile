package com.ustadmobile.lib.contentscrapers.voa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LogIndex;
import com.ustadmobile.lib.contentscrapers.LogResponse;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static com.ustadmobile.lib.contentscrapers.ck12.CK12ContentScraper.RESPONSE_RECEIVED;


public class VoaScraper {

    private final URL scrapUrl;
    private final File voaDirectory;
    private final File destinationDir;
    public boolean isContentUpdated = true;

    public VoaScraper(String url, File destinationDir) throws IOException {
        scrapUrl = new URL(url);
        this.destinationDir = destinationDir;
        voaDirectory = new File(destinationDir, FilenameUtils.getBaseName(scrapUrl.getPath()));
        voaDirectory.mkdirs();
    }

    public void scrapeContent() throws IOException  {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        ContentScraperUtil.setChromeDriverLocation();

        ChromeDriver driver = ContentScraperUtil.setupLogIndexChromeDriver();

        driver.get(scrapUrl.toString());
        WebDriverWait waitDriver = new WebDriverWait(driver, 10000);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);

        WebElement element = driver.findElementByCssSelector("script[type*=json]");
        JavascriptExecutor js = driver;
        String scriptText = (String) js.executeScript("return arguments[0].innerText;",element);

        VoaResponse response = gson.fromJson(scriptText, VoaResponse.class);

        File voaDirectory = new File(destinationDir, FilenameUtils.getBaseName(scrapUrl.getPath()));
        voaDirectory.mkdirs();

        long dateModified = ContentScraperUtil.parseServerDate(response.dateModified.replace("Z", "").replace(" ", "T"));

        File modifiedFile = new File(voaDirectory,  voaDirectory.getName() + ScraperConstants.LAST_MODIFIED_TXT);
        String text;

        boolean isUpdated = true;
        if (ContentScraperUtil.fileHasContent(modifiedFile)) {
            text = FileUtils.readFileToString(modifiedFile, UTF_ENCODING);
            isUpdated = !String.valueOf(dateModified).equalsIgnoreCase(text);
        } else {
            FileUtils.writeStringToFile(modifiedFile, String.valueOf(dateModified), ScraperConstants.UTF_ENCODING);
        }

        if (!isUpdated) {
            isContentUpdated = false;
            return;
        }

        LogEntries les = driver.manage().logs().get(LogType.PERFORMANCE);
        driver.close();


        List<LogIndex> index = new ArrayList<>();

        for (LogEntry le : les) {

            LogResponse log = gson.fromJson(le.getMessage(), LogResponse.class);
            if (RESPONSE_RECEIVED.equalsIgnoreCase(log.message.method)) {
                String mimeType = log.message.params.response.mimeType;
                String urlString = log.message.params.response.url;

                try {

                    URL logUrl = new URL(urlString);
                    File urlDirectory = ContentScraperUtil.createDirectoryFromUrl(voaDirectory, logUrl);
                    File file = ContentScraperUtil.downloadFileFromLogIndex(logUrl, urlDirectory, log);

                    LogIndex logIndex = ContentScraperUtil.createIndexFromLog(urlString, mimeType, urlDirectory, file, log);
                    index.add(logIndex);

                    if(file.getName().contains(voaDirectory.getName())){

                        String voaHtml = FileUtils.readFileToString(file, UTF_ENCODING);
                        Document doc = Jsoup.parse(voaHtml);
                        Elements linkElements = doc.select("video,audio");

                        for(Element link: linkElements){

                            String mediaSource = link.attr("src");
                            URL mediaUrl = new URL(mediaSource);
                            File mediaFolder = ContentScraperUtil.createDirectoryFromUrl(voaDirectory, mediaUrl);
                            File mediaFile = ContentScraperUtil.downloadFileFromLogIndex(mediaUrl, mediaFolder, null);
                            Attributes attrList = link.attributes();
                            String dateType = link.attr("data-type");
                            List<String> keys = new ArrayList<>();
                            for(Attribute attr: attrList){
                                if(!attr.getKey().equals("src")) {
                                    keys.add(attr.getKey());
                                }
                            }
                            for(String key: keys){
                               link.removeAttr(key);
                            }
                            link.attr("src", "../" + mediaFolder.getName() + "/" + mediaFile.getName());
                            LogIndex mediaIndex = ContentScraperUtil.createIndexFromLog(mediaSource,
                                    dateType, mediaFolder, mediaFile, null);
                            index.add(mediaIndex);

                        }

                        FileUtils.writeStringToFile(file, doc.html(), UTF_ENCODING);


                    }


                } catch (IOException e) {
                    System.err.println(urlString);
                    System.err.println(le.getMessage());
                    e.printStackTrace();
                }
            }
        }

        FileUtils.writeStringToFile(new File(voaDirectory, "index.json"), gson.toJson(index), UTF_ENCODING);
        ContentScraperUtil.zipDirectory(voaDirectory, FilenameUtils.getBaseName(scrapUrl.getPath()), destinationDir);
    }
}
