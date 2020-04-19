package com.ustadmobile.staging.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.view.PersonDetailViewField
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.EntityRoleWithGroupName

/**
 * View responsible for Person detail view.
 * PersonDetail Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface PersonDetailView : UstadView {

    /**
     * Sets every field to the view (mostly a Linear Layout) based on the index and the
     * PersonDetailViewField object.
     *
     * @param index The index where the field should go in the (Linear) layout
     * @param field The PersonDetailViewField field representation that has its id, type label & options
     * @param value The value of the field to be set to the view.
     */
    fun setField(index: Int, field: PersonDetailViewField, value: Any?)

    fun doneSettingFields()

    /**
     * Set's Class list provider to the person detail page. This is a list within the person's detail
     * depicting every class that person is a member of.
     *
     * @param clazzListProvider     The class list umprovider of ClazzWithNumStudents type.
     */
    fun setClazzListProvider(clazzListProvider: DataSource.Factory<Int, ClazzWithNumStudents>)

    fun setRoleAssignmentListProvider(roleAssignmentProvider: DataSource.Factory<Int, EntityRoleWithGroupName>)

    /**
     * Handles click call button on the person detail page. Usually fires a call intent on the
     * platform it is implemented in.
     *
     * @param number    The number to call .
     */
    fun handleClickCall(number: String)

    /**
     * Handles click sms button on the person detail page. Usually fires a send sms intent on the
     * platform it is implemented in.
     *
     * @param number    The number to sms/text
     */
    fun handleClickText(number: String)

    /**
     * Updates the image from the given image path to the person detail view. Adds the image in the
     * right position.
     *
     * @param imagePath The path of the image to be shown on the view.
     */
    fun updateImageOnView(imagePath: String)

    /**
     * Clears all person's detail views. Usually called before we render all fields again (ie upon
     * updating the image of the person)
     */
    fun clearAllFields()

    /**
     * Handles finishing this view.
     */
    fun finish()

    fun showFAB(show: Boolean)

    fun showUpdateImageButton(show: Boolean)

    fun addImageFromCamera()

    fun showEnrollInClass(show: Boolean)

    fun showDropout(show: Boolean)

    fun addCustomFieldText(label: CustomField, value: String)

    fun addCustomFieldDropdown(label: CustomField, options: Array<String>, selected: Int)

    fun clearAllCustomFields()

    fun addComponent(value: String, label: String)

    fun updateToolbar(name: String)

    companion object {

        val VIEW_NAME = "PersonDetail"
        val ARG_PERSON_UID = "personUid"
    }

}
