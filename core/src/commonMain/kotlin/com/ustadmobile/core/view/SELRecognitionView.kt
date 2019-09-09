package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture

/**
 * View responsible for recognising other students.
 * SELRecognition Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SELRecognitionView : UstadView {

    /**
     * Sets Current provider
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param listProvider The provider data
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, PersonWithPersonPicture>)


    /**
     * Closes the view.
     */
    fun finish()

    /** Send message to view  */
    fun showMessage(message: String)

    companion object {

        //View name
        val VIEW_NAME = "SELRecognition"

        //Arguments:
        val ARG_RECOGNITION_UID = "argRecognitioUid"
    }

}
