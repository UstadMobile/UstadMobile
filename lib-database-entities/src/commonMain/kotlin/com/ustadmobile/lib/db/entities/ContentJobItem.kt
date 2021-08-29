package com.ustadmobile.lib.db.entities

data class ContentJobItem(

    var cjiUid: Int = 0,

    var cjiJobUid: Int = 0,

    /**
     * Where data is being taken from, this could be
     *  - Web plain file URI e.g. https://server.com/dir/file.epub
     *  - Web resource that needs scraped e.g. https://khanacademy.org/topic/exercise
     *  - A local file URI e.g. file:///dir/file.epub
     */
    var fromUri: String? = null,

    /**
     * Directory where the resulting container files should be saved. If null, then this means
     * use the default container storage directory.
     */
    var toUri: String? = null,

    var cjiIsLeaf: Boolean = true,

    var cjiContentEntryUid: Long = 0,

    var cjiContainerUid: Long = 0,

    var cjiProgress: Long = 0,

    var cjiTotal: Long = 0,

)