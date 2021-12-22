package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["cjiContentEntryUid", "cjiFinishTime"],
        unique = false)])
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

        /**
     * Represents the progress on this item (itself) not including any child items
     */
    var cjiItemProgress: Long = 0,

        /**
     * Represents the total to process on this item (itself) not including any child items
     */
    var cjiItemTotal: Long = 0,

        /**
     * Represents the progress of this item and its child items (inclusive). This should not be set
     * directly, it is managed by triggers and should NOT be updated directly.
     */
    var cjiRecursiveProgress: Long = 0,

        /**
     * Represents the total size of the job and its child items (inclusive). This should not be set
     * directly, it is managed by triggers and should NOT be updated directly.
     */
    var cjiRecursiveTotal: Long = 0,

        /**
     * Represents the status to the process of this job item and not including any child items.
     * Status set to default JobStatus.QUEUED
     */
    var cjiStatus: Int = 4,

        /**
     * Represents the status of the job and its child items(inclusive). This is managed by
     * triggers and should NOT be updated directly. Status set to default JobStatus.QUEUED
     */
    var cjiRecursiveStatus:Int = 4,


    var cjiConnectivityNeeded: Boolean = false,


        /**
     * The plugin id can be set if known. If not known, the runner will guess using the source
     * uri.
     */
    var cjiPluginId: Int = 0,

        /**
     * The number of attempts made so far
     */
    var cjiAttemptCount: Int = 0,

        /**
     *  The parent of this ContentJobItem in the content job itself.
     */
    var cjiParentCjiUid: Long = 0,


        var cjiServerJobId: Long = 0,

        /**
     * time when the job runner started the job item
     */
    var cjiStartTime: Long = 0,

        /**
     * time when the job runner finished the job item
     */
    var cjiFinishTime: Long = 0,

    /**
     * If this ContentJobItem is running an upload, this is the session uuid for the upload
     */
    var cjiUploadSessionUid: String? = null,

        /**
         *  If contentJobItem is cancelled, then contentEntry needs to be deleted
          */
      var cjiContentDeletedOnCancellation:  Boolean = false,

        /**
         * Is used to check the status that the  container has finished processing in the job
         */
        var cjiContainerStatus: Boolean = false

) {
    companion object {

        const val STATUS_DOWNLOAD = 0

        const val STATUS_RUNNING = 5

        const val STATUS_COMPLETE = 10

        const val ACCEPT_NONE = 1

        const val ACCEPT_UNMETERED = 2

        const val ACCEPT_AT_LEAST_ONE_PEER = 4

        const val ACCEPT_METERED = 8

        const val ACCEPT_ANY = ACCEPT_NONE + ACCEPT_UNMETERED + ACCEPT_AT_LEAST_ONE_PEER +
                ACCEPT_METERED

    }
}