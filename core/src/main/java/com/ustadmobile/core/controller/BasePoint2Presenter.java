package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.AboutView;
import com.ustadmobile.core.view.BasePoint2View;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.lib.db.entities.Person;


/**
 * Presenter for BasePoint2 view
 **/
public class BasePoint2Presenter extends UstadBaseController<BasePoint2View> {

    UmAppDatabase repository;

    Long loggedInPersonUid;

    public BasePoint2Presenter(Object context, Hashtable arguments, BasePoint2View view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
    }

    /**
     * Gets logged in person and observes it.
     */
    public void getLoggedInPerson(){
        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        loggedInPersonUid = UmAccountManager.getActiveAccount(context).getPersonUid();
        UmLiveData<Person> personLive = repository.getPersonDao().findByUidLive(loggedInPersonUid);
        personLive.observe(BasePoint2Presenter.this,
                BasePoint2Presenter.this::handlePersonValueChanged);
    }

    /**
     * Called on logged in person changed.
     *
     * @param loggedInPerson    The person changed.
     */
    private void handlePersonValueChanged(Person loggedInPerson){
        if(loggedInPerson != null) {
            view.checkPermissions();
            if (loggedInPerson.isAdmin()) {
                view.showCatalog(true);
                view.showInventory(true);
                view.showSales(true);
                view.showCourses(true);
            } else {
                view.showCatalog(true);
                view.showInventory(true);
                view.showSales(true);
                view.showCourses(true);
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
     * Logs out
     */
    public void handleLogOut(){
        UmAccountManager.setActiveAccount(null, context);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(Login2View.VIEW_NAME, args, context);
    }


    /**
     * Goes to Search activity.
     */
    public void handleClickSearchIcon(){

        //Update: This method will not do anything the Search will figure out where it it
        // has been clicked.
    }

    /**
     * About menu clicked.
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


    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }


}
