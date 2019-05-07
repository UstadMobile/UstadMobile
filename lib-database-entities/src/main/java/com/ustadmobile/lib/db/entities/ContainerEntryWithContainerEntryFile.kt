package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEmbedded

class ContainerEntryWithContainerEntryFile : ContainerEntry {

    @UmEmbedded
    var containerEntryFile: ContainerEntryFile? = null

    constructor(cePath: String, container: Container, entryFile: ContainerEntryFile) : super(cePath, container, entryFile) {
        this.containerEntryFile = entryFile
    }

    constructor()
}
