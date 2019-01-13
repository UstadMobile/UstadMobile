package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackWithDefaultValue;
import com.ustadmobile.core.view.PersonPictureDialogView;
import com.ustadmobile.lib.db.entities.Role;

import java.util.Hashtable;

import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;

public class PersonPictureDialogPresenter extends UstadBaseController<PersonPictureDialogView> {


    UmAppDatabase repository;
    long loggedInPersonUid = 0L;
    long personUid = 0L;

    public PersonPictureDialogPresenter(Object context, Hashtable arguments, PersonPictureDialogView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        loggedInPersonUid = UmAccountManager.getActiveAccount(context).getPersonUid();
        if(arguments.containsKey(ARG_PERSON_UID)){
            personUid = (long) arguments.get(ARG_PERSON_UID);
        }
        checkPermissions();
    }

    public void checkPermissions(){
        PersonDao personDao = repository.getPersonDao();
        personDao.personHasPermission(loggedInPersonUid, personUid,
                Role.PERMISSION_PERSON_PICTURE_UPDATE,
                new UmCallbackWithDefaultValue<>(false,
                    new UmCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            view.showUpdateImageButton(result);
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    }
                ));
    }

    @Override
    public void setUIStrings() {

    }
}
