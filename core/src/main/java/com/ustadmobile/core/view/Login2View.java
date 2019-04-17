package com.ustadmobile.core.view;

public interface Login2View extends UstadView{

    String VIEW_NAME = "Login2";

    String ARG_STARTSYNCING="argStatSync";

    void setInProgress(boolean inProgress);

    void setErrorMessage(String errorMessage);

    void setServerUrl(String serverUrl);

    void setUsername(String username);

    void setPassword(String password);

    void setFinishAfficinityOnView();

    void forceSync();

    void updateVersionOnLogin(String version);

}
