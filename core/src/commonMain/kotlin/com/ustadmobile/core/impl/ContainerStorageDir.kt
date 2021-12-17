package com.ustadmobile.core.impl

class ContainerStorageDir(

    var dirUri: String,

    /**
     * The user friendly name e.g. SD-Card or Phone
     *
     * @return the user friendly name
     */
    var name: String? = null,

    /**
     * The amount of space available for saving contents (in bytes)
     */
    var usableSpace: Long = -1L,


    var removableMedia: Boolean = false

)