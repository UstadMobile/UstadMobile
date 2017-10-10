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

import java.util.Hashtable;

/**
 * The base point view provides two tabs: the downloaded entries of that user
 * and a list of browsable feeds
 * 
 * @author mike
 */
public interface BasePointView extends UstadView{
    
    public static final String VIEW_NAME = "BasePoint";

    /**
     * If true the view should add a third tab for class
     * management (e.g. user is a teacher)
     *
     * @param viewable
     */
    public void setClassListVisible(boolean visible);

    /**
     * Set the top level navigation items for this view. On J2ME these appear in the command menu,
     * on other platforms they appear in the navigation drawer.
     *
     * @param menuItems
     */
    void setMenuItems(BasePointMenuItem[] menuItems);

    /**
     * Show a dialog used to share the application itself offline
     */
    void showShareAppDialog();

    void setShareAppDialogProgressVisible(boolean visible);

    void dismissShareAppDialog();

    void addTab(Hashtable tabArguments);
    
}
