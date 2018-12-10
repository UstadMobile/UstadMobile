package com.ustadmobile.core.view;

public interface LoginView2 extends UstadView {

    String VIEW_NAME = "Login2";

    String ARG_PERSON_UID = "personUid";

    void setFacebookLoginVisible(boolean facebookLoginVisible);

    void setTwitterLoginVisible(boolean twitterLoginVisible);

    void setUsername(String username);

    void setPassword(String password);

    void setErrorMessage(String errorMessage);

    void setInProgress(boolean inProgress);
}
