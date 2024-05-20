package com.ustadmobile.core.domain.xapi.model

class State(
    var stateId: String?,
    var agent: XapiActor?,
    var activityId: String?,
    var content: HashMap<String, Any>?,
    var registration: String?
)
