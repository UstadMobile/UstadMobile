package com.ustadmobile.core.impl

import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.controller.UstadEditPresenter
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

/**
 * Represents the options that are used when navigating from one screen to another screen for
 * purposes of returning a result.
 *
 * @param fromPresenter the presenter that we are navigating from (this may or
 * @param currentEntityValue the current value of the entity, or null if there is none. If provided, the
 * entity is converted into JSON and passed to the edit view as ARG_ENTITY_JSON
 * @param destinationViewName the view name to navigate to e.g. SomeEditView.VIEW_NAME
 * @param entityClass KClass representing the entity (for serialization purposes)
 * @param serializationStrategy Kotlinx serialization strategy
 * @param overwriteDestination if true, the return result destination will be replaced with the
 * current destination. This is normally desired when this is an edit screen (e.g. the user is
 * editing something and going to pick something else), normally not desired in a list screen
 * (e.g. selecting from person list, then selected to create a new person)
 * @param arguments args to pass to the edit screen
 */
class NavigateForResultOptions<T : Any>(
    val fromPresenter: UstadBaseController<*>,
    val currentEntityValue: T?,
    val destinationViewName: String,
    val entityClass: KClass<T>,
    val serializationStrategy: SerializationStrategy<T>,
    val destinationResultKey: String? = null,
    overwriteDestination: Boolean? = null,
    val arguments: MutableMap<String, String> = mutableMapOf()
) {

    val overwriteDestination: Boolean = overwriteDestination ?: (fromPresenter is UstadEditPresenter<*, *>)

    fun copy(newEntityValue: T?,
             newArguments: MutableMap<String, String> = arguments
    ) = NavigateForResultOptions(fromPresenter,
        newEntityValue, destinationViewName, entityClass, serializationStrategy,
        destinationResultKey, overwriteDestination, newArguments)
}


