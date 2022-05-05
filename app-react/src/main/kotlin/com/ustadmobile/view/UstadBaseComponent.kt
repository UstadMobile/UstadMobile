package com.ustadmobile.view

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.core.view.RedirectView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.navigation.RouteManager
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxAppStateManager.getCurrentState
import com.ustadmobile.redux.ReduxSnackBarState
import com.ustadmobile.redux.ReduxToolbarState
import com.ustadmobile.util.*
import io.github.aakira.napier.Napier
import kotlinx.atomicfu.atomic
import kotlinx.browser.window
import kotlinx.coroutines.Runnable
import org.kodein.di.*
import org.w3c.dom.HashChangeEvent
import org.w3c.dom.events.Event
import react.RBuilder
import react.RComponent

abstract class UstadBaseComponent <P: UmProps,S: UmState>(props: P): RComponent<P, S>(props),
    UstadView, DIAware, DoorLifecycleOwner {

    private val lifecycleObservers: MutableList<DoorLifecycleObserver> = concurrentSafeListOf()

    protected val systemImpl : UstadMobileSystemImpl by instance()

    val accountManager: UstadAccountManager by instance()

    var database: UmAppDatabase? = null

    val navController: UstadNavController by instance()

    protected var showAddEntryOptions = false

    private lateinit var progressBarManager: ProgressBarManager

    var searchManager: SearchManager? = null

    var fabManager: FabManager? = null

    protected lateinit var arguments: Map<String, String>

    private val lifecycleStatus = atomic(0)

    private var hashChangeListener:(Event) -> Unit = { (it as HashChangeEvent)
        //Refresh component as argument changes and prevent loading it multiple times
        // This is needed when one view links to the same view (e.g. with different args such as
        // browsing folders etc).

        val viewNamesVal = RouteManager.lookupViewNamesByComponent(this::class)

        if(viewNamesVal != null){
            try {
                val newUstadUrl = UstadUrlComponents.parse(it.newURL)

                if(viewNamesVal.indexOf(newUstadUrl.viewName) != -1  &&
                        viewNamesVal.indexOf(UstadUrlComponents.parse(it.oldURL).viewName) != -1) {
                    Napier.d("UstadBaseComponent: hashChange: trigger onCreateView " +
                        "(oldUrl=${it.oldURL} newUrl=${it.newURL})")
                    arguments = newUstadUrl.arguments
                    onCreateView()
                }
            }catch(e: IllegalArgumentException) {
                Napier.d("old or new url on hash change was not an ustad url:", e)
            }
        }
    }

    val  savedStateHandle: UstadSavedStateHandle?
        get() = navController.currentBackStackEntry?.savedStateHandle

    var ustadComponentTitle: String? = null
        set(value) {
            field = value
            window.setTimeout({
               dispatch(ReduxToolbarState(title = ustadComponentTitle))
            }, MIN_STATE_CHANGE_DELAY_TIME)
        }

    override var loading: Boolean = false
        set(value) {
            field = value
            progressBarManager.progressBarVisibility = value
        }

    override val currentState: Int
        get() = lifecycleStatus.value

    open fun onCreateView(){
        for(observer in lifecycleObservers){
            observer.onStart(this)
        }

        fabManager?.onClickListener = {
            onFabClicked()
        }
        lifecycleStatus.value = DoorLifecycleObserver.STARTED
        database = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_DB)
    }

    open fun onDestroyView(){}

    override fun componentWillMount() {
        window.addEventListener("hashchange",hashChangeListener)
    }

    override fun componentDidMount() {
        Napier.d("UstadBaseComponent: componentDidMount: ${this::class.simpleName}")
        for(observer in lifecycleObservers){
            observer.onStart(this)
        }
        progressBarManager = ProgressBarManager()
        searchManager = SearchManager()
        fabManager = FabManager()

        //Handle both arguments from URL and the ones passed during component rendering i.e from tabs
        arguments = when {
            props.asDynamic().arguments != js("undefined") ->
                props.asDynamic().arguments as Map<String, String>
            else -> urlSearchParamsToMap()
        }
        onCreateView()
    }

    override fun componentDidUpdate(prevProps: P, prevState: S, snapshot: Any) {
        val propsDidChange = props.asDynamic().arguments != js("undefined")
                && !props.asDynamic().arguments.values.equals(prevProps.asDynamic().arguments.values)
        val activeSession = systemImpl.getAppPref(UstadAccountManager.ACCOUNTS_ACTIVE_SESSION_PREFKEY, this)
        val redirected = systemImpl.getAppPref(RedirectView.TAG_REDIRECTED, "false",this).toBoolean()
        val refreshPage = activeSession != null && redirected

        /**
         * Handles tabs behaviour when changing from one tab to another, react components
         * are mounted once and when trying to re-mount it's componentDidUpdate is triggered.
         * This will check and make sure the component has changed by checking if the props has changed
         */
        //22/Apr/22: refreshPage was causing RedirectComponent to run twice.
        // This may need checked.
        if(propsDidChange/* || refreshPage*/){
            Napier.d("UstadBaseComponent: componentDidUpdate: CHANGED: ${this::class.simpleName}")
            if(propsDidChange){
                arguments = props.asDynamic().arguments as Map<String, String>
            }
            if(refreshPage){
                systemImpl.setAppPref(RedirectView.TAG_REDIRECTED, "false", this)
            }
            onCreateView()
        }
    }

    override fun RBuilder.render() {}

    open fun onFabClicked(){}

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        dispatch(ReduxSnackBarState(message, getString(actionMessageId), action))
    }

    override fun runOnUiThread(r: Runnable?) {
        r?.run()
    }

    override val di: DI by DI.lazy {
        extend(getCurrentState().di.instance)
    }

    override fun addObserver(observer: DoorLifecycleObserver) {
        lifecycleObservers.add(observer)
    }

    override fun removeObserver(observer: DoorLifecycleObserver) {
        lifecycleObservers.remove(observer)
    }

    /**
     * Get string from xml language resources
     */
    fun getString(messageId: Int): String {
        return if(messageId == 0) "" else systemImpl.getString(messageId, this)
    }

    /**
     * Get a string with optional label appended to it
     * i.e Field is optional
     */
    fun getStringWithOptionalLabel(messageId: Int): String {
        return getString(messageId)+" (${getString(MessageID.optional)})"
    }

    /**
     * Update state with a delay to make sure we are not updating
     * state when a component is still building
     */
    fun updateUiWithStateChangeDelay(timeOutInMills: Int = MIN_STATE_CHANGE_DELAY_TIME, block:() -> Unit){
        window.setTimeout(block, timeOutInMills)
    }

    override fun componentWillUnmount() {
        for(observer in lifecycleObservers){
            observer.onStop(this)
        }
        lifecycleStatus.value = DoorLifecycleObserver.STOPPED
        window.removeEventListener("hashchange",hashChangeListener)
        progressBarManager.onDestroy()
        searchManager?.onDestroy()
        searchManager = null
        fabManager?.onDestroy()
        fabManager = null
        database = null
        onDestroyView()
    }

    companion object {

        /**
         * Minimum delay time in mills for the state changes to complete before applying to the UI
         * to avoid state update while rendering
         */
        const val MIN_STATE_CHANGE_DELAY_TIME = 200


        /**
         * Maximum delay time in mills for the state changes to complete before applying to the UI
         * to avoid state update while rendering
         */
        const val MAX_STATE_CHANGE_DELAY_TIME = 500
    }
}