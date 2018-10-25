package com.ustadmobile.core.view;

/**
 * Created by Varuna Singh on 8/12/2018.
 */

public interface BasePointView2 extends UstadView {

    public static final String VIEW_NAME = "PeopleHome";

    /**
     * Set the bottom navigation items for this view.
     *
     * @param menuItems String menuItems
     */
    void setBottomNavigationItems(String[] menuItems);
}
