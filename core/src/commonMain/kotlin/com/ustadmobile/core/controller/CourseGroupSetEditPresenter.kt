package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.CourseGroupSetEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupMemberPerson
import com.ustadmobile.lib.db.entities.CourseGroupSet
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class CourseGroupSetEditPresenter(context: Any,
        arguments: Map<String, String>, view: CourseGroupSetEditView,
        lifecycleOwner: LifecycleOwner,
        di: DI)
    : UstadEditPresenter<CourseGroupSetEditView, CourseGroupSet>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): CourseGroupSet? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0L

        val entity =  db.onRepoWithFallbackToDb(2000) {
            it.takeIf { entityUid != 0L }?.courseGroupSetDao?.findByUidAsync(entityUid)
        } ?: CourseGroupSet().apply {
            cgsClazzUid = clazzUid
        }

        val members = db.onRepoWithFallbackToDb(2000){
            it.courseGroupMemberDao.findByGroupSetAsync(entityUid, clazzUid)
        }

        members.forEach { person ->
            person.member = if(person.member == null){
                CourseGroupMember().apply {
                    this.cgmPersonUid = person.personUid
                    this.cgmSetUid = entityUid
                }
            }else{
                person.member
            }
        }
        view.memberList = members

        view.groupList = createGroupList(entity.cgsTotalGroups)

        return entity
    }

    private fun createGroupList(totalGroups: Int): List<IdOption> {
        val groupList = mutableListOf<IdOption>()
        repeat(totalGroups){
            groupList.add(
                IdOption(systemImpl.getString(
                    MessageID.group_number, context).replace("%1\$s", "${it + 1}"),
                    it + 1))
        }
        return groupList
    }

    override fun onLoadFromJson(bundle: Map<String, String>): CourseGroupSet? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        val editEntity: CourseGroupSet = if(entityJsonStr != null) {
            safeParse(di, CourseGroupSet.serializer(), entityJsonStr)
        }else {
            CourseGroupSet()
        }

        view.memberList = safeParse(di, ListSerializer(CourseGroupMemberPerson.serializer()),
            bundle[SAVED_STATE_MEMBER_LIST] ?: "")

        view.groupList = createGroupList(editEntity.cgsTotalGroups)


        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(SAVED_STATE_MEMBER_LIST, json,
            ListSerializer(CourseGroupMemberPerson.serializer()), view.memberList ?: listOf())
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, CourseGroupSet.serializer(), entityVal)
    }

    override fun handleClickSave(entity: CourseGroupSet) {
        presenterScope.launch(doorMainDispatcher()) {

            val members = view.memberList

            repo.withDoorTransactionAsync{ txDb ->
                if(entity.cgsUid == 0L) {
                    entity.cgsUid = txDb.courseGroupSetDao.insertAsync(entity)
                }else {
                    txDb.courseGroupSetDao.updateAsync(entity)
                }

                val toInsertList = members?.filter { it.member?.cgmUid == 0L }?.mapNotNull {
                    it.member?.cgmSetUid = entity.cgsUid
                    it.member
                } ?: listOf()

                val toUpdateList = members?.filter { it.member?.cgmUid != 0L }?.mapNotNull {
                    it.member
                } ?: listOf()

                txDb.courseGroupMemberDao.insertListAsync(toInsertList)
                txDb.courseGroupMemberDao.updateListAsync(toUpdateList)
            }

            finishWithResult(
                safeStringify(di, ListSerializer(CourseGroupSet.serializer()),
                    listOf(entity)))

        }
    }

    /*
     * Shuffle the list, put into groups, change the groupNumber
     */
    fun handleAssignRandomGroupsClicked() {
        val totalGroups = view.groupList?.size ?: 4
        var counter = 1
        val studentList: List<CourseGroupMemberPerson> = view.memberList ?: listOf()
        val assignedList = studentList.shuffled().groupBy { (counter++ % totalGroups)+1 }
            .entries.flatMap {
                it.value.forEach {  person ->
                    person.member?.cgmGroupNumber = it.key
                }
                it.value
            }.sortedBy { it.firstNames }
        view.memberList = assignedList
    }

    fun handleNumberOfGroupsChanged(number: Int) {
        view.takeIf { number != 0 }?.groupList = createGroupList(number)
    }

    companion object {

        const val SAVED_STATE_MEMBER_LIST = "memberList"

    }
}