package com.ustadmobile.core.db

import androidx.room.Database
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.SyncableDoorDatabase
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile

@Database(entities = [NetworkNode::class, EntryStatusResponse::class, DownloadJobItemHistory::class,
    DownloadJob::class, DownloadJobItem::class, DownloadJobItemParentChildJoin::class, Person::class,
    PersonCustomField::class, ContentEntryRelatedEntryJoin::class, ContentCategorySchema::class,
    ContentCategory::class, Language::class, LanguageVariant::class, Container::class,
    ContainerEntry::class, ContainerEntryFile::class, VerbEntity::class, XObjectEntity::class,
    StatementEntity::class, ContextXObjectStatementJoin::class, AgentEntity::class,
    StateEntity::class, StateContentEntity::class,Clazz::class, ClazzMember::class,
    ClazzLog::class,ClazzLogAttendanceRecord::class, FeedEntry::class,PersonField::class,
    PersonCustomFieldValue::class,PersonDetailPresenterField::class,SelQuestion::class,
    SelQuestionResponse::class, SelQuestionResponseNomination::class, SelQuestionSet::class,
    SelQuestionSetRecognition::class, SelQuestionSetResponse::class,
    Schedule::class, DateRange::class, UMCalendar::class,
    ClazzActivity::class, ClazzActivityChange::class,
    ContentEntry::class, ContentEntryContentCategoryJoin::class,
    ContentEntryParentChildJoin::class, ContentEntryRelatedEntryJoin::class,
    Location::class,
    AccessToken::class, PersonAuth::class, Role::class, EntityRole::class,
    PersonGroup::class, PersonGroupMember::class, LocationAncestorJoin::class,
    SelQuestionOption::class, ScheduledCheck::class,
    PersonLocationJoin::class, PersonPicture::class, ScrapeQueueItem::class, ScrapeRun::class,
    ContentEntryStatus::class, ConnectivityStatus::class,
    AuditLog::class, CustomField::class, CustomFieldValue::class, CustomFieldValueOption::class,
    XLangMapEntry::class,SyncNode::class

], version = 24)

abstract class UmAppDatabase : DoorDatabase(), SyncableDoorDatabase {

    var attachmentsDir: String? = null

    override val master: Boolean
        get() = false

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


    abstract val auditLogDao : AuditLogDao
    abstract val clazzActivityChangeDao : ClazzActivityChangeDao
    abstract val clazzActivityDao : ClazzActivityDao
    abstract val clazzLogAttendanceRecordDao: ClazzLogAttendanceRecordDao
    abstract val clazzLogDao : ClazzLogDao
    abstract val customFieldDao: CustomFieldDao
    abstract val customFieldValueDao : CustomFieldValueDao
    abstract val customFieldValueOptionDao : CustomFieldValueOptionDao
    abstract val dateRangeDao : DateRangeDao
    abstract val feedEntryDao : FeedEntryDao
    abstract val personDetailPresenterFieldDao : PersonDetailPresenterFieldDao
    abstract val scheduleDao : ScheduleDao
    abstract val scheduledCheckDao : ScheduledCheckDao
    abstract val selQuestionDao : SelQuestionDao
    abstract val selQuestionOptionDao : SelQuestionOptionDao
    abstract val selQuestionResponseDao : SelQuestionResponseDao
    abstract val selQuestionResponseNominationDao : SelQuestionResponseNominationDao
    abstract val selQuestionSetDao : SelQuestionSetDao
    abstract val selQuestionSetResponseDao : SelQuestionSetResponseDao
    abstract val umCalendarDao : UMCalendarDao


    abstract val xLangMapEntryDao: XLangMapEntryDao

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
