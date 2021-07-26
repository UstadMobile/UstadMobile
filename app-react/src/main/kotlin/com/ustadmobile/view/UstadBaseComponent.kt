package com.ustadmobile.view

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.putResultDestInfo
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.navigation.NavControllerJs
import com.ustadmobile.navigation.RouteManager.lookupDestinationName
import com.ustadmobile.redux.ReduxAppStateManager.dispatch
import com.ustadmobile.redux.ReduxAppStateManager.getCurrentState
import com.ustadmobile.redux.ReduxSnackBarState
import com.ustadmobile.redux.ReduxToolbarState
import com.ustadmobile.util.*
import kotlinx.atomicfu.atomic
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.kodein.di.*
import org.w3c.dom.HashChangeEvent
import org.w3c.dom.events.Event
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import kotlin.reflect.KClass

abstract class UstadBaseComponent <P: RProps,S: RState>(props: P): RComponent<P, S>(props),
    UstadView, DIAware, DoorLifecycleOwner {

    private val lifecycleObservers: MutableList<DoorLifecycleObserver> = concurrentSafeListOf()

    protected val systemImpl : UstadMobileSystemImpl by instance()

    val accountManager: UstadAccountManager by instance()

    lateinit var navController: NavControllerJs

    private lateinit var progressBarManager: ProgressBarManager

    var searchManager: SearchManager? = null

    var fabManager: FabManager? = null

    protected lateinit var arguments: Map<String, String>

    protected abstract val viewName: String?

    private val lifecycleStatus = atomic(0)

    private var hashChangeListener:(Event) -> Unit = { (it as HashChangeEvent)
        if(viewName == getViewNameFromUrl(it.newURL)){
            arguments = urlSearchParamsToMap()
            onCreate()
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

    open fun onCreate(){
        for(observer in lifecycleObservers){
            observer.onStart(this)
        }
        fabManager?.visible = false
        fabManager?.icon = "add"
        fabManager?.onClickListener = {
            onFabClicked()
        }
        lifecycleStatus.value = DoorLifecycleObserver.STARTED
        val umController: UstadNavController by instance()
        navController = umController as NavControllerJs
    }

    override fun componentWillMount() {
        window.addEventListener("hashchange",hashChangeListener)
    }

    override fun componentDidMount() {
        for(observer in lifecycleObservers){
            observer.onStart(this)
        }
        progressBarManager = ProgressBarManager()
        searchManager = SearchManager()
        fabManager = FabManager()

        //Handle both arguments from URL and the ones passed during component rendering
        //i.e from tabs
        arguments = when {
            props.asDynamic().arguments != js("undefined") ->
                props.asDynamic().arguments as Map<String, String>
            else -> urlSearchParamsToMap()
        }
        onCreate()
    }

    override fun componentDidUpdate(prevProps: P, prevState: S, snapshot: Any) {
        val propsDidChange = props.asDynamic().arguments != js("undefined")
                && !props.asDynamic().arguments.values.equals(prevProps.asDynamic().arguments.values)

        //Handles tabs behaviour when changing from one tab to another, react components
        //are mounted once and when trying to re-mount it's componentDidUpdate is triggered.
        //This will check and make sure the component has changed by checking if the props has changed
        if(propsDidChange){
            arguments = props.asDynamic().arguments as Map<String, String>
            onCreate()
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
        extend(getCurrentState().appDi.di)

        bind<CoroutineScope>(DiTag.TAG_PRESENTER_COROUTINE_SCOPE) with provider {
            GlobalScope
        }
    }

    override fun addObserver(observer: DoorLifecycleObserver) {
        lifecycleObservers.add(observer)
    }

    override fun removeObserver(observer: DoorLifecycleObserver) {
        lifecycleObservers.remove(observer)
    }

    open fun getString(messageId: Int): String {
        return if(messageId == 0) "" else systemImpl.getString(messageId, this)
    }

    /**
     * Save the result of a component (e.g. a selection from a list or newly created entity) to the
     * BackStack SavedStateHandle as specified by ARG_RESULT_DEST_ID and ARG_RESULT_DEST_KEY
     */

    @Suppress("UNUSED_PARAMETER") //result used in js code
    fun  <T> saveResultToBackStackSavedStateHandle(result: List<T>) {
        val serializer = getCurrentState().serialization.serializer
        if(serializer != null){
            saveResultToBackStackSavedStateHandle(
                Json.encodeToString(ListSerializer(serializer),
                    js("result")))
        }
    }


    private fun saveResultToBackStackSavedStateHandle(result: String) {
        var saveToDestination = arguments[UstadView.ARG_RESULT_DEST_ID]
        val saveToDestinationViewName = arguments[UstadView.ARG_RESULT_DEST_VIEWNAME]

        if(saveToDestination == null && saveToDestinationViewName != null) {
            saveToDestination = lookupDestinationName(saveToDestinationViewName)?.view
        }

        val saveToKey = arguments[UstadView.ARG_RESULT_DEST_KEY]

        if(saveToDestination != null && saveToKey != null) {
            val destStackEntry = navController.getBackStackEntry(saveToDestination)
            destStackEntry?.savedStateHandle?.set(saveToKey, result)
            navController.popBackStack(saveToDestination, false)
        }else{
            navController.navigateUp()
        }
    }

    /**
     * Navigate to a list view in picker mode for the given entity type and destination view
     *
     * @param entityClass The class for the entity type that is going to be selected
     * @param destinationView The destination view as per the navigation map
     * @param args optional additional args to pass to the list view
     * @param destinationResultKey the key to use in the SavedStateHandle
     */
    fun navigateToPickEntityFromList(entityClass: KClass<*>, destinationView: String,
                                     args: MutableMap<String, String> = mutableMapOf(),
                                     destinationResultKey: String? = entityClass.simpleName,
                                     overwriteDestination: Boolean? = null,
                                     navOptions: UstadMobileSystemCommon.UstadGoOptions = UstadMobileSystemCommon.UstadGoOptions()){
        val currentBackStateEntryVal = navController.currentBackStackEntry
        if(currentBackStateEntryVal != null && destinationResultKey != null)
            args.putResultDestInfo(currentBackStateEntryVal, destinationResultKey,
                overwriteDest = overwriteDestination ?: (this is UstadEditComponent<*>))

        args[UstadView.ARG_LISTMODE] = ListViewMode.PICKER.toString()
        navController.navigate(destinationView, args, navOptions)
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
    }

    companion object {
        const val STATE_CHANGE_DELAY = 100
    }
}