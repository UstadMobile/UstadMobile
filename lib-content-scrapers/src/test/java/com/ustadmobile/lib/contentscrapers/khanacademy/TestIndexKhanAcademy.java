package com.ustadmobile.lib.contentscrapers.khanacademy;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.brainGenieLink;

public class TestIndexKhanAcademy {

    final Dispatcher dispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

            try {

                if (request.getPath().contains("json")) {

                    String fileName = request.getPath().substring(5,
                            request.getPath().length());
                    String body = IOUtils.toString(getClass().getResourceAsStream(fileName), UTF_ENCODING);
                    return new MockResponse().setBody(body);

                } else if (request.getPath().contains("content")) {

                    String fileLocation = request.getPath().substring(8,
                            request.getPath().length());
                    InputStream videoIn = getClass().getResourceAsStream(fileLocation);
                    BufferedSource source = Okio.buffer(Okio.source(videoIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);

                    return new MockResponse().setResponseCode(200).setBody(buffer);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return new MockResponse().setResponseCode(404);
        }
    };

    @Test
    public void scrapeIndex() throws IOException {

        File tmpDir = Files.createTempDirectory("testIndexKhancontentscraper").toFile();
        IndexKhanContentScraper indexScraper = new IndexKhanContentScraper();
        indexScraper.findContent("https://www.khanacademy.org/", tmpDir);

    }


    @Test
    public void givenServerOnline_whenKhanContentScraped_thenShouldConvertAndDownloadAllFiles() throws IOException {

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);


        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repo = db.getRepository("https://localhost", "");

        File tmpDir = Files.createTempDirectory("testIndexKhancontentscraper").toFile();

        IndexKhanContentScraper indexScraper = new IndexKhanContentScraper();
        indexScraper.findContent(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/khanacademy/mainpage.txt").toString(), tmpDir);

        File englishFolder = new File(tmpDir, "en");
        Assert.assertEquals(true, englishFolder.isDirectory());

        File topicFolder = new File(englishFolder, "math");
        Assert.assertEquals(true, topicFolder.isDirectory());

        File subjectFolder = new File(topicFolder, "cc-early-math-counting-topic");
        Assert.assertEquals(true, subjectFolder.isDirectory());

        File tutorialFolder = new File(subjectFolder, "early-math");
        Assert.assertEquals(true, tutorialFolder.isDirectory());

        File coursesFolder = new File(tutorialFolder, "cc-early-math-counting");
        Assert.assertEquals(true, coursesFolder.isDirectory());

        File videoCourseFolder = new File(coursesFolder, "counting-with-small-numbers");
        Assert.assertEquals(true, videoCourseFolder.isDirectory());

        File videoFile = new File(videoCourseFolder, "counting-with-small-numbers.mp4");
        Assert.assertEquals(true, videoFile.isFile());

        ContentEntryDao contentEntryDao = repo.getContentEntryDao();
        ContentEntryParentChildJoinDao parentChildDaoJoin = repo.getContentEntryParentChildJoinDao();

        ContentEntry parentEntry = contentEntryDao.findBySourceUrl("https://www.khanacademy.org/");
        Assert.assertEquals("Main parent content entry exsits", true, parentEntry.getEntryId().equalsIgnoreCase("https://www.khanacademy.org/"));

        ContentEntry subjectEntry = contentEntryDao.findBySourceUrl(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/khanacademy/topicspage.txt").toString());
        ContentEntryParentChildJoin parentChildJoinEntry = parentChildDaoJoin.findParentByChildUuids(subjectEntry.getContentEntryUid());
        Assert.assertEquals(true, parentChildJoinEntry.getCepcjParentContentEntryUid() == parentEntry.getContentEntryUid());

        ContentEntry gradeEntry = contentEntryDao.findBySourceUrl(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/khanacademy/subjectpage.txt").toString());
        ContentEntryParentChildJoin gradeSubjectJoin = parentChildDaoJoin.findParentByChildUuids(gradeEntry.getContentEntryUid());
        Assert.assertEquals(true, gradeSubjectJoin.getCepcjParentContentEntryUid() == subjectEntry.getContentEntryUid());

        ContentEntry headingTopicEntry = contentEntryDao.findBySourceUrl(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/khanacademy/coursespage.txt").toString());
        ContentEntryParentChildJoin subjectHeadingJoin = parentChildDaoJoin.findParentByChildUuids(headingTopicEntry.getContentEntryUid());
        Assert.assertEquals(true, subjectHeadingJoin.getCepcjParentContentEntryUid() == gradeEntry.getContentEntryUid());


    }

    @Test
    public void testPractice() throws IOException {

        File tmpDir = Files.createTempDirectory("testKhanExercisecontentscraper").toFile();

        KhanContentScraper scraper = new KhanContentScraper(tmpDir);
        scraper.scrapeExerciseContent("https://www.khanacademy.org/math/early-math/cc-early-math-counting-topic/cc-early-math-counting/e/counting-out-1-20-objects");
        //   scraper.scrapeExerciseContent("https://www.khanacademy.org/math/early-math/cc-early-math-counting-topic/cc-early-math-counting/e/counting-objects");
    }

    @Test
    public void testArticleContent() throws IOException {

        File tmpDir = Files.createTempDirectory("testKhanArticleContentScraper").toFile();

        KhanContentScraper scraper = new KhanContentScraper(tmpDir);
        scraper.scrapeArticleContent("https://www.khanacademy.org/math/early-math/cc-early-math-place-value-topic/cc-early-math-two-digit-compare/a/comparison-symbols-review");
        //


    }

}
