package com.ustadmobile.view

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxAppStateManager.getCurrentState
import com.ustadmobile.redux.ReduxFabState
import com.ustadmobile.redux.ReduxSnackBarState
import com.ustadmobile.redux.ReduxThemeState
import com.ustadmobile.redux.ReduxToolbarState
import com.ustadmobile.util.ProgressBarManager
import com.ustadmobile.util.SearchManager
import com.ustadmobile.util.getViewNameFromUrl
import com.ustadmobile.util.urlSearchParamsToMap
import kotlinx.atomicfu.atomic
import kotlinx.browser.window
import kotlinx.coroutines.Runnable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.w3c.dom.HashChangeEvent
import org.w3c.dom.events.Event
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

abstract class UstadBaseComponent <P: RProps,S: RState>(props: P): RComponent<P, S>(props),
    UstadView, DIAware, DoorLifecycleOwner {

    private val lifecycleObservers: MutableList<DoorLifecycleObserver> = concurrentSafeListOf()

    protected val systemImpl : UstadMobileSystemImpl by instance()

    val accountManager: UstadAccountManager by instance()

    val umTheme : ReduxThemeState by instance()

    private lateinit var progressBarManager: ProgressBarManager

    var searchManager: SearchManager? = null

    protected abstract val viewName: String?

    private val lifecycleStatus = atomic(0)

    private val defaultFabState = ReduxFabState(icon = "add",visible = false, onClick = ::onFabClicked)

    protected var fabState: ReduxFabState = defaultFabState
        set(value) {
            field = value
            window.setTimeout({
                dispatch(value)
            },STATE_CHANGE_DELAY)
        }

    private var hashChangeListener:(Event) -> Unit = { (it as HashChangeEvent)
        if(viewName == getViewNameFromUrl(it.newURL)){
            onCreate(urlSearchParamsToMap())
        }
    }

    var title: String? = null
        set(value) {
            field = value
            window.setTimeout({
               dispatch(ReduxToolbarState(title = title))
            }, STATE_CHANGE_DELAY)
        }

    override var loading: Boolean = false
        set(value) {
            field = value
            progressBarManager.progressBarVisibility = value
        }

    override val currentState: Int
        get() = lifecycleStatus.value

    open fun onCreate(arguments: Map<String,String>){
        for(observer in lifecycleObservers){
            observer.onStart(this)
        }
        title = null
        lifecycleStatus.value = DoorLifecycleObserver.STARTED
    }

    override fun componentWillMount() {
        window.addEventListener("hashchange",hashChangeListener)
    }

    override fun componentDidMount() {
        for(observer in lifecycleObservers){
            observer.onStart(this)
        }
        dispatch(fabState)
        progressBarManager = ProgressBarManager()
        searchManager = SearchManager()

        //Handle both arguments from URL and the ones passed during component rendering
        //i.e from tabs
        val arguments = when {
            props.asDynamic().arguments != js("undefined") ->
                props.asDynamic().arguments as Map<String, String>
            else -> urlSearchParamsToMap()
        }
        onCreate(arguments)
    }

    override fun componentDidUpdate(prevProps: P, prevState: S, snapshot: Any) {
        val propsDidChange = props.asDynamic().arguments != js("undefined")
                && !props.asDynamic().arguments.values.equals(prevProps.asDynamic().arguments.values)

        //Handles tabs behaviour when changing from one tab to another, react components
        //are mounted once and when trying to re-mount it's componentDidUpdate is triggered.
        //This will check and make sure the component has changed by checking if the props has changed
        if(propsDidChange){
            onCreate(props.asDynamic().arguments as Map<String, String>)
        }
    }

    override fun RBuilder.render() {}

    open fun onFabClicked(event: Event){}

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        dispatch(ReduxSnackBarState(message, getString(actionMessageId), action))
    }

    override fun runOnUiThread(r: Runnable?) {
        r?.run()
    }

    override val di: DI
        get() = getCurrentState().appDi.di

    override fun addObserver(observer: DoorLifecycleObserver) {
        lifecycleObservers.add(observer)
    }

    override fun removeObserver(observer: DoorLifecycleObserver) {
        lifecycleObservers.remove(observer)
    }

    open fun getString(messageId: Int): String {
        return if(messageId == 0) "" else systemImpl.getString(messageId, this)
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
    }

    companion object {
        const val STATE_CHANGE_DELAY = 100
    }
}