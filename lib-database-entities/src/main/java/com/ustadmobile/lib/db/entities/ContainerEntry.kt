package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmIndexField
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
open class ContainerEntry {

    @UmPrimaryKey(autoIncrement = true)
    var ceUid: Long = 0

    @UmIndexField
    var ceContainerUid: Long = 0

    var cePath: String? = null

    var ceCefUid: Long = 0

    constructor()

    constructor(cePath: String, container: Container, entryFile: ContainerEntryFile) {
        this.cePath = cePath
        this.ceCefUid = entryFile.cefUid
        this.ceContainerUid = container.containerUid
    }
}
