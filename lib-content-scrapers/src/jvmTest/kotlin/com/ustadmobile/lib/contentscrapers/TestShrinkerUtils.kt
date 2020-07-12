package com.ustadmobile.lib.contentscrapers

import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.contentformats.epub.opf.OpfItem
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.checkIfPathsToDriversExist
import com.ustadmobile.port.sharedse.util.UmZipUtils

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities
import org.jsoup.nodes.Node
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.HashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import org.kmp.io.KMPPullParserException
import java.util.function.Consumer

@ExperimentalStdlibApi
class TestShrinkerUtils {


    private var tmpDir: File? = null
    private var firstepub: File? = null
    private var secondepub: File? = null
    private var tmpFolder: File? = null

    @Before
    fun before() {
        try {
            initDb()
            checkIfPathsToDriversExist()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    @Throws(IOException::class)
    fun initDb() {
        val db = UmAppDatabase.getInstance(Any())
        db.clearAllTables()

        val `is` = javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/test.epub")
        tmpDir = Files.createTempDirectory("testShrinkerUtils").toFile()
        firstepub = File(tmpDir, "test.epub")
        FileUtils.copyToFile(`is`, firstepub!!)

        val is2 = javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/test.epub")
        tmpFolder = File(tmpDir, "13232")
        secondepub = File(tmpFolder, "13232.epub")
        FileUtils.copyToFile(is2, secondepub!!)

        `is`.close()
        is2.close()

    }


    @Test
    @Throws(IOException::class, KMPPullParserException::class)
    fun givenValidEpub_whenShrunk_shouldConvertAllImagesToWebPAndOutsourceStylesheets() {

        ShrinkerUtil.shrinkEpub(firstepub!!)

        val tmpTest = File(tmpDir, "test")
        val ocfDoc = OcfDocument()
        val ocfFile = File(tmpTest, Paths.get("META-INF", "container.xml").toString())
        val ocfFileInputStream = FileInputStream(ocfFile)
        val ocfParser = UstadMobileSystemImpl.instance
                .newPullParser(ocfFileInputStream)
        ocfDoc.loadFromParser(ocfParser)

        val opfFile = File(tmpTest, ocfDoc.getRootFiles()[0].fullPath!!)
        val document = OpfDocument()
        val opfFileInputStream = FileInputStream(opfFile)
        val xmlPullParser = UstadMobileSystemImpl.instance
                .newPullParser(opfFileInputStream)
        document.loadFromOPF(xmlPullParser)

        var countWebp = 0
        var countCss = 0
        for (manifest in document.getManifestItems().values) {

            val href = manifest.href
            Assert.assertFalse("File does not have png extension",
                    href!!.toLowerCase().endsWith(".png"))

            if (href.contains(".webp")) {
                countWebp++
            }

            if (href.contains(".css")) {
                countCss++
            }

        }

        Assert.assertEquals("16 webp converted in manifest", 16, countWebp.toLong())
        Assert.assertEquals("16 css converted in manifest", 4, countCss.toLong())

    }


    @Test
    @Throws(IOException::class)
    fun givenAnHTMLElementWithMultipleSrc_whenThereIsNoChangesToReplacedFiles_NoChangesShouldBeMade() {

        val tmpDir = Files.createTempDirectory("testShrinker").toFile()

        val folder = File(tmpDir, "folder")
        folder.mkdirs()
        val images = File(tmpDir, "images")
        images.mkdirs()
        val file = File(folder, "shrinker.html")
        val imageFile = File(images, "test1picture.png")

        val `is` = javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/files/test1picture.png")
        FileUtils.copyToFile(`is`, imageFile)

        val testShrinker = javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/shrinker/html-with-multiple-image-img-srcs.html")
        FileUtils.copyToFile(testShrinker, file)

        val replacedFiles = HashMap<File, File>()

        val doc = Jsoup.parse(file, UTF_ENCODING)
        val afterElement = doc.selectFirst("[src]")
        val beforeAttrList = afterElement.attributes()

        ShrinkerUtil.cleanUpAttributeListWithMultipleSrc(afterElement, replacedFiles, file)

        Assert.assertEquals(beforeAttrList, afterElement.attributes())
    }


    @Test
    @Throws(IOException::class)
    fun givenAnHTMLElementWithMultipleSrc_WhenRealWebpPathExistsInDataSrc2_thenOnlyOneSrcAttributeShouldExist() {

        val tmpDir = Files.createTempDirectory("testShrinker").toFile()

        val folder = File(tmpDir, "folder")
        folder.mkdirs()
        val images = File(tmpDir, "images")
        images.mkdirs()
        val file = File(folder, "shrinker.html")
        val imageFile = File(images, "test1picture.png")
        val webp = File(images, "correct.webp")

        val testPicture = "/com/ustadmobile/lib/contentscrapers/files/test1picture.png"
        val inputStream = javaClass.getResourceAsStream(testPicture)
        FileUtils.copyToFile(inputStream, imageFile)

        val testShrinker = "/com/ustadmobile/lib/contentscrapers/shrinker/html-with-multiple-image-img-srcs.html"
        val htmlInput = javaClass.getResourceAsStream(testShrinker)
        FileUtils.copyToFile(htmlInput, file)

        val testWebp = "/com/ustadmobile/lib/contentscrapers/files/correct.webp"
        val webpInput = javaClass.getResourceAsStream(testWebp)
        FileUtils.copyToFile(webpInput, webp)

        val replacedFiles = HashMap<File, File>()
        replacedFiles[imageFile] = webp

        val doc = Jsoup.parse(file, UTF_ENCODING)
        val afterElement = doc.selectFirst("img.data-src2")

        ShrinkerUtil.cleanUpAttributeListWithMultipleSrc(afterElement, replacedFiles, file)

        Assert.assertEquals(2, afterElement.attributes().size().toLong())

    }

    @Test
    @Throws(IOException::class)
    fun givenAnHTMLElementWithMultipleSrc_WhenRealWebpPathIsInSrc_thenOnlyOneSrcAttributeShouldExist() {

        val tmpDir = Files.createTempDirectory("testShrinker").toFile()

        val folder = File(tmpDir, "folder")
        folder.mkdirs()
        val images = File(tmpDir, "images")
        images.mkdirs()
        val file = File(folder, "shrinker.html")
        val imageFile = File(images, "test1picture.png")
        val webp = File(images, "correct.webp")

        val testPicture = "/com/ustadmobile/lib/contentscrapers/files/test1picture.png"
        val inputStream = javaClass.getResourceAsStream(testPicture)
        FileUtils.copyToFile(inputStream, imageFile)


        val testShrinker = "/com/ustadmobile/lib/contentscrapers/shrinker/html-with-multiple-image-img-srcs.html"
        val htmlInput = javaClass.getResourceAsStream(testShrinker)
        FileUtils.copyToFile(htmlInput, file)


        val testWebp = "/com/ustadmobile/lib/contentscrapers/files/correct.webp"
        val webpInput = javaClass.getResourceAsStream(testWebp)
        FileUtils.copyToFile(webpInput, webp)


        val replacedFiles = HashMap<File, File>()
        replacedFiles[imageFile] = webp

        val doc = Jsoup.parse(file, UTF_ENCODING)
        val afterElement = doc.selectFirst("img.src")

        ShrinkerUtil.cleanUpAttributeListWithMultipleSrc(afterElement, replacedFiles, file)

        Assert.assertEquals(2, afterElement.attributes().size().toLong())

    }

    /**
     * Pratham have a jpeg with a color profile problem that prevents the normal webp
     * conversion working on linux. This tests the workaround in shrinkUtils
     */
    @Test
    @Throws(IOException::class)
    fun givennCmykJpg_whenShrunk_shouldBeConvertedToWebP() {
        val tmpDir = Files.createTempDirectory("testShrinker").toFile()

        val invalidImage = File(tmpDir, "invalid.jpg")
        val image = File(tmpDir, "invalid.webp")
        val `is` = javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/shrinker/invalid-jpg.jpg")
        FileUtils.copyToFile(`is`, invalidImage)

        ShrinkerUtil.convertImageToWebp(invalidImage, image)

        Assert.assertEquals(true, ContentScraperUtil.fileHasContent(image))

    }

    @Test(expected = IOException::class)
    @Throws(IOException::class)
    fun givenCorruptZip_whenShrunk_shouldThrowIOException() {
        val tmpDir = Files.createTempDirectory("testShrinker").toFile()

        val epub = File(tmpDir, "invalid-epub.epub")

        FileUtils.copyToFile(javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/files/correct.webp"),
                epub)
        ShrinkerUtil.shrinkEpub(epub)
    }

    @Test
    @Throws(IOException::class)
    fun givenOpfWithImagesThatDoNotExist_whenShrunk_shouldContinueAndOmitMissingFile() {
        val tmpDir = Files.createTempDirectory("testShrinker").toFile()

        val epub = File(tmpDir, "epub.epub")

        FileUtils.copyToFile(javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/shrinker/missing-image.epub"),
                epub)
        ShrinkerUtil.shrinkEpub(epub)

        val zipFile = ZipFile(epub)
        val entry = zipFile.getEntry("META-INF/container.xml")
        val `is` = zipFile.getInputStream(entry)
        val document = Jsoup.parse(UMIOUtils.readStreamToString(`is`), "", Parser.xmlParser())
        val path = document.selectFirst("rootfile").attr("full-path")

        val opfEntry = zipFile.getEntry(path)
        val opfis = zipFile.getInputStream(opfEntry)
        val opfdoc = Jsoup.parse(UMIOUtils.readStreamToString(opfis), "", Parser.xmlParser())
        val manifestitem = opfdoc.selectFirst("manifest item[href=images/images/logowhite.png]")

        Assert.assertEquals("images/images/logowhite.png", manifestitem.attr("href"))

    }

    @Test
    @Throws(IOException::class)
    fun givenInvalidImageFile_whenShrunk_shouldContinueAndOmitInvalidFile() {
        val tmpDir = Files.createTempDirectory("testShrinker").toFile()

        val epub = File(tmpDir, "epub.epub")

        FileUtils.copyToFile(javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/shrinker/missing-image.epub"),
                epub)
        ShrinkerUtil.shrinkEpub(epub)

        val zipFile = ZipFile(epub)
        val entry = zipFile.getEntry("META-INF/container.xml")
        val `is` = zipFile.getInputStream(entry)
        val document = Jsoup.parse(UMIOUtils.readStreamToString(`is`), "", Parser.xmlParser())
        val path = document.selectFirst("rootfile").attr("full-path")

        val opfEntry = zipFile.getEntry(path)
        val opfis = zipFile.getInputStream(opfEntry)
        val opfdoc = Jsoup.parse(UMIOUtils.readStreamToString(opfis), "", Parser.xmlParser())
        val manifestitem = opfdoc.selectFirst("manifest item[href=images/cover.png]")

        Assert.assertEquals("images/cover.png", manifestitem.attr("href"))
    }

    @Test
    @Throws(IOException::class)
    fun testInvalidText() {

        val tmpDir = Files.createTempDirectory("testShrinker").toFile()

        val epub = File(tmpDir, "epub.epub")
        val testhtml = File(tmpDir, "test.xhtml")

        FileUtils.copyToFile(javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/shrinker/invalidchars.epub"),
                epub)

        val zipFile = ZipFile(epub.path)
        val index = zipFile.getEntry("OEBPS/5.xhtml")
        val inputIndex = zipFile.getInputStream(index)

        var html = IOUtils.toString(inputIndex, UTF_ENCODING)

        html = html.replace("&nbsp;".toRegex(), "&#160;")
        html = html.replace("\\u2029".toRegex(), "")
        html = html.replace("<!DOCTYPE html[<!ENTITY nbsp \"&#160;\">]>",
                "<!DOCTYPE html>")
        val doc = Jsoup.parse(html, "", Parser.xmlParser())
        doc.outputSettings().prettyPrint(false)

        FileUtils.writeStringToFile(testhtml, doc.toString(), UTF_ENCODING)


        Assert.assertTrue("Unicode removed", !html.contains("\\u2029"))

        val z = ZipFile(epub.path)
        val i = z.getEntry("OEBPS/2.xhtml")
        val iI = z.getInputStream(i)

        var test = IOUtils.toString(iI, UTF_ENCODING)

        test = test.replace("&nbsp;".toRegex(), "&#160;")
        test = test.replace("\\u2029".toRegex(), "")
        test = test.replace("<!DOCTYPE html[<!ENTITY nbsp \"&#160;\">]>",
                "<!DOCTYPE html>")
        val d = Jsoup.parse(test, "", Parser.xmlParser())
        d.outputSettings().prettyPrint(false)
        d.outputSettings().escapeMode(Entities.EscapeMode.xhtml)

        FileUtils.writeStringToFile(testhtml, d.toString(), UTF_ENCODING)

        Assert.assertTrue("Unicode removed", !test.contains("&nbsp;"))

    }

    @Test
    @Throws(IOException::class)
    fun testAsbEpub() {

        val tmpDir = Files.createTempDirectory("testShrinker").toFile()

        val epub = File(tmpDir, "epub.epub")
        FileUtils.copyToFile(javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/africanbooks/asb18187.epub"),
                epub)

        val options = ShrinkerUtil.EpubShrinkerOptions()
        options.linkHelper = {
            IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.ASB_CSS_HELPER), UTF_ENCODING)
        }
        ShrinkerUtil.shrinkEpub(epub, options)


    }


