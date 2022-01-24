package com.ustadmobile.lib.staging.contentscrapers

import com.ustadmobile.door.entities.NodeIdAndAuth
import java.lang.IllegalStateException

/**
 * Placeholder added 24/June/21 to allow staging scrapers to still compile. All database access
 * must be converted to using the DI
 */
fun replaceMeWithDi(): NodeIdAndAuth = throw IllegalStateException("I should be using di instead")
