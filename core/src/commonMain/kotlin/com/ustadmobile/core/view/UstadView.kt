/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.core.view

import kotlinx.coroutines.Runnable
import kotlin.js.JsName

enum class GetResultMode {
    EDITORNEW, FROMLIST
}

/**
 *
 * @author mike
 */
interface UstadView {


    /**
     * Return the system specific context for this view (e.g. Activity on Android
     * etc)
     *
     * Nullable so that this is compliant with fragment.getViewContext()
     *
     * @return
     */
    val viewContext: Any


    var loading: Boolean

    /**
     * Show a snackbar style notification that an error has happened
     *
     * @param message message to show
     */
    fun showSnackBar(message: String, action: () -> Unit = {}, actionMessageId: Int = 0)

    /**
     * Most UI platforms require that all UI changes are done in a particular thread. This method
     * simply wraps those implementations.
     *
     * @param r Runnable to run on system's UI thread
     */
    @JsName("runOnUiThread")
    fun runOnUiThread(r: Runnable?)
    companion object {


        //Begin common arguments

        const val ARG_ENTITY_UID = "entityUid"

        const val ARG_CONTAINER_UID = "containerUid"

        const val ARG_PARENT_ENTRY_UID = "parentUid"

        const val ARG_PARENT_ENTRY_TITLE = "parentTitle"

        const val ARG_CONTENT_ENTRY_UID = "entryid"

        const val ARG_LEARNER_GROUP_UID = "learnerGroupUid"

        const val ARG_NO_IFRAMES = "noiframe"

        const val ARG_SCHOOL_UID = "schoolUid"

        const val ARG_LEAF = "content_type"

        const val ARG_FILTER_BY_SCHOOLUID = "filterBySchoolUid"

        const val ARG_FILTER_BY_ROLE = "filterByRole"

        const val ARG_LISTMODE = "listMode"

        const val ARG_GETRESULTMODE = "getResultMode"

        const val ARG_LISTADDMODE = "listAddMode"

        const val ARG_FILTER_BY_PERMISSION = "filterByPermission"

        const val ARG_FILTER_BY_CLAZZUID = "filterByClazzUid"

        const val ARG_FILTER_BY_PERSONGROUPUID = "filterByPersonGroupUid"

        const val ARG_CLAZZWORK_UID = "clazzworkUid"

        const val ARG_PERSON_UID = "personUid"

        const val ARG_NEXT = "next"

        const val ARG_SITE = "site"

        const val ARG_SERVER_URL = "serverUrl"

        const val ARG_INTENT = "argIntent"

        const val ARG_SNACK_MESSAGE = "snack_message"

        const val ARG_CODE = "argCode"

        const val ARG_CODE_TABLE = "argCodeTable"

        const val ARG_ENTITY_NAME = "argEntityName"

        const val CURRENT_DEST = ""

        const val ARG_SAVE_TO_DB = "saveDb"

        /**
         * Tasks that involve multiple destinations (e.g. Login - AcceptTerms - PersonEditRegister )
         * might need to pop off multiple destinations from the stack when they are done.
         *
         * The final destination in the stack might be reachable via different routes (e.g. it might
         * have started from the login screen, or the account list screen) . The POPUPTO_ON_FINISH
         * arg is intended to be used in such situations. It can be supplied by the initiator and
         * passed through until it is used by the final destination.
         */
        const val ARG_POPUPTO_ON_FINISH = "popUpToOnFinish"

        /**
         * Argument to pass to tell a fragment where on the back stack a result (e.g. entity selected
         * from a list or newly created) should be saved. This works along the principles outlined
         * here: https://developer.android.com/guide/navigation/navigation-programmatic#returning_a_result .
         *
         * The difference between the approach taken here and the approach in the link above is that
         * we do not automatically save the result to the previous entry in the back stack. When the
         * user goes from fragment a to a list to pick an entity, and then selects to create a new
         * entity, we want to go back directly back from the new entity edit fragment to fragment a
         * (e.g. skip the intermediary list).
         *
         * @see com.ustadmobile.port.android.view.ext.FragmentExtKt#saveResultToBackStackSavedStateHandle
         */
        const val ARG_RESULT_DEST_ID = "result_dest"

        const val ARG_REGISTRATION_ALLOWED = "registration_allowed"

        @JsName("MASTER_SERVER_ROOT_ENTRY_UID")
        const val MASTER_SERVER_ROOT_ENTRY_UID = -4103245208651563007L


    }

}
