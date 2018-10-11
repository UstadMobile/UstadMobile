package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SELSelectConsentView;
import com.ustadmobile.core.view.SELSelectStudentView;
import com.ustadmobile.lib.db.entities.Person;

import java.util.Hashtable;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;

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
        Hashtable args = new Hashtable();
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        args.put(ARG_PERSON_UID, arg);

        impl.go(SELSelectConsentView.VIEW_NAME, args, view.getContext());

    }
}
