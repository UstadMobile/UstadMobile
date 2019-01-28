package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.view.SELEditView;
import com.ustadmobile.core.view.SELQuestionView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
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
import static com.ustadmobile.core.view.SELSelectStudentView.ARG_DONE_CLAZZMEMBER_UIDS;


/**
 * The SELQuestion Presenter - responsible for the logic of loading and displaying the current
 * Question of the SEL task run. This should navigate to the SELEdit Screen with this question upon
 * click Next and pass along every argument needed to run the SEL run for that question and overall
 * SEL run.
 *
 */
public class SELQuestionPresenter
        extends UstadBaseController<SELQuestionView> {

    //Any arguments stored as variables here
    private String doneClazzMemberUids = "";
    private long currentClazzUid = 0;
    private long currentPersonUid = 0;
    private long currentQuestionSetUid = 0;
    private long currentClazzMemberUid = 0;
    private long currentQuestionUid = 0;
    private int currentQuestionIndexId = 0;
    private long currentQuestionSetResponseUid = 0;
    private long currentQuestionResponseUid = 0;
    private String questionText = "";

    private Hashtable gottenArguments;


    public SELQuestionPresenter(Object context, Hashtable arguments, SELQuestionView view) {
        super(context, arguments, view);

        //Get class uid and set it to the Presenter
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }
        //Get person uid and set it to the Presenter
        if(arguments.containsKey(ARG_PERSON_UID)){
            currentPersonUid = (long) arguments.get(ARG_PERSON_UID);
        }
        //Get question set uid and set it to the Presenter
        if(arguments.containsKey(ARG_QUESTION_SET_UID)){
            currentQuestionSetUid = (long) arguments.get(ARG_QUESTION_SET_UID);
        }
        //Get clazz member uid and set it to the Presenter
        if(arguments.containsKey(ARG_CLAZZMEMBER_UID)){
            currentClazzMemberUid = (long) arguments.get(ARG_CLAZZMEMBER_UID);
        }
        //Get question uid and set it to the Presenter
        if(arguments.containsKey(ARG_QUESTION_UID)){
            currentQuestionUid = (long) arguments.get(ARG_QUESTION_UID);
        }
        //Get question index set it to the Presenter
        if(arguments.containsKey(ARG_QUESTION_INDEX_ID)){
            currentQuestionIndexId = (int) arguments.get(ARG_QUESTION_INDEX_ID);
        }
        //Get question set response uid and set it to the Presenter
        if(arguments.containsKey(ARG_QUESTION_SET_RESPONSE_UID)){
            currentQuestionSetResponseUid = (long) arguments.get(ARG_QUESTION_SET_RESPONSE_UID);
        }
        //Get question uid and set it to the Presenter
        if(arguments.containsKey(ARG_QUESTION_RESPONSE_UID)){
            currentQuestionResponseUid = (long) arguments.get(ARG_QUESTION_RESPONSE_UID);
        }
        //Get the question text and set it to the View
        if(arguments.containsKey(ARG_QUESTION_TEXT)){
            questionText = arguments.get(ARG_QUESTION_TEXT).toString();
            view.updateQuestion(questionText);
        }

        //Get question index and total and set it to the View.
        if(arguments.containsKey(ARG_QUESTION_INDEX)){
            if(arguments.containsKey(ARG_QUESTION_TOTAL)){
                view.updateQuestionNumber(arguments.get(ARG_QUESTION_INDEX).toString(),
                        arguments.get(ARG_QUESTION_TOTAL).toString());
            }

        }

        //Add on any SEL things done
        if(arguments.containsKey(ARG_DONE_CLAZZMEMBER_UIDS)){
            doneClazzMemberUids = (String) arguments.get(ARG_DONE_CLAZZMEMBER_UIDS);
        }

        gottenArguments = arguments;

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }

    /**
     * The Next button handler goes to the SELEdit View passing along every info needed for the
     * current SEL run as well as the question seen in this presenter.
     *
     */
    public void handleClickPrimaryActionButton() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Create arguments  - OR- just sent arguments ?
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        args.put(ARG_PERSON_UID, currentPersonUid);
        args.put(ARG_QUESTION_SET_UID, currentQuestionSetUid);
        args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid);
        args.put(ARG_QUESTION_UID, currentQuestionUid);
        args.put(ARG_QUESTION_INDEX_ID, currentQuestionIndexId);
        args.put(ARG_QUESTION_SET_RESPONSE_UID, currentQuestionSetResponseUid);
        args.put(ARG_QUESTION_RESPONSE_UID, currentQuestionResponseUid);
        args.put(ARG_QUESTION_TEXT, questionText);
        args.put(ARG_DONE_CLAZZMEMBER_UIDS, doneClazzMemberUids);

        //TODO: test this:
        //args = gottenArguments;

        view.finish();

        //Go to view
        impl.go(SELEditView.VIEW_NAME, args, view.getContext());

    }

    /**
     * Overridden. Does nothing.
     */
    @Override
    public void setUIStrings() {

    }

}
