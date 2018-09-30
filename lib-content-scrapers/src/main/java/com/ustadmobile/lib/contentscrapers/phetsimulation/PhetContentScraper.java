package com.ustadmobile.lib.contentscrapers.phetsimulation;

import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;


/**
 * The page for each simulation on the website follows the same format
 * <p>
 * The english simulation can be found by using the css selector
 * div.simulation-main-image-panel a.phet-button[href]
 * which provides the button with href link to download the html file.
 * <p>
 * The description of the simulation can be found at the div tag with id "about" to get its content.
 * Within the about html, there is a description used for purpose of tincan which can be found by the selector - p.simulation-panel-indent
 * <p>
 * The translations for the simulation can be found in the table table.phet-table tr
 * In the css selector for that tr row
 * you will get the language in the column with selector - "td.list-highlight-background a[href]"
 * you will get the download link column with selector - "td.img-container a[href]"
 * <p>
 * The download links url have eTag and last modified in the headers to identify new content
 */
public class PhetContentScraper {

    public static final String[] CATEGORY = {
            "iPad/Tablet", "New Sims", "Simulations", "HTML5"};
    private final String url;
    private final File destinationDirectory;
    private final String title;
    private Document simulationDoc;
    private String aboutText;
    private ArrayList<String> langugageList;

    private String aboutDescription;

    public PhetContentScraper(String url, File destinationDir) {
        this.url = url;
        this.destinationDirectory = destinationDir;
        langugageList = new ArrayList<>();
        this.title = url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: <phet html url> <file destination>");
            System.exit(1);
        }

        System.out.println(args[0]);
        System.out.println(args[1]);
        try {
            new PhetContentScraper(args[0], new File(args[1])).scrapeContent();
        } catch (IOException e) {
            System.err.println("Exception running scrapeContent");
            e.printStackTrace();
        }

    }


    public void scrapeContent() throws IOException {

        URL simulationUrl = new URL(url);
        destinationDirectory.mkdirs();

        simulationDoc = Jsoup.connect(url).get();

        if (!simulationDoc.select("div.simulation-main-image-panel a span").hasClass("html-badge")) {
            throw new IllegalArgumentException("File Type not supported");
        }

        aboutText = simulationDoc.getElementById("about").html();
        aboutDescription = Jsoup.parse(aboutText).select("p.simulation-panel-indent").text();

        boolean contentUpdated = false;
        for (Element englishLink : simulationDoc.select("div.simulation-main-image-panel a.phet-button[href]")) {

            String hrefLink = englishLink.attr("href");

            File englishLocation = new File(destinationDirectory, "en");
            englishLocation.mkdirs();

            if (hrefLink.contains("download")) {
                downloadContent(simulationUrl, hrefLink, englishLocation);
                contentUpdated = true;
                break;
            }
        }

        File languageLocation = null;
        for (Element translations : simulationDoc.select("table.phet-table tr")) {

            for (Element langs : translations.select("td.list-highlight-background a[href]")) {

                String hrefLink = langs.attr("href");

                if (hrefLink.contains("translated")) {

                    String langCode = hrefLink.substring(hrefLink.lastIndexOf("/") + 1, hrefLink.length());
                    System.out.println(langCode);
                    langugageList.add(langCode);
                    languageLocation = new File(destinationDirectory, langCode);
                    languageLocation.mkdirs();
                    break;
                }
            }

            for (Element links : translations.select("td.img-container a[href]")) {

                String hrefLink = links.attr("href");

                if (hrefLink.contains("download")) {
                    downloadContent(simulationUrl, hrefLink, languageLocation);
                    contentUpdated = true;
                    break;
                }

            }

        }

        if (contentUpdated) {
            for (File langDirectory : destinationDirectory.listFiles()) {
                if (langDirectory.isDirectory()) {
                    ContentScraperUtil.zipDirectory(langDirectory, langDirectory.getName(), langDirectory.getParentFile());
                }
            }
        }
    }


    /**
     * Find the category for the phet simulation
     *
     * @return a list of categories a single phet simulation could be in
     */
    public ArrayList<OpdsEntryWithRelations> getCategoryRelations() {

        Elements selected = simulationDoc.select("ul.nav-ul div.link-holder span.selected");

        ArrayList<OpdsEntryWithRelations> categoryRelations = new ArrayList<>();
        for (Element category : selected) {

            if (Arrays.stream(CATEGORY).parallel().noneMatch(category.text()::contains)) {

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

        if(!ContentScraperUtil.isFileModified(conn, simulationLocation)){
            return;
        }

        String fileName = hrefLink.substring(hrefLink.lastIndexOf("/") + 1, hrefLink.lastIndexOf("?"));
        File simulationFile = new File(simulationLocation, fileName);

        FileUtils.writeStringToFile(new File(simulationLocation, ScraperConstants.ABOUT_HTML), aboutText, ScraperConstants.UTF_ENCODING);

        FileUtils.copyURLToFile(link, simulationFile);
        String simulationTitle = Jsoup.parse(simulationFile, ScraperConstants.UTF_ENCODING).title();
        try {
            ContentScraperUtil.generateTinCanXMLFile(simulationLocation, simulationTitle,
                    languageLocation.getName(), fileName, ScraperConstants.simulationTinCanFile,
                    this.title + "\\" + languageLocation.getName(),
                    aboutDescription, "en");
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }

    }


    /**
     * Given a directory of phet simulation content, find the languages it was translated to
     *
     * @param destinationDirectory directory of the all phet simulations
     * @return a list of languages the phet simulation was translated to
     * @throws IOException
     */
    public ArrayList<OpdsEntryWithRelations> getTranslations(File destinationDirectory) throws IOException {

        ArrayList<OpdsEntryWithRelations> translationsEntry = new ArrayList<>();

        for (File translationDir : destinationDirectory.listFiles()) {

            if (translationDir.isDirectory()) {
                String langCode = translationDir.getName();
                if (!langugageList.contains(langCode)) {
                    continue;
                }
                for (File contentDirectory : translationDir.listFiles()) {

                    if (title.equalsIgnoreCase(contentDirectory.getName())) {

                        for (File file : contentDirectory.listFiles()) {

                            if (file.getName().endsWith(".html")) {
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

    /**
     * @return the title of the simulation in english
     */
    public String getTitle() {
        return title;
    }
}
