package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;

/**
 * SELSelectStudentView Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SELSelectStudentView extends UstadView {

    String VIEW_NAME = "SELSelectStudent";
    String ARG_STUDENT_DONE = "studentDone";
    String ARG_DONE_CLAZZMEMBER_UIDS = "donePersonUids";

    /**
     * Sets Current SEL answers by students list
     *
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param selStudentsProvider The provider data
     */
    void setSELAnswerListProvider(UmProvider<Person> selStudentsProvider);

    /**
     * Finish the activity (close it)
     */
    void finish();

    /**
     * Method to pop up that one student SEL done, you can select a new student now.
     */
    void showStudentDoneMoveOn();

}
