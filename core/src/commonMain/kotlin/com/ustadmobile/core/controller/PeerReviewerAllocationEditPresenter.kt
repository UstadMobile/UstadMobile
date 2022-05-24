package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.assignRandomly
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.PeerReviewerAllocationEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.AssignmentSubmitterWithAllocations
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import com.ustadmobile.lib.db.entities.PeerReviewerAllocationList
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class PeerReviewerAllocationEditPresenter(context: Any,
        arguments: Map<String, String>, view: PeerReviewerAllocationEditView,
        lifecycleOwner: DoorLifecycleOwner,
        di: DI)
    : UstadEditPresenter<PeerReviewerAllocationEditView, PeerReviewerAllocationList>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    var assignmentUid: Long = 0
    var groupUid: Long = 0
    var clazzUid: Long = 0
    var reviewerCount: Int = 0

    override fun onCreate(savedState: Map<String, String>?) {
        assignmentUid = arguments[UstadView.ARG_CLAZZ_ASSIGNMENT_UID]?.toLongOrNull() ?: 0L
        groupUid = arguments[PeerReviewerAllocationEditView.ARG_ASSIGNMENT_GROUP]?.toLongOrNull() ?: 0L
        clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLongOrNull() ?: 0L
        reviewerCount = arguments[PeerReviewerAllocationEditView.ARG_REVIEWERS_COUNT]?.toIntOrNull() ?: 0
        super.onCreate(savedState)
     }

    override fun onLoadFromJson(bundle: Map<String, String>): PeerReviewerAllocationList? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: PeerReviewerAllocationList? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, PeerReviewerAllocationList.serializer(), entityJsonStr)
        }else {
            editEntity = PeerReviewerAllocationList(null)
        }

        presenterScope.launch(doorMainDispatcher()) {

            if(bundle[SAVED_STATE_SUBMITTER_WITH_ALLOCATIONS] != null){
                val submitterList = safeParseList(di, ListSerializer(AssignmentSubmitterWithAllocations.serializer()),
                    AssignmentSubmitterWithAllocations::class, bundle[SAVED_STATE_SUBMITTER_WITH_ALLOCATIONS]
                    ?: "")

                view.submitterListWithAllocations = submitterList
                return@launch
            }

            val listOfSubmitters = repo.clazzAssignmentDao.getSubmitterListForAssignmentList(
                groupUid,
                clazzUid,
                systemImpl.getString(MessageID.group_number, context).replace("%1\$s","")
            )

            val groupedAllocations = editEntity.allocations?.groupBy { it.praToMarkerSubmitterUid }

            // for each submitter, create reviewCount number of allocations
            val submitterListWithAllocations = listOfSubmitters.map {

                val allocation = groupedAllocations?.get(it.submitterUid) ?: listOf()

                val allocationList = mutableListOf<PeerReviewerAllocation>()

                repeat(reviewerCount){ count ->
                    allocationList.add(allocation.getOrNull(count) ?: PeerReviewerAllocation().apply {
                        this.praAssignmentUid = assignmentUid
                        this.praToMarkerSubmitterUid = it.submitterUid
                    })
                }

                AssignmentSubmitterWithAllocations().apply {
                    submitterUid = it.submitterUid
                    name = it.name
                    this.allocations = allocationList
                }
            }

            view.submitterListWithAllocations = submitterListWithAllocations


        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
        val submitterWithAllocations = view.submitterListWithAllocations ?: listOf()
        savedState.putEntityAsJson(
            SAVED_STATE_SUBMITTER_WITH_ALLOCATIONS,
            ListSerializer(AssignmentSubmitterWithAllocations.serializer()),
            submitterWithAllocations)
    }

    fun handleRandomAssign(){
        val submitters = view.submitterListWithAllocations ?: return

        val toBucket = submitters.assignRandomly(reviewerCount)

        // assign
        submitters.forEach{
            val toList = toBucket[it.submitterUid]?.toList() ?: listOf()
            it.allocations?.forEachIndexed { index, peerReviewerAllocation ->
                peerReviewerAllocation.praMarkerSubmitterUid = toList[index]
            }
        }

        view.submitterListWithAllocations = submitters

    }

    override fun handleClickSave(entity: PeerReviewerAllocationList) {

        val list = view.submitterListWithAllocations?.flatMap { it.allocations ?: listOf() }?.filter { it.praMarkerSubmitterUid != 0L }

        val newEntity = PeerReviewerAllocationList(list)

        finishWithResult(
            safeStringify(di, ListSerializer(PeerReviewerAllocationList.serializer()),
                listOf(newEntity))
        )
    }

    companion object {

        const val SAVED_STATE_SUBMITTER_WITH_ALLOCATIONS = "submitterWithAllocations"

    }

}