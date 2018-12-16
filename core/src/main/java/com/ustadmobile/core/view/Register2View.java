package com.ustadmobile.core.view;

public interface Register2View extends UstadView {

    String VIEW_NAME = "RegisterAccount";

    int FIELD_FIRST_NAME = 1;

    int FIELD_LAST_NAME = 2;

    int FIELD_USERNAME = 3;

    int FIELD_EMAIL = 4;

    int FIELD_PASSWORD = 5;

    int FIELD_CONFIRM_PASSWORD = 6;

    void setErrorMessageView(String errorMessageView);

    void setServerUrl(String url);

    void setInProgress(boolean inProgress);

}
