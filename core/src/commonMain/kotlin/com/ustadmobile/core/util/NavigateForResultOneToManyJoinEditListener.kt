package com.ustadmobile.core.util

import com.ustadmobile.core.impl.NavigateForResultOptions

/**
 * This is an implementation of the OneToManyJoinEditListener that will take the user directly to
 * an edit screen.
 *
 * @see OneToManyJoinEditHelperMp#createGoToEditListener
 */
open class NavigateForResultOneToManyJoinEditListener<T : Any>(
    private val navigateForResultOptions: NavigateForResultOptions<T>,
    private val joinEditHelper: OneToManyJoinEditHelper<T, *>
) : OneToManyJoinEditListener<T> {

    override fun onClickNew() {
        navigateForResultOptions.fromPresenter.navigateForResult(navigateForResultOptions)
    }

    override fun onClickEdit(joinedEntity: T) {
        val navOptions = navigateForResultOptions.copy(newEntityValue = joinedEntity)
        navOptions.fromPresenter.navigateForResult(navOptions)
    }

    override fun onClickDelete(joinedEntity: T) {
        joinEditHelper.onDeactivateEntity(joinedEntity)
    }
}