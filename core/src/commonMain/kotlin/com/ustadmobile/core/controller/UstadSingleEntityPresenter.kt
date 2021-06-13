package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.navigateToErrorScreen
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadSingleEntityView
import com.ustadmobile.door.*
import com.ustadmobile.door.ext.concurrentSafeListOf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.DeserializationStrategy
import org.kodein.di.*
import kotlin.jvm.Volatile
import kotlin.reflect.KClass

abstract class UstadSingleEntityPresenter<V: UstadSingleEntityView<RT>, RT: Any>(
        context: Any,
        arguments: Map<String, String>,
        view: V, di: DI,
        val lifecycleOwner: DoorLifecycleOwner): UstadBaseController<V>(context, arguments, view, di) {

    fun interface OnLoadDataCompletedListener {
        fun onLoadDataCompleted(editPresenter: UstadSingleEntityPresenter<*, *>)
    }

    @Volatile
    private var dataLoadCompleted: Boolean = false

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

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        val mapWithEntityJson = if(savedState?.containsKey(ARG_ENTITY_JSON) == true) {
            savedState
        }else if(arguments.containsKey(ARG_ENTITY_JSON)){
            arguments
        }else {
            null
        }

        if(mapWithEntityJson != null && mapWithEntityJson[ARG_ENTITY_JSON] != null) {
            entity = onLoadFromJson(mapWithEntityJson)
            view.entity = entity
            (view as? UstadEditView<*>)?.fieldsEnabled = true
            onLoadDataComplete()
        }else if(persistenceMode == PersistenceMode.DB) {
            view.loading = true
            (view as? UstadEditView<*>)?.fieldsEnabled = false
            GlobalScope.launch(doorMainDispatcher()) {
                try {
                    listOf(db, repo).forEach {
                        entity = onLoadEntityFromDb(it)
                        view.entity = entity
                    }

                    view.loading = false
                    (view as? UstadEditView<*>)?.fieldsEnabled = true
                    onLoadDataComplete()
                }catch(e: Exception) {
                    navigateToErrorScreen(e)
                }
            }
        }else if(persistenceMode == PersistenceMode.JSON){
            entity = onLoadFromJson(arguments)
            view.entity = entity
            onLoadDataComplete()
        }else if(persistenceMode == PersistenceMode.LIVEDATA) {
            entityLiveData = onLoadLiveData(repo)
            view.loading = true
            if(entityLiveData != null) {
                entityLiveDataObserver = object : DoorObserver<RT?> {
                    override fun onChanged(t: RT?) {
                        view.entity = t
                        view.takeIf { t != null }?.loading = false
                    }
                }.also {
                    entityLiveData?.observe(lifecycleOwner, it)
                    onLoadDataComplete()
                }
            }
        }
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
        super.onDestroy()
        val entityLiveDataObserverVal = entityLiveDataObserver
        val entityLiveDataVal = entityLiveData
        if(entityLiveDataObserverVal != null && entityLiveDataVal != null) {
            entityLiveDataVal.removeObserver(entityLiveDataObserverVal)
        }
        entityLiveData = null
        entityLiveDataObserver = null
    }
}