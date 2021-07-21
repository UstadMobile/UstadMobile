package com.ustadmobile.port.android.view.ext

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.gson.Gson
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.port.android.util.ext.putResultDestInfo
import com.ustadmobile.port.android.view.UstadBaseFragment
import com.ustadmobile.port.android.view.UstadEditFragment
import org.kodein.di.DI
import org.kodein.di.android.x.closestDI
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * Save the result of a fragment (e.g. a selection from a list or newly created entity) to the
 * BackStack SavedStateHandle as specified by ARG_RESULT_DEST_ID and ARG_RESULT_DEST_KEY
 */
fun Fragment.saveResultToBackStackSavedStateHandle(result: List<*>) {
    val di: DI by closestDI()
    val gson: Gson by di.instance()

    saveResultToBackStackSavedStateHandle(gson.toJson(result))
}

fun Fragment.saveResultToBackStackSavedStateHandle(result: String) {
    var saveToDestination = arguments?.getString(UstadView.ARG_RESULT_DEST_ID)
    val saveToDestinationViewName = arguments?.getString(UstadView.ARG_RESULT_DEST_VIEWNAME)

    //This is a transition arrangement so that this function will be able to return results when
    // the process was initiated by the multiplatform functions. We can simply lookup the
    // destination view id
    if(saveToDestination == null && saveToDestinationViewName != null) {
        val di: DI by closestDI()
        val destinationProvider: DestinationProvider = di.direct.instance()

        saveToDestination = destinationProvider.lookupDestinationName(saveToDestinationViewName)
            ?.destinationId?.toString()
    }

    val saveToKey = arguments?.getString(UstadView.ARG_RESULT_DEST_KEY)
    val navController = findNavController()
    if(saveToDestination != null && saveToKey != null) {
        val saveToDestId = saveToDestination.toInt()
        val destStackEntry = navController.getBackStackEntry(saveToDestId)
        destStackEntry.savedStateHandle.set(saveToKey, result)
        findNavController().popBackStack(saveToDestId, false)
    }else{
        navController.navigateUp()
    }
}

private val fragmentNavDefaultOptions: NavOptions by lazy {
        navOptions {
        anim {
            enter = R.anim.anim_slide_in_right
            exit = R.anim.anim_slide_out_left
            popEnter = android.R.anim.slide_in_left
            popExit = R.anim.anim_slide_out_right
        }
    }
}
/**
 * Navigate to an edit view and instruct the destination to save the result to the back stack
 *
 * @entity the entity to be edited (null to create a new entity)
 * @param entityClass The class for the entity type that is going to be selected
 * @param destinationId The destination id as per the navigation map
 * @param overwriteDestination If true, the ARG_RESULT_DEST_ID will be replaced with this fragment.
 *
 * This should normally be done when the user has gone through multiple edit screens e.g. when navigating
 * to holiday calendar edit from edit class and the back stack is as follows:
 *  Person Edit - Class List - Edit Class - Holiday Calendar List - Holiday Calendar Edit
 * The result should be saved into the savedStateHandle for Edit Class, not Person Edit.
 *
 * By default this will be true when navigating from an edit fragment (the likely destination
 * of anything that is being picked), false otherwise (e.g. when the user went via a list screen)
 * @navOptions navOptions to pass to navController.navigate
 */
fun <T> Fragment.navigateToEditEntity(entity: T?, destinationId: Int, entityClass: Class<T>,
                                           destinationResultKey: String = entityClass.simpleName,
                                           overwriteDestination: Boolean? = null,
                                            navOptions: NavOptions? = fragmentNavDefaultOptions,
                                            argBundle:Bundle = Bundle()) {
    val navController = findNavController()
    val backStateEntryVal = navController.currentBackStackEntry

    if(backStateEntryVal != null) {
        //Provide compatibility with multiplatform results
        val currentDestViewName = backStateEntryVal.arguments
            ?.getString(UstadView.ARG_RESULT_DEST_VIEWNAME)
        val currentDestKey = backStateEntryVal.arguments
            ?.getString(UstadView.ARG_RESULT_DEST_KEY)

        val di: DI by closestDI()
        val destProvider: DestinationProvider by di.instance()
        val overwriteDestVal = (this is UstadEditFragment<*>)

        if(!overwriteDestVal && currentDestViewName != null && currentDestKey != null){
            argBundle.putString(UstadView.ARG_RESULT_DEST_VIEWNAME, currentDestViewName)
            argBundle.putString(UstadView.ARG_RESULT_DEST_KEY, currentDestKey)
            val destId = destProvider.lookupDestinationName(currentDestViewName)?.destinationId ?: 0
            argBundle.putString(UstadView.ARG_RESULT_DEST_ID, destId.toString())
        }else {
            argBundle.putResultDestInfo(backStateEntryVal, destinationResultKey,
                overwriteDest = overwriteDestination ?: (this is UstadEditFragment<*>))
        }
    }

    if(entity != null)
        argBundle.putEntityAsJson(UstadEditView.ARG_ENTITY_JSON, entity)

    navController.navigate(destinationId, argBundle, navOptions)
}

/**
 * Navigate to a list view in picker mode for the given entity type and destination id
 *
 * @param entityClass The class for the entity type that is going to be selected
 * @param destinationId The destination id as per the navigation map
 * @param args optional additional args to pass to the list view
 * @param destinationResultKey the key to use in the SavedStateHandle
 */
fun Fragment.navigateToPickEntityFromList(entityClass: Class<*>, destinationId: Int,
                                               args: Bundle = Bundle(),
                                               destinationResultKey: String = entityClass.simpleName,
                                              overwriteDestination: Boolean? = null,
                                              navOptions: NavOptions? = fragmentNavDefaultOptions){
    val navController = findNavController()
    val currentBackStateEntryVal = navController.currentBackStackEntry
    if(currentBackStateEntryVal != null)
        args.putResultDestInfo(currentBackStateEntryVal, destinationResultKey,
                overwriteDest = overwriteDestination ?: (this is UstadEditFragment<*>))

    args.putString(UstadView.ARG_LISTMODE, ListViewMode.PICKER.toString())
    navController.navigate(destinationId, args, navOptions)
}

/**
 * Extension function to request for the permission
 * @param permission Permission to be request
 * @param runAfterFun function to executed after permission is granted
 */
fun Fragment.runAfterRequestingPermissionIfNeeded(permission: String, runAfterFun: (granted: Boolean) -> Unit) {
    if(ContextCompat.checkSelfPermission(requireContext(),permission) == PackageManager.PERMISSION_GRANTED) {
        runAfterFun.invoke(true)
    }else {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            runAfterFun(granted)
        }.launch(permission)
    }
}
