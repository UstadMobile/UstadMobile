package com.ustadmobile.lib.contentscrapers;

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

public class PhetContentScraper{

    public static final String[] CATEGORY = {
            "iPad/Tablet", "New Sims", "Simulations", "HTML5"};
    private final String url;
    private final File destinationDirectory;
    private Document simulationDoc;
    private String aboutText;

    public PhetContentScraper(String url, File destinationDir){
        this.url = url;
        this.destinationDirectory = destinationDir;

    }

    public void scrapContent() throws IOException {

        URL simulationUrl = new URL(url);
        destinationDirectory.mkdirs();

        simulationDoc = Jsoup.connect(url).get();

        if(!simulationDoc.select("div.simulation-main-image-panel a span").hasClass("html-badge")){
            throw new IllegalArgumentException("File Type not supported");
        }

        aboutText = simulationDoc.getElementById("about").html();


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

        String title = Jsoup.connect(link.toString()).get().title().replaceAll("[\\\\/:*?\"<>|]", " ");
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
        ContentScraperUtil.writeStringToFile(eTag, eTagFile);
        ContentScraperUtil.writeStringToFile(lastModified, modifiedFile);
        ContentScraperUtil.writeStringToFile(aboutText, new File(simulationLocation, ScraperConstants.ABOUT_HTML));
        ContentScraperUtil.downloadContent(link, new File(simulationLocation, hrefLink.substring(hrefLink.lastIndexOf("/") + 1, hrefLink.lastIndexOf("?"))));


    }

    public ArrayList<OpdsEntryWithRelations> getTranslations(File destinationDirectory) throws IOException {

        ArrayList<OpdsEntryWithRelations> translationsEntry = new ArrayList<>();

        for(File translationDir: destinationDirectory.listFiles()){

            if(translationDir.isDirectory()){
                String langCode = translationDir.getName();
                if(langCode.equalsIgnoreCase("en")){
                    continue;
                }
                for(File file: translationDir.listFiles()){

                    if(file.getName().endsWith(".html")){

                        String title = Jsoup.parse(file, ScraperConstants.UTF_ENCODING).title();
                        OpdsEntryWithRelations newEntry = new OpdsEntryWithRelations(
                                UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), destinationDirectory.getName() + "\\" + langCode, title);
                        newEntry.setLanguage(langCode);
                        translationsEntry.add(newEntry);
                        break;
                    }

                }
            }
        }

        return translationsEntry;
    }
}
