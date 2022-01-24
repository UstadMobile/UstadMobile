package com.ustadmobile.core.util

/**
 * The One-to-many join edit listener is used in edit views where there is a 1:n relationship (e.g.
 * Class-Schedule, Class-ScopedGrant, School-ScopedGrant, Site-SiteTerms, etc).
 *
 * It needs to react when the user clicks to create a new item, to edit an already joined existing
 * item, or to delete an item (e.g. deactivate the join).
 *
 * This listener would normally be called by the implementation (e.g. Android or JS) to respond
 * to events.
 */
interface OneToManyJoinEditListener<T> {

    /**
     * Handle when the user clicks to join something new. This normally would go to a list pick
     * screen or an edit screen.
     */
    fun onClickNew()

    /**
     * Handle when the user clicks to edit an already joined entity.
     */
    fun onClickEdit(joinedEntity: T)

    /**
     * Handle when the user clicks to delete an already joined entity
     */
    fun onClickDelete(joinedEntity: T)

}