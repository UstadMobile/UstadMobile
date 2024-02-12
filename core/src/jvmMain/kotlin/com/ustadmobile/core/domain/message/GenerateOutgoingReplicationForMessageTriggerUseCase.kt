package com.ustadmobile.core.domain.message

import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.lib.db.entities.UserSession

class GenerateOutgoingReplicationForMessageTriggerUseCase {

    operator fun invoke(
        dbType: Int
    ) : List<String>  = buildList {
        add("""
            CREATE TRIGGER IF NOT EXISTS message_send_trigger
            AFTER INSERT ON Message
            FOR EACH ROW
            BEGIN
            INSERT INTO OutgoingReplication(destNodeId, orTableId, orPk1, orPk2)
            SELECT UserSession.usClientNodeId AS destNodeId,
                   ${Message.TABLE_ID} AS orTableId,
                   NEW.messageUid AS orPk1,
                   0 as orPk2
             FROM UserSession
            WHERE (   UserSession.usPersonUid = NEW.messageSenderPersonUid 
                   OR UserSession.usPersonUid = NEW.messageToPersonUid)
              AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}     
              AND UserSession.usClientNodeId NOT IN 
                  (SELECT ReplicationOperation.repOpRemoteNodeId
                     FROM ReplicationOperation
                    WHERE ReplicationOperation.repOpTableId = ${Message.TABLE_ID});
            END
                   
        """)
    }

}