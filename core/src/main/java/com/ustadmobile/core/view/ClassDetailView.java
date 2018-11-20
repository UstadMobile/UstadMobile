package com.ustadmobile.core.view;

/**
 * ClassDetail Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClassDetailView extends UstadView {

    //The View name
    String VIEW_NAME = "ClassDetail";

    /**
     * Sets the toolbar of the view.
     *
     * @param toolbarTitle The toolbar title
     */
    void setToolbarTitle(String toolbarTitle);

}
