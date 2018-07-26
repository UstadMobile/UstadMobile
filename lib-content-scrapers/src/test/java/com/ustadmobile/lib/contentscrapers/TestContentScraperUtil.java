package com.ustadmobile.lib.contentscrapers;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Okio;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.CONTENT_JSON;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;

public class TestContentScraperUtil {

    private final String RESOURCE_PATH = "/com/ustadmobile/lib/contentscrapers/";
    final Dispatcher dispatcher = new Dispatcher() {

        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {


            if (request.getPath().contains("picture")) {

                int length = "/media/".length();
                String fileName = request.getPath().substring(length,
                        request.getPath().indexOf(".png", length));
                InputStream pictureIn = getClass().getResourceAsStream(RESOURCE_PATH + fileName + ".png");
                return new MockResponse().setResponseCode(200).setBody(Okio.buffer(Okio.source(pictureIn)).buffer());
            }

            return new MockResponse().setResponseCode(404);
        }
    };



    @Test
    public void givenHtmlWithImagesServerOnline_whenDownloadAllResources_thenResourcesDownloaded() throws IOException{
        String htmlWithImage = "<p class=\"MsoNormal\" dir=\"RTL\" style=\"text-align: right; line-height: normal; mso-pagination: none; direction: rtl; unicode-bidi: embed;\"><span style=\"color: #000000;\"><span lang=\"AR-SA\">في</span> <span lang=\"AR-SA\">الشكل</span> <span lang=\"AR-SA\">المقابل</span> <span lang=\"AR-SA\">قياس</span> <img src=\"/media/test1picture.png\" alt=\"\" width=\"30\" height=\"20\" /><span lang=\"AR-SA\">&nbsp;</span><span lang=\"AR-SA\">ب</span><span lang=\"AR-SA\" style=\"font-family: 'Open Sans',sans-serif; mso-fareast-font-family: 'Open Sans';\"> =</span></span></p>\n<p class=\"MsoNormal\" dir=\"RTL\" style=\"text-align: right; line-height: normal; mso-pagination: none; direction: rtl; unicode-bidi: embed;\">&nbsp;</p>\n<p class=\"MsoNormal\" dir=\"RTL\" style=\"text-align: right; line-height: normal; mso-pagination: none; direction: rtl; unicode-bidi: embed;\"><span lang=\"AR-SA\" style=\"font-family: 'Open Sans',sans-serif; mso-fareast-font-family: 'Open Sans';\"><img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"/media/test2picture.png\" width=\"250\" height=\"269\" /></span></p>";

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);


        File tmpDir = Files.createTempDirectory("exercisecontentscraper").toFile();
        String convertedHtml = ContentScraperUtil.downloadAllResources(htmlWithImage, tmpDir, ScraperConstants.HtmlName.DESC.getName() + ScraperConstants.PNG_EXT, mockWebServer.url("/api").url());

        File imageFile = new File(tmpDir, 0 + ScraperConstants.HtmlName.DESC.getName() + ScraperConstants.PNG_EXT);
        //Assert that the image file is downloaded
        Assert.assertTrue("Image Downloaded Successfully", imageFile.exists());

        //Find the image tag in the HTML, make sure that the path is now relative

        Document doc = Jsoup.parse(convertedHtml);
        Element image =  doc.select("img").first();
        Assert.assertTrue("Img Src is pointing to relative path", image.attr("src").equalsIgnoreCase(  tmpDir.getName() + "/" + 0 + ScraperConstants.HtmlName.DESC.getName() + ScraperConstants.PNG_EXT) );
    }



}
