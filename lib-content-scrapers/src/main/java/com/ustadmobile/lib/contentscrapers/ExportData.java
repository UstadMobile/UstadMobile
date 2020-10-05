package com.ustadmobile.lib.contentscrapers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentCategoryDao;
import com.ustadmobile.core.db.dao.ContentCategorySchemaDao;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.db.dao.LanguageVariantDao;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentCategory;
import com.ustadmobile.lib.db.entities.ContentCategorySchema;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.entities.LanguageVariant;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.JSON_EXT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;

public class ExportData {

    private File destinationDirectory;
    private Gson gson;
    private ArrayList<String> pathList;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: <file destination><optional max size of entry><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }

        UMLogUtil.setLevel(args.length == 3 ? args[2] : "");
        UMLogUtil.logInfo(args[0]);
        int size = args.length == 2 ? Integer.parseInt(args[1]) : 1000;

        try {
            new ExportData().export(new File(args[0]), size);
        } catch (IOException e) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
        }


    }

    public void export(File destination, int size) throws IOException {

        destination.mkdirs();
        destinationDirectory = destination;

        gson = new GsonBuilder().create();

        pathList = new ArrayList<>();

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        ContentEntryDao contentEntryDao = repository.getContentEntryDao();
        ContentEntryParentChildJoinDao contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        ContentEntryRelatedEntryJoinDao relatedDao = repository.getContentEntryRelatedEntryJoinDao();

        ContentCategorySchemaDao schemaDao = repository.getContentCategorySchemaDao();
        ContentCategoryDao categoryDao = repository.getContentCategoryDao();
        ContentEntryContentCategoryJoinDao categoryJoinDao = repository.getContentEntryContentCategoryJoinDao();

        LanguageDao languageDao = repository.getLanguageDao();
        LanguageVariantDao variantDao = repository.getLanguageVariantDao();

        List<ContentEntry> contentEntryList = contentEntryDao.getPublicContentEntries();
        List<ContentEntryParentChildJoin> parentChildJoinList = contentParentChildJoinDao.getPublicContentEntryParentChildJoins();
        List<ContentEntryRelatedEntryJoin> relatedJoinList = relatedDao.getPublicContentEntryRelatedEntryJoins();

        UMLogUtil.logDebug("size of contentEntryList is " + contentEntryList.size());
        UMLogUtil.logDebug("size of parentChildJoinList is " + parentChildJoinList.size());
        UMLogUtil.logDebug("size of relatedJoinList is " + relatedJoinList.size());

        List<ContentCategorySchema> schemaList = schemaDao.getPublicContentCategorySchemas();
        List<ContentCategory> categoryList = categoryDao.getPublicContentCategories();
        List<ContentEntryContentCategoryJoin> categoryJoinList = categoryJoinDao.getPublicContentEntryContentCategoryJoins();

        UMLogUtil.logDebug("size of schemaList is " + schemaList.size());
        UMLogUtil.logDebug("size of categoryList is " + categoryList.size());
        UMLogUtil.logDebug("size of categoryJoinList is " + categoryJoinList.size());

        List<Language> langList = languageDao.getPublicLanguages();
        //List<LanguageVariant> langVariantList = variantDao.getPublicLanguageVariants();

        UMLogUtil.logDebug("size of langList is " + langList.size());
        //UMLogUtil.logDebug("size of langVariantList is " + langVariantList.size());

        List<Container> containerList = repository.getContainerDao().findAllPublikContainers();

        UMLogUtil.logDebug("size of container is " + containerList.size());

        saveListToJson(split(contentEntryList, size), "contentEntry.", destinationDirectory);
        saveListToJson(split(parentChildJoinList, size), "contentEntryParentChildJoin.", destinationDirectory);
        saveListToJson(split(relatedJoinList, size), "contentEntryRelatedEntryJoin.", destinationDirectory);

        saveListToJson(split(schemaList, size), "contentCategorySchema.", destinationDirectory);
        saveListToJson(split(categoryList, size), "contentCategory.", destinationDirectory);
        saveListToJson(split(categoryJoinList, size), "contentEntryContentCategoryJoin.", destinationDirectory);

        saveListToJson(split(langList, size), "language.", destinationDirectory);
        saveListToJson(split(langVariantList, size), "languageVariant.", destinationDirectory);

        saveListToJson(split(containerList, size), "container.", destinationDirectory);

        FileUtils.writeStringToFile(new File(destination, "index.json"), gson.toJson(pathList), UTF_ENCODING);

    }

    private <T> Collection<List<T>> saveListToJson(Collection<List<T>> listOfLists, String nameOfFile, File destinationDirectory) {

        Iterator<List<T>> iterator = listOfLists.iterator();
        UMLogUtil.logDebug(nameOfFile + " was split into " + listOfLists.size());
        int count = 0;
        while (iterator.hasNext()) {

            String fileName = nameOfFile + count++ + JSON_EXT;
            File file = new File(destinationDirectory, fileName);
            pathList.add(fileName);
            try {
                FileUtils.writeStringToFile(file, gson.toJson(iterator.next()), UTF_ENCODING);
            } catch (IOException e) {
                UMLogUtil.logError("Error saving file " + nameOfFile + count);
            }
        }
        return listOfLists;
    }

    private <T> Collection<List<T>> split(List<T> list, int size) {
        final AtomicInteger counter = new AtomicInteger(0);

        return list.stream().collect(Collectors.groupingBy
                (it -> counter.getAndIncrement() / size))
                .values();

    }


}
