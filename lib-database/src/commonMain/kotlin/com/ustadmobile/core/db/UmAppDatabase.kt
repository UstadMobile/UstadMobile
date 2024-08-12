package com.ustadmobile.core.db

import com.ustadmobile.door.annotation.DoorDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.db.dao.xapi.ActivityEntityDao
import com.ustadmobile.core.db.dao.xapi.ActivityExtensionDao
import com.ustadmobile.core.db.dao.xapi.ActivityInteractionDao
import com.ustadmobile.core.db.dao.xapi.ActorDao
import com.ustadmobile.core.db.dao.xapi.GroupMemberActorJoinDao
import com.ustadmobile.core.db.dao.xapi.StatementDao
import com.ustadmobile.core.db.dao.xapi.VerbDao
import com.ustadmobile.core.db.dao.xapi.VerbLangMapEntryDao
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.entities.*
import com.ustadmobile.door.room.RoomDatabase
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.xapi.ActorEntity
import com.ustadmobile.lib.db.entities.xapi.GroupMemberActorJoin
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import com.ustadmobile.lib.db.entities.xapi.VerbEntity
import com.ustadmobile.lib.db.entities.xapi.VerbLangMapEntry
import com.ustadmobile.lib.db.entities.xapi.ActivityEntity
import com.ustadmobile.lib.db.entities.xapi.ActivityInteractionEntity
import com.ustadmobile.lib.db.entities.xapi.ActivityLangMapEntry
import com.ustadmobile.core.db.dao.xapi.ActivityLangMapEntryDao
import com.ustadmobile.core.db.dao.xapi.StateDeleteCommandDao
import com.ustadmobile.lib.db.entities.xapi.StateDeleteCommand
import com.ustadmobile.core.db.dao.xapi.StateEntityDao
import com.ustadmobile.lib.db.entities.xapi.StatementContextActivityJoin
import com.ustadmobile.core.db.dao.xapi.StatementContextActivityJoinDao
import com.ustadmobile.core.db.dao.xapi.StatementEntityJsonDao
import com.ustadmobile.core.db.dao.xapi.XapiSessionEntityDao
import com.ustadmobile.lib.db.entities.xapi.ActivityExtensionEntity
import com.ustadmobile.lib.db.entities.xapi.StateEntity
import com.ustadmobile.lib.db.entities.xapi.StatementEntityJson
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity

