package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Statement(
    var actor: Actor? = null,
    var verb: Verb? = null,
    @SerialName("object")
    var `object`: XObject? = null,
    var subStatement: Statement? = null,
    var result: Result? = null,
    var context: XContext? = null,
    var timestamp: String? = null,
    var stored: String? = null,
    var authority: Actor? = null,
    var version: String? = null,
    var id: String? = null,
    var attachments: List<Attachment>? = null,
    var objectType: String? = null
)
