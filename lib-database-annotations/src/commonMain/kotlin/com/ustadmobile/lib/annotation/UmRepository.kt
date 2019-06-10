package com.ustadmobile.lib.database.annotation

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class UmRepository(val delegateType: Int = UmRepositoryMethodType.DELEGATE_TO_DAO) {

    annotation class UmRepositoryMethodType {
        companion object {

            /**
             * If delegating to the webservice
             */
            const val DELEGATE_TO_WEBSERVICE = 1

            const val DELEGATE_TO_DAO = 2

            const val INCREMENT_CHANGE_SEQ_NUMS_THEN_DELEGATE_TO_DAO = 3
        }
    }

}
