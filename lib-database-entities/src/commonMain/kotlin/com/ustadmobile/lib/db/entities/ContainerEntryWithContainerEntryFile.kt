package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ContainerEntryWithContainerEntryFile() : ContainerEntry() {

    @Embedded
    var containerEntryFile: ContainerEntryFile? = null

    constructor(cePath: String = "", container: Container = Container(), entryFile: ContainerEntryFile = ContainerEntryFile()) : this() {
        super.cePath = cePath
        super.ceCefUid = entryFile.cefUid
        super.ceContainerUid = container.containerUid
        containerEntryFile = entryFile
    }

}
