package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.lib.contentscrapers.util.SrtFormat;
import com.ustadmobile.port.sharedse.util.UmZipUtils;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;

public class TestContentScraperUtil {

    private final String RESOURCE_PATH = "/com/ustadmobile/lib/contentscrapers/files/";
    final Dispatcher dispatcher = new Dispatcher() {

        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

            try {


                if (request.getPath().contains("picture")) {

                    int length = "/media/".length();
                    String fileName = request.getPath().substring(length,
                            request.getPath().indexOf(".png", length));
                    InputStream pictureIn = getClass().getResourceAsStream(RESOURCE_PATH + fileName + ".png");

                    BufferedSource source = Okio.buffer(Okio.source(pictureIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);
                    MockResponse response = new MockResponse().setResponseCode(200);
                    response.setHeader("ETag", (String.valueOf(buffer.size())
                            + RESOURCE_PATH).hashCode());
                    if (!request.getMethod().equalsIgnoreCase("HEAD"))
                        response.setBody(buffer);

                    return response;
                }

                return new MockResponse().setResponseCode(404);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println(request.getPath());
            }

            return new MockResponse().setResponseCode(404);
        }
    };


    @Test
    public void givenHtmlWithImagesServerOnline_whenDownloadAllResources_thenResourcesDownloaded() throws IOException {
        String htmlWithImage = "<p class=\"MsoNormal\" dir=\"RTL\" style=\"text-align: right; line-height: normal; mso-pagination: none; direction: rtl; unicode-bidi: embed;\"><span style=\"color: #000000;\"><span lang=\"AR-SA\">في</span> <span lang=\"AR-SA\">الشكل</span> <span lang=\"AR-SA\">المقابل</span> <span lang=\"AR-SA\">قياس</span> <img src=\"/media/test1picture.png\" alt=\"\" width=\"30\" height=\"20\" /><span lang=\"AR-SA\">&nbsp;</span><span lang=\"AR-SA\">ب</span><span lang=\"AR-SA\" style=\"font-family: 'Open Sans',sans-serif; mso-fareast-font-family: 'Open Sans';\"> =</span></span></p>\n<p class=\"MsoNormal\" dir=\"RTL\" style=\"text-align: right; line-height: normal; mso-pagination: none; direction: rtl; unicode-bidi: embed;\">&nbsp;</p>\n<p class=\"MsoNormal\" dir=\"RTL\" style=\"text-align: right; line-height: normal; mso-pagination: none; direction: rtl; unicode-bidi: embed;\"><span lang=\"AR-SA\" style=\"font-family: 'Open Sans',sans-serif; mso-fareast-font-family: 'Open Sans';\"><img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"/media/test2picture.png\" width=\"250\" height=\"269\" /></span></p>";

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        File tmpDir = Files.createTempDirectory("exercisecontentscraper").toFile();
        File resourceLocation = new File(tmpDir, "resource");
        resourceLocation.mkdirs();

        String convertedHtml = ContentScraperUtil.downloadAllResources(htmlWithImage, resourceLocation, mockWebServer.url("/api").url());

        File imageFile = new File(resourceLocation, "media_test1picture.webp");
        //Assert that the image file is downloaded
        Assert.assertTrue("Image Downloaded Successfully", imageFile.exists());

        //Find the image tag in the HTML, make sure that the path is now relative

        Document doc = Jsoup.parse(convertedHtml);
        Element image = doc.select("img").first();
        Assert.assertTrue("Img Src is pointing to relative path", image.attr("src").equalsIgnoreCase(resourceLocation.getName() + "/media_test1picture.webp"));
    }


    @Test
    public void givenEdraak12Date_whenParsingDate_thenDateConvertedToLong() {

        String commonEdraakDate = "2018-01-07T08:19:46.410000";
        String videoEdraakDate = "2018-01-17T18:38:17.612502Z";
        String exerciseEdraakDate = "2018-01-17T21:22:37";

        ContentScraperUtil.parseServerDate(commonEdraakDate);
        ContentScraperUtil.parseServerDate(videoEdraakDate);
        ContentScraperUtil.parseServerDate(exerciseEdraakDate);

    }

    @Test(expected = DateTimeParseException.class)
    public void givenEdraak12Date_whenParsingDate_thenThrowDateTimeParseException() {

        String unCommonEdraakDate = "2018-01-07T08:19:46.4100";

        ContentScraperUtil.parseServerDate(unCommonEdraakDate);
    }

    @Test
    public void givenListOfSrtFormat_whenParsed_thenShouldSaveValidSrtFile() throws IOException {

        List<SrtFormat> list = new ArrayList<>();
        list.add(new SrtFormat("[Voiceover] Put seven\nsquirrels in the box.", 3350, 518));
        list.add(new SrtFormat("All right, so that's", 4917, 3350));
        list.add(new SrtFormat("one, two,", 7857, 4917));
        list.add(new SrtFormat("three, four", 11407, 7857));
        list.add(new SrtFormat("five, six,", 14890, 11407));

        File tmpDir = Files.createTempDirectory("srtFileDirectory").toFile();
        File srtFile = new File(tmpDir, "srtfile.srt");

        ContentScraperUtil.createSrtFile(list, srtFile);

        Assert.assertTrue("SRT file Exists", ContentScraperUtil.fileHasContent(srtFile));

        String srt = FileUtils.readFileToString(srtFile, UTF_ENCODING);

    }

    @Test
    public void givenZip() throws IOException {

        File tmpDir = Files.createTempDirectory("exercisecontentscraper").toFile();
        File contentFolder = new File(tmpDir, "content");
        contentFolder.mkdirs();

        File content = new File(tmpDir, "content.zip");
        FileUtils.copyToFile(getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/africanbooks/asb18187.epub"), content);

        UmZipUtils.unzip(content, contentFolder);

        Map<File, String> hashmap = new HashMap<>();
        ContentScraperUtil.createContainerFromDirectory(contentFolder, hashmap);


    }


}
