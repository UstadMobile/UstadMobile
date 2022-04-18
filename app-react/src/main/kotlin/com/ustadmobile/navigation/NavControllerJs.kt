package com.ustadmobile.navigation

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.core.view.RedirectView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.toUrlQueryString
import io.github.aakira.napier.Napier
import kotlinext.js.asJsObject
import kotlinext.js.jsObject
import kotlinx.browser.document
import kotlinx.browser.window
import kotlin.math.max
import kotlin.math.min

/**
 * Handles all navigation within the web application, it is also responsible to synchronize
 * between our internal stack and browser history.
 *
 * We only pop from the history only when the pop state is triggered from our internal navigation,
 * Otherwise i.e Browser back navigation will only change the currentBackStackEntry position
 * from the stack.
 *
 * This  is due to the reason that, browser can use forward button to navigate and if our
 * internal stack entry was popped that means we won't be able to sync histories any more
 * and the state will be destroyed. So, all saved data will be lost.
 */
class NavControllerJs (): UstadNavController {

    private val logPrefix = "NavControllerJs: "

    /**
     * The internal nav stack. This must be kept here because Javascript doesn't allow
     * retrieving the state of previous items in the stack (which we need to access when
     * using returning results initiated via navigateForResult).
     */
    private val navStack: MutableList<UstadBackStackEntryJs> = mutableListOf()

    override val currentBackStackEntry: UstadBackStackEntry?
        get() = navStack.lastOrNull()


    init {
        Napier.d("$logPrefix: init")
        window.addEventListener("pageshow", {
            console.log("$logPrefix: pageshow: ${document.location}")
            handleHashChange(window.location.href, null)
        })

        window.addEventListener("hashchange", {
            console.log("$logPrefix: hashchange new=${it.asDynamic().newURL} old=${it.asDynamic().oldURL} ")
            handleHashChange(it.asDynamic().newURL.unsafeCast<String>(),
                it.asDynamic().oldURL.unsafeCast<String>())
        })
    }

    private fun dumpNavStackToString() = navStack.joinToString { it.viewName }

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

    @Suppress("MemberVisibilityCanBePrivate") //Will be used in testing
    internal fun handleHashChange(newUrl: String, oldUrl: String?) {
        val currentState = window.history.state?.asJsObject()
        try {
            val urlComponents = UstadUrlComponents.parse(newUrl)

            if(currentState == null) {
                //The user went forward. Save the index into the history state.
                navStack += UstadBackStackEntryJs(urlComponents.viewName, urlComponents.arguments)
                window.history.replaceState(jsObject {
                    asDynamic().index = (navStack.size - 1)
                }, "")
                Napier.d("$logPrefix: push $newUrl index=${navStack.size} onto stack. " +
                    "Stack=(${dumpNavStackToString()})")
            }else {
                //The user went back. If this was done internally, the internal nav stack would
                // already be adjusted. If the user went back using browser navigation, then
                // we need to adjust the internal nav stack.
                val newStackIndex: Int = window.history.state.asDynamic().index.unsafeCast<Int>()
                Napier.d("$logPrefix: user went back. New stack index = $newStackIndex")

                //pop off everything that was on top (if anything)
                popOffNavStackFrom(newStackIndex + 1)

                Napier.d("$logPrefix new stack = (${dumpNavStackToString()})")
            }
        }catch(e: Exception) {
            Napier.d("$logPrefix Not an ustad url: ignoring $newUrl")
        }
    }

    override fun getBackStackEntry(viewName: String): UstadBackStackEntry? {
        return navStack.lastOrNull { it.viewName == viewName }
    }

    override fun popBackStack(viewName: String, inclusive: Boolean) {
        val resolvedPopViewname = when(viewName) {
            UstadView.ROOT_DEST -> RedirectView.VIEW_NAME
            UstadView.CURRENT_DEST -> navStack.lastOrNull()?.viewName ?: RedirectView.VIEW_NAME
            else -> viewName
        }

        val viewNameIndex = max(navStack.indexOfLast { it.viewName == resolvedPopViewname }, 0)

        //When popping off the stack, we are always at the top of the stack
        // e.g. currentIndex must equal (navStack.size-1)
        val deltaIndex = (navStack.size - 1) - viewNameIndex
        val numToGoBack = min(if(inclusive) {
            deltaIndex + 1
        }else {
            deltaIndex
        }, navStack.size)

        // Remove from the internal nav stack.
        Napier.d("$logPrefix: popBackStack to: $viewName (inclusive = $inclusive) " +
            "go back $numToGoBack steps")
        popOffNavStackFrom(navStack.size - numToGoBack)
        if(numToGoBack != 0) {
            window.history.go(numToGoBack * -1)
        }
    }

    override fun navigate(
        viewName: String,
        args: Map<String, String>,
        goOptions: UstadMobileSystemCommon.UstadGoOptions
    ) {
        Napier.d("$logPrefix navigate to $viewName popUpTo='${goOptions.popUpToViewName}' " +
            "(inclusive=${goOptions.popUpToInclusive})")
        val popUpToViewName = goOptions.popUpToViewName
        if(popUpToViewName != null)
            popBackStack(popUpToViewName, goOptions.popUpToInclusive)

        navigateInternal(viewName, args.toMutableMap(),  0)
    }

    private fun navigateInternal(
        viewName: String,
        args: MutableMap<String, String>,
        stepsToGoBackInHistory: Int = 0
    ){
        val params = when {
            args.isEmpty() -> ""
            else -> "?${args.toUrlQueryString()}"
        }

        if(stepsToGoBackInHistory < 0){
            window.history.go(stepsToGoBackInHistory)
        }else{
            window.location.assign("#/$viewName$params")
        }
    }

    fun navigateUp(): Boolean {
        window.history.go(-1)
        return true
    }

    companion object {

        const val PREFIX_STORAGE_KEY = "session_stack_"

        const val KEY_LAST_SHOWN_POSITION = "last_shown_position"
    }
}