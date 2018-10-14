package com.ustadmobile.lib.contentscrapers.ddl;

import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class IndexDdlContent {


    private URL url;
    private File destinationDirectory;

    private List<OpdsEntryWithRelations> entryWithRelationsList;
    private List<OpdsEntryParentToChildJoin> parentToChildJoins;
    private int maxNumber;
    private OpdsEntryWithRelations parentDdl;
    private OpdsEntryWithRelations langEntry;
    private int langCount = 0;

    public void findContent(String urlString, File destinationDir) throws IOException {

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            System.out.println("Index Malformed url" + urlString);
            throw new IllegalArgumentException("Malformed url" + urlString, e);
        }

        destinationDir.mkdirs();
        destinationDirectory = destinationDir;


        entryWithRelationsList = new ArrayList<>();
        parentToChildJoins = new ArrayList<>();


        parentDdl = new OpdsEntryWithRelations(
                UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), "https://www.ddl.af/", "Darakht-e Danesh");

        entryWithRelationsList.add(parentDdl);

        browseLanguages("ps");
        browseLanguages("fa");
        browseLanguages("en");

    }

    private void browseLanguages(String lang) throws IOException {

        Document document = Jsoup.connect("https://www.darakhtdanesh.org/" + lang + "/resources/list")
                .header("X-Requested-With", "XMLHttpRequest").get();

        Elements pageList = document.select("a.page-link");

        langEntry = new OpdsEntryWithRelations(
                UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), lang + "/resources/list", lang);

        entryWithRelationsList.add(langEntry);

        OpdsEntryParentToChildJoin join = new OpdsEntryParentToChildJoin(parentDdl.getUuid(),
                langEntry.getUuid(), langCount);
        parentToChildJoins.add(join);

        maxNumber = 0;
        for (Element page : pageList) {

            String num = page.text();
            try {
                int number = Integer.parseInt(num);
                if (number > maxNumber) {
                    maxNumber = number;
                }
            } catch (NumberFormatException e) {
            }
        }

        browseList(lang, 1);
        langCount++;
    }

    private void browseList(String lang, int count) throws IOException {

        if (count > maxNumber) {
            return;
        }

        Document document = Jsoup.connect("https://www.darakhtdanesh.org/" + lang + "/resources/list?page=" + count)
                .header("X-Requested-With", "XMLHttpRequest").get();

        Elements resourceList = document.select("a[href]");

        for (Element resource : resourceList) {

            String url = resource.attr("href");
            if (url.contains("resource/")) {

                DdlContentScraper scraper = new DdlContentScraper(url, destinationDirectory);
                try {
                    scraper.scrapeContent();
                    ArrayList<OpdsEntryWithRelations> categories = scraper.getCategoryRelations();
                    ArrayList<OpdsEntryWithRelations> files = scraper.getOpdsFiles();
                    int categoryCount = 0;
                    for(OpdsEntryWithRelations category: categories){
                        OpdsEntryParentToChildJoin join = new OpdsEntryParentToChildJoin(langEntry.getUuid(),
                                category.getUuid(), categoryCount++);

                        entryWithRelationsList.add(category);
                        parentToChildJoins.add(join);

                        int fileCount = 0;
                        for(OpdsEntryWithRelations file: files){

                            OpdsEntryParentToChildJoin categoryFileJoin = new OpdsEntryParentToChildJoin(category.getUuid(),
                                    file.getUuid(), fileCount++);

                            parentToChildJoins.add(categoryFileJoin);


                        }
                    }
                    entryWithRelationsList.addAll(files);


                } catch (IOException | URISyntaxException e) {
                    System.out.println("Error downloading resource at " + url);
                }

            }


        }

        browseList(lang, ++count);

    }

}
