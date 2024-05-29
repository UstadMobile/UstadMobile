package com.ustadmobile.core.db.dao.xapi

import com.ustadmobile.lib.db.entities.ClazzEnrolment

object StatementDaoCommon {

    const val FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_CONTENT_ENTRY_ROOT = """
          FROM StatementEntity
         WHERE StatementEntity.statementActorPersonUid = :accountPersonUid
           AND StatementEntity.statementContentEntryUid = :contentEntryUid
           AND CAST(StatementEntity.contentEntryRoot AS INTEGER) = 1
           AND (:courseBlockUid = 0 OR StatementEntity.statementCbUid = :courseBlockUid)
    """

    const val FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CONTENT_ENTRY = """
            $FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_CONTENT_ENTRY_ROOT
        AND (    (CAST(StatementEntity.resultCompletion AS INTEGER) = 1)
              OR (StatementEntity.extensionProgress IS NOT NULL))
        
        
    """

    const val FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CLAZZ_STUDENT = """
               FROM StatementEntity
              WHERE (SELECT EXISTS(
                            SELECT 1
                              FROM ClazzEnrolment
                             WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
                               AND ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                               AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}))
                AND StatementEntity.statementActorPersonUid = :accountPersonUid
                AND StatementEntity.statementClazzUid = :clazzUid
                AND (    (CAST(StatementEntity.resultCompletion AS INTEGER) = 1)
                      OR (StatementEntity.extensionProgress IS NOT NULL))
    """

    const val SORT_FIRST_NAME_ASC = 1

    const val SORT_FIRST_NAME_DESC = 2

    const val SORT_LAST_NAME_ASC = 3

    const val SORT_LAST_NAME_DESC = 4

    const val SORT_LAST_ACTIVE_ASC = 5

    const val SORT_LAST_ACTIVE_DESC = 6

}