package com.ustadmobile.mocks.db

import com.ustadmobile.core.db.SiteTermsDao
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.door.*
import com.ustadmobile.door.daos.ISyncHelperEntitiesDao
import kotlin.reflect.KClass

class DatabaseJs: UmAppDatabase() , DoorDatabaseSyncRepository{
    override val networkNodeDao: NetworkNodeDao
        get() = TODO("Not yet implemented")
    override val downloadJobDao: DownloadJobDao
        get() = TODO("Not yet implemented")
    override val downloadJobItemDao: DownloadJobItemDao
        get() = TODO("Not yet implemented")
    override val downloadJobItemParentChildJoinDao: DownloadJobItemParentChildJoinDao
        get() = TODO("Not yet implemented")
    override val downloadJobItemHistoryDao: DownloadJobItemHistoryDao
        get() = TODO("Not yet implemented")
    override val personDao: PersonDao
        get() = PersonDaoJs()
    override val clazzDao: ClazzDao
        get() = ClazzDaoJs()
    override val clazzEnrolmentDao: ClazzEnrolmentDao
        get() = ClazzEnrolmentDaoJs()
    override val leavingReasonDao: LeavingReasonDao
        get() = TODO("Not yet implemented")
    override val contentEntryDao: ContentEntryDao
        get() = ContentEntryDaoJs()
    override val contentEntryContentCategoryJoinDao: ContentEntryContentCategoryJoinDao
        get() = TODO("Not yet implemented")
    override val contentEntryParentChildJoinDao: ContentEntryParentChildJoinDao
        get() = TODO("Not yet implemented")
    override val contentEntryRelatedEntryJoinDao: ContentEntryRelatedEntryJoinDao
        get() = ContentEntryRelatedEntryJoinDaoJs()
    override val clazzContentJoinDao: ClazzContentJoinDao
        get() = TODO("Not yet implemented")
    override val contentCategorySchemaDao: ContentCategorySchemaDao
        get() = TODO("Not yet implemented")
    override val contentCategoryDao: ContentCategoryDao
        get() = TODO("Not yet implemented")
    override val languageDao: LanguageDao
        get() = TODO("Not yet implemented")
    override val languageVariantDao: LanguageVariantDao
        get() = TODO("Not yet implemented")
    override val scrapeQueueItemDao: ScrapeQueueItemDao
        get() = TODO("Not yet implemented")
    override val personAuthDao: PersonAuthDao
        get() = TODO("Not yet implemented")
    override val accessTokenDao: AccessTokenDao
        get() = TODO("Not yet implemented")
    override val roleDao: RoleDao
        get() = TODO("Not yet implemented")
    override val personGroupDao: PersonGroupDao
        get() = TODO("Not yet implemented")
    override val personGroupMemberDao: PersonGroupMemberDao
        get() = PersonGroupMemberDaoJs()
    override val entityRoleDao: EntityRoleDao
        get() = EntityRoleDaoJs()
    override val personPictureDao: PersonPictureDao
        get() = TODO("Not yet implemented")
    override val scrapeRunDao: ScrapeRunDao
        get() = TODO("Not yet implemented")
    override val contentEntryStatusDao: ContentEntryStatusDao
        get() = TODO("Not yet implemented")
    override val connectivityStatusDao: ConnectivityStatusDao
        get() = TODO("Not yet implemented")
    override val containerDao: ContainerDao
        get() = ContainerDaoJs()
    override val containerEntryDao: ContainerEntryDao
        get() = ContainerEntryDaoJs()
    override val containerEntryFileDao: ContainerEntryFileDao
        get() = TODO("Not yet implemented")
    override val containerETagDao: ContainerETagDao
        get() = TODO("Not yet implemented")
    override val verbDao: VerbDao
        get() = TODO("Not yet implemented")
    override val xObjectDao: XObjectDao
        get() = TODO("Not yet implemented")
    override val reportDao: ReportDao
        get() = TODO("Not yet implemented")
    override val containerImportJobDao: ContainerImportJobDao
        get() = TODO("Not yet implemented")
    override val statementDao: StatementDao
        get() = TODO("Not yet implemented")
    override val contextXObjectStatementJoinDao: ContextXObjectStatementJoinDao
        get() = TODO("Not yet implemented")
    override val stateDao: StateDao
        get() = TODO("Not yet implemented")
    override val stateContentDao: StateContentDao
        get() = TODO("Not yet implemented")
    override val agentDao: AgentDao
        get() = TODO("Not yet implemented")
    override val learnerGroupDao: LearnerGroupDao
        get() = TODO("Not yet implemented")
    override val learnerGroupMemberDao: LearnerGroupMemberDao
        get() = TODO("Not yet implemented")
    override val groupLearningSessionDao: GroupLearningSessionDao
        get() = TODO("Not yet implemented")

