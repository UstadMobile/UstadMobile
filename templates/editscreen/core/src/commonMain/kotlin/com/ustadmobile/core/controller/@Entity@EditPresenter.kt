package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.@Entity@EditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.@Entity@
import com.ustadmobile.lib.db.entities.@EditEntity@
import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

class @Entity@EditPresenter(context: Any,
                          arguments: Map<String, String>, view: @Entity@EditView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadEditPresenter<@Entity@EditView, @EditEntity@>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PERSISTENCE_MODE
        get() = TODO("PERSISTENCE_MODE.DB OR PERSISTENCE_MODE.JSON")

    //TODO: Add any required one to many join helpers here. e.g.
    /*
    private val fooOneToManyJoinEditHelper
            = DefaultOneToManyJoinEditHelper<Foo>(Foo::scheduleUid,
            ARG_SAVEDSTATE_FOOS, Foo.serializer().list,
            Foo.serializer().list) {fooUid = it}
     */

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: setup any joined fields etc. here
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): @EditEntity@? {
        return TODO("Implement load from Database or return null if using PERSISTENCE_MODE.JSON")
    }

    override fun onLoadFromJson(bundle: Map<String, String>): @EditEntity@? {
        val entityJsonStr = bundle[ARG_SAVEDSTATE_ENTITY]
        var editEntity: @EditEntity@? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(@EditEntity@.serializer(), entityJsonStr)
        }else {
            editEntity = @EditEntity@()
        }

        //TODO: Call onLoadFromJsonSavedState on any One to Many Join Helpers here
        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_SAVEDSTATE_ENTITY, null,
                entityVal)

        //TODO: call onSaveState for any One to Many Join Helpers here
    }

    override fun handleClickSave(entity: @EditEntity@) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.@Entity_LowerCase@Uid == 0L) {
                entity.@Entity_LowerCase@Uid = repo.@Entity_LowerCase@Dao.insertAsync(entity)
            }else {
                repo.@Entity_LowerCase@Dao.updateAsync(entity)
            }

            //TODO: call commitToDatabase on any One to Many Join Helpers here e.g.
            /*
            scheduleOneToManyJoinEditHelper.commitToDatabase(repo.scheduleDao) {
                it.scheduleClazzUid = entity.clazzUid
            }
            */

            view.finishWithResult(entity)
        }
    }


    //TODO: Add handleAddOrEdit and handleRemove functions that handle when one-many joins are changed
    //e.g.
    /*
    fun handleAddOrEditFoo(foo: Foo) {
        fooOneToManyJoinEditHelper.onEditResult(foo)
    }

    fun handleRemoveFoo(foo: foo) {
        fooOneToManyJoinEditHelper.onDeactivateEntity(schedule)
    }
     */

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}