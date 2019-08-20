package com.ustadmobile.door.annotation

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)

annotation class Repository(val methodType: Int = 0) {

    companion object {

        const val METHOD_AUTO = 0

        const val METHOD_DELEGATE_TO_DAO = 1

        const val METHOD_DELEGATE_TO_WEB = 2

        const val METHOD_SYNCABLE_GET = 3

        const val METHOD_NOT_ALLOWED = 4
    }
}