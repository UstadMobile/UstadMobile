package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.UserSession

object UserSessionDaoCommon {

    const val FIND_LOCAL_SESSIONS_SQL = """
            SELECT UserSession.*, Person.*, PersonPicture.*
              FROM UserSession
                   JOIN Person 
                        ON Person.personUid = UserSession.usPersonUid
                   LEFT JOIN PersonPicture
                        ON PersonPicture.personPictureUid = UserSession.usPersonUid
             WHERE UserSession.usClientNodeId = (
                   SELECT COALESCE(
                          (SELECT nodeClientId 
                            FROM SyncNode
                           LIMIT 1), 0))
               AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}        
               AND (UserSession.usSessionType & ${UserSession.TYPE_TEMP_LOCAL}) != ${UserSession.TYPE_TEMP_LOCAL}
            """
}