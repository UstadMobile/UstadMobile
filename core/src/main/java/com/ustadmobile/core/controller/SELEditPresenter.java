package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.view.SELEditView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;


/**
 * The SELEdit Presenter.
 */
public class SELEditPresenter
        extends UstadBaseController<SELEditView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    private long currentPersonUid = -1;

    //Provider 
    UmProvider<Person> providerList;

    public SELEditPresenter(Object context, Hashtable arguments, SELEditView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Populate the provider
        providerList = UmAppDatabase.getInstance(context).getClazzMemberDao()
                .findAllPeopleInClassUid(currentClazzUid);

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
