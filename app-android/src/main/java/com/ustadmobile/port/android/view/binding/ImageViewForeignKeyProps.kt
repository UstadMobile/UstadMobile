package com.ustadmobile.port.android.view.binding

import android.graphics.drawable.Drawable
import android.net.Uri
import com.ustadmobile.port.android.view.util.ForeignKeyAttachmentUriAdapter
import kotlinx.coroutines.Job

class ImageViewForeignKeyProps(var foreignKey: Long = 0,
                               var foreignKeyEndpoint: String? = null,
                               var foreignKeyAttachmentUriAdapter: ForeignKeyAttachmentUriAdapter? = null,
                               var foreignKeyLoadingOrDisplayed: Long = -1,
                               var currentJob: Job? = null,
                               var placeholder: Drawable? = null,
                               var autoHide: Boolean = false) {

    /**
     * The image uri that is currently being displayed. This can be checked to see if it has
     * really changed (or not)
     */
    var imageUriDisplayed: Uri? = null
}
