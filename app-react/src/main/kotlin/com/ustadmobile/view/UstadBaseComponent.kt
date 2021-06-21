package com.ustadmobile.view

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.KEY_IFRAME_HEIGHTS
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.redux.ReduxAppStateManager
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxAppStateManager.getCurrentState
import com.ustadmobile.redux.ReduxFabState
import com.ustadmobile.redux.ReduxThemeState
import com.ustadmobile.util.ProgressBarManager
import com.ustadmobile.util.SearchManager
import com.ustadmobile.util.getViewNameFromUrl
import kotlinx.atomicfu.atomic
import kotlinx.browser.localStorage
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

    protected val accountManager: UstadAccountManager by instance()

    val umTheme : ReduxThemeState by instance()

    private lateinit var progressBarManager: ProgressBarManager

    private lateinit var searchManager: SearchManager

    protected abstract val viewName: String?

    private val lifecycleStatus = atomic(0)

    private val defaultFabState = ReduxFabState(icon = "add",visible = false, onClick = ::handleFabClick)

    protected var fabState: ReduxFabState = defaultFabState
        set(value) {
            field = value
            window.setTimeout({
                dispatch(value)
            },STATE_CHANGE_DELAY)
        }

    private var hashChangeListener:(Event) -> Unit = { (it as HashChangeEvent)
        if(viewName == getViewNameFromUrl(it.newURL)){
            onComponentReady()
        }
    }

    var title: String? = null
        set(value) {
            field = value
            window.setTimeout({

            }, STATE_CHANGE_DELAY)
        }

    override var loading: Boolean = false
        set(value) {
            field = value
            progressBarManager.progressBarVisibility = value
        }

    override val currentState: Int
        get() = lifecycleStatus.value

    open fun onComponentReady(){
        for(observer in lifecycleObservers){
            observer.onStart(this)
        }
        lifecycleStatus.value = DoorLifecycleObserver.STARTED
    }

    override fun componentWillMount() {
        window.addEventListener("hashchange",hashChangeListener)
    }

    override fun componentDidMount() {
        for(observer in lifecycleObservers){
            observer.onStart(this)
        }
        localStorage.removeItem(KEY_IFRAME_HEIGHTS)
        dispatch(fabState)
        progressBarManager = ProgressBarManager()
        searchManager = SearchManager()
        onComponentReady()
    }

    override fun RBuilder.render() {}


    open fun handleFabClick(event: Event){}


    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        //snack bar
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
        searchManager.onDestroy()
    }

    companion object {

        const val STATE_CHANGE_DELAY = 100

    }
}