package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SELAnswerListView;
import com.ustadmobile.core.view.SELSelectStudentView;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.Person;

import java.util.Hashtable;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_CLAZZMEMBER_UID;

public class SELAnswerListPresenter extends UstadBaseController<SELAnswerListView> {

    private long currentClazzUid = -1L;

    private UmProvider<Person> selAnswersProvider;

    public SELAnswerListPresenter(Object context, Hashtable arguments, SELAnswerListView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }

    }

    /**
     * The Presenter here's onCreate. This populates the provider and sets it to the View.
     *
     * This will be called when the implementation View is ready.
     * (ie: on Android, this is called in the Fragment's onCreateView() )
     *
     * @param savedState
     */
    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);

        //TODO: write the correct one
        selAnswersProvider = UmAppDatabase.getInstance(context).getClazzMemberDao()
                .findAllPeopleInClassUid(currentClazzUid);

        view.setSELAnswerListProvider(selAnswersProvider);
    }

    /**
     * Handles when Record SEL FAB button is pressed.
     * It should open a new Record SEL activity.
     */
    public void handleClickRecordSEL() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();


        Hashtable args = new Hashtable();
        args.put(ARG_CLAZZ_UID, currentClazzUid);

        impl.go(SELSelectStudentView.VIEW_NAME, args, view.getContext());

    }


    /**
     * UstadBaseController's setUiString().
     *
     * Right not it doesn't do anything.
     */
    @Override
    public void setUIStrings() {

    }
}
