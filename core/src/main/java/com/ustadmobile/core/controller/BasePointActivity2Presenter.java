package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AboutView;
import com.ustadmobile.core.view.BasePointView2;
import com.ustadmobile.core.view.BulkUploadMasterView;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.core.view.SettingsView;
import com.ustadmobile.lib.db.entities.Person;

import java.util.Hashtable;

import static com.ustadmobile.core.view.Login2View.ARG_STARTSYNCING;

public class BasePointActivity2Presenter extends UstadBaseController<BasePointView2> {

    //Database repository
    UmAppDatabase repository;

    private boolean syncStarted = false;

    /**
     * Gets arguments and initialises
     * @param context       Context
     * @param arguments     Arguments
     * @param view          View
     */
    public BasePointActivity2Presenter(Object context, Hashtable arguments, BasePointView2 view) {
        super(context, arguments, view);

        if(getArguments() != null && getArguments().containsKey(ARG_STARTSYNCING)){
            if(getArguments().get(ARG_STARTSYNCING).equals("true")){
                syncStarted = true;
            }
        }
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
    private void handlePersonValueChanged(Person loggedInPerson){
        if(loggedInPerson != null) {
            view.updatePermissionCheck();
            if (loggedInPerson.isAdmin()) {
                view.showBulkUploadForAdmin(true);
                view.showSettings(true);

            } else {
                view.showBulkUploadForAdmin(false);
                view.showSettings(false);
            }
        }
    }

    /**
     * Shows the share app dialog screen
     */
    public void handleClickShareIcon(){
        view.showShareAppDialog();
    }

    /**
     * Goes to bulk upload screen.
     */
    public void handleClickBulkUpload(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(BulkUploadMasterView.VIEW_NAME, args, context);
    }

    /**
     * Logs out of the application.
     */
    public void handleLogOut(){
        UmAccountManager.setActiveAccount(null, context);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(Login2View.VIEW_NAME, args, context);
    }

    /**
     * Goes to settings screen view.
     */
    public void handleClickSettingsIcon(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(SettingsView.VIEW_NAME, args, context);
    }

    /**
     * Goes to Search activity. This method will not do anything. The Search will figure out
     * where it has been clicked.
     */
    public void handleClickSearchIcon(){

        //Update: This method will not do anything the Search will figure out where it it
        // has been clicked.
    }

    /**
     * About menu clicked. Goes to about screen
     */
    public void handleClickAbout(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(AboutView.VIEW_NAME, args, getContext());
    }

    /**
     * Confirm that user wants to share the app which will get the app set up file and share it
     * upon getting it from System Impl.
     */
    public void handleConfirmShareApp(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Get setup file
        impl.getAppSetupFile(getContext(), false, new UmCallback() {

            @Override
            public void onSuccess(Object result) {
                //Share it on the view
                view.shareAppSetupFile(result.toString());
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * Gets sync started flag
     * @return  true if syncStarted set to true, else false
     */
    public boolean isSyncStarted() {
        return syncStarted;
    }

}