@DoorDatabase(entities = [
    ClazzLog::class, ClazzLogAttendanceRecord::class,
    Schedule::class, HolidayCalendar::class, Holiday::class,
    Person::class,
    Clazz::class, ClazzEnrolment::class, LeavingReason::class,
    ContentEntry::class, ContentEntryContentCategoryJoin::class, ContentEntryParentChildJoin::class,
    ContentEntryRelatedEntryJoin::class, ContentCategorySchema::class, ContentCategory::class,
    Language::class, LanguageVariant::class,
    PersonAuth::class,
    PersonGroup::class, PersonGroupMember::class,
    PersonPicture::class,
    VerbEntity::class, ActivityEntity::class, StatementEntity::class,
    ActorEntity::class,
    SyncNode::class,
    Comments::class,
    Report::class,
    Site::class,
    SiteTerms::class,
    PersonParentJoin::class,
    ScopedGrant::class,
    ErrorReport::class,
    ClazzAssignment::class,  CourseAssignmentSubmission::class,
    CourseAssignmentSubmissionFile::class, CourseAssignmentMark::class,
    PeerReviewerAllocation::class,
    PersonAuth2::class,
    UserSession::class,
    ContentJob::class, ContentEntryImportJob::class, CourseBlock::class, CourseTerminology::class,
    CourseGroupSet::class, CourseGroupMember::class,
    ContentEntryPicture::class,
    ActivityInteractionEntity::class,
    CoursePicture::class,
    DiscussionPost::class,
    ExternalAppPermission::class,
    Message::class,
    StudentResult::class,
    ContentEntryVersion::class,
    TransferJob::class,
    TransferJobItem::class,
    CacheLockJoin::class,
    OfflineItem::class,
    OfflineItemPendingTransferJob::class,
    DeletedItem::class,
    EnrolmentRequest::class,
    CoursePermission::class,
    SystemPermission::class,
    CourseBlockPicture::class,
    ContentEntryPicture2::class,
    TransferJobError::class,
    VerbLangMapEntry::class,
    GroupMemberActorJoin::class,
    ActivityLangMapEntry::class,
    ActivityExtensionEntity::class,
    StatementContextActivityJoin::class,
    XapiSessionEntity::class,
    StatementEntityJson::class,
    StateEntity::class,
    StateDeleteCommand::class,

    //Door entities
    OutgoingReplication::class,
    ReplicationOperation::class,
    PendingRepositorySession::class,
    DoorNode::class,

], version = 200)
expect abstract class UmAppDatabase : RoomDatabase {

    abstract fun personDao(): PersonDao

    abstract fun clazzDao(): ClazzDao

    abstract fun courseBlockDao(): CourseBlockDao

    abstract fun courseTerminologyDao(): CourseTerminologyDao

    abstract fun courseGroupSetDao(): CourseGroupSetDao

    abstract fun courseGroupMemberDao(): CourseGroupMemberDao

    abstract fun clazzEnrolmentDao(): ClazzEnrolmentDao

    abstract fun leavingReasonDao(): LeavingReasonDao

    abstract fun contentEntryDao(): ContentEntryDao

    abstract fun contentEntryContentCategoryJoinDao(): ContentEntryContentCategoryJoinDao

    abstract fun contentEntryParentChildJoinDao(): ContentEntryParentChildJoinDao

    abstract fun contentEntryRelatedEntryJoinDao(): ContentEntryRelatedEntryJoinDao

    abstract fun contentCategorySchemaDao(): ContentCategorySchemaDao

    abstract fun contentCategoryDao(): ContentCategoryDao

    abstract fun languageDao(): LanguageDao

    abstract fun languageVariantDao(): LanguageVariantDao

    abstract fun personAuthDao(): PersonAuthDao

    abstract fun personGroupDao(): PersonGroupDao

    abstract fun personGroupMemberDao(): PersonGroupMemberDao

    abstract fun personPictureDao(): PersonPictureDao

    abstract fun verbDao(): VerbDao

    abstract fun activityEntityDao(): ActivityEntityDao

    abstract fun reportDao(): ReportDao

    abstract fun statementDao(): StatementDao

    abstract fun actorDao(): ActorDao

    abstract fun clazzLogAttendanceRecordDao(): ClazzLogAttendanceRecordDao
    abstract fun clazzLogDao(): ClazzLogDao

    abstract fun scheduleDao(): ScheduleDao

    abstract fun holidayCalendarDao(): HolidayCalendarDao
    abstract fun holidayDao(): HolidayDao

    abstract fun clazzAssignmentDao(): ClazzAssignmentDao

    abstract fun courseAssignmentSubmissionDao(): CourseAssignmentSubmissionDao

    abstract fun courseAssignmentSubmissionFileDao(): CourseAssignmentSubmissionFileDao

    abstract fun courseAssignmentMarkDao(): CourseAssignmentMarkDao

    abstract fun commentsDao(): CommentsDao

    abstract fun syncNodeDao(): SyncNodeDao

    abstract fun siteDao(): SiteDao

    abstract fun siteTermsDao(): SiteTermsDao

    abstract fun personParentJoinDao(): PersonParentJoinDao

    abstract fun scopedGrantDao(): ScopedGrantDao

    abstract fun errorReportDao(): ErrorReportDao

    abstract fun personAuth2Dao(): PersonAuth2Dao

    abstract fun userSessionDao(): UserSessionDao

    abstract fun contentEntryImportJobDao(): ContentEntryImportJobDao

    abstract fun coursePictureDao(): CoursePictureDao

    abstract fun contentEntryPictureDao(): ContentEntryPictureDao

    abstract fun messageDao(): MessageDao

    abstract fun peerReviewerAllocationDao(): PeerReviewerAllocationDao

    abstract fun discussionPostDao(): DiscussionPostDao

    abstract fun externalAppPermissionDao(): ExternalAppPermissionDao

    abstract fun contentEntryVersionDao(): ContentEntryVersionDao

    abstract fun outgoingReplicationDao(): OutgoingReplicationDao

    abstract fun transferJobDao(): TransferJobDao

    abstract fun transferJobItemDao(): TransferJobItemDao

    abstract fun cacheLockJoinDao(): CacheLockJoinDao

    abstract fun offlineItemDao(): OfflineItemDao

    abstract fun deletedItemDao(): DeletedItemDao

    abstract fun enrolmentRequestDao(): EnrolmentRequestDao

    abstract fun coursePermissionDao(): CoursePermissionDao

    abstract fun systemPermissionDao(): SystemPermissionDao

    abstract fun courseBlockPictureDao(): CourseBlockPictureDao

    abstract fun contentEntryPicture2Dao(): ContentEntryPicture2Dao

    abstract fun transferJobErrorDao(): TransferJobErrorDao

    abstract fun studentResultDao(): StudentResultDao

    abstract fun verbLangMapEntryDao(): VerbLangMapEntryDao

    abstract fun groupMemberActorJoinDao(): GroupMemberActorJoinDao

    abstract fun activityLangMapEntryDao(): ActivityLangMapEntryDao

    abstract fun activityInteractionDao(): ActivityInteractionDao

    abstract fun activityExtensionDao(): ActivityExtensionDao

    abstract fun statementContextActivityJoinDao(): StatementContextActivityJoinDao

    abstract fun xapiSessionEntityDao(): XapiSessionEntityDao

    abstract fun statementEntityJsonDao(): StatementEntityJsonDao

    abstract fun stateEntityDao(): StateEntityDao

    abstract fun stateDeleteCommandDao(): StateDeleteCommandDao

}
