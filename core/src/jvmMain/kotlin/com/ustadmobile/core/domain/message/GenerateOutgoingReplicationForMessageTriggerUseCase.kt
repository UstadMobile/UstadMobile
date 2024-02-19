package com.ustadmobile.core.domain.message

import com.ustadmobile.door.DoorDbType
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.lib.db.entities.UserSession

class GenerateOutgoingReplicationForMessageTriggerUseCase {

    operator fun invoke(
        dbType: Int
    ) : List<String>  = buildList {
        val insertOutgoingReplicationSql ="""
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
        """

        when(dbType) {
            DoorDbType.SQLITE -> {
                add("""
                    CREATE TRIGGER IF NOT EXISTS message_send_trigger
                    AFTER INSERT ON Message
                    FOR EACH ROW
                    BEGIN
                    $insertOutgoingReplicationSql
                    END    
                """)
            }

            DoorDbType.POSTGRES -> {
                add("""
                    CREATE OR REPLACE FUNCTION message_send_fn() RETURNS TRIGGER AS $$
                    BEGIN
                    $insertOutgoingReplicationSql
                    RETURN NEW;
                    END $$ LANGUAGE plpgsql
                """)
                add("""
                    CREATE TRIGGER message_send_trig AFTER INSERT 
                    ON Message
                    FOR EACH ROW EXECUTE PROCEDURE message_send_fn()
                """)
            }
        }

    }

}