package com.ustadmobile.lib.contentscrapers.phetsimulation;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

public class TestPhetContentScraper {


    private String EN_LOCATION_FILE = "/com/ustadmobile/lib/contentscrapers/phetsimulation/simulation_en.html";
    private String ES_LOCATION_FILE = "/com/ustadmobile/lib/contentscrapers/phetsimulation/simulation_es.html";
    private String HTML_FILE_LOCATION = "/com/ustadmobile/lib/contentscrapers/phetsimulation/phet-html-detail.html";
    private String JAR_FILE_LOCATION = "/com/ustadmobile/lib/contentscrapers/phetsimulation/phet-jar-detail.html";
    private String FLASH_FILE_LOCATION = "/com/ustadmobile/lib/contentscrapers/phetsimulation/phet-flash-detail.html";

    private final String PHET_MAIN_CONTENT = "/com/ustadmobile/lib/contentscrapers/phetsimulation/phet-main-content.html";

    private String SIM_EN = "simulation_en.html";
    private String SIM_ES = "simulation_es.html";


    final Dispatcher dispatcher = new Dispatcher() {


        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

            try {

                if (request.getPath().startsWith("/api/simulation")) {

                    return new MockResponse().setBody(readFile(HTML_FILE_LOCATION));

                } else if (request.getPath().contains(PHET_MAIN_CONTENT)) {

                    return new MockResponse().setBody(readFile(PHET_MAIN_CONTENT));

                } else if (request.getPath().equals("/media/simulation_en.html?download")) {

                    MockResponse mock = new MockResponse();
                    mock.setBody(readFile(EN_LOCATION_FILE));
                    mock.addHeader("ETag", "16adca-5717010854ac0");
                    mock.addHeader("Last-Modified", "Fri, 20 Jul 2018 15:36:51 GMT");

                    return mock;
                } else if (request.getPath().contains("/media/simulation_es.html?download")) {

                    MockResponse mock = new MockResponse();
                    mock.setBody(readFile(ES_LOCATION_FILE));
                    mock.addHeader("ETag", "16adca-5717010854ac0");
                    mock.addHeader("Last-Modified", "Fri, 20 Jul 2018 15:36:51 GMT");

                    return mock;
                } else if (request.getPath().contains("flash")) {

                    return new MockResponse().setBody(readFile(FLASH_FILE_LOCATION));
                } else if (request.getPath().contains("jar")) {

                    return new MockResponse().setBody(readFile(JAR_FILE_LOCATION));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return new MockResponse().setResponseCode(404);
        }
    };

    public Buffer readFile(String location) throws IOException {
        InputStream videoIn = getClass().getResourceAsStream(location);
        BufferedSource source = Okio.buffer(Okio.source(videoIn));
        Buffer buffer = new Buffer();
        source.readAll(buffer);

        return buffer;
    }


    public void AssertAllFiles(File tmpDir, PhetContentScraper scraper) {

        File englishLocation = new File(tmpDir, "en");
        Assert.assertTrue("English Folder exists", englishLocation.isDirectory());

        File titleDirectory = new File(englishLocation, scraper.getTitle());
        Assert.assertTrue("English Simulation Folder exists", titleDirectory.isDirectory());

        File aboutFile = new File(titleDirectory, ScraperConstants.ABOUT_HTML);
        Assert.assertTrue("About File English Exists", aboutFile.length() > 0);

        File englishSimulation = new File(titleDirectory, SIM_EN);
        Assert.assertTrue("English Simulation exists", englishSimulation.length() > 0);

        File engETag = new File(titleDirectory, "simulation_en" + ScraperConstants.ETAG_TXT);
        Assert.assertTrue("English ETag exists", engETag.length() > 0);

        File titleZip = new File(englishLocation, scraper.getTitle() + ScraperConstants.ZIP_EXT);
        Assert.assertTrue("English Simulation Folder exists", titleZip.length() > 0);

        File spanishDir = new File(tmpDir, "es");
        Assert.assertTrue("Spanish Folder exists", spanishDir.isDirectory());

        File spanishTitleDirectory = new File(spanishDir, scraper.getTitle());
        Assert.assertTrue("Spanish Zip exists", spanishTitleDirectory.isDirectory());

        File aboutSpanishFile = new File(spanishTitleDirectory, ScraperConstants.ABOUT_HTML);
        Assert.assertTrue("About File English Exists", aboutSpanishFile.length() > 0);

        File spanishSimulation = new File(spanishTitleDirectory, SIM_ES);
        Assert.assertTrue("Spanish Simulation exists", spanishSimulation.length() > 0);

        File spanishETag = new File(spanishTitleDirectory, "simulation_es" + ScraperConstants.ETAG_TXT);
        Assert.assertTrue("Spanish ETag exists", spanishETag.length() > 0);

        File spanishTitleZip = new File(spanishDir, scraper.getTitle() + ScraperConstants.ZIP_EXT);
        Assert.assertTrue("Spanish Title Zip exists", spanishTitleZip.length() > 0);

    }


    @Test
    public void givenServerOnline_whenPhetContentScraped_thenShouldConvertAndDownload() throws IOException {
        File tmpDir = Files.createTempDirectory("testphetcontentscraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        String mockServerUrl = mockWebServer.url("/api/simulation/equality-explorer-two-variables").toString();
        PhetContentScraper scraper = new PhetContentScraper(mockServerUrl, tmpDir);
        scraper.scrapeContent();

        AssertAllFiles(tmpDir, scraper);
    }

    @Test
    public void givenServerOnline_whenPhetContentScrapedAgain_thenShouldNotDownloadFilesAgain() throws IOException {
        File tmpDir = Files.createTempDirectory("testphetcontentscraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        String mockServerUrl = mockWebServer.url("/api/simulation/equality-explorer-two-variables").toString();
        PhetContentScraper scraper = new PhetContentScraper(mockServerUrl, tmpDir);
        scraper.scrapeContent();
        File englishLocation = new File(tmpDir, "en");
        File titleDirectory = new File(englishLocation, scraper.getTitle());
        File englishSimulation = new File(titleDirectory, SIM_EN);

        long firstSimDownload = englishSimulation.lastModified();

        scraper.scrapeContent();

        long lastModified = englishSimulation.lastModified();

        Assert.assertEquals("didnt download 2nd time", firstSimDownload, lastModified);

    }

    @Test(expected = IllegalArgumentException.class)
    public void givenServerOnline_whenPhetContentScraped_thenShouldThrowIllegalArgumentJarNotSupported() throws IOException {
        File tmpDir = Files.createTempDirectory("testphetcontentscraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        PhetContentScraper scraper = new PhetContentScraper(mockWebServer.url("/legacy/jar").toString(), tmpDir);
        scraper.scrapeContent();

    }

    @Test(expected = IllegalArgumentException.class)
    public void givenServerOnline_whenPhetContentScraped_thenShouldThrowIllegalArgumentFlashNotSupported() throws IOException {
        File tmpDir = Files.createTempDirectory("testphetcontentscraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        PhetContentScraper scraper = new PhetContentScraper(mockWebServer.url("/legacy/flash").toString(), tmpDir);
        scraper.scrapeContent();

    }

    @Test
    public void givenServerOnline_whenUrlFound_findAllSimulations() throws IOException {

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repo = db.getRepository("https://localhost", "");
        db.clearAllTables();

        IndexPhetContentScraper index = new IndexPhetContentScraper();
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        File tmpDir = Files.createTempDirectory("testphetindexscraper").toFile();

        index.findContent(mockWebServer.url(PHET_MAIN_CONTENT).toString(), tmpDir);

        ContentEntryDao contentEntryDao = repo.getContentEntryDao();
        ContentEntryParentChildJoinDao parentChildDaoJoin = repo.getContentEntryParentChildJoinDao();
        ContentEntryFileDao fileDao = repo.getContentEntryFileDao();
        ContentEntryContentEntryFileJoinDao fileEntryJoin = repo.getContentEntryContentEntryFileJoinDao();
        ContentEntryContentCategoryJoinDao categoryJoinDao = repo.getContentEntryContentCategoryJoinDao();
        ContentEntryRelatedEntryJoinDao relatedJoin = repo.getContentEntryRelatedEntryJoinDao();

        ContentEntry parentEntry = contentEntryDao.findBySourceUrl("https://phet.colorado.edu/");
        Assert.assertEquals("Main parent content entry exsits", true, parentEntry.getEntryId().equalsIgnoreCase("https://phet.colorado.edu/"));

        ContentEntry categoryEntry = contentEntryDao.findBySourceUrl("/en/simulations/category/math");
        ContentEntryParentChildJoin parentChildJoinEntry = parentChildDaoJoin.findParentByChildUuids(categoryEntry.getContentEntryUid());
        Assert.assertEquals("Category Math entry exists", true, parentChildJoinEntry.getCepcjParentContentEntryUid() == parentEntry.getContentEntryUid());

        ContentEntry englishSimulationEntry = contentEntryDao.findBySourceUrl("/api/simulation/test");
        Assert.assertEquals("Simulation entry english exists", true, englishSimulationEntry.getEntryId().equalsIgnoreCase("/api/simulation/test"));

        List<ContentEntryParentChildJoin> categorySimulationEntryLists = parentChildDaoJoin.findListOfParentsByChildUuid(englishSimulationEntry.getContentEntryUid());
        boolean hasMathCategory = false;
        for(ContentEntryParentChildJoin category : categorySimulationEntryLists){

            if(category.getCepcjParentContentEntryUid() == categoryEntry.getContentEntryUid()){
                hasMathCategory = true;
                break;
            }
        }
        Assert.assertEquals("Parent child join between category and simulation exists",true, hasMathCategory);

        ContentEntry spanishEntry = contentEntryDao.findBySourceUrl("es/test");
        Assert.assertEquals("Simulation entry spanish exists", true, spanishEntry.getEntryId().equalsIgnoreCase("es/test"));

        ContentEntryRelatedEntryJoin spanishEnglishJoin = relatedJoin.findPrimaryByTranslation(spanishEntry.getContentEntryUid());
        Assert.assertEquals("Related Join with Simulation Exists - Spanish Match",true, spanishEnglishJoin.getCerejRelatedEntryUid() == spanishEntry.getContentEntryUid());
        Assert.assertEquals("Related Join with Simulation Exists - English Match",true, spanishEnglishJoin.getCerejContentEntryUid() == englishSimulationEntry.getContentEntryUid());

        List<ContentEntryContentEntryFileJoin> listOfFiles = fileEntryJoin.findChildByParentUUid(englishSimulationEntry.getContentEntryUid());
        Assert.assertEquals(true, listOfFiles.size() > 0);

        ContentEntryFile file = fileDao.findByUid(listOfFiles.get(0).getCecefjContentEntryFileUid());
        Assert.assertEquals(true, ScraperConstants.MIMETYPE_ZIP.equalsIgnoreCase(file.getMimeType()));

    }


    @Test
    public void givenDirectoryOfTranslationsIsCreated_findAllTranslationRelations() throws IOException {

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        db.clearAllTables();

        File tmpDir = Files.createTempDirectory("testphetcontentscraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        PhetContentScraper scraper = new PhetContentScraper(mockWebServer.url("/api/simulation/equality-explorer-two-variables").toString(), tmpDir);
        scraper.scrapeContent();

        ArrayList<ContentEntry> translationList = scraper.getTranslations(tmpDir, db.getContentEntryDao(), "", db.getLanguageDao(), db.getLanguageVariantDao());

    }

    @Test
    public void givenParametersFromGradleCommandLineAndServerOnline_whenPhetContentScraped_thenShouldConvertAndDownload() throws IOException {

        if (System.getProperty("phetUrl") != null && System.getProperty("phetDir") != null) {
            File tmp = new File(System.getProperty("phetDir"));
            PhetContentScraper scraper = new PhetContentScraper(System.getProperty("phetUrl"), tmp);
            scraper.scrapeContent();
            AssertAllFiles(tmp, scraper);
        }
    }

    @Test
    public void givenParametersFromGradleCommandLineAndServerOnline_findSimulationsAndDownload() throws IOException {
        IndexPhetContentScraper content = new IndexPhetContentScraper();
        if (System.getProperty("findPhetUrl") != null && System.getProperty("findPhetDir") != null) {
            content.findContent(System.getProperty("findPhetUrl"), new File(System.getProperty("findPhetDir")));
        }
    }


}
