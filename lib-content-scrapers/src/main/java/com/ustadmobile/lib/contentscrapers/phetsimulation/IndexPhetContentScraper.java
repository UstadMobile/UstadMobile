package com.ustadmobile.lib.contentscrapers.phetsimulation;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.db.dao.LanguageVariantDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LanguageList;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.Language;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;

/**
 * The Phet Simulation Website provides a list of all the available Html5 Content in one of their categories found at
 * https://phet.colorado.edu/en/simulations/category/html
 * <p>
 * By using a css selector: td.simulation-list-item span.sim-badge-html
 * We can get the url to each simulation in that category to give to PhetContentScraper to scrap its content
 */
public class IndexPhetContentScraper {

    public static final String PHET = "Phet";
    private File destinationDirectory;
    private URL url;
    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntryFileDao contentEntryFileDao;
    private ContentEntryContentEntryFileJoinDao contentEntryFileJoin;
    private ContentEntryContentCategoryJoinDao contentEntryCategoryJoinDao;
    private ContentEntryRelatedEntryJoinDao contentEntryRelatedJoinDao;
    private ContentEntryFileStatusDao contentFileStatusDao;
    private LanguageDao languageDao;
    private Language englishLang;
    private LanguageVariantDao languageVariantDao;


    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <phet html url> <file destination><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }
        UMLogUtil.setLevel(args.length == 3 ? args[2] : "");
        UMLogUtil.logInfo(args[0]);
        UMLogUtil.logInfo(args[1]);
        try {
            new IndexPhetContentScraper().findContent(args[0], new File(args[1]));
        } catch (IOException e) {
            UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logFatal("Exception running findContent phet");
        }
    }

    /**
     * Given a phet url, find the content and download
     *
     * @param urlString      url link to phet category
     * @param destinationDir destination folder for phet content
     * @throws IOException
     */
    public void findContent(String urlString, File destinationDir) throws IOException {

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            UMLogUtil.logError("Index Malformed url" + urlString);
            throw new IllegalArgumentException("Malformed url" + urlString, e);
        }

        destinationDir.mkdirs();
        destinationDirectory = destinationDir;

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        contentEntryFileDao = repository.getContentEntryFileDao();
        contentEntryFileJoin = repository.getContentEntryContentEntryFileJoinDao();
        contentEntryCategoryJoinDao = repository.getContentEntryContentCategoryJoinDao();
        contentEntryRelatedJoinDao = repository.getContentEntryRelatedEntryJoinDao();
        contentFileStatusDao = db.getContentEntryFileStatusDao();
        languageDao = repository.getLanguageDao();
        languageVariantDao = repository.getLanguageVariantDao();

        new LanguageList().addAllLanguages();

        Document document = Jsoup.connect(urlString).get();

        browseCategory(document);

    }


    private void browseCategory(Document document) throws IOException {

        Elements simulationList = document.select("td.simulation-list-item span.sim-badge-html");

        englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English");

        ContentEntry masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);


        ContentEntry phetParentEntry = ContentScraperUtil.createOrUpdateContentEntry("https://phet.colorado.edu/", "Phet Interactive Simulations",
                "https://phet.colorado.edu/", PHET, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                "INTERACTIVE SIMULATIONS\nFOR SCIENCE AND MATH", false, EMPTY_STRING,
                "https://phet.colorado.edu/images/phet-social-media-logo.png", EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, phetParentEntry, 1);

        for (Element simulation : simulationList) {

            String path = simulation.parent().attr("href");
            String simulationUrl = new URL(url, path).toString();
            String title = simulationUrl.substring(simulationUrl.lastIndexOf("/") + 1);
            String thumbnail = simulation.parent().selectFirst("img").attr("src");

            PhetContentScraper scraper = new PhetContentScraper(simulationUrl, destinationDirectory);
            try {
                scraper.scrapeContent();
                ContentEntry englishSimContentEntry = ContentScraperUtil.createOrUpdateContentEntry(path, title,
                        simulationUrl, PHET, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                        scraper.getAboutDescription(), true, EMPTY_STRING,
                        thumbnail, EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                boolean isEnglishUpdated = scraper.getLanguageUpdatedMap().get("en");
                File enLangLocation = new File(destinationDirectory, "en");
                File englishContentFile = new File(enLangLocation, title + ScraperConstants.ZIP_EXT);
                if (isEnglishUpdated) {

                    ContentScraperUtil.insertContentEntryFile(englishContentFile, contentEntryFileDao, contentFileStatusDao, englishSimContentEntry,
                            ContentScraperUtil.getMd5(englishContentFile), contentEntryFileJoin, true, ScraperConstants.MIMETYPE_ZIP);


                } else {
                    ContentScraperUtil.checkAndUpdateDatabaseIfFileDownloadedButNoDataFound(englishContentFile, englishSimContentEntry, contentEntryFileDao,
                            contentEntryFileJoin, contentFileStatusDao, ScraperConstants.MIMETYPE_ZIP, true);

                }

                ArrayList<ContentEntry> categoryList = scraper.getCategoryRelations(contentEntryDao, englishLang);
                ArrayList<ContentEntry> translationList = scraper.getTranslations(destinationDirectory, contentEntryDao, thumbnail, languageDao, languageVariantDao);

                // TODO remove all categories that no longer exist
                // TODO remove all categories that dont belong in a phet simulation anymore

                int categoryCount = 0;
                for (ContentEntry category : categoryList) {

                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, phetParentEntry, category, categoryCount++);
                    ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentParentChildJoinDao, category, englishSimContentEntry, 0);

                    int translationsCount = 1;
                    for (ContentEntry translation : translationList) {

                        ContentScraperUtil.insertOrUpdateRelatedContentJoin(contentEntryRelatedJoinDao, translation, englishSimContentEntry, ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION);

                        String langCode = scraper.getContentEntryLangMap().get(translation.getContentEntryUid());

                        File langLocation = new File(destinationDirectory, langCode);
                        File content = new File(langLocation, title + ScraperConstants.ZIP_EXT);
                        if (scraper.getLanguageUpdatedMap().get(langCode)) {

                            ContentScraperUtil.insertContentEntryFile(content, contentEntryFileDao, contentFileStatusDao, translation,
                                    ContentScraperUtil.getMd5(content), contentEntryFileJoin, true, ScraperConstants.MIMETYPE_ZIP);


                        } else {
                            ContentScraperUtil.checkAndUpdateDatabaseIfFileDownloadedButNoDataFound(content, translation, contentEntryFileDao,
                                    contentEntryFileJoin, contentFileStatusDao, ScraperConstants.MIMETYPE_ZIP, true);

                        }

                        ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentParentChildJoinDao, category, translation, translationsCount++);
                    }
                }

            } catch (Exception e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Failed to scrape Phet Content for url" + simulationUrl);
            }
        }
    }

}
