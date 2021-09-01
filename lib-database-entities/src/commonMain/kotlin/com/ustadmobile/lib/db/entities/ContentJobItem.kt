package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContentJobItem(

    @PrimaryKey(autoGenerate = true)
    var cjiUid: Long = 0,

    var cjiJobUid: Long = 0,

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

    var cjiParentContentEntryUid: Long = 0,

    var cjiContainerUid: Long = 0,

    var cjiProgress: Long = 0,

    var cjiTotal: Long = 0,

    var cjiStatus: Int = 0,

    var cjiConnectivityAcceptable: Int = 0,

    /**
     * The plugin id can be set if known. If not known, the runner will guess using the source
     * uri.
     */
    var cjiPluginId: Int = 0

) {
    companion object {

        const val ACCEPT_NONE = 1

        const val ACCEPT_UNMETERED = 2

        const val ACCEPT_AT_LEAST_ONE_PEER = 4

        const val ACCEPT_METERED = 8

        const val ACCEPT_ANY = ACCEPT_NONE + ACCEPT_UNMETERED + ACCEPT_AT_LEAST_ONE_PEER +
                ACCEPT_METERED

    }
}