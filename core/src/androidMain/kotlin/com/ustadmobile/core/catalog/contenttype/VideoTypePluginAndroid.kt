package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerUploadJob

class VideoTypePluginAndroid: VideoTypePlugin() {

    override suspend fun importToContainer(importJob: ContainerUploadJob, progressListener: (Int) -> Unit): Container {

    }
}