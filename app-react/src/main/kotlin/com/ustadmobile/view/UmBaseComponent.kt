package com.ustadmobile.view

import com.ccfraser.muirwik.components.mSnackbar
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.model.statemanager.AppBarState
import com.ustadmobile.util.StateManager
import com.ustadmobile.util.StateManager.getCurrentState
import kotlinx.atomicfu.atomic
import kotlinx.browser.document
import kotlinx.coroutines.Runnable
import org.kodein.di.DI
import org.kodein.di.DIAware
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

open class UmBaseComponent <P: RProps,S: RState>(props: P): RComponent<P, S>(props),
    UstadView, DIAware, DoorLifecycleOwner {

    private var builder: RBuilder? = null

    private val lifecycleObservers: MutableList<DoorLifecycleObserver> = concurrentSafeListOf()

    val systemImpl =  UstadMobileSystemImpl.instance

    private val lifecycleStatus = atomic(0)


    override fun componentDidMount() {
        for(observer in lifecycleObservers){
            observer.onStart(this)
        }
        lifecycleStatus.value = DoorLifecycleObserver.STARTED
        val searchInput = document.getElementsByName("input").item(0)
        searchInput?.addEventListener("onChange", {
            console.log(it)
        })
        //StateManager.subscribe (searchChangeListener)
    }

    open fun onSearchSubmitted(text:String){
        console.log(text)
    }

    override fun RBuilder.render() {
        builder = this
        lifecycleStatus.value = DoorLifecycleObserver.CREATED
    }

    override var loading: Boolean = false
        get() = field
        set(value) {
            field = value
            StateManager.dispatch(AppBarState(loading = value))
        }

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        builder?.mSnackbar (message = message)
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

    override fun componentWillUnmount() {
        for(observer in lifecycleObservers){
            observer.onStop(this)
        }
        lifecycleStatus.value = DoorLifecycleObserver.STOPPED
    }


}