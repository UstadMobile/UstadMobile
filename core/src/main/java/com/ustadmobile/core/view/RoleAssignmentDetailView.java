package com.ustadmobile.core.view;


import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.EntityRoleWithGroupName;

/**
 * Core View. Screen is for RoleAssignmentDetail's View
 */
public interface RoleAssignmentDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "RoleAssignmentDetail";

    //Any argument keys:
    String ENTITYROLE_UID = "EntityRoleUid";

    void updateRoleAssignmentOnView(EntityRole entityRoleWithGroupName,
                                    int groupSelected, int roleSelected);

    void setGroupPresets(String[] presets, int position);

    void setRolePresets(String[] presets, int position);

    void setScopePresets(String[] presets, int position);

    void setAssigneePresets(String[] presets, int position);

    void setGroupSelected(int id);
    void setRoleSelected(int id);
    void setScopeSelected(int id);
    void setAssigneeSelected(int id);

    void setScopeAndAssigneeSelected(int tableId);

    void updateScopeList(int tableId);

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

