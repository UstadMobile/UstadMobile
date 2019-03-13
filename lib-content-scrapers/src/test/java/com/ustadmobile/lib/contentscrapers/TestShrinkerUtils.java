package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument;
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument;
import com.ustadmobile.core.contentformats.epub.opf.OpfItem;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMIOUtils;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;

public class TestShrinkerUtils {


    private File tmpDir;
    private File firstepub;
    private File secondepub;
    private File tmpFolder;

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

    }


    @Test
    public void givenValidEpub_whenShrunk_shouldConvertAllImagesToWebPAndOutsourceStylesheets() throws IOException, XmlPullParserException {

        ShrinkerUtil.shrinkEpub(firstepub);

        File tmpTest = new File(tmpDir, "test");
        OcfDocument ocfDoc = new OcfDocument();
        File ocfFile = new File(tmpTest, Paths.get("META-INF", "container.xml").toString());
        FileInputStream ocfFileInputStream = new FileInputStream(ocfFile);
        XmlPullParser ocfParser = UstadMobileSystemImpl.getInstance()
                .newPullParser(ocfFileInputStream);
        ocfDoc.loadFromParser(ocfParser);

        File opfFile = new File(tmpTest, ocfDoc.getRootFiles().get(0).getFullPath());
        OpfDocument document = new OpfDocument();
        FileInputStream opfFileInputStream = new FileInputStream(opfFile);
        XmlPullParser xmlPullParser = UstadMobileSystemImpl.getInstance()
                .newPullParser(opfFileInputStream);
        document.loadFromOPF(xmlPullParser);

        int countWebp = 0;
        int countCss = 0;
        for (OpfItem manifest : document.getManifestItems().values()) {

            String href = manifest.getHref();
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
