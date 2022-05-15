package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadSingleEntityView
import com.ustadmobile.door.*
import com.ustadmobile.door.ext.concurrentSafeListOf
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.serialization.DeserializationStrategy
import org.kodein.di.*
import kotlin.jvm.Volatile
import kotlin.reflect.KClass

abstract class UstadSingleEntityPresenter<V: UstadSingleEntityView<RT>, RT: Any>(
    context: Any,
    arguments: Map<String, String>,
    view: V,
    di: DI,
    val lifecycleOwner: DoorLifecycleOwner,
    activeSessionRequired: Boolean = true
): UstadBaseController<V>(
    context, arguments, view, di, activeSessionRequired
) {

    fun interface OnLoadDataCompletedListener {
        fun onLoadDataCompleted(editPresenter: UstadSingleEntityPresenter<*, *>)
    }

    @Volatile
    private var dataLoadCompleted: Boolean = false

    private var onCreateException: Exception? = null

    private var isStarted: Boolean = false

    protected var entity: RT? = null

    enum class PersistenceMode{
        DB, JSON, LIVEDATA
    }

    abstract val persistenceMode: PersistenceMode

    var entityLiveData: DoorLiveData<RT?>? = null

    var entityLiveDataObserver: DoorObserver<RT?>? = null

    val systemImpl: UstadMobileSystemImpl by instance()

    val accountManager: UstadAccountManager by instance()

    val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = TAG_DB)

    val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = TAG_REPO)

    private val onLoadCompletedListeners: MutableList<OnLoadDataCompletedListener> = concurrentSafeListOf()

    private val logPrefix = "UstadSingleEntityPresenter(${this::class.simpleName}): "

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        val mapWithEntityJson = if(savedState?.containsKey(ARG_ENTITY_JSON) == true) {
            Napier.d("$logPrefix found savedState contains ARG_ENTITY_JSON")
            savedState
        }else if(arguments.containsKey(ARG_ENTITY_JSON)){
            Napier.d("$logPrefix arguments contain $ARG_ENTITY_JSON")
            arguments
        }else {
            null
        }

        if(mapWithEntityJson != null && mapWithEntityJson[ARG_ENTITY_JSON] != null) {
            Napier.d("$logPrefix Json present in args or savedstate. " +
                "Load from JSON: ${mapWithEntityJson[ARG_ENTITY_JSON]}")
            entity = onLoadFromJson(mapWithEntityJson)
            view.entity = entity
            (view as? UstadEditView<*>)?.fieldsEnabled = true
            onLoadDataComplete()
        }else if(persistenceMode == PersistenceMode.DB) {
            Napier.d("$logPrefix Load from DB")
            view.loading = true
            (view as? UstadEditView<*>)?.fieldsEnabled = false
            presenterScope.launch {
                try {
                    listOf(db, repo).forEach {
                        entity = onLoadEntityFromDb(it)
                        view.entity = entity
                    }

                    view.loading = false
                    (view as? UstadEditView<*>)?.fieldsEnabled = true
                    onLoadDataComplete()
                }catch(e: Exception) {
                    if(e !is CancellationException) {
                        Napier.e("$logPrefix load exception", e)
                        if(isStarted){
                            navigateToErrorScreen(e)
                        }else{
                            onCreateException = e
                        }
                    }
                }
            }
        }else if(persistenceMode == PersistenceMode.JSON){
            Napier.d("$logPrefix PersistenceMode = JSON, load")
            entity = onLoadFromJson(arguments)
            view.entity = entity
            (view as? UstadEditView<*>)?.fieldsEnabled = true
            onLoadDataComplete()
        }else if(persistenceMode == PersistenceMode.LIVEDATA) {
            Napier.d("$logPrefix PersistenceMode = LiveData, load")
            entityLiveData = onLoadLiveData(repo)
            view.loading = true
            if(entityLiveData != null) {
                entityLiveDataObserver = object : DoorObserver<RT?> {
                    override fun onChanged(t: RT?) {
                        entity = t
                        view.entity = entity
                        view.takeIf { t != null }?.loading = false
                    }
                }.also {
                    entityLiveData?.observe(lifecycleOwner, it)
                    onLoadDataComplete()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        isStarted = true
        onCreateException?.also {
            navigateToErrorScreen(it)
        }
        onCreateException = null
    }

    /**
     * This function will be called after loading data is completed (whether the data has been
     * loaded from the database or JSON).
     *
     * This is the right place to start observing for incoming results from other screens.
     */
    protected open fun onLoadDataComplete() {
        dataLoadCompleted = true
        onLoadCompletedListeners.forEach {
            it.onLoadDataCompleted(this)
        }

        onLoadCompletedListeners.clear()
    }

    fun addOnLoadDataCompletedListener(listener: OnLoadDataCompletedListener) {
        if(dataLoadCompleted)
            listener.onLoadDataCompleted(this)
        else
            onLoadCompletedListeners += listener
    }

    fun removeOnLoadDataCompletedListener(listener: OnLoadDataCompletedListener) {
        onLoadCompletedListeners -= listener
    }

    open suspend fun onLoadEntityFromDb(db: UmAppDatabase): RT? {
        return null
    }

    open fun onLoadLiveData(repo: UmAppDatabase): DoorLiveData<RT?>?{
        return null
    }

    open fun onLoadFromJson(bundle: Map<String, String>): RT? {
        return null
    }

    /**
     * Get a LiveData to watch for values being returned from other screens. The data
     * will be automatically deserialized from JSON.
     *
     * Observation will only start after loading has been completed.
     */
    fun <T: Any> getSavedStateResultLiveData(keyName: String,
                               deserializationStrategy: DeserializationStrategy<List<T>>,
                               resultClass: KClass<T>
    ): DoorLiveData<List<T>> {
        val wrapper = DoorMutableLiveData<List<T>>()

        val savedState = requireSavedStateHandle()
        val lifecycle = lifecycleOwner
        GlobalScope.launch(doorMainDispatcher()) {
            addOnLoadDataCompletedListener {
                savedState.getLiveData<String?>(keyName).observe(lifecycle) {
                    if(it == null) {
                        wrapper.sendValue(listOf())
                        return@observe
                    }

                    val deserialized = safeParseList(di, deserializationStrategy,
                        resultClass, it)

                    wrapper.sendValue(deserialized)
                }
            }
        }

        return wrapper
    }

    /**
     * Observe for a result being returned from other screens. The data
     * will be automatically deserialized from JSON.
     *
     * Observation will only start after loading has been completed.
     */
    fun <T: Any> observeSavedStateResult(keyName: String,
                                deserializationStrategy: DeserializationStrategy<List<T>>,
                                resultClass: KClass<T>,
                                observer: DoorObserver<List<T>>) {
        getSavedStateResultLiveData(keyName, deserializationStrategy, resultClass)
            .observe(lifecycleOwner, observer)
    }

    override fun onDestroy() {
        val entityLiveDataObserverVal = entityLiveDataObserver
        val entityLiveDataVal = entityLiveData
        if(entityLiveDataObserverVal != null && entityLiveDataVal != null) {
            entityLiveDataVal.removeObserver(entityLiveDataObserverVal)
        }
        entityLiveData = null
        entityLiveDataObserver = null

        super.onDestroy()
    }
}