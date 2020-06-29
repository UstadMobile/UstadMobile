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

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.impl.UmCallback
import kotlin.js.JsName

/**
 *
 * @author mike
 */
interface EpubContentView : UstadView {

    @JsName("setContainerTitle")
    var containerTitle: String?

    @JsName("setSpineUrls")
    fun setSpineUrls(urls: Array<String>?, index :Int)

    @JsName("setPageTitle")
    var pageTitle: String?


    @JsName("setTableOfContents")
    var tableOfContents: EpubNavItem?

    @JsName("setCoverImage")
    var coverImageUrl: String?

    @JsName("setAuthorName")
    var authorName: String


    /**
     * Set if the progress bar is visible or not
     *
     * @param progressVisible true for the progress bar to be visible (e.g. container is being mounted/parsed),
     * false otherwise.
     */
    @JsName("setProgressBarVisible")
    var progressVisible: Boolean

    /**
     * Set the progress bar progress percentage to show. -1 for indeterminate, 0-100 for a percentage
     *
     * @param progress -1
     *
     * @return
     */
    @JsName("setProgressBarProgress")
    var progressValue: Int

    /**
     *
     * @param spinePos
     */
    @JsName("goToLinearSpinePosition")
    var spinePosition: Int

    companion object {

        const val VIEW_NAME = "EpubContent"

        const val ARG_INITIAL_PAGE_HREF = "initialPageHref"

    }
}
