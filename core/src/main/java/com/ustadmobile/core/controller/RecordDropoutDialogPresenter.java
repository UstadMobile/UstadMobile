package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.RecordDropoutDialogView;

import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;


/**
 * Presenter for RecordDropoutDialog view
 **/
public class RecordDropoutDialogPresenter extends UstadBaseController<RecordDropoutDialogView> {

    UmAppDatabase repository;

    PersonDao personDao;
    ClazzMemberDao clazzMemberDao;

    long personUid;

    private boolean otherNGO, move, cry, sickness, permission, school, transportation, personal, other;

    public RecordDropoutDialogPresenter(Object context, Hashtable arguments, RecordDropoutDialogView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        personDao = repository.getPersonDao();
        clazzMemberDao = repository.getClazzMemberDao();

        if(getArguments().containsKey(ARG_PERSON_UID)){
            personUid = (long) getArguments().get(ARG_PERSON_UID);
        }
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }

    public void handleClickOk(){
        clazzMemberDao.inactivateClazzMemberForPerson(personUid, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

    }

    public UmAppDatabase getRepository() {
        return repository;
    }

    public void setRepository(UmAppDatabase repository) {
        this.repository = repository;
    }

    public boolean isOtherNGO() {
        return otherNGO;
    }

    public void setOtherNGO(boolean otherNGO) {
        this.otherNGO = otherNGO;
    }

    public boolean isMove() {
        return move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }

    public boolean isCry() {
        return cry;
    }

    public void setCry(boolean cry) {
        this.cry = cry;
    }

    public boolean isSickness() {
        return sickness;
    }

    public void setSickness(boolean sickness) {
        this.sickness = sickness;
    }

    public boolean isPermission() {
        return permission;
    }

    public void setPermission(boolean permission) {
        this.permission = permission;
    }

    public boolean isSchool() {
        return school;
    }

    public void setSchool(boolean school) {
        this.school = school;
    }

    public boolean isTransportation() {
        return transportation;
    }

    public void setTransportation(boolean transportation) {
        this.transportation = transportation;
    }

    public boolean isPersonal() {
        return personal;
    }

    public void setPersonal(boolean personal) {
        this.personal = personal;
    }

    public boolean isOther() {
        return other;
    }

    public void setOther(boolean other) {
        this.other = other;
    }
}
