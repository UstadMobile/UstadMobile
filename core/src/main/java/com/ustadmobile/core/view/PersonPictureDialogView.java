package com.ustadmobile.core.view;

public interface PersonPictureDialogView {
    String VIEW_NAME="PersonPictureDialogView";
    String ARG_PERSON_IMAGE_PATH = "PersonImagePath";
    void finish();
    void setPictureOnView(String imagePath);
}
