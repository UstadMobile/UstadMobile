package com.ustadmobile.core.db

import androidx.room.Database
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile
import com.ustadmobile.core.db.dao.SaleItemReminderDao
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao
import com.ustadmobile.core.db.dao.SaleVoiceNoteDao
import com.ustadmobile.core.db.dao.SaleProductGroupJoinDao
import com.ustadmobile.core.db.dao.SaleProductGroupDao
import com.ustadmobile.core.db.dao.SalePaymentDao
import com.ustadmobile.core.db.dao.SaleProductPictureDao
import com.ustadmobile.core.db.dao.SaleProductDao
import com.ustadmobile.core.db.dao.SaleItemDao
import com.ustadmobile.core.db.dao.SaleDao

@Database(entities = [
    NetworkNode::class, EntryStatusResponse::class, DownloadJobItemHistory::class,
    DownloadJob::class, DownloadJobItem::class, DownloadJobItemParentChildJoin::class, Person::class,
    Clazz::class, ClazzMember::class, PersonCustomField::class, PersonCustomFieldValue::class,
    ContentEntry::class, ContentEntryContentCategoryJoin::class, ContentEntryParentChildJoin::class,
    ContentEntryRelatedEntryJoin::class, ContentCategorySchema::class, ContentCategory::class,
    Language::class, LanguageVariant::class, AccessToken::class, PersonAuth::class, Role::class,
    EntityRole::class, PersonGroup::class, PersonGroupMember::class, Location::class,
    LocationAncestorJoin::class, PersonLocationJoin::class, PersonPicture::class,
    ScrapeQueueItem::class, ScrapeRun::class, ContentEntryStatus::class, ConnectivityStatus::class,
    Container::class, ContainerEntry::class, ContainerEntryFile::class,
    VerbEntity::class, XObjectEntity::class, StatementEntity::class,
    ContextXObjectStatementJoin::class, AgentEntity::class,
    StateEntity::class, StateContentEntity::class

    //Goldozi :
    ,Sale::class, SaleItem::class, SalePayment::class,
    SaleProductGroup::class,
    SaleProductGroupJoin::class,
    SaleProductPicture::class, SaleProduct::class,
    SaleVoiceNote::class, SaleProductParentJoin::class,
    SaleItemReminder::class,
    DashboardEntry::class, DashboardTag::class, DashboardEntryTag::class
    ], version = 26)

abstract class UmAppDatabase : DoorDatabase() {

    var isMaster: Boolean = false

    var attachmentsDir: String? = null


    abstract val networkNodeDao: NetworkNodeDao

    abstract val entryStatusResponseDao: EntryStatusResponseDao

    abstract val downloadJobDao: DownloadJobDao

    abstract val downloadJobItemDao: DownloadJobItemDao

    abstract val downloadJobItemParentChildJoinDao: DownloadJobItemParentChildJoinDao

    abstract val downloadJobItemHistoryDao: DownloadJobItemHistoryDao

    abstract val personDao: PersonDao

    abstract val clazzDao: ClazzDao

    abstract val clazzMemberDao: ClazzMemberDao

    abstract val contentEntryDao: ContentEntryDao

    abstract val personCustomFieldDao: PersonCustomFieldDao

    abstract val personCustomFieldValueDao: PersonCustomFieldValueDao

    abstract val contentEntryContentCategoryJoinDao: ContentEntryContentCategoryJoinDao

    abstract val contentEntryParentChildJoinDao: ContentEntryParentChildJoinDao

    abstract val contentEntryRelatedEntryJoinDao: ContentEntryRelatedEntryJoinDao

    abstract val contentCategorySchemaDao: ContentCategorySchemaDao

    abstract val contentCategoryDao: ContentCategoryDao

    abstract val languageDao: LanguageDao

    abstract val languageVariantDao: LanguageVariantDao

    abstract val scrapeQueueItemDao: ScrapeQueueItemDao

    abstract val personAuthDao: PersonAuthDao

    abstract val accessTokenDao: AccessTokenDao

    abstract val roleDao: RoleDao

    abstract val personGroupDao: PersonGroupDao

    abstract val personGroupMemberDao: PersonGroupMemberDao

