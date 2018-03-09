package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;

/**
 * Created by mike on 3/8/18.
 */

public interface PersonListView extends UstadView {

    String VIEW_NAME = "PersonList";

    void setProvider(UmProvider<Person> provider);

}
