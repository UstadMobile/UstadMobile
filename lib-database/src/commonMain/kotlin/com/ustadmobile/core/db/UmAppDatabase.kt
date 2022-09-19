package com.ustadmobile.core.db

import com.ustadmobile.door.annotation.DoorDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.annotation.MinReplicationVersion
import com.ustadmobile.door.entities.*
import com.ustadmobile.door.room.RoomDatabase
import com.ustadmobile.lib.db.entities.*

@DoorDatabase(entities = [NetworkNode::class,
    ClazzLog::class, ClazzLogAttendanceRecord::class,
    Schedule::class, DateRange::class, HolidayCalendar::class, Holiday::class,
    ScheduledCheck::class,
    AuditLog::class, CustomField::class, CustomFieldValue::class, CustomFieldValueOption::class,
    Person::class,
    Clazz::class, ClazzEnrolment::class, LeavingReason::class,
    ContentEntry::class, ContentEntryContentCategoryJoin::class, ContentEntryParentChildJoin::class,
    ContentEntryRelatedEntryJoin::class, ContentCategorySchema::class, ContentCategory::class,
    Language::class, LanguageVariant::class, AccessToken::class, PersonAuth::class, Role::class,
    EntityRole::class, PersonGroup::class, PersonGroupMember::class,
    PersonPicture::class,
    ScrapeQueueItem::class, ScrapeRun::class, ConnectivityStatus::class,
    Container::class, ContainerEntry::class, ContainerEntryFile::class,
    VerbEntity::class, XObjectEntity::class, StatementEntity::class,
    ContextXObjectStatementJoin::class, AgentEntity::class,
    StateEntity::class, StateContentEntity::class, XLangMapEntry::class,
    SyncNode::class, LocallyAvailableContainer::class, ContainerETag::class,
    School::class,
    SchoolMember::class, Comments::class,
    Report::class,
    Site::class, ContainerImportJob::class,
    LearnerGroup::class, LearnerGroupMember::class,
    GroupLearningSession::class,
    SiteTerms::class, ClazzContentJoin::class,
    PersonParentJoin::class,
    ScopedGrant::class,
    ErrorReport::class,
    ClazzAssignment::class, ClazzAssignmentContentJoin::class, CourseAssignmentSubmission::class,
    CourseAssignmentSubmissionAttachment::class, CourseAssignmentMark::class,
    ClazzAssignmentRollUp::class,
    PersonAuth2::class,
    UserSession::class,
    ContentJob::class, ContentJobItem::class, CourseBlock::class, CourseTerminology::class,
    CourseGroupSet::class, CourseGroupMember::class,

    //Door Helper entities
//    SqliteChangeSeqNums::class,
//    UpdateNotification::class,
    ChangeLog::class,
    ZombieAttachmentData::class,
    DoorNode::class,
    ReplicationStatus::class,

    ClazzLogReplicate::class,
    ClazzLogAttendanceRecordReplicate::class,
    CourseAssignmentSubmissionReplicate::class,
    CourseAssignmentSubmissionAttachmentReplicate::class,
    CourseAssignmentMarkReplicate::class,
    CourseBlockReplicate::class,
    CourseTerminologyReplicate::class,
    CourseGroupSetReplicate::class,
    CourseGroupMemberReplicate::class,
    ScheduleReplicate::class,
    HolidayCalendarReplicate::class,
    HolidayReplicate::class,
    PersonReplicate::class,
    ClazzReplicate::class,
    ClazzEnrolmentReplicate::class,
    LeavingReasonReplicate::class,
    ContentEntryReplicate::class,
    ContentEntryContentCategoryJoinReplicate::class,
    ContentEntryParentChildJoinReplicate::class,
    ContentEntryRelatedEntryJoinReplicate::class,
    ContentCategorySchemaReplicate::class,
    ContentCategoryReplicate::class,
    LanguageReplicate::class,
    LanguageVariantReplicate::class,
    PersonGroupReplicate::class,
    PersonGroupMemberReplicate::class,
    PersonPictureReplicate::class,
    ContainerReplicate::class,
    VerbEntityReplicate::class,
    XObjectEntityReplicate::class,
    StatementEntityReplicate::class,
    ContextXObjectStatementJoinReplicate::class,
    AgentEntityReplicate::class,
    StateEntityReplicate::class,
    StateContentEntityReplicate::class,
    XLangMapEntryReplicate::class,
    SchoolReplicate::class,
    SchoolMemberReplicate::class,
    CommentsReplicate::class,
    ReportReplicate::class,
    SiteReplicate::class,
    LearnerGroupReplicate::class,
    LearnerGroupMemberReplicate::class,
    GroupLearningSessionReplicate::class,
    SiteTermsReplicate::class,
    ClazzContentJoinReplicate::class,
    PersonParentJoinReplicate::class,
    ScopedGrantReplicate::class,
    ErrorReportReplicate::class,
    ClazzAssignmentReplicate::class,
    ClazzAssignmentContentJoinReplicate::class,
    PersonAuth2Replicate::class,
    UserSessionReplicate::class,
    CoursePicture::class,
    CoursePictureReplicate::class,
    ContentEntryPicture::class,
    ContentEntryPictureReplicate::class,
    Chat::class,
    ChatMember::class,
    Message::class,
    MessageReplicate::class,
    ChatReplicate::class,
    ChatMemberReplicate::class,
    MessageRead::class,
    MessageReadReplicate::class,
    CourseDiscussion::class,
    CourseDiscussionReplicate::class,
    DiscussionTopic::class,
    DiscussionTopicReplicate::class,
    DiscussionPost::class,
    DiscussionPostReplicate::class


], version = 107)
@MinReplicationVersion(60)
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

    abstract val clazzContentJoinDao: ClazzContentJoinDao

    // abstract val syncStatusDao: SyncStatusDao

    abstract val contentCategorySchemaDao: ContentCategorySchemaDao

    abstract val contentCategoryDao: ContentCategoryDao

    abstract val languageDao: LanguageDao

    abstract val languageVariantDao: LanguageVariantDao

    abstract val scrapeQueueItemDao: ScrapeQueueItemDao

    abstract val personAuthDao: PersonAuthDao

    abstract val personGroupDao: PersonGroupDao

    abstract val personGroupMemberDao: PersonGroupMemberDao

    abstract val entityRoleDao: EntityRoleDao

    abstract val personPictureDao: PersonPictureDao

    abstract val connectivityStatusDao: ConnectivityStatusDao

    abstract val containerDao: ContainerDao

    abstract val containerEntryDao: ContainerEntryDao

    abstract val containerEntryFileDao: ContainerEntryFileDao

    abstract val containerETagDao: ContainerETagDao

    abstract val verbDao: VerbDao

    abstract val xObjectDao: XObjectDao

    abstract val reportDao: ReportDao

    abstract val containerImportJobDao: ContainerImportJobDao

    abstract val statementDao: StatementDao

    abstract val contextXObjectStatementJoinDao: ContextXObjectStatementJoinDao

    abstract val stateDao: StateDao

    abstract val stateContentDao: StateContentDao

    abstract val agentDao: AgentDao

    abstract val learnerGroupDao: LearnerGroupDao

    abstract val learnerGroupMemberDao: LearnerGroupMemberDao

    abstract val groupLearningSessionDao: GroupLearningSessionDao

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

    abstract val clazzAssignmentContentJoinDao: ClazzAssignmentContentJoinDao

    abstract val clazzAssignmentRollUpDao: ClazzAssignmentRollUpDao

    abstract val courseAssignmentSubmissionDao: CourseAssignmentSubmissionDao

    abstract val courseAssignmentSubmissionAttachmentDao: CourseAssignmentSubmissionAttachmentDao

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

    abstract val contentJobItemDao: ContentJobItemDao

    abstract val contentJobDao: ContentJobDao

    abstract val coursePictureDao: CoursePictureDao

    abstract val contentEntryPictureDao: ContentEntryPictureDao

    abstract val chatDao: ChatDao

    abstract val chatMemberDao: ChatMemberDao

    abstract val messageDao: MessageDao

    abstract val messageReadDao: MessageReadDao

    abstract val courseDiscussionDao: CourseDiscussionDao

    abstract val discussionTopicDao: DiscussionTopicDao

    abstract val discussionPostDao: DiscussionPostDao


//    companion object {
//
//        const val TAG_DB = DoorTag.TAG_DB
//
//        const val TAG_REPO = DoorTag.TAG_REPO
//
//
//
//
//
//    }


}
