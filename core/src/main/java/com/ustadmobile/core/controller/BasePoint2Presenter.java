package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.PersonPictureDao;
import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.core.db.dao.SalePaymentDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.AboutView;
import com.ustadmobile.core.view.BasePoint2View;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.core.view.UserProfileView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonPicture;


/**
 * Presenter for BasePoint2 view
 **/
public class BasePoint2Presenter extends UstadBaseController<BasePoint2View> {

    UmAppDatabase repository;

    Long loggedInPersonUid;

    SaleDao saleDao;

    UmLiveData<Integer> preOrderLive;

    UmLiveData<Integer> paymentsDueLive;

    SalePaymentDao salePaymentDao;
    PersonPictureDao personPictureDao;

    public BasePoint2Presenter(Object context, Hashtable arguments, BasePoint2View view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        saleDao = repository.getSaleDao();
        salePaymentDao = repository.getSalePaymentDao();
        personPictureDao = repository.getPersonPictureDao();

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

        UmLiveData<PersonPicture> personPictureLive =
                repository.getPersonPictureDao().findByPersonUidLive(loggedInPersonUid);
        personPictureLive.observe(BasePoint2Presenter.this,
                BasePoint2Presenter.this::handlePersonPictureChanged);


    }

    private void handlePersonPictureChanged(PersonPicture personPicture){
        if (personPicture != null)
            view.updateImageOnView(personPictureDao.getAttachmentPath(
                    personPicture.getPersonPictureUid()));
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

                //Find pic and update on view
                personPictureDao = repository.getPersonPictureDao();
                personPictureDao.findByPersonUidAsync(loggedInPerson.getPersonUid(),
                        new UmCallback<PersonPicture>() {
                    @Override
                    public void onSuccess(PersonPicture personPicture) {
                        if (personPicture != null)
                            view.updateImageOnView(personPictureDao.getAttachmentPath(
                                    personPicture.getPersonPictureUid()));
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
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
        UmAccountManager.updatePasswordHash(null, context, UstadMobileSystemImpl.getInstance());
        Hashtable args = new Hashtable();
        impl.go(Login2View.VIEW_NAME, args, context);
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

        getLoggedInPerson();
    }

    public void updateDueCountOnView(){

        preOrderLive = saleDao.getPreOrderSaleCountLive();
        preOrderLive.observe(BasePoint2Presenter.this,
                BasePoint2Presenter.this::handlePreOrderCountUpdate);

        paymentsDueLive = salePaymentDao.getPaymentsDueCountLive();
        paymentsDueLive.observe(BasePoint2Presenter.this,
                BasePoint2Presenter.this::handlePaymnetsDueCountUpdate);

    }

    private int preOrderCount = 0, paymentsDueCount = 0;

    public void handlePreOrderCountUpdate(Integer count){
        preOrderCount = count;
        //view.updateNotificationForSales(count);
        view.updateNotificationForSales(preOrderCount + paymentsDueCount);
    }

    public void handlePaymnetsDueCountUpdate(Integer count){
        paymentsDueCount = count;
        view.updateNotificationForSales(preOrderCount + paymentsDueCount);
    }

    public void handleClickPersonIcon(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        impl.go(UserProfileView.VIEW_NAME, args, context);
    }

}
