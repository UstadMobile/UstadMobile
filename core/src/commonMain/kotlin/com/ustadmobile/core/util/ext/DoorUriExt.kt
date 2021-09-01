package com.ustadmobile.core.util.ext

import com.ustadmobile.door.DoorUri

/**
 * Where the receiver DoorUri is a directory, delete all its contents recursively, but do
 * not delete the directory itself
 */
expect suspend fun DoorUri.emptyRecursively()

