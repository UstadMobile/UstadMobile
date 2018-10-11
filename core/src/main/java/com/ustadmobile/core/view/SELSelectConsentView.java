package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.core.db.UmProvider;

/**
 * SELSelectConsent Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SELSelectConsentView extends UstadView {

    String VIEW_NAME = "SELSelectConsent";

    void finish();


}
