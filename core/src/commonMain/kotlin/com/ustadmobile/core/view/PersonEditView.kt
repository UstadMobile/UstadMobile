package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.EntityRoleWithGroupName

/**
 * View for the editing a person - responsible for creating edit fields with the right edit types
 * as well as custom fields. PersonEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface PersonEditView : UstadView {

    /**
     * Sets every field to the view (mostly a Linear Layout) based on the index and the
     * PersonDetailViewField object.
     *
     * @param index The index where the field should go in the (Linear) layout
     * @param field The PersonDetailViewField field representation that has its id, type label & options
     * @param value The value of the field to be set to the view.
     */
    fun setField(index: Int, fieldUid: Long, field: PersonDetailViewField, value: Any?)

    /**
     * Sets the class list um provider to the view.
     *
     * @param clazzListProvider     The class list provider of ClazzWithNumStudents type
     */
    fun setClazzListProvider(clazzListProvider: DataSource.Factory<Int, ClazzWithNumStudents>)

    fun setRoleAssignmentListProvider(roleAssignmentProvider: DataSource.Factory<Int, EntityRoleWithGroupName>)

    /**
     * This will close the activity (and finish it)
     */
    fun finish()

    /**
     * This will update the toolbar's title
     *
     * @param titleName The title string
     */
    fun updateToolbarTitle(titleName: String)

    /**
     * Starts the camera intent - this will save the image in the view to be used later.
     */
    fun addImageFromCamera()

    /**
     * Updates the image path on the view.
     *
     * @param imagePath     The image path to be updated on the view.
     */
    fun updateImageOnView(imagePath: String)

    /**
     * Clears all fields in the edit view - usually called when the image view gets updated and
     * any of the edit fields gets updated - ie the whole edit screen gets rendered again.
     */
    fun clearAllFields()

    fun disableFields(disable: Boolean)

    fun addCustomFieldText(label: CustomField, value: String)

    fun addCustomFieldDropdown(label: CustomField, options: Array<String?>, selected: Int)

    fun clearAllCustomFields()

    fun sendMessage(messageId: Int)

    fun setInProgress(inProgress: Boolean)

    companion object {

        val VIEW_NAME = "PersonEdit"

        val IMAGE_MAX_HEIGHT = 1024
        val IMAGE_MAX_WIDTH = 1024
        val IMAGE_QUALITY = 75
    }


}
