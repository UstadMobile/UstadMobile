package com.ustadmobile.core.view;

public interface Login2View extends UstadView{

    String VIEW_NAME = "Login2";
    String ARG_LOGIN_USERNAME = "LoginUsername";

    void setInProgress(boolean inProgress);

    void setErrorMessage(String errorMessage);

    void setServerUrl(String serverUrl);

    void setPassword(String password);

    void forceSync();

    void updateLastActive();

    void updateUsername(String username);

    void setFinishAfficinityOnView();

}
