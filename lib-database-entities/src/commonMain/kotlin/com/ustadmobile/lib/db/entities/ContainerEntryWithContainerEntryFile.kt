package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import com.ustadmobile.lib.database.annotation.UmEmbedded

class ContainerEntryWithContainerEntryFile(cePath: String = "", container: Container = Container(), entryFile: ContainerEntryFile = ContainerEntryFile()) : ContainerEntry(cePath, container, entryFile) {

    @UmEmbedded
    @Embedded
    var containerEntryFile: ContainerEntryFile? = entryFile

}
