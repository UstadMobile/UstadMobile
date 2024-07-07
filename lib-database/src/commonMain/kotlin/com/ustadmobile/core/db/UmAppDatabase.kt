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
import com.ustadmobile.core.db.dao.xapi.StatementContextActivityJoin
import com.ustadmobile.core.db.dao.xapi.StatementContextActivityJoinDao
import com.ustadmobile.core.db.dao.xapi.StatementEntityJsonDao
import com.ustadmobile.core.db.dao.xapi.XapiSessionEntityDao
import com.ustadmobile.lib.db.entities.xapi.ActivityExtensionEntity
import com.ustadmobile.lib.db.entities.xapi.StatementEntityJson
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity

@DoorDatabase(entities = [NetworkNode::class,
    ClazzLog::class, ClazzLogAttendanceRecord::class,
    Schedule::class, HolidayCalendar::class, Holiday::class,
    Person::class,
    Clazz::class, ClazzEnrolment::class, LeavingReason::class,
    ContentEntry::class, ContentEntryContentCategoryJoin::class, ContentEntryParentChildJoin::class,
    ContentEntryRelatedEntryJoin::class, ContentCategorySchema::class, ContentCategory::class,
    Language::class, LanguageVariant::class, AccessToken::class, PersonAuth::class, Role::class,
    PersonGroup::class, PersonGroupMember::class,
    PersonPicture::class,
    ScrapeQueueItem::class, ScrapeRun::class, ConnectivityStatus::class,
    Container::class, ContainerEntry::class, ContainerEntryFile::class,
    VerbEntity::class, ActivityEntity::class, StatementEntity::class,
    ActorEntity::class,
    StateEntity::class, StateContentEntity::class, XLangMapEntry::class,
    SyncNode::class, LocallyAvailableContainer::class, ContainerETag::class,
    School::class,
    SchoolMember::class, Comments::class,
    Report::class,
    Site::class,
    ContainerImportJob::class,
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
    Chat::class,
    ContentEntryPicture::class,
    ActivityInteractionEntity::class,

    //Door Helper entities
//    SqliteChangeSeqNums::class,
//    UpdateNotification::class,
    DoorNode::class,
    CoursePicture::class,
    DiscussionPost::class,
    ExternalAppPermission::class,
    ChatMember::class,
    Message::class,
    MessageRead::class,
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

    //Door entities
    OutgoingReplication::class,
    ReplicationOperation::class,
    PendingRepositorySession::class,

], version = 194)
expect abstract class UmAppDatabase : RoomDatabase {

    /*
        Changes from 38-39:
        1. Added personGroupUid to Person
        2. Added personGroupFlag to PersonGroup
        3. Removed groupPersonUid from PersonGroup

        Changes from 36:
        1. Added school uid to Clazz
        2. Added school Phone number to School
        3. Added schoolGender to School
        4. Added schoolHolidayCalendar to School
        5. Added SchoolMember and SchoolMemberDao
        6. Added ClazzWork, ClazzWorkContentJoin, Comments,ClazzWorkQuestion,ClazzWorkQuestionOption
        7. Added ContainerUploadJob
        Changes in 34:
        Added School and Assignment based entities
        Updated Clazz : added clazzFeatures and removed individual feature bits
     */

    abstract val networkNodeDao: NetworkNodeDao

    abstract val personDao: PersonDao

    abstract val clazzDao: ClazzDao

    abstract val courseBlockDao: CourseBlockDao

    abstract val courseTerminologyDao: CourseTerminologyDao

    abstract val courseGroupSetDao: CourseGroupSetDao

    abstract val courseGroupMemberDao: CourseGroupMemberDao

    abstract val clazzEnrolmentDao: ClazzEnrolmentDao

    abstract val leavingReasonDao: LeavingReasonDao

    abstract val contentEntryDao: ContentEntryDao

    abstract val contentEntryContentCategoryJoinDao: ContentEntryContentCategoryJoinDao

    abstract val contentEntryParentChildJoinDao: ContentEntryParentChildJoinDao

    abstract val contentEntryRelatedEntryJoinDao: ContentEntryRelatedEntryJoinDao

    // abstract val syncStatusDao: SyncStatusDao

    abstract val contentCategorySchemaDao: ContentCategorySchemaDao

    abstract val contentCategoryDao: ContentCategoryDao

    abstract val languageDao: LanguageDao

    abstract val languageVariantDao: LanguageVariantDao

    abstract val scrapeQueueItemDao: ScrapeQueueItemDao

    abstract val personAuthDao: PersonAuthDao

    abstract val personGroupDao: PersonGroupDao

    abstract val personGroupMemberDao: PersonGroupMemberDao

    abstract val personPictureDao: PersonPictureDao

    abstract val connectivityStatusDao: ConnectivityStatusDao

    abstract val containerDao: ContainerDao

    abstract val containerEntryDao: ContainerEntryDao

    abstract val containerEntryFileDao: ContainerEntryFileDao

    abstract val containerETagDao: ContainerETagDao

    abstract val verbDao: VerbDao

    abstract val activityEntityDao: ActivityEntityDao

    abstract val reportDao: ReportDao

    abstract val containerImportJobDao: ContainerImportJobDao

    abstract val statementDao: StatementDao

    abstract val stateDao: StateDao

    abstract val stateContentDao: StateContentDao

    abstract val actorDao: ActorDao

    abstract val clazzLogAttendanceRecordDao: ClazzLogAttendanceRecordDao
    abstract val clazzLogDao: ClazzLogDao

    abstract val scheduleDao: ScheduleDao

    abstract val holidayCalendarDao: HolidayCalendarDao
    abstract val holidayDao: HolidayDao
    abstract val schoolDao: SchoolDao

    abstract val xLangMapEntryDao: XLangMapEntryDao

    abstract val locallyAvailableContainerDao: LocallyAvailableContainerDao

    abstract val schoolMemberDao: SchoolMemberDao

    abstract val clazzAssignmentDao: ClazzAssignmentDao

    abstract val courseAssignmentSubmissionDao: CourseAssignmentSubmissionDao

    abstract val courseAssignmentSubmissionFileDao: CourseAssignmentSubmissionFileDao

    abstract val courseAssignmentMarkDao: CourseAssignmentMarkDao

    abstract val commentsDao: CommentsDao

    abstract val syncNodeDao: SyncNodeDao

    abstract val siteDao: SiteDao

    abstract val siteTermsDao: SiteTermsDao

    abstract val personParentJoinDao: PersonParentJoinDao

    abstract val scopedGrantDao: ScopedGrantDao

    abstract val errorReportDao: ErrorReportDao

    abstract val personAuth2Dao: PersonAuth2Dao

    abstract val userSessionDao: UserSessionDao

    abstract val contentEntryImportJobDao: ContentEntryImportJobDao

    abstract val contentJobDao: ContentJobDao

    abstract val coursePictureDao: CoursePictureDao

    abstract val contentEntryPictureDao: ContentEntryPictureDao

    abstract val chatDao: ChatDao

    abstract val chatMemberDao: ChatMemberDao

    abstract val messageDao: MessageDao

    abstract val messageReadDao: MessageReadDao

    abstract val peerReviewerAllocationDao: PeerReviewerAllocationDao

    abstract val discussionPostDao: DiscussionPostDao

    abstract val externalAppPermissionDao: ExternalAppPermissionDao

    abstract val contentEntryVersionDao: ContentEntryVersionDao

    abstract val outgoingReplicationDao: OutgoingReplicationDao

    abstract val transferJobDao: TransferJobDao

    abstract val transferJobItemDao: TransferJobItemDao

    abstract val cacheLockJoinDao: CacheLockJoinDao

    abstract val offlineItemDao: OfflineItemDao

    abstract val deletedItemDao: DeletedItemDao

    abstract val enrolmentRequestDao: EnrolmentRequestDao

    abstract val coursePermissionDao: CoursePermissionDao

    abstract val systemPermissionDao: SystemPermissionDao

    abstract val courseBlockPictureDao: CourseBlockPictureDao

    abstract val contentEntryPicture2Dao: ContentEntryPicture2Dao

    abstract val transferJobErrorDao: TransferJobErrorDao

    abstract val studentResultDao: StudentResultDao

    abstract val verbLangMapEntryDao: VerbLangMapEntryDao

    abstract val groupMemberActorJoinDao: GroupMemberActorJoinDao

    abstract val activityLangMapEntryDao: ActivityLangMapEntryDao

    abstract val activityInteractionDao: ActivityInteractionDao

    abstract val activityExtensionDao: ActivityExtensionDao

    abstract val statementContextActivityJoinDao: StatementContextActivityJoinDao

    abstract val xapiSessionEntityDao: XapiSessionEntityDao

    abstract val statementEntityJsonDao: StatementEntityJsonDao

}
