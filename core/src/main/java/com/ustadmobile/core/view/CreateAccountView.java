package com.ustadmobile.core.view;

public interface CreateAccountView extends UstadView {

    String VIEW_NAME = "CreateAccount";

    int FIELD_FIRSTNAME = 1;

    int FIELD_LASTNAME = 2;

    int FIELD_USERNAME = 3;

    int FIELD_EMAIL = 4;

    int FIELD_PHONE = 5;

    int FIELD_PASSWORD = 6;

    int FIELD_CONFIRM_PASSWORD = 7;

    void setEnabled(boolean enabled);

    String getFieldValue(int fieldCode);

    void setErrorMessage(String errorMessage);

}
