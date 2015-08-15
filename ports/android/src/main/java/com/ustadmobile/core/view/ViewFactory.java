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

package com.ustadmobile.core.view;

import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.port.android.view.CatalogViewAndroid;
import com.ustadmobile.port.android.view.ContainerViewAndroid;
import com.ustadmobile.port.android.view.LoginViewAndroid;

/**
 * Created by mike on 07/07/15.
 */
public class ViewFactory {

    public static LoginView makeLoginView() {
        return new LoginViewAndroid();
    };

    public static CatalogView makeCatalogView() {
        return new CatalogViewAndroid();
    }

    /**
     * This method should return a ContainerView for the given entry.  The entry
     * must be opened using UstadMobileSystemImpl.openContainer first.
     *
     * @param entry The entry to be opened - used to provide title etc.
     * @param openURI The URI returned by UstadMobileSystemImpl.openContainer
     * @param mimeType the mime type of the container
     *
     * @return ContainerView to be used to show this container.
     */
    public static ContainerView makeContainerView(UstadJSOPDSEntry entry, String openURI, String mimeType) {
        return new ContainerViewAndroid();
    }
}
