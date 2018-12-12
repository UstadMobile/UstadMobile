package com.ustadmobile.core.view;

public interface LoginView2 extends UstadView{

    void setInProgress(boolean inProgress);

    void setErrorMessage(String errorMessage);

    void setServerUrl(String serverUrl);

    void setUsername(String username);

    void setPassword(String password);

}
