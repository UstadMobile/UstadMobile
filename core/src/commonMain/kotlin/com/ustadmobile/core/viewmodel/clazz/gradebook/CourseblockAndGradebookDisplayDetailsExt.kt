package com.ustadmobile.core.viewmodel.clazz.gradebook

import com.ustadmobile.lib.db.composites.CourseBlockAndGradebookDisplayDetails

val CourseBlockAndGradebookDisplayDetails.thumbnailUri: String?
    get() = courseBlockPicture?.cbpThumbnailUri ?: contentEntryPicture2?.cepThumbnailUri
