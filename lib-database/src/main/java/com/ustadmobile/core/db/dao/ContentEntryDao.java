package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmQueryFindByPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.ContentEntryWithContentEntryStatus;
import com.ustadmobile.lib.db.entities.ContentEntryWithStatusAndMostRecentContainerUid;
import com.ustadmobile.lib.db.entities.DistinctCategorySchema;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;
import com.ustadmobile.lib.db.entities.ContentEntry;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ContentEntryDao implements SyncableDao<ContentEntry, ContentEntryDao> {

    @UmInsert
    public abstract Long[] insert(List<ContentEntry> contentEntries);

    @UmQuery("SELECT * FROM ContentEntry WHERE contentEntryUid=:entryUuid")
    public abstract ContentEntry findByEntryId(long entryUuid);

    @UmQuery("SELECT * FROM ContentEntry")
    public abstract List<ContentEntry> getAllEntries();

    @UmQuery("SELECT * FROM ContentEntry WHERE sourceUrl = :sourceUrl LIMIT 1")
    public abstract ContentEntry findBySourceUrl(String sourceUrl);

    @UmQuery("SELECT ContentEntry.* FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    public abstract UmProvider<ContentEntry> getChildrenByParentUid(long parentUid);

    @UmQuery("SELECT COUNT(*) FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    public abstract void getCountNumberOfChildrenByParentUUid(long parentUid, UmCallback<Integer> callback);


    @UmQuery("SELECT * FROM ContentEntry where contentEntryUid = :parentUid LIMIT 1")
    public abstract void getContentByUuid(long parentUid, UmCallback<ContentEntry> callback);


    @UmQuery("SELECT ContentEntry.* FROM ContentEntry LEFT JOIN ContentEntryRelatedEntryJoin " +
            "ON ContentEntryRelatedEntryJoin.cerejRelatedEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryRelatedEntryJoin.relType = 1 AND ContentEntryRelatedEntryJoin.cerejRelatedEntryUid != :entryUuid")
    public abstract void findAllLanguageRelatedEntries(long entryUuid, UmCallback<List<ContentEntry>> umCallback);


    @UmQuery("SELECT DISTINCT ContentCategory.contentCategoryUid, ContentCategory.name AS categoryName, " +
            "ContentCategorySchema.contentCategorySchemaUid, ContentCategorySchema.schemaName FROM ContentEntry " +
            "LEFT JOIN ContentEntryContentCategoryJoin ON ContentEntryContentCategoryJoin.ceccjContentEntryUid = ContentEntry.contentEntryUid " +
            "LEFT JOIN ContentCategory ON ContentCategory.contentCategoryUid = ContentEntryContentCategoryJoin.ceccjContentCategoryUid " +
            "LEFT JOIN ContentCategorySchema ON ContentCategorySchema.contentCategorySchemaUid = ContentCategory.ctnCatContentCategorySchemaUid " +
            "LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid " +
            "AND ContentCategory.contentCategoryUid != 0 ORDER BY ContentCategory.name")
    public abstract void findListOfCategories(long parentUid, UmCallback<List<DistinctCategorySchema>> umCallback);


    @UmQuery("SELECT DISTINCT Language.* from Language " +
            "LEFT JOIN ContentEntry ON ContentEntry.primaryLanguageUid = Language.langUid " +
            "LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid ORDER BY Language.name")
    public abstract void findUniqueLanguagesInList(long parentUid, UmCallback<List<Language>> umCallback);

    @UmUpdate
    public abstract void update(ContentEntry entity);

    @UmQueryFindByPrimaryKey
    public abstract void findByUid(Long entryUid, UmCallback<ContentEntry> umCallback);

    @UmQuery("SELECT ContentEntry.*, ContentEntryStatus.* FROM ContentEntry " +
            "LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.contentEntryUid = :contentEntryUid")
    public abstract void findByUidWithContentEntryStatus(long contentEntryUid,
                                                         UmCallback<ContentEntryWithContentEntryStatus> callback);

    @UmQuery("SELECT ContentEntry.*, ContentEntryStatus.* FROM ContentEntry " +
            "LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.sourceUrl = :sourceUrl")
    public abstract void findBySourceUrlWithContentEntryStatus(String sourceUrl,
                                                               UmCallback<ContentEntryWithContentEntryStatus> callback);

    @UmQuery("SELECT * FROM ContentEntry WHERE publik")
    public abstract List<ContentEntry> getPublicContentEntries();

    @UmQuery("SELECT ContentEntry.*,ContentEntryStatus.*, " +
            "(SELECT containerUid FROM Container " +
            "WHERE containerContentEntryUid =  ContentEntry.contentEntryUid ORDER BY lastModified DESC LIMIT 1) as mostRecentContainer " +
            "FROM ContentEntry "+
            "LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid " +
            "AND " +
            "(:langParam = 0 OR ContentEntry.primaryLanguageUid = :langParam) " +
            "AND " +
            "(:categoryParam0 = 0 OR :categoryParam0 IN (SELECT ceccjContentCategoryUid FROM ContentEntryContentCategoryJoin " +
            "WHERE ceccjContentEntryUid = ContentEntry.contentEntryUid))")
    public abstract UmProvider<ContentEntryWithStatusAndMostRecentContainerUid> getChildrenByParentUidWithCategoryFilter(long parentUid, long langParam, long categoryParam0);


    @UmQuery("SELECT ContentEntry.*, ContentEntryStatus.*, " +
            "(SELECT containerUid FROM Container  " +
            "WHERE containerContentEntryUid =  ContentEntry.contentEntryUid ORDER BY lastModified DESC LIMIT 1) as mostRecentContainer " +
            "FROM DownloadSet \n" +
            "LEFT JOIN ContentEntry on  DownloadSet.dsRootContentEntryUid = ContentEntry.contentEntryUid\n" +
            "LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid \n ")
    public abstract UmProvider<ContentEntryWithStatusAndMostRecentContainerUid> getDownloadedRootItems();


}
