package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * ContentEntryImportJob represents a piece of Content to import. It can be observed for purposes
 * of displaying progress to the user.
 */
@Entity(
    indices = [
        Index(
            value = ["cjiContentEntryUid", "cjiFinishTime"],
            unique = false
        )
    ]
)

/**
 * @param sourceUri Where data is being taken from, this could be
 *  - ContentEntry (leaf)
 *     e.g. https://server.com/endpoint/umapp/index.html#ContentEntryDetail?entityUid=1234
 *  - A plain HTTP file that can be imported
 *     e.g. https://server.com/dir/file.epub
 *  - Web resource that needs scraped
 *     e.g. https://khanacademy.org/topic/exercise
 *  - A local file URI that can be imported
 *     e.g. file:///dir/file.epub
 *
 *  @param cjiParams if not null, then this is a simple Json key-value map that can be used to pass
 *  import parameters (e.g. subtitles to add etc).
 */
@Serializable
data class ContentEntryImportJob(

    @PrimaryKey(autoGenerate = true)
    var cjiUid: Long = 0,

    var sourceUri: String? = null,

    /**
     * Where the filename is not in the URI (e.g. temporary uploaded files etc), this property keeps
     * the original filename (e.g. as when the user selected it).
     */
    var cjiOriginalFilename: String? = null,

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
     * The ContentEntryVersion (once known).
     */
    var cjiContentEntryVersion: Long = 0,

    /**
     * Represents the progress on this item
     */
    var cjiItemProgress: Long = 0,

    /**
     * Represents the total to process on this item
     */
    var cjiItemTotal: Long = 0,

    /**
     * Represents the status to the process of this job item and not including any child items (as
     * per JobStatus).
     * Status set to default JobStatus.QUEUED
     */
    var cjiStatus: Int = 4,

    /**
     * Represents the status of the job and its child items(inclusive). This is managed by
     * triggers and should NOT be updated directly. Status set to default JobStatus.QUEUED
     */
    var cjiRecursiveStatus:Int = 4,

    /**
     * The importer id can be set if known. If not known, the runner will guess using the source
     * uri.
     */
    var cjiPluginId: Int = 0,

    /**
     *  The parent of this ContentJobItem in the content job itself.
     */
    var cjiParentCjiUid: Long = 0,

    /**
     * time when the job runner started the job item
     */
    var cjiStartTime: Long = 0,

    /**
     * time when the job runner finished the job item
     */
    var cjiFinishTime: Long = 0,

    /**
     *  If true, if this ContentJobItem is cancelled, then any associated contentEntry should be set
     *  as inactive (e.g. if something is being imported as new content, but the job is canceled,
     *  the ContentEntry itself must be removed. If this is an update or something else, then we
     *  don't make the contententry inactive
     */
    var cjiContentDeletedOnCancellation:  Boolean = false,

    /**
     * CompressionLevel to use for import - integer constants as per CompressionLevel (on core)
     */
    @ColumnInfo(defaultValue = "3")
    var cjiCompressionLevel: Int = 3,

    var cjiError: String? = null,

    var cjiErrorDismissed: Boolean = false,

    var cjiOwnerPersonUid: Long = 0,

    var cjiParams: String? = null,

) {
    companion object {
        const val TABLE_ID = 720
    }
}