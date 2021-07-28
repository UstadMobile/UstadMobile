package com.ustadmobile.lib.db.entities

data class ContentJob(

        var cjUid: Long = 0,

        //Where data should be saved (null = default device storage)
        var toUri: String? = null,

        var cjProgress: Long = 0,

        var cjTotal: Long = 0,

        var params: String? = null
)