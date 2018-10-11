package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.view.SELQuestionView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;


/**
 * The SELQuestion Presenter.
 */
public class SELQuestionPresenter
        extends UstadBaseController<SELQuestionView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    private long currentPersonUid = -1;


    public SELQuestionPresenter(Object context, Hashtable arguments, SELQuestionView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }
        if(arguments.containsKey(ARG_PERSON_UID)){
            currentPersonUid = (long) arguments.get(ARG_PERSON_UID);
        }

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
