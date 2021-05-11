package com.ustadmobile.view

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.model.statemanager.SnackBarState
import com.ustadmobile.model.statemanager.ToolbarTitle
import com.ustadmobile.util.ProgressBarManager
import com.ustadmobile.util.RouteManager.getPathName
import com.ustadmobile.util.SearchManager
import com.ustadmobile.util.StateManager
import com.ustadmobile.util.StateManager.getCurrentState
import kotlinx.atomicfu.atomic
import kotlinx.browser.window
import kotlinx.coroutines.Runnable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.w3c.dom.HashChangeEvent
import org.w3c.dom.events.Event
import react.*

open class UstadBaseComponent <P: RProps,S: RState>(props: P): RComponent<P, S>(props),
    UstadView, DIAware, DoorLifecycleOwner {

    private val lifecycleObservers: MutableList<DoorLifecycleObserver> = concurrentSafeListOf()

    val systemImpl : UstadMobileSystemImpl by instance()

    protected val accountManager: UstadAccountManager by instance()

    val umTheme : StateManager.UmTheme by instance()

    private val lifecycleStatus = atomic(0)

    protected var searchManager: SearchManager? = null

    private var progressBarManager: ProgressBarManager? = null

    private var hashChangeListener:(Event) -> Unit = { (it as HashChangeEvent)
        onViewChanged(getPathName(it.newURL))
    }

    var title: String? = null
        get() = field
        set(value) {
            field = value
            value?.let { StateManager.dispatch(ToolbarTitle(it)) }
        }

    override var loading: Boolean = false
        get() = field
        set(value) {
            progressBarManager?.progressBarVisibility = value
            field = value
        }

    override fun componentDidMount() {
        for(observer in lifecycleObservers){
            observer.onStart(this)
        }
        lifecycleStatus.value = DoorLifecycleObserver.STARTED
        searchManager = SearchManager("um-search")
        progressBarManager = ProgressBarManager()
        window.addEventListener("hashchange",hashChangeListener)
    }

    override fun RBuilder.render() {
        lifecycleStatus.value = DoorLifecycleObserver.CREATED
    }

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        StateManager.dispatch(SnackBarState(message,
            systemImpl.getString(actionMessageId, this)) { action() })
    }

    override fun runOnUiThread(r: Runnable?) {
        r?.run()
    }

    override val di: DI
        get() = getCurrentState().di

    override val currentState: Int
        get() = lifecycleStatus.value

    override fun addObserver(observer: DoorLifecycleObserver) {
        lifecycleObservers.add(observer)
    }

    override fun removeObserver(observer: DoorLifecycleObserver) {
        lifecycleObservers.remove(observer)
    }

    open fun onViewChanged(viewName:String?){}

    override fun componentWillUnmount() {
        for(observer in lifecycleObservers){
            observer.onStop(this)
        }
        lifecycleStatus.value = DoorLifecycleObserver.STOPPED
        searchManager?.onDestroy()
        progressBarManager?.onDestroy()
        window.removeEventListener("hashchange",hashChangeListener)
    }
}