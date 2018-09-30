package com.ustadmobile.lib.contentscrapers.phetsimulation;

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

/**
 * The Phet Simulation Website provides a list of all the available Html5 Content in one of their categories found at
 * https://phet.colorado.edu/en/simulations/category/html
 * <p>
 * By using a css selector: td.simulation-list-item span.sim-badge-html
 * We can get the url to each simulation in that category to give to PhetContentScraper to scrap its content
 */
public class IndexPhetContentScraper {

    static final int ENTRY_SIZE_LINK_LENGTH = 1000;
    private File destinationDirectory;
    private URL url;
    private ArrayList<OpdsEntryWithRelations> entryWithRelationsList;
    private ArrayList<OpdsEntryParentToChildJoin> parentToChildJoins;


    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: <phet html url> <file destination>");
            System.exit(1);
        }

        System.out.println(args[0]);
        System.out.println(args[1]);
        try {
            new IndexPhetContentScraper().findContent(args[0], new File(args[1]));
        } catch (IOException e) {
            System.err.println("Exception running findContent");
            e.printStackTrace();
        }
    }

    /**
     * Given a phet url, find the content and download
     *
     * @param urlString      url link to phet category
     * @param destinationDir destination folder for phet content
     * @throws IOException
     */
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


    private void browseCategory(Document document) throws IOException {

        Elements simulationList = document.select("td.simulation-list-item span.sim-badge-html");

        OpdsEntryWithRelations parentPhet = new OpdsEntryWithRelations(
                UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), "https://phet.colorado.edu/", "Phet Interactive Simulations");

        entryWithRelationsList.add(parentPhet);

        for (Element simulation : simulationList) {

            String path = simulation.parent().attr("href");
            String simulationUrl = new URL(url, path).toString();
            String title = simulationUrl.substring(simulationUrl.lastIndexOf("/") + 1, simulationUrl.length());

            OpdsEntryWithRelations simulationChild = new OpdsEntryWithRelations(
                    UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), path, title);

            ArrayList<OpdsEntryWithRelations> categoryList;
            ArrayList<OpdsEntryWithRelations> translationList;

            PhetContentScraper scraper = new PhetContentScraper(simulationUrl, destinationDirectory);
            try {
                scraper.scrapeContent();

                categoryList = scraper.getCategoryRelations();
                translationList = scraper.getTranslations(destinationDirectory);

                entryWithRelationsList.add(simulationChild);
                int count = 0;
                for (OpdsEntryWithRelations category : categoryList) {

                    entryWithRelationsList.add(category);

                    OpdsEntryParentToChildJoin phetToCategoryJoin = new OpdsEntryParentToChildJoin(parentPhet.getUuid(),
                            category.getUuid(), count++);

                    OpdsEntryParentToChildJoin categoryToSimulationJoin = new OpdsEntryParentToChildJoin(category.getUuid(),
                            simulationChild.getUuid(), count++);

                    parentToChildJoins.add(phetToCategoryJoin);
                    parentToChildJoins.add(categoryToSimulationJoin);

                    for (OpdsEntryWithRelations translation : translationList) {

                        OpdsEntryParentToChildJoin categoryToSimulationTranslationJoin = new OpdsEntryParentToChildJoin(category.getUuid(),
                                translation.getUuid(), count++);

                        parentToChildJoins.add(categoryToSimulationTranslationJoin);


                    }
                }


                OpdsLink newEntryLink = new OpdsLink(simulationChild.getUuid(), "application/zip",
                        destinationDirectory.getPath() + "\\" + title + ".zip", OpdsEntry.LINK_REL_ACQUIRE);
                newEntryLink.setLength(ENTRY_SIZE_LINK_LENGTH);
                simulationChild.setLinks(Collections.singletonList(newEntryLink));

            } catch (Exception e) {
                System.out.println(e.getCause());
            }
        }
    }

}
