package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

class ContainerEntryWithContainerEntryFile(cePath: String = "", container: Container = Container(), entryFile: ContainerEntryFile = ContainerEntryFile()) : ContainerEntry(cePath, container, entryFile) {

    @Embedded
    var containerEntryFile: ContainerEntryFile? = entryFile

}
