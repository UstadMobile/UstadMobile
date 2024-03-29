package com.ustadmobile.core.components

import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.core.impl.nav.NavCommand
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.impl.nav.PopNavCommand
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.toUrlQueryString
import remix.run.router.Location
import io.github.aakira.napier.Napier
import js.objects.jso
import kotlinx.browser.sessionStorage
import kotlinx.coroutines.*
import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set
import react.*
import react.router.NavigateFunction
import react.router.NavigateOptions
import react.router.useLocation
import react.router.useNavigate
import kotlin.js.Json
import kotlin.js.json

const val KEY_NAV_CONTROLLER_POPUPTO_PAGE = "navControllerPopUpTo"
const val KEY_NAV_CONTROLLER_POPUPTO_INCLUSIVE = "navControllerPopUpToInc"
const val KEY_NAV_CONTROLLER_CLEAR_STACK = "navControllerClearStack"
const val KEY_NAV_CONTROLLER_NAVTO_AFTER_POP = "navControllerGoToAfterPop"
const val KEY_HAVHOST_ROOT_KEY = "navHostRoot"
const val KEY_NAV_CONTROLLER_STACK_CLEARED = "navControllerStackCleared"
const val KEY_NAVCONTROLLER_HIT_POPUP_TO_TARGET = "navHostHitPopupto"

const val NAVHOST_CLEARSTACK_VIEWNAME = "ClearStack"


/**
 * The NavHost makes it possible to an use the Android Jetpack style popUpTo screen option so that we
 * can pop entries off the history stack as required to reach a certain screen.
 *
 * Examples:
 *
 * When the user goes from ListScreen to make a new item, then edits the item, and saves it,
 * they are brought to the detail screen for that item. When they click back, they should go back
 * directly to the list screen. The navigation from the new item to the detail screen will use the
 * option popUpTo = CURRENT_DEST, popUpToInclusive = true
 *
 * The user is in the list of students in a class. They navigate to the person list to pick a person.
 * This screen navigates to the enrolment edit screen for the enrolment details to be filled in. The
 * user should return to the student list once this is done. If the user clicks back, they should go
 * back to the screen they were at before the student list.
 *
 * When the user logs in as a new user or switches accounts, the entire history stack should be
 * cleared. The stack could contain screens that the new user does not have permission to access.
 *
 * How it works:
 *
 * Navigate with popUpTo:
 *
 * useNavControllerEffect will save options (including the popUpTo target) into SessionStorage.
 * The effect hook will run once for each page. It will navigate back until it finds the popUpTo
 * target. The component children will be hidden whilst this is going on for efficiency.
 *
 * Clear history stack:
 * The key of the first entry will be saved when first run as the firstEntryKey. The NavHost will
 * navigate backwards until it reaches this key.  Once it reaches this key, it will navigate forward
 * to ClearStack (an empty Component). This will clear everything that was on top of the stack.
 * It will then navigate back again, and replace the root page with the destination. This is needed
 * to block forward navigation that would access
 *
 */

//As per https://github.com/remix-run/history/blob/main/docs/api-reference.md#location.key
// the first key will always be "default"

/**
 * the location.key of the first entry in the history stack. This is used to spot when we have
 * reached the end of the stack.
 *
 * The initial location key will be "default" (as per
 * https://github.com/remix-run/history/blob/main/docs/api-reference.md#location.key ).
 *
 * If/when we use navigation with the replace option, then the key that represents the first entry
 * in the stack will change. If replace navigation is used when on the firstKey, the state
 * key "isNewFirstLocation" must be set. The NavHost effect will then update firstLocationKey
 */
private var firstLocationKey: String
    get() = web.storage.sessionStorage.getItem("firstLocationKey") ?: "default"
    set(value) {
        web.storage.sessionStorage.setItem("firstLocationKey", value)
    }

fun NavigateOptions.setIsNewFirstLocation(fromLocation: Location<*>) {
    if(replace == true && fromLocation.key == firstLocationKey) {
        state = json("isNewFirstLocation" to true)
    }
}

private fun Storage.clearNavHostCommands() {
    removeItem(KEY_NAV_CONTROLLER_POPUPTO_PAGE)
    removeItem(KEY_NAV_CONTROLLER_POPUPTO_INCLUSIVE)
    removeItem(KEY_NAV_CONTROLLER_CLEAR_STACK)
    removeItem(KEY_NAV_CONTROLLER_NAVTO_AFTER_POP)
    removeItem(KEY_HAVHOST_ROOT_KEY)
}

/**
 * NavHostFunction will execute a NavCommand. These are collected from the ViewModel's
 * CommandFlowUstadNavController flow.
 *
 * If no pop or stack clearing is required, the navigation will be executed immediately using the
 * normal NavigateFunction. If popping the stack is required, then the children will be hidden, and
 * we use the NavigateFunction to start going back.
 */
