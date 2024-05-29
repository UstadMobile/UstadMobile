package com.ustadmobile.core.db.dao

object ContentEntryDaoCommon {

    const val SORT_TITLE_ASC = 1

    const val SORT_TITLE_DESC = 2

    const val FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_CONTENT_ENTRY_ROOT = """
          FROM StatementEntity
         WHERE StatementEntity.statementActorPersonUid = :accountPersonUid
           AND CAST(StatementEntity.contentEntryRoot AS INTEGER) = 1
           AND StatementEntity.statementContentEntryUid = :entryUuid
           AND (:courseBlockUid = 0 OR StatementEntity.statementCbUid = :courseBlockUid)
    """
}

