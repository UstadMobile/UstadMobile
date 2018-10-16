package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.view.ClazzEditView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;


/**
 * The ClazzEdit Presenter.
 */
public class ClazzEditPresenter
        extends UstadBaseController<ClazzEditView> {

    //Any arguments stored as variables here
    //eg: private long clazzUid = -1;


    public ClazzEditPresenter(Object context, Hashtable arguments, ClazzEditView view) {
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

    public void handleClickPrimaryActionButton(long selectedObjectUid) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Create arguments
        Hashtable args = new Hashtable();
        //eg: args.put(ARG_CLAZZ_UID, selectedObjectUid);

        //Go to view
        //eg: impl.go(SELEditView.VIEW_NAME, args, view.getContext());
    }

    @Override
    public void setUIStrings() {

    }

}