    override val syncresultDao: SyncResultDao
        get() = TODO("Not yet implemented")
    override val clazzLogAttendanceRecordDao: ClazzLogAttendanceRecordDao
        get() = TODO("Not yet implemented")
    override val clazzLogDao: ClazzLogDao
        get() = TODO("Not yet implemented")
    override val customFieldDao: CustomFieldDao
        get() = TODO("Not yet implemented")
    override val customFieldValueDao: CustomFieldValueDao
        get() = TODO("Not yet implemented")
    override val customFieldValueOptionDao: CustomFieldValueOptionDao
        get() = TODO("Not yet implemented")
    override val scheduleDao: ScheduleDao
        get() = ScheduleDaoJs()
    override val scheduledCheckDao: ScheduledCheckDao
        get() = TODO("Not yet implemented")
    override val holidayCalendarDao: HolidayCalendarDao
        get() = TODO("Not yet implemented")
    override val holidayDao: HolidayDao
        get() = TODO("Not yet implemented")
    override val schoolDao: SchoolDao
        get() = TODO("Not yet implemented")
    override val xLangMapEntryDao: XLangMapEntryDao
        get() = TODO("Not yet implemented")
    override val locallyAvailableContainerDao: LocallyAvailableContainerDao
        get() = TODO("Not yet implemented")
    override val schoolMemberDao: SchoolMemberDao
        get() = TODO("Not yet implemented")
    override val clazzAssignmentDao: ClazzAssignmentDao
        get() = TODO("Not yet implemented")
    override val clazzAssignmentContentJoinDao: ClazzAssignmentContentJoinDao
        get() = TODO("Not yet implemented")
    override val clazzAssignmentRollUpDao: ClazzAssignmentRollUpDao
        get() = TODO("Not yet implemented")

    override val commentsDao: CommentsDao
        get() = TODO("Not yet implemented")
    override val syncNodeDao: SyncNodeDao
        get() = TODO("Not yet implemented")
    override val siteDao: SiteDao
        get() = TODO("Not yet implemented")
    override val siteTermsDao: SiteTermsDao
        get() = TODO("Not yet implemented")
    override val personParentJoinDao: PersonParentJoinDao
        get() = TODO("Not yet implemented")
    override val scopedGrantDao: ScopedGrantDao
        get() = TODO("Not yet implemented")
    override val errorReportDao: ErrorReportDao
        get() = TODO("Not yet implemented")
    override val personAuth2Dao: PersonAuth2Dao
        get() = TODO("Not yet implemented")
    override val userSessionDao: UserSessionDao
        get() = TODO("Not yet implemented")
    override val dbVersion: Int
        get() = TODO("Not yet implemented")

    override fun clearAllTables() {

    }

    override val clientId: Int
        get() = TODO("Not yet implemented")
    override val config: RepositoryConfig
        get() = TODO("Not yet implemented")
    override var connectivityStatus: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    override val db: DoorDatabase
        get() = TODO("Not yet implemented")
    override val dbPath: String
        get() = TODO("Not yet implemented")
    override val syncHelperEntitiesDao: ISyncHelperEntitiesDao
        get() = TODO("Not yet implemented")
    override val tableIdMap: Map<String, Int>
        get() = TODO("Not yet implemented")

    override suspend fun activeMirrors(): List<MirrorEndpoint> {
        TODO("Not yet implemented:activeMirrors")
    }

    override suspend fun addMirror(mirrorEndpoint: String, initialPriority: Int): Int {
        TODO("Not yet implemented:addMirror")
    }

    override fun <T : Any> addSyncListener(entityClass: KClass<T>, syncListener: SyncListener<T>) {
        TODO("Not yet implemented:addSyncListener")
    }

    override fun addTableChangeListener(listener: TableChangeListener) {
        TODO("Not yet implemented:addTableChangeListener")
    }

    override fun addWeakConnectivityListener(listener: RepositoryConnectivityListener) {
        TODO("Not yet implemented:addWeakConnectivityListener")
    }

    override suspend fun dispatchUpdateNotifications(tableId: Int) {
        TODO("Not yet implemented:dispatchUpdateNotifications")
    }

    override fun <T : Any> handleSyncEntitiesReceived(
        entityClass: KClass<T>,
        entitiesIncoming: List<T>
    ) {
        TODO("Not yet implemented:handleSyncEntitiesReceived")
    }

    override fun handleTableChanged(tableName: String) {
        TODO("Not yet implemented:handleTableChanged")
    }

    override suspend fun invalidateAllTables() {}

    override fun nextId(tableId: Int): Long {
        TODO("Not yet implemented:Not yet implemented")
    }

    override suspend fun nextIdAsync(tableId: Int): Long {
        TODO("Not yet implemented:Not yet implemented")
    }

    override suspend fun removeMirror(mirrorId: Int) {
        TODO("Not yet implemented:removeMirror")
    }

    override fun <T : Any> removeSyncListener(
        entityClass: KClass<T>,
        syncListener: SyncListener<T>
    ) {
        TODO("Not yet implemented:Not yet implemented")
    }

    override fun removeTableChangeListener(listener: TableChangeListener) {
        TODO("Not yet implemented:Not yet implemented")
    }

    override fun removeWeakConnectivityListener(listener: RepositoryConnectivityListener) {
        TODO("Not yet implemented:removeWeakConnectivityListener")
    }

    override suspend fun sync(tablesToSync: List<Int>?): List<SyncResult> {
        TODO("Not yet implemented:sync")
    }

    override suspend fun updateMirrorPriorities(newPriorities: Map<Int, Int>) {
        TODO("Not yet implemented:updateMirrorPriorities")
    }


    companion object {

        const val ALLOW_ACCESS = true

        fun getInstance(context: Any, dbName: String): DatabaseJs {
            return DatabaseJs()
        }
    }
}