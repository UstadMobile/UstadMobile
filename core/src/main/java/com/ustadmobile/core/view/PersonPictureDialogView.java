package com.ustadmobile.core.view;

public interface PersonPictureDialogView extends UstadView, DismissableDialog{
    String VIEW_NAME="PersonPictureDialogView";
    String ARG_PERSON_IMAGE_PATH = "PersonImagePath";
    String ARG_PERSON_UID = "PersonUid";
    void finish();
    void setPictureOnView(String imagePath);
    void showUpdateImageButton(boolean show);

}
