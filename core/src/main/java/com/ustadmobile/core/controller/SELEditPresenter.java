package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionResponseDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionResponseNominationDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetResponseDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.SELEditView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SELQuestionView;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionResponse;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionResponseNomination;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSetResponse;

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
 * The SELEdit Presenter.
 */
public class SELEditPresenter
        extends CommonHandlerPresenter<SELEditView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    private long currentPersonUid = -1;
    private long currentQuestionSetUid = -1;
    private long currentQuestionUid = -1;
    private long currentClazzMemberUid = -1;
    private int currentQuestionIndexId = 0;
    private long currentQuestionSetResponseUid = -1;
    private long currentQuestionResponseUid = -1;

    //Provider 
    UmProvider<Person> providerList;

    public SELEditPresenter(Object context, Hashtable arguments, SELEditView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }
        if(arguments.containsKey(ARG_PERSON_UID)){
            currentPersonUid = (long) arguments.get(ARG_PERSON_UID);
        }
        if(arguments.containsKey(ARG_CLAZZMEMBER_UID)){
            currentClazzMemberUid = (long) arguments.get(ARG_CLAZZMEMBER_UID);
        }
        if(arguments.containsKey(ARG_QUESTION_SET_UID)){
            currentQuestionSetUid = (long) arguments.get(ARG_QUESTION_SET_UID);
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
            view.updateHeading(arguments.get(ARG_QUESTION_TEXT).toString());
        }

        if(arguments.containsKey(ARG_QUESTION_INDEX)){
            if(arguments.containsKey(ARG_QUESTION_TOTAL)){
                view.updateHeading(arguments.get(ARG_QUESTION_INDEX).toString(),
                        arguments.get(ARG_QUESTION_TOTAL).toString());
            }

        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Populate the provider
        providerList = UmAppDatabase.getInstance(context).getClazzMemberDao()
                .findAllPeopleInClassUid(currentClazzUid);

        //set Provider.
        view.setListProvider(providerList);

    }

    public void handleClickPrimaryActionButton() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        SocialNominationQuestionDao  questionDao =
                UmAppDatabase.getInstance(context).getSocialNominationQuestionDao();
        SocialNominationQuestionSetResponseDao questionSetResponseDao =
                UmAppDatabase.getInstance(context).getSocialNominationQuestionSetResponseDao();
        SocialNominationQuestionResponseDao questionResponseDao =
                UmAppDatabase.getInstance(context).getSocialNominationQuestionResponseDao();


        //TODO:Check: Go to Next SEL question part of this set. Or End. (ie: get back to SELAnswerFragment
        // ie: go to SELQuestionActivity or SELAnswerFragment.

        //Before we go to the next one. We need to end the current one.
        questionSetResponseDao.findByUidAsync(currentQuestionSetResponseUid,
                new UmCallback<SocialNominationQuestionSetResponse>() {
            @Override
            public void onSuccess(SocialNominationQuestionSetResponse currentQuestionSetResponse) {
                currentQuestionSetResponse.setSocialNominationQuestionSetResponseFinishTime(
                        System.currentTimeMillis());

                questionSetResponseDao.updateAsync(currentQuestionSetResponse, new UmCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer questionSetResponseUpdatedResult) {

                        //Find total number of questions as well.
                        int totalSELQuestions = questionDao.findTotalNumberOfQuestions();


                        questionDao.findNextQuestionByQuestionSetUidAsync(currentQuestionSetUid,
                            currentQuestionIndexId, new UmCallback<SocialNominationQuestion>() {
                                @Override
                                public void onSuccess(SocialNominationQuestion nextQuestion) {


                                    if(nextQuestion != null) {

                                        SocialNominationQuestionSetResponse newResponse = new SocialNominationQuestionSetResponse();
                                        newResponse.setSocialNominationQuestionSetResponseStartTime(System.currentTimeMillis());
                                        newResponse.setSocialNominationQuestionSetResponseSocialNominationQuestionSetUid(currentQuestionSetUid);
                                        newResponse.setSocialNominationQuestionSetResponseClazzMemberUid(currentClazzMemberUid);

                                        questionSetResponseDao.insertAsync(newResponse, new UmCallback<Long>() {
                                            @Override
                                            public void onSuccess(Long result) {

                                                view.finish();

                                                //Make a question response for the next Question for this Response-Set instance.
                                                SocialNominationQuestionResponse questionResponse = new SocialNominationQuestionResponse();
                                                questionResponse.setSocialNominationQuestionResponseSocialNominationQuestionSetResponseUid(currentQuestionSetResponseUid);
                                                questionResponse.setSocialNominationQuestionResponseUid(questionResponseDao.insert(questionResponse));

                                                //Create arguments
                                                Hashtable args = new Hashtable();
                                                args.put(ARG_CLAZZ_UID, currentClazzUid);
                                                args.put(ARG_PERSON_UID, currentPersonUid);
                                                args.put(ARG_QUESTION_SET_UID, currentQuestionSetUid);
                                                args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid);
                                                args.put(ARG_QUESTION_UID, nextQuestion.getSocialNominationQuestionUid());
                                                args.put(ARG_QUESTION_SET_RESPONSE_UID, currentQuestionSetResponseUid);
                                                args.put(ARG_QUESTION_INDEX_ID, nextQuestion.getQuestionIndex());
                                                args.put(ARG_QUESTION_TEXT, nextQuestion.getQuestionText());
                                                args.put(ARG_QUESTION_INDEX, nextQuestion.getQuestionIndex());
                                                args.put(ARG_QUESTION_TOTAL, totalSELQuestions);
                                                args.put(ARG_QUESTION_RESPONSE_UID, questionResponse.getSocialNominationQuestionResponseUid());

                                                //impl.go(SELEditView.VIEW_NAME, args, view.getContext());
                                                impl.go(SELQuestionView.VIEW_NAME, args, view.getContext());

                                            }

                                            @Override
                                            public void onFailure(Throwable exception) {
                                                System.out.println("Fail-3");
                                            }
                                        });


                                    }else{
                                        //TODO. end the SEL activitieS properly.
                                        System.out.println("All Question gone through OK..");
                                        view.finish();
                                        //TODO: Maybe go to SELAnswerFragment
                                    }
                                }

                                @Override
                                public void onFailure(Throwable exception) {
                                    System.out.println("Fail-2");
                                }
                            });
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        System.out.println("Fail-1");
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }

    @Override
    public void setUIStrings() {

    }

    @Override
    public void handleCommonPressed(Object arg) {
        //TODO: Record nomination and highlight selected.
        System.out.println("Handling nomination pressed..");

        ClazzMemberDao clazzMemberDao = UmAppDatabase.getInstance(context).getClazzMemberDao();
        SocialNominationQuestionResponseNominationDao questionResponseNominationDao =
                UmAppDatabase.getInstance(context).getSocialNominationQuestionResponseNominationDao();

        clazzMemberDao.findByPersonUidAndClazzUidAsync((Long) arg, currentClazzUid,
                new UmCallback<ClazzMember>() {
            @Override
            public void onSuccess(ClazzMember result) {

                SocialNominationQuestionResponseNomination responseNomination =
                        new SocialNominationQuestionResponseNomination();
                responseNomination.setSocialNominationQuestionResponseNominationSocialNominationQuestionResponseUId(currentQuestionResponseUid);
                responseNomination.setSocialNominationQuestionResponseNominationClazzMemberUid(result.getClazzMemberUid());

                questionResponseNominationDao.insert(responseNomination);

            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

    }
}
