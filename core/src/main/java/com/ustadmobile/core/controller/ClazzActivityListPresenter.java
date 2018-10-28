package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.view.ClazzActivityListView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzActivity;


/**
 * The ClazzActivityList Presenter.
 */
public class ClazzActivityListPresenter
        extends UstadBaseController<ClazzActivityListView> {

    //Any arguments stored as variables here
    //eg: private long clazzUid = -1;

    //Provider 
    UmProvider<ClazzActivity> providerList;

    public ClazzActivityListPresenter(Object context, Hashtable arguments, ClazzActivityListView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        //eg: if(arguments.containsKey(ARG_CLAZZ_UID)){
        //    currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        //}

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Populate the provider
        //eg: providerList = UmAppDatabase.getInstance(context).getClazzMemberDao()
        //        .findAllPeopleInClassUid(currentClazzUid);

        //set Provider.
        view.setListProvider(providerList);

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
