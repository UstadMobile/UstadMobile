package com.ustadmobile.lib.db.entities

class ContentJobItem {

    //Where data is being gathered from (e.g. remote)
    var fromUri: String? = null

    //Where data should be saved (null = default device storage)
    var toUri: String? = null

}