package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.PersonPictureDialogView;

import java.io.File;
import java.util.Objects;

import io.reactivex.annotations.NonNull;

public class PersonPictureDialogFragment extends UstadDialogFragment implements
        PersonPictureDialogView, AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog {

    AlertDialog dialog;
    View rootView;

    ImageView theImage;
    Button updateImageButton;
    String imagePath = "";


    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        LayoutInflater inflater =
                (LayoutInflater)Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);

        rootView = inflater.inflate(R.layout.fragment_person_picture_dialog, null);
        theImage = rootView.findViewById(R.id.fragment_person_picture_dialog_imageview);
        updateImageButton =
                rootView.findViewById(R.id.fragment_person_picture_dialog_update_picture_button);

        if(getArguments().containsKey(ARG_PERSON_IMAGE_PATH)){
            imagePath = getArguments().getString(ARG_PERSON_IMAGE_PATH);
            //Update image on Dialog
            setPictureOnView(imagePath);

        }

        android.support.v7.app.AlertDialog.Builder builder =
                new android.support.v7.app.AlertDialog.Builder(Objects.requireNonNull(getContext()));
        //builder.setTitle(R.string.person_picture);


        builder.setView(rootView);
        dialog = builder.create();
        dialog.setOnShowListener(this);

        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        return dialog;
    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void finish() {

    }

    @Override
    public void setPictureOnView(String imagePath) {

        Uri profileImage = Uri.fromFile(new File(imagePath));

        Picasso.with(getContext()).load(profileImage).into(theImage);

    }
}
