package com.ustadmobile.core.util

import com.ustadmobile.core.controller.UstadBaseController
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

/**
 * This is
 */
open class GoToEditOneToManyJoinEditListener<T : Any>(
    private val presenter: UstadBaseController<*>,
    private val editViewName: String,
    private val entityClass: KClass<T>,
    private val serializationStrategy: SerializationStrategy<T>,
    private val joinEditHelper: OneToManyJoinEditHelper<T, *>,
    private val destinationResultKey: String? = (joinEditHelper as? OneToManyJoinEditHelperMp<*>)?.returnSavedStateKey
) : OneToManyJoinEditListener<T> {

    override fun onClickNew() {
        presenter.navigateToEditEntity(null,
            editViewName, entityClass, serializationStrategy, destinationResultKey,
            overwriteDestination = true)
    }

    override fun onClickEdit(joinedEntity: T) {
        presenter.navigateToEditEntity(joinedEntity,
            editViewName, entityClass, serializationStrategy, destinationResultKey,
            overwriteDestination = true)
    }

    override fun onClickDelete(joinedEntity: T) {
        joinEditHelper.onDeactivateEntity(joinedEntity)
    }
}