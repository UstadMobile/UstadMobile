package com.ustadmobile.lib.db.entities

data class ContentJobItem(

    var cjiUid: Int = 0,

    var cjiJobUid: Int = 0,

    //Where data is being gathered from (e.g. remote)
    var fromUri: String? = null,

    //Where data should be saved (null = default device storage)
    var toUri: String? = null,

    var cjiIsLeaf: Boolean = true,

    var cjiContentEntryUid: Long = 0,

    var cjiParentContentEntryUid: Long = 0,

    var cjiContainerUid: Long = 0,

    var cjiProgress: Long = 0,

    var cjiTotal: Long = 0

)