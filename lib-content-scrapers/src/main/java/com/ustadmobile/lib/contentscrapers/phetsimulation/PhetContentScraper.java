package com.ustadmobile.lib.contentscrapers.phetsimulation;

import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.db.dao.LanguageVariantDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.entities.LanguageVariant;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.REQUEST_HEAD;
import static com.ustadmobile.lib.contentscrapers.phetsimulation.IndexPhetContentScraper.PHET;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;


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
    private final File containerDir;
    private Document simulationDoc;
    private String aboutText;
    private ArrayList<String> langugageList;
    private Map<String, Boolean> languageMapUpdate;
    private Map<String, String> languageUrlMap;
    private Map<Long, String> langIdMap;

    private String aboutDescription;
    private URL simulationUrl;

    public PhetContentScraper(String url, File destinationDir, File containerDir) {
        this.url = url;
        this.destinationDirectory = destinationDir;
        langugageList = new ArrayList<>();
        languageMapUpdate = new HashMap<>();
        languageUrlMap = new HashMap<>();
        this.containerDir = containerDir;
        this.title = url.substring(url.lastIndexOf("/") + 1);
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: <phet html url> <file destination><file container><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }
        UMLogUtil.setLevel(args.length == 4 ? args[3] : "");
        UMLogUtil.logInfo(args[0]);
        UMLogUtil.logInfo(args[1]);
        try {
            new PhetContentScraper(args[0], new File(args[1]), new File(args[2])).scrapeContent();
        } catch (IOException e) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logError("Exception running scrapeContent phet");
        }

    }


    public void scrapeContent() throws IOException {

        simulationUrl = new URL(url);
        destinationDirectory.mkdirs();

        simulationDoc = Jsoup.connect(url).get();

        if (!simulationDoc.select("div.simulation-main-image-panel a span").hasClass("html-badge")) {
            throw new IllegalArgumentException("File Type not supported for url " + simulationUrl.toString());
        }

        aboutText = simulationDoc.getElementById("about").html();
        aboutDescription = Jsoup.parse(aboutText).select("p.simulation-panel-indent").text();

        boolean contentUpdated;
        for (Element englishLink : simulationDoc.select("div.simulation-main-image-panel a.phet-button[href]")) {

            String hrefLink = englishLink.attr("href");

            File englishLocation = new File(destinationDirectory, "en");
            englishLocation.mkdirs();

            if (hrefLink.contains("download")) {
                contentUpdated = downloadContent(simulationUrl, hrefLink, englishLocation);
                languageMapUpdate.put(englishLocation.getName(), contentUpdated);
                languageUrlMap.put(englishLocation.getName(), hrefLink);
                break;
            }
        }

        File languageLocation = null;
        for (Element translations : simulationDoc.select("table.phet-table tr")) {

            for (Element langs : translations.select("td.list-highlight-background a[href]")) {

                String hrefLink = langs.attr("href");

                if (hrefLink.contains("translated")) {

                    String langCode = hrefLink.substring(hrefLink.lastIndexOf("/") + 1);
                    langugageList.add(langCode);
                    languageLocation = new File(destinationDirectory, langCode);
                    languageLocation.mkdirs();
                    break;
                }
            }

            for (Element links : translations.select("td.img-container a[href]")) {

                String hrefLink = links.attr("href");

                if (hrefLink.contains("download")) {
                    boolean isLanguageUpdated = downloadContent(simulationUrl, hrefLink, languageLocation);
                    languageMapUpdate.put(languageLocation.getName(), isLanguageUpdated);
                    languageUrlMap.put(languageLocation.getName(), hrefLink);
                    break;
                }

            }

        }
    }

    public Map<String, Boolean> getLanguageUpdatedMap() {
        return languageMapUpdate;
    }

    public String getAboutDescription() {
        return aboutDescription;
    }

    public Map<String, String> getLanguageUrlMap() {
        return languageUrlMap;
    }

    /**
     * Find the category for the phet simulation
     *
     * @param contentEntryDao
     * @return a list of categories a single phet simulation could be in
     */
    public ArrayList<ContentEntry> getCategoryRelations(ContentEntryDao contentEntryDao, Language language) {

        Elements selected = simulationDoc.select("ul.nav-ul div.link-holder span.selected");

        ArrayList<ContentEntry> categoryRelations = new ArrayList<>();
        for (Element category : selected) {

            if (Arrays.stream(CATEGORY).parallel().noneMatch(category.text()::contains)) {

                try {
                    String categoryName = category.text(); // category name
                    String path = category.parent().attr("href"); // url path to category

                    ContentEntry categoryContentEntry = ContentScraperUtil.createOrUpdateContentEntry(path, categoryName,
                            new URL(simulationUrl, path).toString(), PHET, LICENSE_TYPE_CC_BY, language.getLangUid(), null,
                            EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                            EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                    categoryRelations.add(categoryContentEntry);
                } catch (IOException ie) {
                    UMLogUtil.logError("Error creating category entry" + category.text() + " for url" + simulationUrl.toString());
                }
            }
        }

        return categoryRelations;

    }

    private boolean downloadContent(URL simulationUrl, String hrefLink, File languageLocation) {
        HttpURLConnection conn = null;
        String fileName = null;
        try {
            URL link = new URL(simulationUrl, hrefLink);

            File simulationLocation = new File(languageLocation, title);
            simulationLocation.mkdirs();

            conn = (HttpURLConnection) link.openConnection();
            conn.setRequestMethod(REQUEST_HEAD);

            fileName = hrefLink.substring(hrefLink.lastIndexOf("/") + 1, hrefLink.lastIndexOf("?"));
            File simulationFile = new File(simulationLocation, fileName);

            boolean isUpdated = ContentScraperUtil.isFileModified(conn, languageLocation, fileName);
            if (ContentScraperUtil.fileHasContent(simulationLocation)) {
                isUpdated = false;
                FileUtils.deleteDirectory(simulationLocation);
            }

            if (!isUpdated) {
                return false;
            }

            FileUtils.writeStringToFile(new File(simulationLocation, ScraperConstants.ABOUT_HTML), aboutText, ScraperConstants.UTF_ENCODING);

            FileUtils.copyURLToFile(link, simulationFile);

            String simulationTitle = Jsoup.parse(simulationFile, ScraperConstants.UTF_ENCODING).title();
            try {
                ContentScraperUtil.generateTinCanXMLFile(simulationLocation, simulationTitle,
                        languageLocation.getName(), fileName, ScraperConstants.SIMULATION_TIN_CAN_FILE,
                        languageLocation.getName() + "\\" + this.title,
                        aboutDescription, "en");
            } catch (ParserConfigurationException | TransformerException e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Tin can file not created for " + link.toString());
            }

        } catch (Exception e) {
            UMLogUtil.logError("Error download content for url " + simulationUrl + " with href " + hrefLink);
            if (fileName != null) {
                ContentScraperUtil.deleteETagOrModified(languageLocation, fileName);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return true;
    }


    /**
     * Given a directory of phet simulation content, find the languages it was translated to
     *
     * @param destinationDirectory directory of the all phet simulations
     * @param thumbnailUrl
     * @return a list of languages the phet simulation was translated to
     * @throws IOException
     */
    public ArrayList<ContentEntry> getTranslations(File destinationDirectory, ContentEntryDao contentEntryDao, String thumbnailUrl, LanguageDao languageDao, LanguageVariantDao languageVariantDao) throws IOException {

        ArrayList<ContentEntry> translationsEntry = new ArrayList<>();
        langIdMap = new HashMap<>();

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

                                try {
                                    String langTitle = simulationDoc.selectFirst("td a[href*=_" + langCode + "] span").text();

                                    String path = simulationUrl.toString().replace("/en/", "/" + langCode + "/");
                                    URL translationUrl = new URL(path);
                                    String[] country = langCode.replaceAll("_", "-").split("-");

                                    String lang = country[0];
                                    String variant = country.length > 1 ? country[1] : "";

                                    Language language = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao, lang);
                                    LanguageVariant languageVariant = ContentScraperUtil.insertOrUpdateLanguageVariant(languageVariantDao, variant, language);

                                    ContentEntry languageContentEntry = ContentScraperUtil.createOrUpdateContentEntry(translationUrl.getPath(), langTitle,
                                            translationUrl.toString(), PHET, LICENSE_TYPE_CC_BY, language.getLangUid(), languageVariant != null ? languageVariant.getLangVariantUid() : null,
                                            getAboutDescription(), true, EMPTY_STRING, thumbnailUrl,
                                            EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                                    langIdMap.put(languageContentEntry.getContentEntryUid(), langCode);

                                    translationsEntry.add(languageContentEntry);
                                    break;
                                } catch (Exception e) {
                                    UMLogUtil.logError("Error while creating a entry for translated " +
                                            "content lang code " + langCode + " in phet url " + url);
                                }
                            }
                        }
                    }
                }
            }
        }

        return translationsEntry;
    }

    public Map<Long, String> getContentEntryLangMap() {
        return langIdMap;
    }

    /**
     * @return the title of the simulation in english
     */
    public String getTitle() {
        return title;
    }
}
