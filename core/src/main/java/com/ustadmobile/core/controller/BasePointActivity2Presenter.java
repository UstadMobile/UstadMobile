package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BasePointView2;
import com.ustadmobile.core.view.BulkUploadMasterView;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.core.view.SettingsView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.UmAccount;

import java.util.Hashtable;

public class BasePointActivity2Presenter extends UstadBaseController<BasePointView2> {


    UmAppDatabase repository;

    public BasePointActivity2Presenter(Object context, Hashtable arguments, BasePointView2 view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

    }

    /**
     * Gets logged in person and observes it.
     */
    public void getLoggedInPerson(){
        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        Long loggedInPersonUid = UmAccountManager.getActiveAccount(context).getPersonUid();
        UmLiveData<Person> personLive = repository.getPersonDao().findByUidLive(loggedInPersonUid);
        personLive.observe(BasePointActivity2Presenter.this,
                BasePointActivity2Presenter.this::handlePersonValueChanged);
    }

    /**
     * Called on logged in person changed.
     *
     * @param loggedInPerson    The person changed.
     */
    public void handlePersonValueChanged(Person loggedInPerson){
        if(loggedInPerson != null) {
            if (loggedInPerson.isAdmin()) {
                view.showBulkUploadForAdmin(true);
                view.showSettings(true);
            } else {
                view.showBulkUploadForAdmin(false);
                view.showSettings(false);
            }
        }
    }

    public void handleClickShareIcon(){
        view.showShareAppDialog();
    }

    public void handleClickBulkUpload(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(BulkUploadMasterView.VIEW_NAME, args, context);
    }

    public void handleLogOut(){
        UmAccountManager.setActiveAccount(null, context);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(Login2View.VIEW_NAME, args, context);
    }

    public void handleClickSettingsIcon(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();

        impl.go(SettingsView.VIEW_NAME, args, context);

    }


    public void handleConfirmShareApp(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Get setup file
        impl.getAppSetupFile(getContext(), false, new UmCallback() {

            @Override
            public void onSuccess(Object result) {

                view.shareAppSetupFile(result.toString());

            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }

    @Override
    public void setUIStrings() {

    }
}
