package com.ustadmobile.lib.db.entities.xapi

import androidx.room.Entity
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.ReplicateLastModified
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_WHERE_NEWER
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT
import kotlinx.serialization.Serializable

@Entity(
    primaryKeys = arrayOf("seActorUid", "seHash")
)
@Serializable
@ReplicateEntity(
    tableId = StateEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(
    arrayOf(
        Trigger(
            name = "stateentity_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [ TRIGGER_UPSERT ]
        )
    )
)

/**
 * Used to store the xAPI State for State Resource as per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#23-state-resource
 *
 * This entity is also used to store H5P state data, which uses a slightly different API.
 *
 * @param seActorUid Uid of the related actor entity (this MUST be an agent as per the spec Xapi
 *        Spec - Communication section 2.3)
 * @param seHash - hash of other keys that are part of the identifier - activityId, registrationUuid,
 *        stateId, (h5psubcontent id if applicable)
 * @param seStateId the stateId as per the xAPI spec, if storing H5P content data, then this is the
 *        dataId as per https://github.com/h5p/h5p-php-library/blob/4599291d7ce0cfb90edd188b181416f31514748e/js/h5p.js#L2395 .
 *        The dataId and dataType are synonymous as per the H5P source code - the parameter is called
 *        dataId in the getUserData function, dataType in the contentUserDataAjax function.
 *
 * @param seContent content itself. If the content is plain text (contentType = text/ * or application/json)
 *        then this is stored as a simple string. If not, this will be Base64 encoded binary data.
 *        SCORM 1.2 had a 4KB limit and SCORM 2004 has a 64KB limit, so huge content is not expected.
 *        See https://community.articulate.com/discussions/articulate-storyline/api-suspend-data-limit
 *        https://community.articulate.com/articles/learning-more-about-your-lms-suspend-data-and-resume-behavior
 * @param seH5PPreloaded if this is storing H5P data AND the preloaded is true, then true, otherwise false
 * @param seH5PSubContentId if this is storing H5P data, then the subContentId as per
 *        https://github.com/h5p/h5p-php-library/blob/4599291d7ce0cfb90edd188b181416f31514748e/js/h5p.js#L2460
 *        This defaults to '0' if storing H5P data and no subContentId is set
 */
data class StateEntity(
    var seActorUid: Long = 0,

    var seHash: Long  = 0,

    var seActivityUid: Long  = 0,

    var seStateId: String = "",

    @ReplicateEtag
    @ReplicateLastModified
    var seLastMod: Long = 0,

    //Reserved for future use
    var seTimeStored: Long = 0,

    var seContentType: String? = null,

    //Reserved for future use
    var seCompressed: Int = 0,

    var seContent: String = "",

    var seDeleted: Boolean = false,

    var seRegistrationHi: Long? = null,

    var seRegistrationLo: Long? = null,

    var seH5PPreloaded: Boolean = false,

    var seH5PSubContentId: String? = null,

) {

    companion object {

        const val TABLE_ID = 3289

    }
}