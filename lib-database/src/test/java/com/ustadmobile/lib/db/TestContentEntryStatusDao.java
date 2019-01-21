package com.ustadmobile.lib.db;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;

import org.junit.Before;

import java.util.Arrays;

public class TestContentEntryStatusDao {

    private UmAppDatabase appDb;

    private UmAppDatabase appRepo;

    @Before
    protected void before() {
        appDb = UmAppDatabase.getInstance(null, "db1");
        appRepo = appDb.getRepository("http://localhost/dummy/", "");

        appDb.clearAllTables();

    }

    protected void insertContentEntities() {
        ContentEntry rootEntry = new ContentEntry("root", "test root", false,
                true);
        ContentEntry subCategory1 = new ContentEntry("Sub Category 1", "test category", false,
                true);
        ContentEntry subCategory2 = new ContentEntry("Sub Category 2", "test sub 2", false,
                true);

        ContentEntry subCat1Leaf = new ContentEntry("Leaf 1", "Leaf 1", true,
                true);
        ContentEntry subCat2Leaf = new ContentEntry("Leaf 2", "Leaf 2", true,
                true);

        ContentEntryFile subCat1LeafFile = new ContentEntryFile(1000);
        ContentEntryFile subCat2LeafFile = new ContentEntryFile(1500);

        ContentEntryDao entryDao = appRepo.getContentEntryDao();
        rootEntry.setContentEntryUid(entryDao.insert(rootEntry));
        subCategory1.setContentEntryUid(entryDao.insert(subCategory1));
        subCategory2.setContentEntryUid(entryDao.insert(subCategory2));
        subCat1Leaf.setContentEntryUid(entryDao.insert(subCat1Leaf));
        subCat2Leaf.setContentEntryUid(entryDao.insert(subCat2Leaf));

        appRepo.getContentEntryParentChildJoinDao().insertList(Arrays.asList(
                new ContentEntryParentChildJoin(rootEntry, subCategory1, 0),
                new ContentEntryParentChildJoin(rootEntry, subCategory2, 1),
                new ContentEntryParentChildJoin(subCategory1, subCat1Leaf, 0),
                new ContentEntryParentChildJoin(subCategory2, subCat2Leaf, 0)
        ));

        ContentEntryFileDao entryFileDao = appRepo.getContentEntryFileDao();
        subCat1LeafFile.setContentEntryFileUid(entryFileDao.insert(subCat1LeafFile));
        subCat2LeafFile.setContentEntryFileUid(entryFileDao.insert(subCat2LeafFile));

        ContentEntryContentEntryFileJoin subcat1LeafJoin = new ContentEntryContentEntryFileJoin(
                subCat1Leaf, subCat1LeafFile);
        ContentEntryContentEntryFileJoin subcat2LeafJoin = new ContentEntryContentEntryFileJoin(
                subCat2Leaf, subCat2LeafFile);
        appRepo.getContentEntryContentEntryFileJoinDao().insertList(Arrays.asList(subcat1LeafJoin,
                subcat2LeafJoin));
    }

    public void givenBlankDatabase_whenContentEntriesInserted_thenInvalidatedContentEntryStatusEntitiesShouldBeCreated() {
        insertContentEntities();




    }

    public void givenContentEntitiesWithParents_whenRefreshCalled_thenEntryStatusDaoShouldUpdate() {

    }

    public void givenExistingContentEntries_whenEntitiesAreUpdated_thenContentEntryStatusShouldBeInvalidated() {

    }


}
