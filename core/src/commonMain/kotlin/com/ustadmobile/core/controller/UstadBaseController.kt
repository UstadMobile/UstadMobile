/*
    This file is part of Ustad Mobile.
    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.
    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:
    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).
    If you want a commercial license to remove the above restriction you must
    contact us.
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 */
package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.*
import com.ustadmobile.core.impl.nav.UstadBackStackEntry
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.impl.nav.navigateToErrorScreen
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UmPlatformUtil
import com.ustadmobile.core.util.ext.putResultDestInfo
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_KEY
import com.ustadmobile.core.view.UstadView.Companion.ARG_RESULT_DEST_VIEWNAME
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.kodein.di.*
import kotlin.js.JsName

/**
 * Base Controller that provides key functionality for any view :
 * it contains the data or a reference to the data and handles events and
 * logic.
 *
 * @author mike
 */
abstract class UstadBaseController<V : UstadView>(
    override val context: Any,
    protected val arguments: Map<String, String>,
    val view: V,
    override val di: DI,
    open val activeSessionRequired: Boolean = true
): UmLifecycleOwner, DIAware {

    private val lifecycleListeners = mutableListOf<UmLifecycleListener>()

    private val lifecycleStatus = atomic(0)

    private var created: Boolean = false

    protected var savedState: Map<String, String>? = null
        private set

    /**
     * The NavController might be null if being used in screens outside the normal navigation flow
     * e.g. OnboardingActivity, EpubActivity, etc.
     *
     * Where using the NavController is required, use requireNavController()
     */
    protected val ustadNavController: UstadNavController? by instanceOrNull()

    private var backStackEntry: UstadBackStackEntry? = null

    /**
     * The last time the contents of this presenter was saved to the back stack saved state handle.
     * Helps to avoid duplicate save work.
     */
    var lastStateSaveTime: Long = 0
        private set

    /**
     * There are two possible scenarios for using a presenter:
     *
     *  1) Top level view e.g. ClazzDetail reached by navigation with the navcontroller. There is
     *     only one top level view active at a time.
     *  2) Embedded and not reached via the navcontroller (e.g. ClazzDetailOverview which is
     *     displayed as a tab).
     *
     *  This can be used to control which presenters are observing for the end of a user session to
     *  avoid a situation where multiple presenters react to a user being logged out, to decide
     *  whether or not the presenter should observe for return results, etc.
     */
    protected open val navChild: Boolean
        get() = arguments[UstadView.ARG_NAV_CHILD]?.toBoolean() == true

    /**
     * A coroutine scope that is tied to the lifecycle of the presenter. It will be canceled after
     * onDestroy. This avoids longer-running async processes accidentally interacting with a view
     * that has been destroyed etc.
     */
    protected val presenterScope: CoroutineScope by instance(tag = DiTag.TAG_PRESENTER_COROUTINE_SCOPE)

    private val activeSessionObserver = DoorObserver<UserSessionWithPersonAndEndpoint?> {
        if(it == null) {
            presenterScope.launch {
                navigateToStartNewUserSession()
            }
        }
    }

    fun requireNavController(): UstadNavController {
        return ustadNavController
            ?: throw IllegalStateException("RequireNavController: ustadNavController is null")
    }

    /**
     * Handle when the presenter is created. Analogous to Android's onCreate
     *
     * @param savedState savedState if any
     */
    @JsName("onCreate")
    open fun onCreate(savedState: Map<String, String>?) {
        if(created)
            throw IllegalStateException("onCreate must be called ONCE AND ONLY ONCE! It has already been called")

        created = true
        this.savedState = savedState
        backStackEntry = ustadNavController?.currentBackStackEntry

        for (listener in lifecycleListeners) {
            listener.onLifecycleCreate(this)
        }


        lifecycleStatus.value = CREATED

        if(activeSessionRequired && !navChild) {
            val accountManager: UstadAccountManager = direct.instance()
            val lifecycleOwner: DoorLifecycleOwner? = direct.instanceOrNull()
            if(lifecycleOwner != null) {
                accountManager.activeUserSessionLive.observe(lifecycleOwner, activeSessionObserver)
            }
        }
    }

    /**
     * Handle when the presenter is about to become visible. Analogous to Android's onStart
     */
    open fun onStart() {

        for (listener in lifecycleListeners) {
            listener.onLifecycleStart(this)
        }


        lifecycleStatus.value = STARTED
    }

    /**
     * Handle when the presenter has become visible. Analogous to Android's onResume
     */
    @JsName("onResume")
    open fun onResume() {
        for (listener in lifecycleListeners) {
            listener.onLifecycleResume(this)
        }


        lifecycleStatus.value = RESUMED
    }

    open fun onPause() {
        for (listener in lifecycleListeners) {
            listener.onLifecyclePause(this)
        }


        lifecycleStatus.value = PAUSED
    }

    /**
     * Handle when the presenter is no longer visible. Analogous to Android's onStop
     */
    open fun onStop() {
        for (listener in lifecycleListeners) {
            listener.onLifecycleStop(this)
        }

        lifecycleStatus.value = STOPPED
    }

    /**
     * Called when the view is destroyed and removed from memory. Analogous to Android's onDestroy
     */
    open fun onDestroy() {
        for (listener in lifecycleListeners) {
            listener.onLifecycleDestroy(this)
        }


        lifecycleStatus.value = DESTROYED
        backStackEntry = null
    }

    override fun addLifecycleListener(listener: UmLifecycleListener) {
        lifecycleListeners.add(listener)

        when (lifecycleStatus.value) {
            CREATED -> listener.onLifecycleCreate(this)

            STARTED -> listener.onLifecycleStart(this)

            RESUMED -> listener.onLifecycleResume(this)

            PAUSED -> listener.onLifecyclePause(this)

            STOPPED -> listener.onLifecycleStop(this)

            DESTROYED -> listener.onLifecycleDestroy(this)
        }
    }

    override fun removeLifecycleListener(listener: UmLifecycleListener) {
        lifecycleListeners.remove(listener)
    }

    /**
     * This is used in roughly the same way as on Android. It can be called by the underlying
     * platform (e.g. Androids own onSaveInstanceState when the app is being destroyed) or
     * directly by the presenter e.g. to save the state of the entity before a user navigates away
     * to pick something.
     */
    open fun onSaveInstanceState(savedState: MutableMap<String, String>) {

    }

    /**
     * Save state to the nav controller savedStateHandle for the current back stack entry.
     * This will call onSaveInstanceState and save the resulting string map into the savedstatehandle.
     */
    open fun saveStateToNavController() {
        val stateMap = mutableMapOf<String, String>()
        onSaveInstanceState(stateMap)

        val stateHandle = backStackEntry?.savedStateHandle
        if(stateHandle != null) {
            stateMap.forEach {
                stateHandle[it.key] = it.value
            }
        }

        lastStateSaveTime = systemTimeInMillis()
    }


    /**
     * Save the result to the savedStateHandle on the backstack as directed by the current
     * arguments. ARG_RESULT_DEST_VIEWNAME and ARG_RESULT_DEST_KEY will specify which entry on
     * the backstack to lookup, and what key name should be used in the saved state handle once
     * it has been looked up.
     */
    fun finishWithResult(result: String) {
        val saveToViewName = arguments[ARG_RESULT_DEST_VIEWNAME]
        val saveToKey = arguments[ARG_RESULT_DEST_KEY]

        if(saveToViewName != null && saveToKey != null){
            val destBackStackEntry = requireNavController().getBackStackEntry(saveToViewName)
            destBackStackEntry?.savedStateHandle?.set(saveToKey, result)

            requireNavController().popBackStack(saveToViewName, false)
        }else {
            requireNavController().popBackStack(UstadView.CURRENT_DEST, true)
        }
    }


    fun requireSavedStateHandle(): UstadSavedStateHandle {
        return requireNavController().currentBackStackEntry?.savedStateHandle
            ?: throw IllegalStateException("Require saved state handle: no current back stack entry")
    }

    private fun <T: Any> NavigateForResultOptions<T>.putPresenterResultDestInfo() {
        val currentBackStackEntryVal = backStackEntry
        val effectiveResultKey = destinationResultKey ?: entityClass.simpleName
            ?: throw IllegalArgumentException("putPresenterResultDestInfo: no destination key and no class name")

        if(currentBackStackEntryVal != null) {
            arguments.putResultDestInfo(currentBackStackEntryVal, effectiveResultKey,
                overwriteDestination)
        }
    }

    /**
     * Navigate to another screen for purposes of returning a result. This allows the initiator
     * (e.g. this presenter) to take the user to another screen for picking another entity (e.g.
     * from a Class Member screen to the Person List screen to pick someone to add to the class.
     * This process can continue via multiple screens (e.g. in the person list screen, the user
     * might choose to add a new person, which is then returned to the original initiator).
     *
     * This works by:
     *
     * 1) The initiator passes arguments UstadView.ARG_RESULT_DEST_VIEWNAME and UstadView.ARG_RESULT_DEST_KEY
     *    that specify the destination view name for the result and a key name.
     *
     * 2) Edit/list screens use the arguments received to lookup the SavedState handle for the
     *    initiator and then saves the result into the specified key of the saved state handle. The
     *    edit/list screen then pops the back stack
     *
     * 3) The initiator observes the saved state handle to watch the incoming result
     *
     * This is roughly modeled on the recommended process for Android NavController as per
     *  https://developer.android.com/guide/navigation/navigation-programmatic#returning_a_result
     */
    fun <T: Any> navigateForResult(options: NavigateForResultOptions<T>) {
        saveStateToNavController()
        options.putPresenterResultDestInfo()

        val currentEntityValue = options.currentEntityValue
        if(currentEntityValue != null) {
            options.arguments[UstadEditView.ARG_ENTITY_JSON] = safeStringify(
                di, options.serializationStrategy, options.entityClass,currentEntityValue)
        }

        requireNavController().navigate(options.destinationViewName, options.arguments)
    }

    /**
     * Navigate to the error screen. Pass details of the exception so it is recorded and displayed
     * to the user accordingly.
     */
    fun navigateToErrorScreen(exception: Exception) {
        requireNavController().navigateToErrorScreen(exception, di, context)
    }

    /**
     * Navigate when the user does not have an active session and needs to start one. E.g.
     * When the user logs out of the currently active account, when the app starts and there
     * is no currently active account, or when the active session is terminated remotely (e.g. due
     * to password change or parental consent change).
     *
     * In this case the backstack will be cleared. Navigation will be as follows:
     * If the user has remaining sessions on the device:
     *   Navigate to AccountList to select an existing session (with the option to start a new session)
     *
     * If there are no remaining sessions and server selection is allowed:
     *   Navigate to SiteEnterLink for the user to select what server to connect to
     *
     * If there are no remaining sessions and the app is locked to a single server:
     *   Navigate to Login for the user to login
     */
    suspend fun navigateToStartNewUserSession() {
        val accountManager: UstadAccountManager = direct.instance()
        val impl: UstadMobileSystemImpl = direct.instance()
        val numAccountsRemaining = accountManager.activeSessionCount()
        val canSelectServer = if(UmPlatformUtil.isWeb) false else impl.getAppConfigBoolean(
            AppConfig.KEY_ALLOW_SERVER_SELECTION, context)

        //Wherever the user is going now, we must wipe the backstack
        val goOptions = UstadMobileSystemCommon.UstadGoOptions(
            popUpToViewName = UstadView.ROOT_DEST, popUpToInclusive = false)

        when {
            numAccountsRemaining == 0 && canSelectServer -> {
                impl.go(SiteEnterLinkView.VIEW_NAME, mapOf(), context, goOptions)
            }

            numAccountsRemaining == 0 && !canSelectServer -> {
                impl.go(Login2View.VIEW_NAME, mapOf(), context, goOptions)
            }

            numAccountsRemaining > 0 -> {
                impl.go(
                    AccountListView.VIEW_NAME,
                    mapOf(
                        AccountListView.ARG_ACTIVE_ACCOUNT_MODE to AccountListView.ACTIVE_ACCOUNT_MODE_INLIST,
                        UstadView.ARG_TITLE to impl.getString(MessageID.select_account, context),
                        UstadView.ARG_LISTMODE to ListViewMode.PICKER.toString()),
                    context, goOptions)
            }
        }
    }


    companion object {

        const val NOT_CREATED = 0

        const val CREATED = 1

        const val STARTED = 2

        const val RESUMED = 3

        const val PAUSED = 4

        const val STOPPED = 5

        const val DESTROYED = 6
    }

}
