package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;


/**
 * Core View. Screen is for SelectProducer's View
 */
public interface SelectProducerView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SelectProducer";

    //Any argument keys:
    String ARG_PRODUCER_UID = "ArgProducerUid";

    /**
     * Method to finish the screen / view.
     */
    void finish();


    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    void setListProvider(UmProvider<Person> listProvider);


}

