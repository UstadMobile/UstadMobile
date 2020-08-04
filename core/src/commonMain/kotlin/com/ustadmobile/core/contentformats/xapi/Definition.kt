package com.ustadmobile.core.contentformats.xapi

class Definition {

    var name: Map<String, String>? = null

    var description: Map<String, String>? = null

    var type: String? = null

    var extensions: Map<String, Any>? = null

    var moreInfo: String? = null

    var interactionType: String? = null

    var correctResponsePattern: List<String>? = null

    var choices: List<Interaction>? = null

    var scale: List<Interaction>? = null

    var source: List<Interaction>? = null

    var target: List<Interaction>? = null

    var steps: List<Interaction>? = null

    inner class Interaction {

        var id: String? = null

        var description: Map<String, String>? = null
    }
}
