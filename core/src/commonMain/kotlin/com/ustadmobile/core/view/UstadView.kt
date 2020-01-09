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
        const val ARG_CONTAINER_UID = "containerUid"

        const val ARG_CONTENT_ENTRY_UID = "entryid"
    }

}
