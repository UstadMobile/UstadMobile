package com.ustadmobile.core.view;

import com.ustadmobile.core.controller.CallPersonRelatedDialogPresenter;

import java.util.LinkedHashMap;

public interface CallPersonRelatedDialogView extends UstadView {
    String VIEW_NAME="CallPersonRelatedDialogView";
    void finish();
    void setOnDisplay(LinkedHashMap<Integer, CallPersonRelatedDialogPresenter.NameWithNumber> numbers);
    void handleClickCall(String number);
}
