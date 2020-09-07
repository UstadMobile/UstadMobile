package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
open class ContainerEntry() {

    @PrimaryKey(autoGenerate = true)
    var ceUid: Long = 0

    @ColumnInfo(index = true)
    var ceContainerUid: Long = 0

    /**
     * The path of this entry inside the container. It is relative and should not begin with slash
     * e.g. epub/book.opf NOT /epub/book.opf
     */
    var cePath: String? = null

    var ceCefUid: Long = 0

    constructor(cePath: String, container: Container, entryFile: ContainerEntryFile) : this() {
        this.cePath = cePath
        this.ceCefUid = entryFile.cefUid
        this.ceContainerUid = container.containerUid
    }
}
