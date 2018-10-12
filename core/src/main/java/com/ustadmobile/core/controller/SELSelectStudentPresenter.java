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

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_CLAZZMEMBER_UID;

//public class SELSelectStudentPresenter extends UstadBaseController<SELSelectStudentView>  {
public class SELSelectStudentPresenter extends CommonHandlerPresenter<SELSelectStudentView>  {

    UmProvider<Person> selStudentsProvider;
    private long currentClazzUid = -1;

    public SELSelectStudentPresenter(Object context, Hashtable arguments,
                                     SELSelectStudentView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        selStudentsProvider = UmAppDatabase.getInstance(context).getClazzMemberDao()
                .findAllPeopleInClassUid(currentClazzUid);

        view.setSELAnswerListProvider(selStudentsProvider);
    }

    @Override
    public void setUIStrings() {

    }

    @Override
    public void handleCommonPressed(Object arg) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        ClazzMemberDao clazzMemberDao = UmAppDatabase.getInstance(context).getClazzMemberDao();
        Long currentPersonUid = (Long) arg;
        Hashtable args = new Hashtable();
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        args.put(ARG_PERSON_UID, currentPersonUid);


        clazzMemberDao.findByPersonUidAndClazzUidAsync(currentPersonUid, currentClazzUid,
                new UmCallback<ClazzMember>() {
                    @Override
                    public void onSuccess(ClazzMember clazzMember) {
                        args.put(ARG_CLAZZMEMBER_UID, clazzMember.getClazzMemberUid());
                        impl.go(SELSelectConsentView.VIEW_NAME, args, view.getContext());
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        System.out.println("SELSelectStudentPresenter - Fail");
                    }
                });



    }
}