    @Test
    @Throws(IOException::class)
    fun testPrathamEpub() {

        val tmpDir = Files.createTempDirectory("testShrinker").toFile()

        val zip = File(tmpDir, "epub.zip")
        FileUtils.copyToFile(javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/pratham/24620-a-book-for-puchku.zip"),
                zip)
        val epub = File(tmpDir, "24620-a-book-for-puchku.epub")
        UmZipUtils.unzip(zip, tmpDir)

        val options = ShrinkerUtil.EpubShrinkerOptions()
        options.styleElementHelper = { styleElement ->
            val text = styleElement.text()
            if (text.startsWith("@font-face") || text.startsWith(".english")) {
                ShrinkerUtil.STYLE_OUTSOURCE_TO_LINKED_CSS
            } else {
                ShrinkerUtil.STYLE_DROP
            }
        }
        options.editor = { document ->
            val elements = document.select("p")
            val elementsToRemove = ArrayList<Element>()
            for (element in elements) {
                if (element.text().isEmpty()) {
                    elementsToRemove.add(element)
                }
            }
            elementsToRemove.forEach(Consumer<Element> { it.remove() })
            document.head().append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable-no\" />")
            document
        }
        options.linkHelper = {
            IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.PRATHAM_CSS_HELPER), UTF_ENCODING)
        }
        val epubFolder = ShrinkerUtil.shrinkEpub(epub, options)
        ContentScraperUtil.zipDirectory(epubFolder, "fixed.epub", tmpDir)


    }

    companion object {

        fun countManifestItemsByMediaType(opf: OpfDocument, mimeType: String): Int {
            var count = 0
            for (item in opf.getManifestItems().values) {
                if (mimeType == item.mediaType)
                    count++
            }

            return count
        }

        fun countManifestItemsWithExtension(opf: OpfDocument, extension: String): Int {
            var extension = extension
            var count = 0
            extension = extension.toLowerCase()
            for (item in opf.getManifestItems().values) {
                if (item.href!!.toLowerCase().endsWith(extension))
                    count++
            }

            return count
        }
    }


}
