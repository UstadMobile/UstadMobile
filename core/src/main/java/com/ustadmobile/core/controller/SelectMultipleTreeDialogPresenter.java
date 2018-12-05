package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.view.SelectMultipleTreeDialogView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;


/**
 * The SelectMultipleTreeDialog Presenter.
 */
public class SelectMultipleTreeDialogPresenter
        extends UstadBaseController<SelectMultipleTreeDialogView> {

    //Any arguments stored as variables here
    //eg: private long clazzUid = -1;


    public SelectMultipleTreeDialogPresenter(Object context, Hashtable arguments,
                                             SelectMultipleTreeDialogView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        //eg: if(arguments.containsKey(ARG_CLAZZ_UID)){
        //    currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        //}

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }

    public void handleClickPrimaryActionButton() {
        //TODO: Check if nothing else required. The finish() should call the onResult method in parent activity, etc. Make sure you send the list
        view.finish();
    }

    @Override
    public void setUIStrings() {

    }

}
