package com.ustadmobile.lib.contentscrapers.PhetSimulation;

import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class PhetContentScraper{

    public static final String[] CATEGORY = {
            "iPad/Tablet", "New Sims", "Simulations", "HTML5"};
    private final String url;
    private final File destinationDirectory;
    private final String title;
    private Document simulationDoc;
    private String aboutText;
    private ArrayList<String> langugageList;

    private final String simulationType = "http://adlnet.gov/expapi/activities/simulation";
    private String aboutDescription;

    public PhetContentScraper(String url, File destinationDir){
        this.url = url;
        this.destinationDirectory = destinationDir;
        langugageList = new ArrayList<>();
        this.title = url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    public void scrapContent() throws IOException {

        URL simulationUrl = new URL(url);
        destinationDirectory.mkdirs();

        simulationDoc = Jsoup.connect(url).get();

        if(!simulationDoc.select("div.simulation-main-image-panel a span").hasClass("html-badge")){
            throw new IllegalArgumentException("File Type not supported");
        }

        aboutText = simulationDoc.getElementById("about").html();
        aboutDescription = Jsoup.parse(aboutText).select("p.simulation-panel-indent").text();

        boolean contentUpdated = false;
        for(Element englishLink: simulationDoc.select("div.simulation-main-image-panel a.phet-button[href]")){

            String hrefLink = englishLink.attr("href");

            File englishLocation = new File(destinationDirectory, "en");
            englishLocation.mkdirs();

            if(hrefLink.contains("download")){
                downloadContent(simulationUrl, hrefLink, englishLocation);
                contentUpdated = true;
                break;
            }
        }

        File languageLocation = null;
        for(Element translations: simulationDoc.select("table.phet-table tr")){

            for(Element langs: translations.select("td.list-highlight-background a[href]")){

                String hrefLink = langs.attr("href");

                if(hrefLink.contains("translated")) {

                    String langCode = hrefLink.substring(hrefLink.lastIndexOf("/") + 1, hrefLink.length());
                    System.out.println(langCode);
                    langugageList.add(langCode);
                    languageLocation = new File(destinationDirectory, langCode);
                    languageLocation.mkdirs();
                    break;
                }
            }

            for(Element links: translations.select("td.img-container a[href]")){

                String hrefLink = links.attr("href");

                if(hrefLink.contains("download")){
                    downloadContent(simulationUrl, hrefLink, languageLocation);
                    contentUpdated = true;
                    break;
                }

            }

        }

        if(contentUpdated){
            for(File langDirectory: destinationDirectory.listFiles()){
                if(langDirectory.isDirectory()){
                    ContentScraperUtil.zipDirectory(langDirectory, langDirectory.getName(), langDirectory.getParentFile());
                }
            }
        }
    }

    public ArrayList<OpdsEntryWithRelations> getCategoryRelations(){

        Elements selected = simulationDoc.select("ul.nav-ul div.link-holder span.selected");

        ArrayList<OpdsEntryWithRelations> categoryRelations = new ArrayList<>();
        for(Element category: selected){

            if(Arrays.stream(CATEGORY).parallel().noneMatch(category.text()::contains)){

                String categoryName = category.text(); // category name
                String path = category.parent().attr("href"); // url path to category

                OpdsEntryWithRelations newEntry = new OpdsEntryWithRelations(
                        UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), path, categoryName);

                categoryRelations.add(newEntry);
                System.out.println(categoryName);
            }
        }

        return categoryRelations;

    }

    private void downloadContent(URL simulationUrl, String hrefLink, File languageLocation) throws IOException {

        URL link = new URL(simulationUrl, hrefLink);

        File simulationLocation = new File(languageLocation, title);
        simulationLocation.mkdirs();

        System.out.println(link);
        URLConnection conn = link.openConnection();

        String eTag = conn.getHeaderField("ETag").replaceAll("\"", "");
        String lastModified = conn.getHeaderField("Last-Modified");
        System.out.println(eTag);
        System.out.println(lastModified);

        File eTagFile = new File(simulationLocation, ScraperConstants.ETAG_TXT);
        File modifiedFile = new File(simulationLocation, ScraperConstants.LAST_MODIFIED_TXT);

        if(eTagFile.length() > 0){

            String text = new String(Files.readAllBytes(eTagFile.toPath()));
            if(text.equalsIgnoreCase(eTag)){
                return;
            }

        }

        String fileName = hrefLink.substring(hrefLink.lastIndexOf("/") + 1, hrefLink.lastIndexOf("?"));
        File simulationFile = new File(simulationLocation, fileName);



        ContentScraperUtil.writeStringToFile(eTag, eTagFile);
        ContentScraperUtil.writeStringToFile(lastModified, modifiedFile);
        ContentScraperUtil.writeStringToFile(aboutText, new File(simulationLocation, ScraperConstants.ABOUT_HTML));
        ContentScraperUtil.downloadContent(link, simulationFile);
        String simulationTitle  = Jsoup.parse(simulationFile, ScraperConstants.UTF_ENCODING).title();
        try {
            ContentScraperUtil.generateTinCanXMLFile(simulationLocation, simulationTitle,
                    languageLocation.getName(), fileName, simulationType,
                    this.title + "\\" + languageLocation.getName(),
                    aboutDescription, "en");
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<OpdsEntryWithRelations> getTranslations(File destinationDirectory) throws IOException {

        ArrayList<OpdsEntryWithRelations> translationsEntry = new ArrayList<>();

        for(File translationDir: destinationDirectory.listFiles()){

            if(translationDir.isDirectory()){
                String langCode = translationDir.getName();
                if(!langugageList.contains(langCode)){
                    continue;
                }
                for(File contentDirectory: translationDir.listFiles()){

                    if(title.equalsIgnoreCase(contentDirectory.getName())){

                        for(File file: contentDirectory.listFiles()){

                            if(file.getName().endsWith(".html")){
                                // TODO recheck entry id for translations
                                String langTitle = Jsoup.parse(file, ScraperConstants.UTF_ENCODING).title();
                                OpdsEntryWithRelations newEntry = new OpdsEntryWithRelations(
                                        UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), this.title + "\\" + langCode, langTitle);
                                newEntry.setLanguage(langCode);
                                translationsEntry.add(newEntry);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return translationsEntry;
    }

    public String getTitle() {
        return title;
    }
}
