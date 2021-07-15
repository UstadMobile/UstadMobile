package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ClazzDetailView.Companion.ARG_TABS
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DISPLAY_CONTENT_BY_CLAZZ
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DISPLAY_CONTENT_BY_OPTION
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.kodein.di.DI
import org.kodein.di.instance


class ClazzDetailPresenter(context: Any,
                           arguments: Map<String, String>, view: ClazzDetailView, di: DI,
                           lifecycleOwner: DoorLifecycleOwner)
    : UstadDetailPresenter<ClazzDetailView, Clazz>(context, arguments, view, di, lifecycleOwner) {

    private val scope: CoroutineScope by instance(tag = DiTag.TAG_PRESENTER_COROUTINE_SCOPE)

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return false //This has no effect because the button is controlled by the overview presenter
    }

    override fun onLoadFromJson(bundle: Map<String, String>): Clazz? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        val tabsJson = bundle[ARG_TABS]
        var editEntity: Clazz? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di,  Clazz.serializer(), entityJsonStr)
        }else {
            editEntity = Clazz()
        }

        if(tabsJson != null){
            view.tabs = safeParse(di, ListSerializer(String.serializer()), tabsJson)
        }else{
            GlobalScope.launch {
                setupTabs(editEntity)
            }
        }



        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
        savedState.putEntityAsJson(ARG_TABS, null, view.tabs)
    }


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Clazz? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val clazz = withContext(Dispatchers.Default) {
            withTimeoutOrNull(2000) { db.clazzDao.findByUidAsync(entityUid) }
        } ?: Clazz()

        setupTabs(clazz)

        return clazz
    }

    private suspend fun setupTabs(clazz: Clazz) {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val personUid = accountManager.activeAccount.personUid
        scope.launch {
            val commonArgs = mapOf(UstadView.ARG_NAV_CHILD to true.toString())

            val coreTabs = listOf(
                ClazzDetailOverviewView.VIEW_NAME.appendQueryArgs(
                    commonArgs + mapOf(ARG_ENTITY_UID to entityUid.toString())
                ),
                ContentEntryList2View.VIEW_NAME.appendQueryArgs(
                    commonArgs + mapOf(
                        ARG_CLAZZUID to entityUid.toString(),
                        ARG_DISPLAY_CONTENT_BY_OPTION to ARG_DISPLAY_CONTENT_BY_CLAZZ)
                ),
                ClazzMemberListView.VIEW_NAME.appendQueryArgs(
                    commonArgs + mapOf(ARG_CLAZZUID to entityUid.toString())
                ))

            val permissionAndFeatureBasedTabs = CLAZZ_FEATURES.filter { featureFlag ->
                (clazz.clazzFeatures and featureFlag) > 0 && db.clazzDao.personHasPermissionWithClazz(personUid, entityUid,
                    FEATURE_PERMISSION_MAP[featureFlag] ?: -1)
            }.map {
                (VIEWNAME_MAP[it] ?: "INVALID").appendQueryArgs(
                    commonArgs + mapOf(ARG_CLAZZUID to entityUid.toString())
                )
            }

            view.tabs =  coreTabs + permissionAndFeatureBasedTabs
        }
    }

    override fun onLoadDataComplete() {
        super.onLoadDataComplete()

        observeSavedStateResult(ContentEntryList2Presenter.SAVEDSTATE_KEY_ENTRY,
                ListSerializer(ContentEntry.serializer()),
                ContentEntry::class) {
            val entry = it.firstOrNull() ?: return@observeSavedStateResult
            GlobalScope.launch {
                val entriesInClazz = repo.clazzContentJoinDao.listOfEntriesInClazz(arguments[ARG_ENTITY_UID]?.toLong() ?: 0)

                if(entriesInClazz.contains(entry.contentEntryUid)) {

                    view.showSnackBar(
                            systemImpl.getString(MessageID.content_already_added_to_class, context)
                                    .replace("%1\$s",entry.title ?: ""))

                    return@launch
                }

                ClazzContentJoin().apply {
                    ccjClazzUid = arguments[ARG_ENTITY_UID]?.toLong() ?: return@apply
                    ccjContentEntryUid = entry.contentEntryUid
                    ccjUid = repo.clazzContentJoinDao.insert(this)
                }

                view.showSnackBar(
                        systemImpl.getString(MessageID.added_to_class_content, context)
                                .replace("%1\$s",entry.title ?: ""))
            }
            requireSavedStateHandle()[ContentEntryList2Presenter.SAVEDSTATE_KEY_ENTRY] = null
        }

    }

    companion object {

        val CLAZZ_FEATURES = listOf(Clazz.CLAZZ_FEATURE_ATTENDANCE, Clazz.CLAZZ_FEATURE_CLAZZWORK)

        //Map of the feature flag to the permission flag required for that tab to be visible
        val FEATURE_PERMISSION_MAP = mapOf(
                Clazz.CLAZZ_FEATURE_ATTENDANCE to Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT,
                Clazz.CLAZZ_FEATURE_CLAZZWORK to Role.PERMISSION_CLAZZWORK_SELECT)

        val VIEWNAME_MAP = mapOf<Long, String>(
                Clazz.CLAZZ_FEATURE_ATTENDANCE to ClazzLogListAttendanceView.VIEW_NAME,
                Clazz.CLAZZ_FEATURE_CLAZZWORK to ClazzWorkListView.VIEW_NAME
        )
    }

}