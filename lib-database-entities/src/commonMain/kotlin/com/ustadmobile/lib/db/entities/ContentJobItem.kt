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
     *  - ContentEntry (leaf)
     *     e.g. https://server.com/endpoint/umapp/index.html#ContentEntryDetail?entityUid=1234
     *  - A plain HTTP file that can be imported
     *     e.g. https://server.com/dir/file.epub
     *  - Web resource that needs scraped
     *     e.g. https://khanacademy.org/topic/exercise
     *  - A local file URI that can be imported
     *     e.g. file:///dir/file.epub
     */
    var sourceUri: String? = null,

    var cjiIsLeaf: Boolean = true,

    /**
     * Where the ContentEntryUid is set to 0, this indicates that the job must extract metadata
     * from the sourceUri and generate a new ContentEntry.
     */
    var cjiContentEntryUid: Long = 0,

    /**
     * The ParentContentEntryUid can be set when the ContentEntryUid is 0. The job runner will
     * then create a ContentEntryParentChildJoin to the specified parent when it creates the
     * ContentEntry itself.
     */
    var cjiParentContentEntryUid: Long = 0,

    /**
     * The Container UID might be specified
     */
    var cjiContainerUid: Long = 0,

    var cjiProgress: Long = 0,

    var cjiTotal: Long = 0,

    var cjiStatus: Int = 0,

    var cjiConnectivityAcceptable: Int = 0,

    /**
     * The plugin id can be set if known. If not known, the runner will guess using the source
     * uri.
     */
    var cjiPluginId: Int = 0,

    /**
     * The number of attempts made so far
     */
    var cjiAttemptCount: Int = 0

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