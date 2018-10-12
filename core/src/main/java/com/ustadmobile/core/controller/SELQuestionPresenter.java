package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.view.SELEditView;
import com.ustadmobile.core.view.SELQuestionView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_CLAZZMEMBER_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_INDEX_ID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_RESPONSE_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_SET_RESPONSE_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_SET_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_UID;
import static com.ustadmobile.core.view.SELQuestionView.ARG_QUESTION_INDEX;
import static com.ustadmobile.core.view.SELQuestionView.ARG_QUESTION_TEXT;
import static com.ustadmobile.core.view.SELQuestionView.ARG_QUESTION_TOTAL;


/**
 * The SELQuestion Presenter.
 */
public class SELQuestionPresenter
        extends UstadBaseController<SELQuestionView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    private long currentPersonUid = -1;
    private long currentQuestionSetUid = -1;
    private long currentClazzMemberUid = -1;
    private long currentQuestionUid = -1;
    private int currentQuestionIndexId = 0;
    private long currentQuestionSetResponseUid = -1;
    private long currentQuestionResponseUid = -1;


    public SELQuestionPresenter(Object context, Hashtable arguments, SELQuestionView view) {
        super(context, arguments, view);

        /*
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        args.put(ARG_PERSON_UID, currentPersonUid);
        args.put(, questionSet.getSocialNominationQuestionSetUid());
        args.put(, currentClazzMemberUid);
        args.put(, nextQuestion.getSocialNominationQuestionUid());
        args.put(, nextQuestion.getQuestionIndex());
        args.put(, questionSetResponseUid);
         */
        //Get arguments and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }
        if(arguments.containsKey(ARG_PERSON_UID)){
            currentPersonUid = (long) arguments.get(ARG_PERSON_UID);
        }
        if(arguments.containsKey(ARG_QUESTION_SET_UID)){
            currentQuestionSetUid = (long) arguments.get(ARG_QUESTION_SET_UID);
        }
        if(arguments.containsKey(ARG_CLAZZMEMBER_UID)){
            currentClazzMemberUid = (long) arguments.get(ARG_CLAZZMEMBER_UID);
        }
        if(arguments.containsKey(ARG_QUESTION_UID)){
            currentQuestionUid = (long) arguments.get(ARG_QUESTION_UID);
        }
        if(arguments.containsKey(ARG_QUESTION_INDEX_ID)){
            currentQuestionIndexId = (int) arguments.get(ARG_QUESTION_INDEX_ID);
        }
        if(arguments.containsKey(ARG_QUESTION_SET_RESPONSE_UID)){
            currentQuestionSetResponseUid = (long) arguments.get(ARG_QUESTION_SET_RESPONSE_UID);
        }
        if(arguments.containsKey(ARG_QUESTION_RESPONSE_UID)){
            currentQuestionResponseUid = (long) arguments.get(ARG_QUESTION_RESPONSE_UID);
        }

        if(arguments.containsKey(ARG_QUESTION_TEXT)){
            view.updateQuestion(arguments.get(ARG_QUESTION_TEXT).toString());
        }

        if(arguments.containsKey(ARG_QUESTION_INDEX)){
            if(arguments.containsKey(ARG_QUESTION_TOTAL)){
                view.updateQuestionNumber(arguments.get(ARG_QUESTION_INDEX).toString(),
                        arguments.get(ARG_QUESTION_TOTAL).toString());
            }

        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }

    public void handleClickPrimaryActionButton() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        view.finish();

        //Create arguments
        Hashtable args = new Hashtable();
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        args.put(ARG_PERSON_UID, currentPersonUid);
        args.put(ARG_QUESTION_SET_UID, currentQuestionSetUid);
        args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid);
        args.put(ARG_QUESTION_UID, currentQuestionUid);
        args.put(ARG_QUESTION_INDEX_ID, currentQuestionIndexId);
        args.put(ARG_QUESTION_SET_RESPONSE_UID, currentQuestionSetResponseUid);
        args.put(ARG_QUESTION_RESPONSE_UID, currentQuestionResponseUid);


        //Go to view
        impl.go(SELEditView.VIEW_NAME, args, view.getContext());


    }

    @Override
    public void setUIStrings() {

    }

}