    abstract val entityRoleDao: EntityRoleDao

    abstract val locationDao: LocationDao

    abstract val locationAncestorJoinDao: LocationAncestorJoinDao

    abstract val personLocationJoinDao: PersonLocationJoinDao

    abstract val personPictureDao: PersonPictureDao

    abstract val scrapeRunDao: ScrapeRunDao

    abstract val contentEntryStatusDao: ContentEntryStatusDao

    abstract val connectivityStatusDao: ConnectivityStatusDao

    abstract val containerDao: ContainerDao

    abstract val containerEntryDao: ContainerEntryDao

    abstract val containerEntryFileDao: ContainerEntryFileDao

    abstract val verbDao: VerbDao

    abstract val xObjectDao: XObjectDao

    abstract val statementDao: StatementDao

    abstract val contextXObjectStatementJoinDao: ContextXObjectStatementJoinDao

    abstract val stateDao: StateDao

    abstract val stateContentDao: StateContentDao

    abstract val agentDao: AgentDao

    //abstract val syncablePrimaryKeyDao: SyncablePrimaryKeyDao


    //Goldozi bit:

    abstract val saleDao: SaleDao

    abstract val saleItemDao: SaleItemDao

    abstract val saleProductDao: SaleProductDao

    abstract val saleProductPictureDao: SaleProductPictureDao

    abstract val salePaymentDao: SalePaymentDao

    abstract val saleProductGroupDao: SaleProductGroupDao

    abstract val saleProductGroupJoinDao: SaleProductGroupJoinDao

    abstract val saleVoiceNoteDao: SaleVoiceNoteDao

    abstract val saleProductParentJoinDao: SaleProductParentJoinDao

    abstract val saleItemReminderDao: SaleItemReminderDao

    abstract val dashboardEntryDao:DashboardEntryDao

    abstract val dashboardTagDao:DashboardTagDao

    abstract val dashboarfEntryTagDao:DashboardEntryTagDao

    //@UmRepository
    //abstract fun getRepository(baseUrl: String?, auth: String?): UmAppDatabase

    // @UmSyncOutgoing
    // abstract fun syncWith(otherDb: UmAppDatabase, accountUid: Long, sendLimit: Int, receiveLimit: Int)


    // fun invalidateDeviceBits() {
    //     syncablePrimaryKeyDao.invalidateDeviceBits()
    // }

    // @UmSyncCountLocalPendingChanges
    // abstract fun countPendingLocalChanges(accountUid: Long, deviceId: Int): Int


    //end of Goldozi bit.

    fun validateAuth(personUid: Long, auth: String): Boolean {
        return if (personUid == 0L) true else accessTokenDao.isValidToken(personUid, auth)//Anonymous or guest access
    }
    companion object {

        @Volatile
        private var instance: UmAppDatabase? = null

        private val namedInstances = mutableMapOf<String, UmAppDatabase>()

        /**
         * For use by other projects using this app as a library. By calling setInstance before
         * any other usage (e.g. in the Android Application class) a child class of this database (eg.
         * with additional entities) can be used.
         *
         * @param instance
         */
        @Synchronized
        @JsName("setInstance")
        fun setInstance(instance: UmAppDatabase) {
            UmAppDatabase.instance = instance
        }

        /**
         * For use by other projects using this app as a library. By calling setInstance before
         * any other usage (e.g. in the Android Application class) a child class of this database (eg.
         * with additional entities) can be used.
         *
         * @param instance
         * @param dbName
         */
        @Synchronized
        @JsName("setInstanceWithName")
        fun setInstance(instance: UmAppDatabase, dbName: String) {
            namedInstances[dbName] = instance
        }

        fun getInstance(context: Any) = lazy { Companion.getInstance(context, "UmAppDatabase") }.value

        @Synchronized
        fun getInstance(context: Any, dbName: String): UmAppDatabase {
            var db = namedInstances[dbName]

            if (db == null) {
                var builder = DatabaseBuilder.databaseBuilder(
                        context, UmAppDatabase::class, dbName)
                //builder = addMigrations(builder)
                //db = addCallbacks(builder).build()
                db = builder.build()
                namedInstances[dbName] = db
            }
            return db
        }


    }

}
