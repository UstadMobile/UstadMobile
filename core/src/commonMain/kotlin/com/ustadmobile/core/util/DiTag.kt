package com.ustadmobile.core.util

object DiTag {

    /**
     * The root directory for a given context (e.g. endpoint or 'singleton') where data will be
     * kept. Under this directory would be container files, attachments, and potentially databases.
     */
    const val TAG_CONTEXT_DATA_ROOT = 13

    /**
     * The default directory to which ContainerEntryFile data will be saved. This is not necessarily
     * the only directory.
     */
    @Deprecated("use ContainerStorageManager")
    const val TAG_DEFAULT_CONTAINER_DIR = 11

    const val TAG_GOOGLE_API = 12

    const val TAG_PRESENTER_COROUTINE_SCOPE = 14

    const val XPP_FACTORY_NSAWARE = 1

    const val XPP_FACTORY_NSUNAWARE = 0

    const val TAG_FILE_FFMPEG = 30

    const val TAG_FILE_FFPROBE = 31

    const val TAG_FILE_UPLOAD_TMP_DIR = 32



}