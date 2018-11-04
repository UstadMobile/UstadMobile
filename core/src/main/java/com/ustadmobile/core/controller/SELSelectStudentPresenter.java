package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SELSelectConsentView;
import com.ustadmobile.core.view.SELSelectStudentView;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.Person;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_CLAZZMEMBER_UID;

/**
 * SELSelectStudent's Presenter - Responsible for showing every Clazz Member that will participate
 * in the SEL questions run task before consent, recognition and aswers.
 *
 */
public class SELSelectStudentPresenter extends CommonHandlerPresenter<SELSelectStudentView>  {

    private long currentClazzUid = -1;

    public SELSelectStudentPresenter(Object context, Hashtable arguments,
                                     SELSelectStudentView view) {
        super(context, arguments, view);

        //Get Clazz Uid for the current Clazz.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }

    }

    /**
     * In Order:
     *      1. Get all Clazz Members and set it to the View.
     *
     * @param savedState    The saved state.
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        UmProvider<Person> selStudentsProvider = UmAppDatabase.getInstance(context).getClazzMemberDao()
                .findAllPeopleInClassUid(currentClazzUid);

        view.setSELAnswerListProvider(selStudentsProvider);
    }

    /**
     * Handles primary press on the student list - Selects that student that runs the SEL. Passes
     * its Clazz Member Ui (gets it first) and begins asking for consent.
     *
     * @param arg   The argument to be passed to the presenter for primary action pressed.
     */
    @Override
    public void handleCommonPressed(Object arg) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        ClazzMemberDao clazzMemberDao = UmAppDatabase.getInstance(context).getClazzMemberDao();
        Long currentPersonUid = (Long) arg;
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        args.put(ARG_PERSON_UID, currentPersonUid);

        clazzMemberDao.findByPersonUidAndClazzUidAsync(currentPersonUid, currentClazzUid,
                new UmCallback<ClazzMember>() {
                    @Override
                    public void onSuccess(ClazzMember clazzMember) {
                        args.put(ARG_CLAZZMEMBER_UID, clazzMember.getClazzMemberUid());
                        impl.go(SELSelectConsentView.VIEW_NAME, args, view.getContext());
                        view.finish();
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
    }

    /**
     * Handles secondary press on the students - Nothing for this screen. Does nothing.
     *
     * @param arg   The argument to be passed to the presenter for secondary action pressed.
     */
    @Override
    public void handleSecondaryPressed(Object arg) {
        // No secondary button for this here.
    }

    /**
     * Overridden. Does nothing.
     */
    @Override
    public void setUIStrings() {

    }
}
