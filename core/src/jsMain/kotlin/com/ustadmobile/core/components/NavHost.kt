package com.ustadmobile.core.components

import com.ustadmobile.core.hooks.ustadViewName
import history.Location
import kotlinx.browser.sessionStorage
import kotlinx.coroutines.*
import kotlinx.js.jso
import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set
import react.*
import react.router.useLocation
import react.router.useNavigate

const val KEY_NAV_CONTROLLER_POPUPTO_PAGE = "navControllerPopUpTo"
const val KEY_NAV_CONTROLLER_POPUPTO_INCLUSIVE = "navControllerPopUpToInc"
const val KEY_NAV_CONTROLLER_CLEAR_STACK = "navControllerClearStack"
const val KEY_NAV_CONTROLLER_NAVTO_AFTER_POP = "navControllerGoToAfterPop"
const val KEY_HAVHOST_ROOT_KEY = "navHostRoot"
const val KEY_NAV_CONTROLLER_STACK_CLEARED = "navControllerStackCleared"
const val KEY_NAVCONTROLLER_HIT_POPUP_TO_TARGET = "navHostHitPopupto"

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

//Still needed: work on handling edge case when
const val NAVHOST_CLEARSTACK_VIEWNAME = "ClearStack"

val NavHost = FC<PropsWithChildren> { props ->

    fun Storage.clearNavHostCommands() {
        removeItem(KEY_NAV_CONTROLLER_POPUPTO_PAGE)
        removeItem(KEY_NAV_CONTROLLER_POPUPTO_INCLUSIVE)
        removeItem(KEY_NAV_CONTROLLER_CLEAR_STACK)
        removeItem(KEY_NAV_CONTROLLER_NAVTO_AFTER_POP)
        removeItem(KEY_HAVHOST_ROOT_KEY)
    }

    val navigateFn = useNavigate()
    val location: Location = useLocation()
    val firstEntryKey = useMemo(dependencies = emptyArray()) {
        sessionStorage[KEY_HAVHOST_ROOT_KEY]
            ?: location.key.also { sessionStorage[KEY_HAVHOST_ROOT_KEY] = it }
    }

    var showChildren by useState(
        sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_PAGE] == null
            && sessionStorage[KEY_NAV_CONTROLLER_CLEAR_STACK] != false.toString()
    )

    val coroutineScope = useMemo(dependencies = emptyArray()) {
        CoroutineScope(Dispatchers.Main + Job())
    }

    var navTimeoutJob: Job? by useState { null }

    useEffect(dependencies = arrayOf(location.key)) {
        navTimeoutJob?.cancel()
        console.log("NavHost: useEffect check: key = ${location.key}")
        val popupToTarget = sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_PAGE]
        val navToAfterPop = sessionStorage[KEY_NAV_CONTROLLER_NAVTO_AFTER_POP]
        val popUpToInclusive = sessionStorage[KEY_NAV_CONTROLLER_POPUPTO_INCLUSIVE]?.toBoolean() ?: false
        val clearStack = sessionStorage[KEY_NAV_CONTROLLER_CLEAR_STACK]?.toBoolean() ?: false
        val clearStackHitPlaceholder  = sessionStorage[KEY_NAV_CONTROLLER_STACK_CLEARED]?.toBoolean() ?: false
        val popUpToHitDestination = sessionStorage[KEY_NAVCONTROLLER_HIT_POPUP_TO_TARGET]?.toBoolean() ?: false

        when {
            //We don't have any other pop or stack operation pending, show NavHost children
            popupToTarget == null && !clearStack && location.pathname != "/$NAVHOST_CLEARSTACK_VIEWNAME" -> {
                showChildren = true
            }

            //We have reached the popupTo destination, but popUpToInclusive is set, so we need to pop
            //once more, then execute any pending navigation
            popupToTarget == location.ustadViewName && popUpToInclusive-> {
                sessionStorage[KEY_NAVCONTROLLER_HIT_POPUP_TO_TARGET] = true.toString()
                navigateFn(-1)

                //if stack is empty, then just do the navigation anyway.
            }

            //We have reached the popUpTo destination (and popUpToInclusive = false), popUpTo
            // was provided, and the destination was hit. Now it is time to execute forward nav
            popupToTarget == location.ustadViewName || popUpToHitDestination -> {
                sessionStorage.clearNavHostCommands()
                if(navToAfterPop != null){
                    navigateFn(navToAfterPop)
                }
            }

            //ClearStack was set, we have now reached the first entry in the history, but we did not
            // yet navigate to the dummy placeholder (NAVHOST_CLEARSTACK_VIEWNAME) to clear the
            // top off the stack and prevent forward navigation
            clearStack && location.key == firstEntryKey && !clearStackHitPlaceholder -> {
                navigateFn("/$NAVHOST_CLEARSTACK_VIEWNAME")
            }

            //This is the dummy placeholder (NAVHOST_CLEARSTACK_VIEWNAME), mark that it has been hit
            //then navigate back
            location.pathname == "/$NAVHOST_CLEARSTACK_VIEWNAME" -> {
                sessionStorage[KEY_NAV_CONTROLLER_STACK_CLEARED] = true.toString()
                navigateFn(-1)
            }

            //The stack was set to be cleared, and the placeholder has been hit. Navigate to the
            // destination
            clearStack && clearStackHitPlaceholder -> {
                sessionStorage.clearNavHostCommands()
                if(navToAfterPop != null) {
                    navigateFn(navToAfterPop, jso {
                        replace = true
                    })
                }
            }

            //We need to continue popping off history - there is a popUpToTarget, but we have not
            // reached it yet, or clearStack has been set and we have not yet cleared the stack
            else -> {
                navigateFn(-1)

                //if stack is empty, set clearStack = true,
                navTimeoutJob = coroutineScope.launch {
                    delay(250)
                    sessionStorage[KEY_NAV_CONTROLLER_CLEAR_STACK] = true.toString()
                    navigateFn("/$NAVHOST_CLEARSTACK_VIEWNAME")
                }
            }
        }
    }

    if(showChildren) {
        + props.children
    }
}