package com.ustadmobile.core.view


import com.ustadmobile.lib.db.entities.EntityRole
import com.ustadmobile.lib.db.entities.EntityRoleWithGroupName

/**
 * Core View. Screen is for RoleAssignmentDetail's View
 */
interface RoleAssignmentDetailView : UstadView {

    fun updateRoleAssignmentOnView(entityRoleWithGroupName: EntityRole,
                                   groupSelected: Int, roleSelected: Int)

    fun setGroupPresets(presets: Array<String>, position: Int)

    fun setRolePresets(presets: Array<String>, position: Int)

    fun setScopePresets(presets: Array<String?>, position: Int)

    fun setAssigneePresets(presets: Array<String>, position: Int)

    fun setGroupSelected(id: Int)
    fun setRoleSelected(id: Int)
    fun setScopeSelected(id: Int)
    fun setAssigneeSelected(id: Int)

    fun setScopeAndAssigneeSelected(tableId: Int)

    fun updateScopeList(tableId: Int)

    fun individualClicked()

    fun groupClicked()

    fun updateGroupName(individual: Boolean)

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "RoleAssignmentDetail"

        //Any argument keys:
        val ENTITYROLE_UID = "EntityRoleUid"
    }


}

