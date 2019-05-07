package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmPrimaryKey

class XapiStatement {

    @UmPrimaryKey
    var uuid: String? = null

    var xapiAgentUid: Long = 0

    var xapiActivityUid: Long = 0

    var xapiActorUid: Long = 0

    var verbUid: Long = 0

    var authorityUid: Long = 0

    var statementRef: String? = null

    var isResultSuccess: Boolean = false

    var isResultComplete: Boolean = false

    var resultResponse: String? = null

    var resultDuration: Long = 0

    var resultScoreScaled: Float = 0.toFloat()

    var resultScaleRaw: Float = 0.toFloat()

    var resultScoreMin: Float = 0.toFloat()

    var resultScoreMax: Float = 0.toFloat()

    var resultExtensions: String? = null

    var resultProgress: Int = 0

    var stored: Long = 0

    var timestamp: Long = 0

    var contextRegistration: String? = null

    var version: String? = null

    var fullStatement: String? = null
}
