package com.ustadmobile.core.db

import com.ustadmobile.door.IncomingReplicationEvent
import com.ustadmobile.door.IncomingReplicationListener
import com.ustadmobile.door.ext.replicationNotificationDispatcher
import com.ustadmobile.lib.db.entities.*
import kotlinx.serialization.json.*

/**
 * usersession change: full revalidation (onNewNode) for the nodeClientId
 * scopedgrant change: full revalidation for anyone who is in the linked group
 * persongroupmember change: full revalidation for any node with a session linked to that personUid
 * clazz change: full revalidation for anyone who has permission to select the class
 * clazzenrolment: full revalidation for anyone with permission to select the class
 * schoolenrolment: full revalidation for anyone with permission to select the school.
 */
class RepIncomingListener(private val db: UmAppDatabase) : IncomingReplicationListener {

    private fun JsonArray.mapLongPropertyOrThrow(propName: String): List<Long> {
        return map { it.jsonObject.get(propName)?.jsonPrimitive?.long
            ?: throw IllegalArgumentException("JsonArray does not have valid long property: $propName")
        }
    }

    override suspend fun onIncomingReplicationProcessed(incomingReplicationEvent: IncomingReplicationEvent) {
        val jsonArray: JsonArray = incomingReplicationEvent.incomingReplicationData

        when(incomingReplicationEvent.tableId) {
            UserSession.TABLE_ID -> {
                val affectedNodes = jsonArray.mapNotNull {
                    it.jsonObject.get("usClientNodeId")?.jsonPrimitive?.longOrNull
                }

                affectedNodes.forEach {
                    db.replicationNotificationDispatcher.onNewDoorNode(it, "")
                }
            }

            ScopedGrant.TABLE_ID -> {
                val affectedGroups = jsonArray.map {
                    it.jsonObject.get("sgGroupUid")?.jsonPrimitive?.longOrNull
                        ?: throw IllegalArgumentException("RepIncomingListener: invalid ScopedGrant.sgGroupUid")
                }

                val affectedNodes = affectedGroups.chunked(100).flatMap {
                    db.userSessionDao.findActiveNodesIdsByGroupUids(it)
                }.toSet()

                affectedNodes.forEach {
                    db.replicationNotificationDispatcher.onNewDoorNode(it, "")
                }
            }



            PersonGroupMember.TABLE_ID -> {
                val personUids = jsonArray.mapLongPropertyOrThrow("groupMemberPersonUid")

                val affectedNodes = personUids.chunked(100).flatMap {
                    db.userSessionDao.findActiveNodeIdsByPersonUids(it)
                }.toSet()

                affectedNodes.forEach {
                    db.replicationNotificationDispatcher.onNewDoorNode(it, "")
                }
            }

            Clazz.TABLE_ID -> {
                val clazzUids = jsonArray.mapLongPropertyOrThrow("clazzUid")
                val affectedNodes = clazzUids.chunked(100).flatMap {
                    db.userSessionDao.findAllActiveNodeIdsWithClazzBasedPermission(clazzUids)
                }.toSet()

                affectedNodes.forEach {
                    db.replicationNotificationDispatcher.onNewDoorNode(it, "")
                }
            }

            ClazzEnrolment.TABLE_ID -> {
                val clazzUids = jsonArray.mapNotNull {
                    it.jsonObject.get("clazzEnrolmentClazzUid")?.jsonPrimitive?.longOrNull
                        ?: throw IllegalArgumentException("ClazzEnrolment has invalid clazz uid!")
                }.toSet().toList()

                val affectedNodes = db.userSessionDao.findAllActiveNodeIdsWithClazzBasedPermission(
                    clazzUids)
                affectedNodes.forEach {
                    db.replicationNotificationDispatcher.onNewDoorNode(it, "")
                }
            }

            SchoolMember.TABLE_ID -> {
                val schoolUids = jsonArray.mapNotNull {
                    it.jsonObject.get("schoolUid")?.jsonPrimitive?.longOrNull
                }.toSet().toList()

                val affectedNodes = db.userSessionDao.findAllActiveNodeIdsWithSchoolBasedPermission(
                    schoolUids)
                affectedNodes.forEach {
                    db.replicationNotificationDispatcher.onNewDoorNode(it, "")
                }
            }
        }


    }
}