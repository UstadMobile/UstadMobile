package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.UserSession

object UserSessionDaoCommon {

    const val FIND_LOCAL_SESSIONS_SQL = """
            SELECT UserSession.*, Person.*
              FROM UserSession
                   JOIN Person ON UserSession.usPersonUid = Person.personUid
             WHERE UserSession.usClientNodeId = (
                   SELECT COALESCE(
                          (SELECT nodeClientId 
                            FROM SyncNode
                           LIMIT 1), 0))
               AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}        
            """
}