package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_EPUB;

public class TestShrinkerUtils {


    private File tmpDir;
    private File firstepub;
    private File secondepub;
    private ContentEntryFileDao contentFileDao;
    private File tmpFolder;

    @Before
    public void before() {
        try {
            initDb();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void initDb() throws IOException {
        UmAppDatabase db = UmAppDatabase.getInstance(null);
        db.clearAllTables();
        UmAppDatabase repo = db.getRepository("https://localhost", "");
        contentFileDao = repo.getContentEntryFileDao();
        ContentEntryFileStatusDao statusDao = db.getContentEntryFileStatusDao();


        InputStream is = getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/test.epub");
        tmpDir = Files.createTempDirectory("testShrinkerUtils").toFile();
        firstepub = new File(tmpDir, "test.epub");
        FileUtils.copyToFile(is, firstepub);

        InputStream is2 = getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/test.epub");
        tmpFolder = new File(tmpDir, "13232");
        secondepub = new File(tmpFolder, "13232.epub");
        FileUtils.copyToFile(is2, secondepub);

        is.close();
        is2.close();

        ContentEntryFile firstfileentry = new ContentEntryFile();
        firstfileentry.setContentEntryFileUid(242343);
        firstfileentry.setFileSize(firstepub.length());
        firstfileentry.setMimeType(MIMETYPE_EPUB);
        firstfileentry.setMd5sum(ContentScraperUtil.getMd5(firstepub));
        firstfileentry.setLastModified(firstepub.lastModified());
        contentFileDao.insert(firstfileentry);

        ContentEntryFileStatus firststatus = new ContentEntryFileStatus();
        firststatus.setCefsContentEntryFileUid(242343);
        firststatus.setFilePath(firstepub.getPath());
        statusDao.insert(firststatus);

        ContentEntryFile secondEntry = new ContentEntryFile();
        secondEntry.setContentEntryFileUid(223);
        secondEntry.setFileSize(secondepub.length());
        secondEntry.setMimeType(MIMETYPE_EPUB);
        secondEntry.setMd5sum(ContentScraperUtil.getMd5(secondepub));
        secondEntry.setLastModified(secondepub.lastModified());
        contentFileDao.insert(secondEntry);

        ContentEntryFileStatus secondStatus = new ContentEntryFileStatus();
        secondStatus.setCefsContentEntryFileUid(223);
        secondStatus.setFilePath(secondepub.getPath());
        statusDao.insert(secondStatus);

    }

    @Test
    public void givenValidEpub_whenShrunk_shouldConvertAllImagesToWebPAndOutsourceStylesheets() throws IOException {

        ShrinkerUtil.main(new String[]{"db"});

        Assert.assertEquals("Failed to delete the tmp Folder for epub test after shrinking", false, new File(tmpDir, "test").exists());
        Assert.assertEquals("Failed to delete the tmp Folder for epub 13232 after shrinking", false, new File(tmpFolder, "13232").exists());

        ZipFile zipFile = new ZipFile(firstepub);
        ZipEntry entry = zipFile.getEntry("META-INF/container.xml");
        InputStream is = zipFile.getInputStream(entry);
        Document document = Jsoup.parse(UMIOUtils.readStreamToString(is), "", Parser.xmlParser());
        String path = document.selectFirst("rootfile").attr("full-path");

        ZipEntry opfEntry = zipFile.getEntry(path);
        InputStream opfis = zipFile.getInputStream(opfEntry);
        Document opfdoc = Jsoup.parse(UMIOUtils.readStreamToString(opfis), "", Parser.xmlParser());
        Elements manifestitems = opfdoc.select("manifest item");

        int countWebp = 0;
        int countCss = 0;
        for (Element manifest : manifestitems) {

            String href = manifest.attr("href");
            Assert.assertEquals("png file still exists in manifest", false, href.contains(".png"));

            if (href.contains(".webp")) {
                countWebp++;
            }

            if (href.contains(".css")) {
                countCss++;
            }

        }

        Assert.assertEquals("16 webp converted in manifest", 16, countWebp);
        Assert.assertEquals("16 css converted in manifest", 4, countCss);

        countWebp = 0;
        countCss = 0;
        Enumeration<? extends ZipEntry> entryList = zipFile.entries();
        while (entryList.hasMoreElements()) {

            ZipEntry entryElement = entryList.nextElement();
            String href = entryElement.getName();
            Assert.assertEquals("png file still exists in epub", false, href.contains(".png"));

            if (href.contains(".webp")) {
                countWebp++;
            }

            if (href.contains(".css")) {
                countCss++;
            }

        }

        Assert.assertEquals("16 webp available in epub", 16, countWebp);
        Assert.assertEquals("4 css available in epub", 4, countCss);


    }

}
