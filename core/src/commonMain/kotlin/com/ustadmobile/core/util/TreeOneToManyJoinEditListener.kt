package com.ustadmobile.core.util

interface TreeOneToManyJoinEditListener<T>: OneToManyJoinEditListener<T> {

    /**
     * Handle when the user clicks to indent a jointed entity
     */
    fun onClickIndent(joinedEntity: T)

    /**
     * Handle when the user clicks to unindent a jointed entity
     */
    fun onClickUnIndent(joinedEntity: T)

    /**
     * Handle when the user clicks to hide a jointed entity
     */
    fun onClickHide(joinedEntity: T)

}