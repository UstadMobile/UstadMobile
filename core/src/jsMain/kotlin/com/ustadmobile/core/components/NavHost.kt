package com.ustadmobile.core.components

import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.core.impl.nav.NavCommand
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.impl.nav.PopNavCommand
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.toUrlQueryString
import history.Location
import io.github.aakira.napier.Napier
import js.core.jso
import kotlinx.browser.sessionStorage
import kotlinx.coroutines.*
import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set
import react.*
import react.router.NavigateFunction
import react.router.useLocation
import react.router.useNavigate

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
    private val location: Location,
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
                    !cmd.goOptions.clearStack
                ) {
                    /*
                     * When there is no stack popping:
                     * popUpToView == null
                     * popUpToView is the current view, and popUpToInclusive = false (edge case)
                     * AND clearStack is false in both cases
                     */
                    Napier.d("NavHostFunction: go to /${cmd.viewName}?${cmd.args.toUrlQueryString()}")
                    navigateFn.invoke("/${cmd.viewName}?${cmd.args.toUrlQueryString()}", jso {
                        replace = popUpToView == location.ustadViewName && cmd.goOptions.popUpToInclusive
                    })
                }else {
                    Napier.d("NavHostFunction: pop, then go /${cmd.viewName}?${cmd.args.toUrlQueryString()}")
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
        fun Storage.clearNavHostCommands() {
            removeItem(KEY_NAV_CONTROLLER_POPUPTO_PAGE)
            removeItem(KEY_NAV_CONTROLLER_POPUPTO_INCLUSIVE)
            removeItem(KEY_NAV_CONTROLLER_CLEAR_STACK)
            removeItem(KEY_NAV_CONTROLLER_NAVTO_AFTER_POP)
            removeItem(KEY_HAVHOST_ROOT_KEY)
        }

        /*
         * If we are already at the end of the history, calling navigateFunction(-1) will have no
         * effect
         *
         * This is used as a timeout. It will then initiate navigation to the Clear Stack placeholder.
         */
        fun CoroutineScope.launchClearStackTimeout() = launch {
            delay(250)
            sessionStorage[KEY_NAV_CONTROLLER_CLEAR_STACK] = true.toString()
            navigateFn.invoke("/$NAVHOST_CLEARSTACK_VIEWNAME")
        }


        val firstEntryKey = useMemo(dependencies = emptyArray()) {
            sessionStorage[KEY_HAVHOST_ROOT_KEY]
                ?: location.key.also { sessionStorage[KEY_HAVHOST_ROOT_KEY] = it }
        }


        //This should be cancelled - needs to use state and effect
        val coroutineScope = useMemo(dependencies = emptyArray()) {
            CoroutineScope(Dispatchers.Main + Job())
        }

        var navTimeoutJob: Job? by useState { null }

        useEffect(dependencies = arrayOf(location.key)) {
            navTimeoutJob?.cancel()

            val popupToTarget = sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_PAGE]
            val navToAfterPop = sessionStorage[KEY_NAV_CONTROLLER_NAVTO_AFTER_POP]
            val popUpToInclusive = sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_INCLUSIVE]?.toBoolean() ?: false
            val clearStack = sessionStorage[KEY_NAV_CONTROLLER_CLEAR_STACK]?.toBoolean() ?: false
            val clearStackHitPlaceholder  = sessionStorage[KEY_NAV_CONTROLLER_STACK_CLEARED]?.toBoolean() ?: false
            val popUpToHitDestination = sessionStorage[KEY_NAVCONTROLLER_HIT_POPUP_TO_TARGET]?.toBoolean() ?: false
            Napier.d("NavHost: useEffect check: key = ${location.key} " +
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
                    showChildren = true
                }

                //We have reached the popupTo destination, but popUpToInclusive is set, so we need to pop
                //once more, then execute any pending navigation
                popupToTarget == location.ustadViewName && popUpToInclusive-> {
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
                    sessionStorage.clearNavHostCommands()
                    if(navToAfterPop != null){
                        navigateFn.invoke(navToAfterPop)
                    }
                }

                //ClearStack was set, we have now reached the first entry in the history, but we did not
                // yet navigate to the dummy placeholder (NAVHOST_CLEARSTACK_VIEWNAME) to clear the
                // top off the stack and prevent forward navigation
                clearStack && location.key == firstEntryKey && !clearStackHitPlaceholder -> {
                    navigateFn.invoke("/$NAVHOST_CLEARSTACK_VIEWNAME")
                }

                //This is the dummy placeholder (NAVHOST_CLEARSTACK_VIEWNAME), mark that it has been hit
                //then navigate back
                location.pathname == "/$NAVHOST_CLEARSTACK_VIEWNAME" -> {
                    sessionStorage[KEY_NAV_CONTROLLER_STACK_CLEARED] = true.toString()
                    navigateFn.invoke(-1)
                }

                //The stack was set to be cleared, and the placeholder has been hit. Navigate to the
                // destination
                clearStack && clearStackHitPlaceholder -> {
                    sessionStorage.clearNavHostCommands()
                    if(navToAfterPop != null) {
                        navigateFn.invoke(navToAfterPop, jso {
                            replace = true
                        })
                    }
                }

                //We need to continue popping off history - there is a popUpToTarget, but we have not
                // reached it yet, or clearStack has been set and we have not yet cleared the stack
                else -> {
                    showChildren = false
                    navigateFn.invoke(-1)

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
