package com.ustadmobile

import com.ustadmobile.core.db.SiteTermsDao
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorDatabaseSyncRepository

class ReactDatabase: UmAppDatabase() {
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
        get() = TODO("Not yet implemented")
    override val clazzDao: ClazzDao
        get() = TODO("Not yet implemented")
    override val clazzEnrolmentDao: ClazzEnrolmentDao
        get() = TODO("Not yet implemented")
    override val leavingReasonDao: LeavingReasonDao
        get() = TODO("Not yet implemented")
    override val contentEntryDao: ContentEntryDao
        get() = TODO("Not yet implemented")
    override val contentEntryContentCategoryJoinDao: ContentEntryContentCategoryJoinDao
        get() = TODO("Not yet implemented")
    override val contentEntryParentChildJoinDao: ContentEntryParentChildJoinDao
        get() = TODO("Not yet implemented")
    override val contentEntryRelatedEntryJoinDao: ContentEntryRelatedEntryJoinDao
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
        get() = TODO("Not yet implemented")
    override val entityRoleDao: EntityRoleDao
        get() = TODO("Not yet implemented")
    override val personPictureDao: PersonPictureDao
        get() = TODO("Not yet implemented")
    override val scrapeRunDao: ScrapeRunDao
        get() = TODO("Not yet implemented")
    override val contentEntryStatusDao: ContentEntryStatusDao
        get() = TODO("Not yet implemented")
    override val connectivityStatusDao: ConnectivityStatusDao
        get() = TODO("Not yet implemented")
    override val containerDao: ContainerDao
        get() = TODO("Not yet implemented")
    override val containerEntryDao: ContainerEntryDao
        get() = TODO("Not yet implemented")
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
    override val contentEntryProgressDao: ContentEntryProgressDao
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
        get() = TODO("Not yet implemented")
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
    override val clazzWorkDao: ClazzWorkDao
        get() = TODO("Not yet implemented")
    override val clazzWorkSubmissionDao: ClazzWorkSubmissionDao
        get() = TODO("Not yet implemented")
    override val clazzWorkContentJoinDao: ClazzWorkContentJoinDao
        get() = TODO("Not yet implemented")
    override val clazzWorkQuestionDao: ClazzWorkQuestionDao
        get() = TODO("Not yet implemented")
    override val clazzWorkQuestionOptionDao: ClazzWorkQuestionOptionDao
        get() = TODO("Not yet implemented")
    override val commentsDao: CommentsDao
        get() = TODO("Not yet implemented")
    override val clazzWorkQuestionResponseDao: ClazzWorkQuestionResponseDao
        get() = TODO("Not yet implemented")
    override val syncNodeDao: SyncNodeDao
        get() = TODO("Not yet implemented")
    override val deviceSessionDao: DeviceSessionDao
        get() = TODO("Not yet implemented")
    override val siteDao: SiteDao
        get() = TODO("Not yet implemented")
    override val siteTermsDao: SiteTermsDao
        get() = TODO("Not yet implemented")
    override val dbVersion: Int
        get() = TODO("Not yet implemented")

    override fun clearAllTables() {

    }

    companion object {
        fun getInstance(context: Any, dbName: String): ReactDatabase {
            return ReactDatabase()
        }
    }
}