package com.ustadmobile.lib.contentscrapers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.CONTENT_JSON;

public class TestContentScraperUtil {


    @Test
    public void givenHtmlWithImagesServerOnline_whenDownloadAllResources_thenResourcesDownloaded() throws IOException{
        String htmlWithImage = "<p class=\"MsoNormal\" dir=\"RTL\" style=\"text-align: right; line-height: normal; mso-pagination: none; direction: rtl; unicode-bidi: embed;\"><span style=\"color: #000000;\"><span lang=\"AR-SA\">في</span> <span lang=\"AR-SA\">الشكل</span> <span lang=\"AR-SA\">المقابل</span> <span lang=\"AR-SA\">قياس</span> <img src=\"https://s3.amazonaws.com/edraak-progs-bucket/program_assets/repository/math-geo-trig-oers-v1/images/1516223272-blobid1516223137195.png\" alt=\"\" width=\"30\" height=\"20\" /><span lang=\"AR-SA\">&nbsp;</span><span lang=\"AR-SA\">ب</span><span lang=\"AR-SA\" style=\"font-family: 'Open Sans',sans-serif; mso-fareast-font-family: 'Open Sans';\"> =</span></span></p>\n<p class=\"MsoNormal\" dir=\"RTL\" style=\"text-align: right; line-height: normal; mso-pagination: none; direction: rtl; unicode-bidi: embed;\">&nbsp;</p>\n<p class=\"MsoNormal\" dir=\"RTL\" style=\"text-align: right; line-height: normal; mso-pagination: none; direction: rtl; unicode-bidi: embed;\"><span lang=\"AR-SA\" style=\"font-family: 'Open Sans',sans-serif; mso-fareast-font-family: 'Open Sans';\"><img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"https://s3.amazonaws.com/edraak-progs-bucket/program_assets/repository/math-geo-trig-oers-v1/images/1515316255-blobid1515316135733.png\" width=\"250\" height=\"269\" /></span></p>";
        File tmpDir = Files.createTempDirectory("exercisecontentscraper").toFile();
        String convertedHtml = ContentScraperUtil.downloadAllResources(htmlWithImage, tmpDir, ScraperConstants.HtmlName.DESC.getName() + ScraperConstants.PNG_EXT);

        File imageFile = new File(tmpDir, ScraperConstants.HtmlName.DESC.getName() + ScraperConstants.PNG_EXT);
        //Assert that the image file is downloaded
        Assert.assertTrue("Image Downloaded Successfully", imageFile.exists());

        //Find the image tag in the HTML, make sure that the path is now relative

        Document doc = Jsoup.parse(convertedHtml);
        Element image =  doc.select("img").first();
        Assert.assertTrue("Img Src is pointing to relative path", image.attr("src").equalsIgnoreCase(  tmpDir.getName() + "/" + 0 + ScraperConstants.HtmlName.DESC.getName() + ScraperConstants.PNG_EXT) );
    }



}
