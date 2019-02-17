package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.contentformats.epub.opf.OpfDocument;
import com.ustadmobile.core.contentformats.epub.opf.OpfItem;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithFilePath;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_EPUB;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;

public class TestShrinkerUtils {


    private File tmpDir;
    private File firstepub;
    private File secondepub;
    private ContentEntryFileDao contentFileDao;
    private File tmpFolder;
    private UmAppDatabase repo;

    @Before
    public void before() {
        try {
            initDb();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int countManifestItemsByMediaType(OpfDocument opf, String mimeType) {
        int count = 0;
        for (OpfItem item : opf.getManifestItems().values()) {
            if (mimeType.equals(item.getMediaType()))
                count++;
        }

        return count;
    }

    public static int countManifestItemsWithExtension(OpfDocument opf, String extension) {
        int count = 0;
        extension = extension.toLowerCase();
        for (OpfItem item : opf.getManifestItems().values()) {
            if (item.getHref().toLowerCase().endsWith(extension))
                count++;
        }

        return count;
    }


    public void initDb() throws IOException {
        UmAppDatabase db = UmAppDatabase.getInstance(null);
        db.clearAllTables();
        repo = db.getRepository("https://localhost", "");
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
    public void givenDabaseEntries_whenShrunk_shouldUpdateAllEpubInDb() {

        List<ContentEntryFileWithFilePath> epublist = contentFileDao.findEpubsFiles();

        ShrinkerUtil.shrinkAllEpubInDatabase(repo);

        List<ContentEntryFileWithFilePath> updatedEpubList = contentFileDao.findEpubsFiles();

        Assert.assertTrue(updatedEpubList.get(0).getFileSize() < epublist.get(0).getFileSize());

    }


    @Test
    public void givenValidEpub_whenShrunk_shouldConvertAllImagesToWebPAndOutsourceStylesheets() throws IOException {

        ShrinkerUtil.shrinkEpub(firstepub);

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
            Assert.assertFalse("File does not have png extension",
                    href.toLowerCase().endsWith(".png"));

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


    @Test
    public void givenAnHTMLElementWithMultipleSrc_whenThereIsNoChangesToReplacedFiles_NoChangesShouldBeMade() throws IOException {

        File tmpDir = Files.createTempDirectory("testShrinker").toFile();

        File folder = new File(tmpDir, "folder");
        folder.mkdirs();
        File images = new File(tmpDir, "images");
        images.mkdirs();
        File file = new File(folder, "shrinker.html");
        File imageFile = new File(images, "test1picture.png");

        InputStream is = getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/files/test1picture.png");
        FileUtils.copyToFile(is, imageFile);

        InputStream testShrinker = getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/shrinker/html-with-multiple-image-img-srcs.html");
        FileUtils.copyToFile(testShrinker, file);

        HashMap<File, File> replacedFiles = new HashMap<>();

        Document doc = Jsoup.parse(file, UTF_ENCODING);
        Element afterElement = doc.selectFirst("[src]");
        Attributes beforeAttrList = afterElement.attributes();

        ShrinkerUtil.cleanUpAttributeListWithMultipleSrc(afterElement, replacedFiles, file);

        Assert.assertEquals(beforeAttrList, afterElement.attributes());
    }


    @Test
    public void givenAnHTMLElementWithMultipleSrc_WhenRealWebpPathExistsInDataSrc2_thenOnlyOneSrcAttributeShouldExist() throws IOException {

        File tmpDir = Files.createTempDirectory("testShrinker").toFile();

        File folder = new File(tmpDir, "folder");
        folder.mkdirs();
        File images = new File(tmpDir, "images");
        images.mkdirs();
        File file = new File(folder, "shrinker.html");
        File imageFile = new File(images, "test1picture.png");
        File webp = new File(images, "correct.webp");

        String testPicture = "/com/ustadmobile/lib/contentscrapers/files/test1picture.png";
        InputStream inputStream = getClass().getResourceAsStream(testPicture);
        FileUtils.copyToFile(inputStream, imageFile);

        String testShrinker = "/com/ustadmobile/lib/contentscrapers/shrinker/html-with-multiple-image-img-srcs.html";
        InputStream htmlInput = getClass().getResourceAsStream(testShrinker);
        FileUtils.copyToFile(htmlInput, file);

        String testWebp = "/com/ustadmobile/lib/contentscrapers/files/correct.webp";
        InputStream webpInput = getClass().getResourceAsStream(testWebp);
        FileUtils.copyToFile(webpInput, webp);

        HashMap<File, File> replacedFiles = new HashMap<>();
        replacedFiles.put(imageFile, webp);

        Document doc = Jsoup.parse(file, UTF_ENCODING);
        Element afterElement = doc.selectFirst("img.data-src2");

        ShrinkerUtil.cleanUpAttributeListWithMultipleSrc(afterElement, replacedFiles, file);

        Assert.assertEquals(2, afterElement.attributes().size());

    }

    @Test
    public void givenAnHTMLElementWithMultipleSrc_WhenRealWebpPathIsInSrc_thenOnlyOneSrcAttributeShouldExist() throws IOException {

        File tmpDir = Files.createTempDirectory("testShrinker").toFile();

        File folder = new File(tmpDir, "folder");
        folder.mkdirs();
        File images = new File(tmpDir, "images");
        images.mkdirs();
        File file = new File(folder, "shrinker.html");
        File imageFile = new File(images, "test1picture.png");
        File webp = new File(images, "correct.webp");

        String testPicture = "/com/ustadmobile/lib/contentscrapers/files/test1picture.png";
        InputStream inputStream = getClass().getResourceAsStream(testPicture);
        FileUtils.copyToFile(inputStream, imageFile);


        String testShrinker = "/com/ustadmobile/lib/contentscrapers/shrinker/html-with-multiple-image-img-srcs.html";
        InputStream htmlInput = getClass().getResourceAsStream(testShrinker);
        FileUtils.copyToFile(htmlInput, file);


        String testWebp = "/com/ustadmobile/lib/contentscrapers/files/correct.webp";
        InputStream webpInput = getClass().getResourceAsStream(testWebp);
        FileUtils.copyToFile(webpInput, webp);


        HashMap<File, File> replacedFiles = new HashMap<>();
        replacedFiles.put(imageFile, webp);

        Document doc = Jsoup.parse(file, UTF_ENCODING);
        Element afterElement = doc.selectFirst("img.src");

        ShrinkerUtil.cleanUpAttributeListWithMultipleSrc(afterElement, replacedFiles, file);

        Assert.assertEquals(2, afterElement.attributes().size());

    }

    /**
     * Pratham have a jpeg with a color profile problem that prevents the normal webp
     * conversion working on linux. This tests the workaround in shrinkUtils
     */
    @Test
    public void givennCmykJpg_whenShrunk_shouldBeConvertedToWebP() throws IOException {
        File tmpDir = Files.createTempDirectory("testShrinker").toFile();

        File invalidImage = new File(tmpDir, "invalid.jpg");
        File image = new File(tmpDir, "invalid.webp");
        InputStream is = getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/shrinker/invalid-jpg.jpg");
        FileUtils.copyToFile(is, invalidImage);

        ShrinkerUtil.convertImageToWebp(invalidImage, image);

        Assert.assertEquals(true, ContentScraperUtil.fileHasContent(image));

    }

    @Test(expected = IOException.class)
    public void givenCorruptZip_whenShrunk_shouldThrowIOException() throws IOException {
        File tmpDir = Files.createTempDirectory("testShrinker").toFile();

        File epub = new File(tmpDir, "invalid-epub.epub");

        FileUtils.copyToFile(getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/files/correct.webp"),
                epub);
        ShrinkerUtil.shrinkEpub(epub);
    }

    @Test
    public void givenOpfWithImagesThatDoNotExist_whenShrunk_shouldContinueAndOmitMissingFile() throws IOException {
        File tmpDir = Files.createTempDirectory("testShrinker").toFile();

        File epub = new File(tmpDir, "epub.epub");

        FileUtils.copyToFile(getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/shrinker/missing-image.epub"),
                epub);
        ShrinkerUtil.shrinkEpub(epub);

        ZipFile zipFile = new ZipFile(epub);
        ZipEntry entry = zipFile.getEntry("META-INF/container.xml");
        InputStream is = zipFile.getInputStream(entry);
        Document document = Jsoup.parse(UMIOUtils.readStreamToString(is), "", Parser.xmlParser());
        String path = document.selectFirst("rootfile").attr("full-path");

        ZipEntry opfEntry = zipFile.getEntry(path);
        InputStream opfis = zipFile.getInputStream(opfEntry);
        Document opfdoc = Jsoup.parse(UMIOUtils.readStreamToString(opfis), "", Parser.xmlParser());
        Element manifestitem = opfdoc.selectFirst("manifest item[href=images/images/logowhite.png]");

        Assert.assertEquals("images/images/logowhite.png", manifestitem.attr("href"));

    }

    @Test
    public void givenInvalidImageFile_whenShrunk_shouldContinueAndOmitInvalidFile() throws IOException {
        File tmpDir = Files.createTempDirectory("testShrinker").toFile();

        File epub = new File(tmpDir, "epub.epub");

        FileUtils.copyToFile(getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/shrinker/missing-image.epub"),
                epub);
        ShrinkerUtil.shrinkEpub(epub);

        ZipFile zipFile = new ZipFile(epub);
        ZipEntry entry = zipFile.getEntry("META-INF/container.xml");
        InputStream is = zipFile.getInputStream(entry);
        Document document = Jsoup.parse(UMIOUtils.readStreamToString(is), "", Parser.xmlParser());
        String path = document.selectFirst("rootfile").attr("full-path");

        ZipEntry opfEntry = zipFile.getEntry(path);
        InputStream opfis = zipFile.getInputStream(opfEntry);
        Document opfdoc = Jsoup.parse(UMIOUtils.readStreamToString(opfis), "", Parser.xmlParser());
        Element manifestitem = opfdoc.selectFirst("manifest item[href=images/cover.png]");

        Assert.assertEquals("images/cover.png", manifestitem.attr("href"));
    }


}