class NavHostFunction(
    private val navigateFn: NavigateFunction,
    private val location: Location<*>,
    private val onHideChildren: () -> Unit,
)  {

    operator fun invoke(cmd: NavCommand) {
        fun String.effectivePopUpTo(): String {
            return if(this == UstadView.CURRENT_DEST) {
                location.ustadViewName
            }else {
                this
            }
        }

        when(cmd) {
            is NavigateNavCommand -> {
                val popUpToView = cmd.goOptions.popUpToViewName?.effectivePopUpTo()

                if((popUpToView == null || popUpToView == location.ustadViewName) &&
                    (!cmd.goOptions.clearStack || location.key == firstLocationKey)
                ) {
                    /*
                     * When there is no stack popping:
                     * popUpToView == null
                     * popUpToView is the current view, and popUpToInclusive = false (edge case)
                     * AND clearStack is false, or there is no history remaining (therefor location.key == firstLocationKey)
                     */
                    Napier.d("NavHostFunction: go to /${cmd.viewName}?${cmd.args.toUrlQueryString()}")
                    val replaceNav = popUpToView == location.ustadViewName && cmd.goOptions.popUpToInclusive
                        || cmd.goOptions.clearStack

                    navigateFn.invoke("/${cmd.viewName}?${cmd.args.toUrlQueryString()}", jso {
                        replace = replaceNav
                        setIsNewFirstLocation(fromLocation = location)
                    })
                }else {
                    Napier.d("NavHostFunction: pop, then go /${cmd.viewName}?${cmd.args.toUrlQueryString()}")
                    sessionStorage.clearNavHostCommands()
                    sessionStorage.removeItem(KEY_NAV_CONTROLLER_STACK_CLEARED)
                    sessionStorage.removeItem(KEY_NAVCONTROLLER_HIT_POPUP_TO_TARGET)

                    if(popUpToView != null) {
                        sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_PAGE] = popUpToView
                    }

                    sessionStorage[KEY_NAV_CONTROLLER_CLEAR_STACK] = cmd.goOptions.clearStack.toString()
                    sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_INCLUSIVE] = cmd.goOptions.popUpToInclusive.toString()
                    sessionStorage[KEY_NAV_CONTROLLER_NAVTO_AFTER_POP] = "/${cmd.viewName}?${cmd.args.toUrlQueryString()}"
                    onHideChildren()
                    navigateFn.invoke(-1)
                }
            }
            is PopNavCommand -> {
                val popUpTo = cmd.viewName.effectivePopUpTo()

                if(popUpTo == location.ustadViewName && cmd.inclusive) {
                    Napier.d("NavHostFunction: pop to current viewname $popUpTo (inclusive) " +
                            "e.g. go back one")
                    navigateFn(-1)
                }else if(popUpTo != location.ustadViewName) {
                    Napier.d("NavHostFunction: pop to $popUpTo inclusive = ${cmd.inclusive}")
                    sessionStorage.removeItem(KEY_NAVCONTROLLER_HIT_POPUP_TO_TARGET)
                    sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_PAGE] = cmd.viewName
                    sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_INCLUSIVE] = cmd.inclusive.toString()
                    onHideChildren()
                    navigateFn.invoke(-1)
                }else {
                    Napier.d("NavHostFunction: pop up to current viewname, inclusive = false?? Nothing to do.")
                }
            }
            else -> {
                Napier.d("NavHostFunction: command ignored: $cmd")
                //do nothing
            }
        }
    }

}

val NavHostContext = createContext<NavHostFunction>()

