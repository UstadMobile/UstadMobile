package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ClazzDetailView.Companion.ARG_TABS
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.kodein.di.DI
import org.kodein.di.instance


class ClazzDetailPresenter(context: Any,
                           arguments: Map<String, String>, view: ClazzDetailView, di: DI,
                           lifecycleOwner: LifecycleOwner)
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
            presenterScope.launch {
                setupTabs(editEntity)
            }
        }



        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, Clazz.serializer(),
                entityVal)
        savedState.putEntityAsJson(ARG_TABS, json, ListSerializer(String.serializer()), view.tabs)
    }


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Clazz? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val clazz = withContext(Dispatchers.Default) {
            db.onRepoWithFallbackToDb(2000) { it.clazzDao.findByUidAsync(entityUid) }
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

            val groupsTab = CourseGroupSetListView.VIEW_NAME.appendQueryArgs(
                commonArgs + mapOf(ARG_CLAZZUID to entityUid.toString())
            )

            val desiredTabs = coreTabs + permissionAndFeatureBasedTabs + groupsTab
            if(view.tabs != desiredTabs)
                view.tabs =  desiredTabs
        }
    }

    companion object {

        val CLAZZ_FEATURES = listOf(Clazz.CLAZZ_FEATURE_ATTENDANCE)

        //Map of the feature flag to the permission flag required for that tab to be visible
        val FEATURE_PERMISSION_MAP = mapOf(
                Clazz.CLAZZ_FEATURE_ATTENDANCE to Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT)

        val VIEWNAME_MAP = mapOf<Long, String>(
                Clazz.CLAZZ_FEATURE_ATTENDANCE to ClazzLogListAttendanceView.VIEW_NAME
        )
    }

}