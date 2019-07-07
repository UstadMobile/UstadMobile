package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SelectLanguageDialogPresenter;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.SelectLanguageDialogView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Locale;
import java.util.Objects;

import io.reactivex.annotations.NonNull;

public class SelectLanguageDialogFragment extends UstadDialogFragment implements
        SelectLanguageDialogView, DismissableDialog {

    AlertDialog dialog;
    View rootView;

    private SelectLanguageDialogPresenter mPresenter;
    //Context (Activity calling this)
    private Context mAttachedContext;
    Toolbar toolbar;
    TextView english, dari, pashto;




    public static SelectLanguageDialogFragment newInstance(){
        SelectLanguageDialogFragment fragment = new SelectLanguageDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater =
                (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;

        rootView = inflater.inflate(R.layout.fragment_select_language_dialog, null);

        english = rootView.findViewById(R.id.fragment_select_language_dialog_english);
        dari = rootView.findViewById(R.id.fragment_select_language_dialog_dari);
        pashto = rootView.findViewById(R.id.fragment_select_language_dialog_pashto);


        english.setOnClickListener(v -> handleClickEnglish());
        dari.setOnClickListener(v -> handleClickDari());
        pashto.setOnClickListener(v -> handleClickPashto());

        mPresenter = new SelectLanguageDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Set any view components and its listener (post presenter work)


        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.choose_language);
        builder.setView(rootView);
        dialog = builder.create();

        return dialog;

    }

    private void handleClickEnglish(){
        Locale englishLocale = Locale.forLanguageTag("en");
        //TODO
        Locale.setDefault(englishLocale);
        Configuration config = new Configuration();
        config.locale = englishLocale;
        getActivity().getBaseContext().getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());

        //ChangeLocaleHelper.setLocale(getContext(), "en");
        finish();

    }
    private void handleClickDari(){
        Locale dariLocale = Locale.forLanguageTag("fa");
        //TODO
        Locale.setDefault(dariLocale);
        Configuration config = new Configuration();
        config.locale = dariLocale;
        getActivity().getBaseContext().getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());

        //ChangeLocaleHelper.setLocale(getContext(), "fa");

        finish();
    }
    private void handleClickPashto(){
        Locale pashtoLocale = Locale.forLanguageTag("ps");
        //TODO
        Locale.setDefault(pashtoLocale);
        Configuration config = new Configuration();
        config.locale = pashtoLocale;
        getActivity().getBaseContext().getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());
        //ChangeLocaleHelper.setLocale(getContext(), "ps");

        finish();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mAttachedContext = context;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        this.mAttachedContext = null;
    }

    @Override
    public void finish(){
        dialog.dismiss();
        getActivity().finish();
        startActivity(getActivity().getIntent());
    }

}