val NavHost = FC<PropsWithChildren> { props ->

    var showChildren by useState(
        sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_PAGE] == null
            && sessionStorage[KEY_NAV_CONTROLLER_CLEAR_STACK] != false.toString()
    )

    val navigateFn = useNavigate()

    val location = useLocation()

    val navHostFunction = useMemo(navigateFn, location) {
        NavHostFunction(navigateFn, location) { showChildren = false }
    }

    NavHostContext(navHostFunction) {


        /*
         * If we are already at the end of the history, calling navigateFunction(-1) will have no
         * effect
         *
         * This is used as a timeout. It will then initiate navigation to the Clear Stack placeholder.
         */
        fun CoroutineScope.launchClearStackTimeout() = launch {
            delay(250)
            Napier.v("NavHost: action: clearStackTimeout: apparently at the end of history. go to clearstack")
            sessionStorage[KEY_NAV_CONTROLLER_CLEAR_STACK] = true.toString()
            navigateFn.invoke("/$NAVHOST_CLEARSTACK_VIEWNAME")
        }



        //This should be cancelled - needs to use state and effect
        val coroutineScope = useMemo(dependencies = emptyArray()) {
            CoroutineScope(Dispatchers.Main + Job())
        }

        var navTimeoutJob: Job? by useState { null }

        useEffect(dependencies = arrayOf(location.key)) {
            console.log("NavHost: key = ${location.key}")

            val isNewFirstLocation = location.state?.unsafeCast<Json>()?.get("isNewFirstLocation") == true
            if(isNewFirstLocation) {
                console.log("NavHost: New first location key = ${location.key}")
                firstLocationKey = location.key
            }

            navTimeoutJob?.cancel()

            val popupToTarget = sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_PAGE]
            val navToAfterPop = sessionStorage[KEY_NAV_CONTROLLER_NAVTO_AFTER_POP]
            val popUpToInclusive = sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_INCLUSIVE]?.toBoolean() ?: false
            val clearStack = sessionStorage[KEY_NAV_CONTROLLER_CLEAR_STACK]?.toBoolean() ?: false
            val clearStackHitPlaceholder  = sessionStorage[KEY_NAV_CONTROLLER_STACK_CLEARED]?.toBoolean() ?: false
            val popUpToHitDestination = sessionStorage[KEY_NAVCONTROLLER_HIT_POPUP_TO_TARGET]?.toBoolean() ?: false
            Napier.v("NavHost: useEffect check: key = ${location.key} " +
                    "current viewname = ${location.ustadViewName} " +
                    "popUpToTarget = $popupToTarget " +
                    "navtoAfterPop = $navToAfterPop " +
                    "popUpToInclusive = $popUpToInclusive " +
                    "clearStack = $clearStack " +
                    "clearStackHitPlaceholder = $clearStackHitPlaceholder " +
                    "popUpToHitDestination = $popUpToHitDestination")

            when {
                //We don't have any other pop or stack operation pending, show NavHost children
                popupToTarget == null && !clearStack && location.pathname != "/$NAVHOST_CLEARSTACK_VIEWNAME" -> {
                    Napier.v("NavHost: action: no pop or other operation pending, show children")
                    showChildren = true
                }

                //We have reached the popupTo destination, but popUpToInclusive is set, so we need to pop
                //once more, then execute any pending navigation
                popupToTarget == location.ustadViewName && popUpToInclusive-> {
                    Napier.v("NavHost: action: reached popUpTo destination, popUpToInclusive is set, pop once more")
                    showChildren = false
                    sessionStorage[KEY_NAVCONTROLLER_HIT_POPUP_TO_TARGET] = true.toString()
                    navigateFn.invoke(-1)

                    if(navToAfterPop != null) {
                        navTimeoutJob = coroutineScope.launchClearStackTimeout()
                    }
                }

                //We have reached the popUpTo destination (and popUpToInclusive = false), popUpTo
                // was provided, and the destination was hit. Now it is time to execute forward nav
                popupToTarget == location.ustadViewName || popUpToHitDestination -> {
                    Napier.v("NavHost: action: reached popupToDestination with inclusive = false, " +
                            "or inclusive was true and destination was hit")
                    sessionStorage.clearNavHostCommands()
                    if(navToAfterPop != null){
                        //Popping is done, navigate to the final destination
                        navigateFn.invoke(navToAfterPop)
                    }else {
                        //There is no further navigation, so show the children
                        showChildren = true
                    }
                }

                //ClearStack was set, we have now reached the first entry in the history, but we did not
                // yet navigate to the dummy placeholder (NAVHOST_CLEARSTACK_VIEWNAME) to clear the
                // top off the stack and prevent forward navigation
                clearStack && location.key == firstLocationKey && !clearStackHitPlaceholder -> {
                    Napier.v("NavHost: action: clearstack was set, reached first entry in history, " +
                            "but did not yet navigate to dummy placeholder. Going to /$NAVHOST_CLEARSTACK_VIEWNAME")
                    navigateFn.invoke("/$NAVHOST_CLEARSTACK_VIEWNAME")
                }

                //This is the dummy placeholder (NAVHOST_CLEARSTACK_VIEWNAME), mark that it has been hit
                //then navigate back
                location.pathname == "/$NAVHOST_CLEARSTACK_VIEWNAME" -> {
                    Napier.v("NavHost: action: reached stack clear dummy placeholder. Going back")
                    sessionStorage[KEY_NAV_CONTROLLER_STACK_CLEARED] = true.toString()
                    navigateFn.invoke(-1)
                }

                //The stack was set to be cleared, and the placeholder has been hit. Navigate to the
                // destination
                clearStack && clearStackHitPlaceholder -> {
                    Napier.v("NavHost: action: clearStack was set and dummy placeholder was hit")
                    sessionStorage.clearNavHostCommands()
                    if(navToAfterPop != null) {
                        navigateFn.invoke(navToAfterPop, jso {
                            replace = true
                            setIsNewFirstLocation(fromLocation = location)
                        })
                    }
                }

                //We need to continue popping off history - there is a popUpToTarget, but we have not
                // reached it yet, or clearStack has been set and we have not yet cleared the stack
                else -> {
                    Napier.v("NavHost: action: need to continue history popping")
                    showChildren = false

                    //Handling this needs to be double checked - e.g. if popUpTo is set to something not in the stack
                    navigateFn.takeIf { location.key != firstLocationKey }?.invoke(-1)

                    //Set a timeout in case we have hit the go back limit, in which case navigateFn(-1)
                    // will have no effect.
                    navTimeoutJob = coroutineScope.launchClearStackTimeout()
                }
            }
        }

        if(showChildren) {
            + props.children
        }
    }
}
