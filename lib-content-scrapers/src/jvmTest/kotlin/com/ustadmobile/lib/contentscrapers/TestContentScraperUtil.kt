package com.ustadmobile.lib.contentscrapers

import com.ustadmobile.lib.contentscrapers.util.SrtFormat
import com.ustadmobile.port.sharedse.util.UmZipUtils

import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import org.junit.Assert
import org.junit.Test

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.time.format.DateTimeParseException
import java.util.ArrayList
import java.util.HashMap

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import okio.*
import org.junit.Before


class TestContentScraperUtil {

    @Before
    fun setup(){
        ContentScraperUtil.checkIfPathsToDriversExist()
    }

    private val RESOURCE_PATH = "/com/ustadmobile/lib/contentscrapers/files/"
    internal val dispatcher: Dispatcher = object : Dispatcher() {

        @Throws(InterruptedException::class)
        override fun dispatch(request: RecordedRequest): MockResponse {

            try {
                val requestPath = request.path ?: ""

                if (requestPath.contains("picture")) {

                    val length = "/media/".length
                    val fileName = requestPath.substring(length,
                        requestPath.indexOf(".png", length))
                    val pictureIn = javaClass.getResourceAsStream("$RESOURCE_PATH$fileName.png")

                    val source = pictureIn.source().buffer()
                    val buffer = Buffer()
                    source.readAll(buffer)
                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", (buffer.size.toString() + RESOURCE_PATH).hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(buffer)

                    return response
                }

                return MockResponse().setResponseCode(404)
            } catch (e: IOException) {
                e.printStackTrace()
                System.err.println(request.path)
            }

            return MockResponse().setResponseCode(404)
        }
    }


    @Test
    @Throws(IOException::class)
    fun givenHtmlWithImagesServerOnline_whenDownloadAllResources_thenResourcesDownloaded() {
        val htmlWithImage = "<p class=\"MsoNormal\" dir=\"RTL\" style=\"text-align: right; line-height: normal; mso-pagination: none; direction: rtl; unicode-bidi: embed;\"><span style=\"color: #000000;\"><span lang=\"AR-SA\">في</span> <span lang=\"AR-SA\">الشكل</span> <span lang=\"AR-SA\">المقابل</span> <span lang=\"AR-SA\">قياس</span> <img src=\"/media/test1picture.png\" alt=\"\" width=\"30\" height=\"20\" /><span lang=\"AR-SA\">&nbsp;</span><span lang=\"AR-SA\">ب</span><span lang=\"AR-SA\" style=\"font-family: 'Open Sans',sans-serif; mso-fareast-font-family: 'Open Sans';\"> =</span></span></p>\n<p class=\"MsoNormal\" dir=\"RTL\" style=\"text-align: right; line-height: normal; mso-pagination: none; direction: rtl; unicode-bidi: embed;\">&nbsp;</p>\n<p class=\"MsoNormal\" dir=\"RTL\" style=\"text-align: right; line-height: normal; mso-pagination: none; direction: rtl; unicode-bidi: embed;\"><span lang=\"AR-SA\" style=\"font-family: 'Open Sans',sans-serif; mso-fareast-font-family: 'Open Sans';\"><img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"/media/test2picture.png\" width=\"250\" height=\"269\" /></span></p>"

        val mockWebServer = MockWebServer()
        mockWebServer.dispatcher = dispatcher

        val tmpDir = Files.createTempDirectory("exercisecontentscraper").toFile()
        val resourceLocation = File(tmpDir, "resource")
        resourceLocation.mkdirs()

        val convertedHtml = ContentScraperUtil.downloadAllResources(htmlWithImage, resourceLocation,
            mockWebServer.url("/api").toUrl()
        )

        val imageFile = File(resourceLocation, "media_test1picture.webp")
        //Assert that the image file is downloaded
        Assert.assertTrue("Image Downloaded Successfully", imageFile.exists())

        //Find the image tag in the HTML, make sure that the path is now relative

        val doc = Jsoup.parse(convertedHtml)
        val image = doc.select("img").first()
        Assert.assertTrue("Img Src is pointing to relative path",
            image?.attr("src").equals(resourceLocation.name + "/media_test1picture.webp", ignoreCase = true) ?: false)
    }


    @Test
    fun givenEdraak12Date_whenParsingDate_thenDateConvertedToLong() {

        val commonEdraakDate = "2018-01-07T08:19:46.410000"
        val videoEdraakDate = "2018-01-17T18:38:17.612502Z"
        val exerciseEdraakDate = "2018-01-17T21:22:37"

        ContentScraperUtil.parseServerDate(commonEdraakDate)
        ContentScraperUtil.parseServerDate(videoEdraakDate)
        ContentScraperUtil.parseServerDate(exerciseEdraakDate)

    }

    @Test(expected = DateTimeParseException::class)
    fun givenEdraak12Date_whenParsingDate_thenThrowDateTimeParseException() {

        val unCommonEdraakDate = "2018-01-07T08:19:46.4100"

        ContentScraperUtil.parseServerDate(unCommonEdraakDate)
    }

    @Test
    @Throws(IOException::class)
    fun givenListOfSrtFormat_whenParsed_thenShouldSaveValidSrtFile() {

        val list = ArrayList<SrtFormat>()
        list.add(SrtFormat("[Voiceover] Put seven\nsquirrels in the box.", 3350, 518))
        list.add(SrtFormat("All right, so that's", 4917, 3350))
        list.add(SrtFormat("one, two,", 7857, 4917))
        list.add(SrtFormat("three, four", 11407, 7857))
        list.add(SrtFormat("five, six,", 14890, 11407))

        val tmpDir = Files.createTempDirectory("srtFileDirectory").toFile()
        val srtFile = File(tmpDir, "srtfile.srt")

        ContentScraperUtil.createSrtFile(list, srtFile)

        Assert.assertTrue("SRT file Exists", ContentScraperUtil.fileHasContent(srtFile))

        val srt = FileUtils.readFileToString(srtFile, UTF_ENCODING)

    }

    @Test
    @Throws(IOException::class)
    fun givenZip() {

        val tmpDir = Files.createTempDirectory("exercisecontentscraper").toFile()
        val contentFolder = File(tmpDir, "content")
        contentFolder.mkdirs()

        val content = File(tmpDir, "content.zip")
        FileUtils.copyToFile(javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/africanbooks/asb18187.epub"), content)

        UmZipUtils.unzip(content, contentFolder)

        val hashmap = HashMap<File, String>()
        ContentScraperUtil.createContainerFromDirectory(contentFolder, hashmap)


    }


}
