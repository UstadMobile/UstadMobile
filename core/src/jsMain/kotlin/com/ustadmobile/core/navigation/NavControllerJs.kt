package com.ustadmobile.core.navigation

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.core.view.RedirectView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.toUrlQueryString
import io.github.aakira.napier.Napier
import kotlinext.js.jsObject
import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.HashChangeEvent
import org.w3c.dom.History
import org.w3c.dom.events.Event
import kotlin.math.max
import kotlin.math.min

/**
 * Handles all navigation within the web application and keeps the internal navigation stack in
 * sync with the history in the browser.
 *
 * @param ustadUrlDivider Normally the endpoint server and the viewname / args are separated by /#/
 * however on testing this might be different.
 */
class NavControllerJs(
    private val ustadUrlDivider: String = UstadUrlComponents.DEFAULT_DIVIDER,
    private val storagePrefix: String = "ustadnav_",
    private val json: Json
): UstadNavController {

    private val logPrefix = "NavControllerJs: "

    /**
     * The internal nav stack. This must be kept here because Javascript doesn't allow
     * retrieving the state of previous items in the stack (which we need to access when
     * using returning results initiated via navigateForResult).
     */
    private val navStack: MutableList<UstadBackStackEntryJs> = mutableListOf()

    /**
     * We have to track the current index because the user can go back, and then go forward. If they
     * go forward, we should restore the state as expected. Data is only removed from the stack if
     * and when the user goes back n steps, and then forward (hence the new forward history will
     * overwrite anything from that position in the stack)
     */
    private val currentStackIndex: Int
        get() {
            val historyStateIndex = window.history.stateIndex
            if(historyStateIndex != -1) {
                return historyStateIndex
            }else {
                //User must have gone forward to a new page that should be in the nav stack
                try {
                    val currentViewUri = UstadUrlComponents.parse(window.location.href, ustadUrlDivider).viewUri
                    val index = navStack.indexOfLast { it.jsViewUri == currentViewUri }
                    Napier.d("NavControllerJs: CurrentStackIndex index=$index")
                    return index
                }catch(e: IllegalArgumentException) {
                    Napier.d(throwable = e) { "NavControllerJs: current location is not yet an ustadurl "}
                    return -1
                }
            }
        }

    private var History.stateIndex : Int
        get() {
            val currentState = state
            val stackIndex =  if(state != null) currentState.asDynamic().stateIndex else -1
            val stackIndexVal : Int = if(stackIndex != js("undefined"))
                stackIndex.unsafeCast<Int>()
            else
                -1

            return stackIndexVal
        }

        set(value) {
            val newStateObj = state ?: jsObject {  }
            newStateObj.asDynamic().stateIndex = value
            window.history.replaceState(newStateObj, "")
        }

    private fun saveStackSizeAndIndex() {
        sessionStorage.setItem("$storagePrefix.$SUBKEY_STACKSIZE", navStack.size.toString())
        sessionStorage.setItem("$storagePrefix.$SUBKEY_STACKINDEX", currentStackIndex.toString())
    }



    /**
     * Use the current location href to find the correct back stack entry. popstate and hashchange
     * might happen after the page already changed.
     */
    override val currentBackStackEntry: UstadBackStackEntry?
        get() {
            return navStack.getOrNull(currentStackIndex)
        }

    /**
     * When a navigation event occurs that is going to another destination AND popping off the stack,
     * we need to call history.go(-steps), *wait* for the hash to change, and then initiate forward
     * navigation to the final viewUri. This often happens when a user completes a task of some kind
     * (e.g. after adding a person/course etc, the system will navigate to the detail view for the
     * object just created. If the user clicks back, they should go back to the list, not the edit
     * view.)
     *
     * This is required to ensure that the browser's history state and our own remain in sync.
     */
    private var pendingNavigation: String? = null

    private val hashChangeListener : (Event) -> Unit = {
        val hashEvt = it as HashChangeEvent
        console.log("$logPrefix: hashchange new=${hashEvt.newURL} old=${hashEvt.oldURL} ")
        handleHashChange(hashEvt.newURL, hashEvt.oldURL)
    }

    init {
        Napier.d("$logPrefix: init")

        val storedStackSize = sessionStorage.getItem("$storagePrefix.$SUBKEY_STACKSIZE")?.toInt() ?: 0

        if(storedStackSize != 0) {
            Napier.d("$logPrefix: loading stack from session storage")
            //rebuild the stack
            for(i in 0 until storedStackSize) {
                navStack.add(UstadBackStackEntryJs.loadFromSessionStorage(
                    "$storagePrefix.$SUBKEY_STACKITEMS.$i", json))
            }
        }

        Napier.d("$logPrefix: creating new stack")

        var initUrlComponents: UstadUrlComponents
        try {
            initUrlComponents = UstadUrlComponents.parse(window.location.href, ustadUrlDivider)
        }catch (e: Exception) {
            initUrlComponents = UstadUrlComponents(window.location.href, RedirectView.VIEW_NAME, "")
        }

        //If the current url is not on the stack (e.g. freshly loaded, or redirect using window.location
        // from another screen ( e.g. by guardRoute ), then add the current screen to the nav stack.
        if(navStack.lastOrNull()?.jsViewUri != initUrlComponents.viewUri) {
            navStack += UstadBackStackEntryJs(initUrlComponents.viewName, initUrlComponents.arguments,
                initUrlComponents.viewUri, "$storagePrefix.$SUBKEY_STACKITEMS.${navStack.size}", json)
            window.history.stateIndex = (navStack.size - 1)
            saveStackSizeAndIndex()
        }

        Napier.d("$logPrefix: init: navStack = ${dumpNavStackToString()}")

        window.addEventListener("hashchange", hashChangeListener)
    }

    private fun dumpNavStackToString() : String {
        val stackIndexVal = currentStackIndex
        return "(" + navStack.mapIndexed { index: Int, stackEntry: UstadBackStackEntryJs ->
            if(index == stackIndexVal){
                "*${stackEntry.viewName}*"
            }else {
                stackEntry.viewName
            }
        }.joinToString() + " index=$stackIndexVal)"
    }


    /**
     * Pop everything off the nav stack from a given index
     *
     * @param index where to pop off from (inclusive)
     */
    private fun popOffNavStackFrom(index: Int) {
        for(i in (navStack.size - 1) downTo (index)) {
            val currentEntry = navStack[i]
            navStack.removeAt(i)
            Napier.d("$logPrefix: remove ${currentEntry.viewName}")
        }
    }

    /**
     * Generates the next storage key for the stack. The default index assumes that the entry has not
     * yet been added to the stack
     */
    private fun stackStorageKey(index: Int) : String {
        return "$storagePrefix.$SUBKEY_STACKITEMS.${navStack.size}"
    }

    @Suppress("MemberVisibilityCanBePrivate") //Will be used in testing
    internal fun handleHashChange(newUrl: String, oldUrl: String?) {
        try {
            val newStackIndexVal = window.history.stateIndex

            if(newStackIndexVal == -1) {
                //The user went forward. Save the index into the history state.
                window.history.stateIndex = (navStack.size - 1)
                Napier.d("$logPrefix: user went forwards to $newUrl . " +
                    "stack=(${dumpNavStackToString()})")
            }else {
                //The user went back. If this was done internally, the internal nav stack would
                // already be adjusted. If the user went back using browser navigation, then
                // we need to adjust the internal nav stack.
                Napier.d("$logPrefix: user went back. new stack = (${dumpNavStackToString()})")

                val pendingNavVal = pendingNavigation
                if(pendingNavVal != null) {
                    //run pending navigation
                    pendingNavigation = null
                    Napier.d("$logPrefix : run pending navigation to $pendingNavVal" +
                        " stack=${dumpNavStackToString()}")
                    window.location.replace("#/${pendingNavVal}")
                }
            }
        }catch(e: Exception) {
            Napier.d("$logPrefix Not an ustad url: ignoring $newUrl")
        }
    }

    override fun getBackStackEntry(viewName: String): UstadBackStackEntry? {
        return navStack.lastOrNull { it.viewName == viewName }
    }

    private fun calcNumStepsToGoBack(viewName: String, inclusive: Boolean) : Int{
        val resolvedPopViewname = when(viewName) {
            UstadView.ROOT_DEST -> navStack.firstOrNull()?.viewName ?: RedirectView.VIEW_NAME
            UstadView.CURRENT_DEST -> navStack.lastOrNull()?.viewName ?: RedirectView.VIEW_NAME
            else -> viewName
        }

        //The user might not be at the top of the nav stack. If the user is going back we should
        // only consider items below the current position in the stack (exclude forward steps).
        val navStackUpToCurrent = navStack.subList(0, currentStackIndex + 1)

        val viewNameIndex = max(navStackUpToCurrent.indexOfLast { it.viewName == resolvedPopViewname },
            0)

        //When popping off the stack, we are always at the top of the stack
        // e.g. currentIndex must equal (navStack.size-1)
        val deltaIndex = (navStackUpToCurrent.size - 1) - viewNameIndex
        return min(if(inclusive) {
            deltaIndex + 1
        }else {
            deltaIndex
        }, navStackUpToCurrent.size)
    }

    override fun popBackStack(viewName: String, inclusive: Boolean) {
        val numToGoBack = calcNumStepsToGoBack(viewName, inclusive)

        Napier.d("$logPrefix: POPBACKSTACK to: '$viewName' (inclusive = $inclusive) " +
            "go back $numToGoBack steps navStack=${dumpNavStackToString()}")
        if(numToGoBack != 0) {
            window.history.go(numToGoBack * -1)
        }
    }

    override fun navigate(
        viewName: String,
        args: Map<String, String>,
        goOptions: UstadMobileSystemCommon.UstadGoOptions
    ) {
        Napier.d("$logPrefix NAVIGATE to $viewName popUpTo='${goOptions.popUpToViewName}' " +
            "(inclusive=${goOptions.popUpToInclusive}) Current Stack=${dumpNavStackToString()}")
        val popUpToViewName = goOptions.popUpToViewName

        val stepsToGoBack = if(popUpToViewName != null) {
            calcNumStepsToGoBack(popUpToViewName, goOptions.popUpToInclusive)
        }else {
            0
        }

        val params = when {
            args.isEmpty() -> ""
            else -> "?${args.toUrlQueryString()}"
        }
        val viewUri = "$viewName$params"

        val currentStackIndexVal = currentStackIndex
        if(stepsToGoBack == 0) {
            // We can go directly to the new destination.
            // The new back stack entry must be added to the stack immediately. The hashchange event
            // will happen after the new component is already mounted (causing the new component to
            // save it's data to saved state handle for the previous entry).

            //If there is anything in the forward history, now is the time to remove it from the stack
            popOffNavStackFrom(currentStackIndexVal + 1)
            val newBackStackEntry = UstadBackStackEntryJs(viewName, args, viewUri,
                stackStorageKey(currentStackIndexVal + 1), json)
            navStack += newBackStackEntry
            saveStackSizeAndIndex()
            Napier.d("UstadNavController: navigate directly to #/$viewUri " +
                "navStack=${dumpNavStackToString()}")
            window.location.assign("#/$viewUri")
        }else if(stepsToGoBack == 1) {
            //we can use window.location.replace to replace the current location in the history state.
            // no need to use history.go(-n).
            sessionStorage.removeItem(stackStorageKey(currentStackIndexVal))
            navStack[currentStackIndex] = UstadBackStackEntryJs(viewName, args, viewUri,
                stackStorageKey(currentStackIndexVal), json)
            saveStackSizeAndIndex()
            Napier.d("UstadNavController: navigate using location.replace to #/$viewUri " +
                "navStack=${dumpNavStackToString()}")
            window.location.replace("#/$viewUri")
        } else {
            //We need to popoff entries from the stack, then, once that is complete, go to the new
            // destination. See note on pendingNavigation var.
            pendingNavigation = viewUri
            popOffNavStackFrom(navStack.size - stepsToGoBack)
            navStack += UstadBackStackEntryJs(viewName, args, viewUri,
                stackStorageKey(navStack.size), json)
            saveStackSizeAndIndex()
            Napier.d("UstadNavController: popoff, then navigate using replace. " +
                "Go ${stepsToGoBack-1} steps back first. navStack=${dumpNavStackToString()}")
            window.history.go(-(stepsToGoBack-1))
        }
    }

    fun navigateUp(): Boolean {
        Napier.d("$logPrefix: NAVIGATEUP")
        window.history.go(-1)
        return true
    }

    /**
     * This is not normally needed, but is needed for testing which runs in a single context to
     * avoid any conflicts.
     */
    fun unplug() {
        window.removeEventListener("hashchange", hashChangeListener)
    }

    companion object {
        private const val SUBKEY_STACKITEMS = "stackitems"

        private const val SUBKEY_STACKSIZE = "stacksize"

        private const val SUBKEY_STACKINDEX = "stackindex"
    }

}