package com.ustadmobile.port.android.data.local

interface ServiceConstant {

    enum class Table(val tableName: String) {
        NETWORK_NODE("NetworkNode"),
        CLAZZ_LOG("ClazzLog"),
        CLAZZ_LOG_ATTENDANCE_RECORD("ClazzLogAttendanceRecord"),
        SCHEDULE("Schedule"),
        DATE_RANGE("DateRange"),
        HOLIDAY_CALENDAR("HolidayCalendar"),
        HOLIDAY("Holiday"),
        SCHEDULED_CHECK("ScheduledCheck"),
        AUDIT_LOG("AuditLog"),
        CUSTOM_FIELD("CustomField"),
        CUSTOM_FIELD_VALUE("CustomFieldValue"),
        CUSTOM_FIELD_VALUE_OPTION("CustomFieldValueOption"),
        PERSON("Person"),
        CLAZZ("Clazz"),
        CLAZZ_ENROLMENT("ClazzEnrolment"),
        LEAVING_REASON("LeavingReason"),
        CONTENT_ENTRY("ContentEntry"),
        CONTENT_ENTRY_CONTENT_CATEGORY_JOIN("ContentEntryContentCategoryJoin"),
        CONTENT_ENTRY_PARENT_CHILD_JOIN("ContentEntryParentChildJoin"),
        CONTENT_ENTRY_RELATED_ENTRY_JOIN("ContentEntryRelatedEntryJoin"),
        CONTENT_CATEGORY_SCHEMA("ContentCategorySchema"),
        CONTENT_CATEGORY("ContentCategory"),
        LANGUAGE("Language"),
        LANGUAGE_VARIANT("LanguageVariant"),
        ACCESS_TOKEN("AccessToken"),
        PERSON_AUTH("PersonAuth"),
        ROLE("Role"),
        ENTITY_ROLE("EntityRole"),
        PERSON_GROUP("PersonGroup"),
        PERSON_GROUP_MEMBER("PersonGroupMember"),
        PERSON_PICTURE("PersonPicture"),
        SCRAPE_QUEUE_ITEM("ScrapeQueueItem"),
        SCRAPE_RUN("ScrapeRun"),
        CONNECTIVITY_STATUS("ConnectivityStatus"),
        CONTAINER("Container"),
        CONTAINER_ENTRY("ContainerEntry"),
        CONTAINER_ENTRY_FILE("ContainerEntryFile"),
        VERB_ENTITY("VerbEntity"),
        X_OBJECT_ENTITY("XObjectEntity"),
        STATEMENT_ENTITY("StatementEntity"),
        CONTEXT_X_OBJECT_STATEMENT_JOIN("ContextXObjectStatementJoin");

    }

    companion object {
        const val databaseName = "newDB.db"
    }
}
