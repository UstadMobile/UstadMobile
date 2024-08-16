package com.ustadmobile.core.util

object DiTag {

    /**
     * The root directory for a given context (e.g. endpoint or 'singleton') where data will be
     * kept. Under this directory would be container files, attachments, and potentially databases.
     */
    const val TAG_CONTEXT_DATA_ROOT = 13

    const val TAG_ADMIN_PASS_FILE = 81

    const val TAG_GOOGLE_API = 12

    const val XPP_FACTORY_NSAWARE = 1

    const val XPP_FACTORY_NSUNAWARE = 0

    const val TAG_FILE_UPLOAD_TMP_DIR = 32

    /**
     * Used to bind the main temporary directory on JVM (Server/Desktop) and Android
     */
    const val TAG_TMP_DIR = 42


}