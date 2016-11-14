package com.ustadmobile.core.view;

/**
 * Created by varuna on 20/02/16.
 */
public interface ClassListView extends UstadView{

    /**
     * Sets the list of classes to be viewed here
     *
     * @param classList
     */
    public void setClassList(String[] classList);

    void setClassStatus(int index, int statusCode, String statusMessage);


}
