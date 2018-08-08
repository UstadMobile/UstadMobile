package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class IndexPhetContentScraper {

    static final int ENTRY_SIZE_LINK_LENGTH = 1000;
    private File destinationDirectory;
    private URL url;
    private ArrayList<OpdsEntryWithRelations> entryWithRelationsList;
    private ArrayList<OpdsEntryParentToChildJoin> parentToChildJoins;

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

        Document document = Jsoup.connect(urlString).get();

        browseCategory(document);

    }


    public void browseCategory(Document document) throws IOException {
        Elements simulationList = document.select("td.simulation-list-item span.sim-badge-html");
        OpdsEntryWithRelations parentPhet = new OpdsEntryWithRelations(
                UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), UUID.randomUUID().toString(), "Phet Interactive Simulations");

        entryWithRelationsList.add(parentPhet);

        for(Element simulation: simulationList){

            String path = simulation.parent().attr("href");
            String simulationUrl = new URL(url, path).toString();
            String title = simulationUrl.substring(simulationUrl.lastIndexOf("/"), simulationUrl.length());

            OpdsEntryWithRelations simulationChild = new OpdsEntryWithRelations(
                    UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), path, title);

            ArrayList<OpdsEntryWithRelations> categoryList;

            PhetContentScraper scraper = new PhetContentScraper();
            try {
                categoryList = scraper.convert(simulationUrl, new File(destinationDirectory, title));
                entryWithRelationsList.add(simulationChild);
                int count = 0;
                for(OpdsEntryWithRelations category: categoryList){

                    entryWithRelationsList.add(category);

                    OpdsEntryParentToChildJoin phetToCategoryJoin = new OpdsEntryParentToChildJoin(parentPhet.getUuid(),
                            category.getUuid(), count++);

                    OpdsEntryParentToChildJoin join = new OpdsEntryParentToChildJoin(category.getUuid(),
                        simulationChild.getUuid(), count++);

                    parentToChildJoins.add(phetToCategoryJoin);
                    parentToChildJoins.add(join);
                }

                OpdsLink newEntryLink = new OpdsLink(simulationChild.getUuid(), "application/zip",
                        destinationDirectory.getPath() + title + ".zip", OpdsEntry.LINK_REL_ACQUIRE);
                newEntryLink.setLength(ENTRY_SIZE_LINK_LENGTH);
                simulationChild.setLinks(Collections.singletonList(newEntryLink));

            }catch (Exception e){
                System.out.println(e.getCause());
            }
        }
    }

}
