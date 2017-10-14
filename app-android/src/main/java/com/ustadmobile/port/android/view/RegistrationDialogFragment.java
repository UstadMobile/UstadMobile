package com.ustadmobile.port.android.view;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.RegistrationPresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.RegistrationView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by varuna on 7/28/2017.
 */

public class RegistrationDialogFragment extends UstadDialogFragment
        implements RegistrationView, DismissableDialog, View.OnClickListener{

    private View mView;

    private RegistrationPresenter mPresenter;

    private boolean editMode = false;

    private String activeUsername = null;

    private ArrayList<AutoCompleteTextView> fieldList = new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Check if user is already logged in..
        if(impl.getActiveUser(getContext()) != null){
            editMode = true;
            activeUsername = impl.getActiveUser(getContext());
        }else{
            editMode = false;
        }

        final String minPassPrompt = impl.getString(MessageID.field_password_min, getContext());
        // Inflate the layout for this fragment
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mView = inflater.inflate(R.layout.fragment_register_dialog, container, false);

        mView.findViewById(R.id.fragment_register_dialog_register_button).setOnClickListener(this);

        AutoCompleteTextView usernameFragment = mView.findViewById(R.id.fragment_register_dialog_username_text);
        final AutoCompleteTextView passwordFragment = mView.findViewById(R.id.fragment_register_dialog_password_text);

        usernameFragment.setFilters(new InputFilter[] {
                new InputFilter.AllCaps() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        return String.valueOf(source).toLowerCase().replace(" ", "");
                    }
                }
        });

        passwordFragment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (passwordFragment.getText().toString().trim().length() < 5) {
                        passwordFragment.setError(minPassPrompt);
                    } else {
                        // your code here
                        passwordFragment.setError(null);
                    }
                } else {
                    if (passwordFragment.getText().toString().trim().length() < 5) {
                        passwordFragment.setError(minPassPrompt);
                    } else {
                        // your code here
                        passwordFragment.setError(null);
                    }
                }

            }});

        if(editMode){
            usernameFragment.setEnabled(false);
            usernameFragment.setText(activeUsername);
            usernameFragment.setHint(impl.getString(MessageID.cannot_update, getContext()) + " " +
                    impl.getString(MessageID.username, getContext()));
            passwordFragment.setEnabled(false);
            passwordFragment.setText("PasswordCannotBeUpdated");
            passwordFragment.setHint(impl.getString(MessageID.cannot_update, getContext()) + " " +
                    impl.getString(MessageID.password, getContext()));
            ((Button)mView.findViewById(R.id.fragment_register_dialog_register_button)).setText(
                    impl.getString(MessageID.update, getContext())
            );
            ((TextView)mView.findViewById(R.id.fragment_register_dialog_title)).setText(
                    impl.getString(MessageID.update, getContext())
            );
        }else{
            usernameFragment.setHint(impl.getString(MessageID.username, getContext()));
            passwordFragment.setHint(impl.getString(MessageID.password, getContext()));
            ((Button)mView.findViewById(R.id.fragment_register_dialog_register_button)).setText(
                    impl.getString(MessageID.register, getContext())
            );
            ((TextView)mView.findViewById(R.id.fragment_register_dialog_title)).setText(
                    impl.getString(MessageID.register, getContext())
            );
        }

        fieldList.add(usernameFragment);
        fieldList.add(passwordFragment);

        try {
            mPresenter = new RegistrationPresenter(getContext(), this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(mResultListener != null)
            mPresenter.setResultListener(mResultListener);

        return mView;
    }

    /**
     * Hides keyboard
     */
    public void hideKeyboard(){
        InputMethodManager imm =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
    }

    /**
     * Shows all options
     * @param paramView
     * @param paramMotionEvent
     * @param options
     * @param autoTextField
     * @return
     */
    public boolean showOptions(View paramView, MotionEvent paramMotionEvent,
                               String[] options, AutoCompleteTextView autoTextField ) {

        hideKeyboard();

        if (options.length > 0) {
            final ArrayAdapter<String> adapterT = new ArrayAdapter<>(getActivity(),
                    android.R.layout.select_dialog_singlechoice, options);
            // show all suggestions
            if (!autoTextField.getText().toString().equals(""))
                adapterT.getFilter().filter(null);
            autoTextField.showDropDown();
        }
        return false;
    }

    public class InputFilterMinMax implements InputFilter {

        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                                   int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

    public void hideNonUserFields(){

    }

    @Override
    public void addField(int fieldName, final int fieldType, final String[] options)
            throws SQLException {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        final LinearLayout fieldLayout = (LinearLayout)mView.findViewById(
                R.id.fragment_register_dialog_field_layout);

        TextInputLayout textInputLayout = new TextInputLayout(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);


        final AutoCompleteTextView autoTextField = new AutoCompleteTextView(getContext());
        ArrayAdapter<String> adapter;
        if(options != null) {
            adapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.select_dialog_singlechoice, options);
            autoTextField.setThreshold(1);
            autoTextField.setAdapter(adapter);
        }

        autoTextField.setId(fieldName);
        autoTextField.setHint(UstadMobileSystemImpl.getInstance().getString(fieldName,
                getContext()));
        autoTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        autoTextField.setSingleLine();
        autoTextField.setMinLines(1);
        autoTextField.setMaxLines(1);


        // TODO Auto-generated method stub

        Calendar myCalendar = Calendar.getInstance();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        myCalendar.set(Calendar.YEAR, year);
        int minYear = year - 10;
        int maxYear = year + 10;
        System.out.println("year: min/max" + year + " " + minYear + "/" + maxYear);


        switch(fieldType){
            case RegistrationPresenter.TYPE_AUTOCOMPETE_TEXT_VIEW:
                autoTextField.setInputType(InputType.TYPE_NULL);
                autoTextField.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                        if (paramMotionEvent.getAction() == MotionEvent.ACTION_UP) {
                            if(autoTextField.getError() != null){
                                autoTextField.setError(null);
                            }
                            return showOptions(paramView, paramMotionEvent, options, autoTextField);
                        }
                        return false;
                    }

                    public boolean showMe(View paramView, MotionEvent paramMotionEvent) {
                        return showOptions(paramView, paramMotionEvent, options, autoTextField);
                    }
                });

                autoTextField.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showOptions(view, null, options, autoTextField);
                    }
                });

                autoTextField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        hideKeyboard();
                        if (options.length > 0) {
                            final ArrayAdapter<String> adapterT = new ArrayAdapter<>(getActivity(),
                                    android.R.layout.select_dialog_singlechoice, options);
                            // show all suggestions
                            if (!autoTextField.getText().toString().equals(""))
                                adapterT.getFilter().filter(null);
                            autoTextField.showDropDown();
                        }
                    }
                });

                autoTextField.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                            long id) {
                        System.out.println("Selected: " + arg1);
                        String selected = parent.getItemAtPosition((int)id).toString();
                        if(selected.equals(impl.getString(MessageID.options_uni_none, getContext()))){
                            LinearLayout layout = (LinearLayout) mView.findViewById(
                                    R.id.fragment_register_dialog_field_layout);
                            final int childCount = layout.getChildCount();
                            for (int i = 0; i < childCount; i++) {
                                View v = layout.getChildAt(i);
                                TextInputLayout t = (TextInputLayout)v;
                                View a = ((TextInputLayout) v).getChildAt(0);
                                int vid = ((FrameLayout) a).getChildAt(0).getId();

                                if (vid > 0 && !mPresenter.userFields.contains(vid)
                                        && vid != R.id.fragment_register_dialog_username_text
                                        && vid != R.id.fragment_register_dialog_password_text
                                        ){
                                    v.setVisibility(View.GONE);
                                    for (int j=0; j<fieldList.size(); j++){
                                        AutoCompleteTextView thisField = fieldList.get(j);
                                        if(thisField.getId() == vid){
                                            thisField.setVisibility(View.GONE);
                                            fieldList.set(j, thisField);
                                        }
                                    }

                                }else{
                                    v.setVisibility(View.VISIBLE);
                                }

                            }
                        }else{
                            //Show all
                            LinearLayout layout = (LinearLayout) mView.findViewById(
                                    R.id.fragment_register_dialog_field_layout);
                            final int childCount = layout.getChildCount();
                            for (int i = 0; i < childCount; i++) {
                                View v = layout.getChildAt(i);
                                v.setVisibility(View.VISIBLE);
                            }

                        }

                    }
                });
                break;
            case RegistrationPresenter.TYPE_SPINNER:
                ArrayAdapter<String> adapterS =
                        new ArrayAdapter<>(getActivity(),
                                android.R.layout.select_dialog_singlechoice, options);
                Spinner dropDownSpinner = new Spinner(getContext());
                dropDownSpinner.setAdapter(adapterS);
                dropDownSpinner.setId(fieldName);
                textInputLayout.addView(dropDownSpinner);
                fieldLayout.addView(textInputLayout, params);
                AutoCompleteTextView prevElS =  fieldList.get(fieldList.size()-1);
                prevElS.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                //fieldList.add(dropDownSpinner);
                break;
            case RegistrationPresenter.TYPE_CLASS_DATETIME:
                autoTextField.setInputType(InputType.TYPE_CLASS_DATETIME);
                break;
            case RegistrationPresenter.TYPE_CLASS_NUMBER:
                autoTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case RegistrationPresenter.TYPE_CLASS_PHONE:
                autoTextField.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            case RegistrationPresenter.TYPE_CLASS_TEXT:
                autoTextField.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case RegistrationPresenter.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                autoTextField.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
                break;
            case RegistrationPresenter.TYPE_CLASS_PERCENTAGE:
                autoTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                autoTextField.setFilters(new InputFilter[]{new InputFilterMinMax("0", "100")});
                break;
            case RegistrationPresenter.TYPE_CLASS_YEAR:
                autoTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                autoTextField.setFilters(
                        new InputFilter[]{
                                new InputFilterMinMax(minYear, maxYear)
                        }
                );

                break;
            default:
                break;
        }
        if (editMode) {
            String value = mPresenter.getUserDetail(activeUsername, fieldName, getContext());
            autoTextField.setText(value);
        }

        textInputLayout.addView(autoTextField);
        fieldLayout.addView(textInputLayout, params);
        AutoCompleteTextView prevElN =  fieldList.get(fieldList.size()-1);
        prevElN.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        fieldList.add(autoTextField);
    }

    @Override
    public void onClick(View v) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(v.getId() == R.id.fragment_register_dialog_register_button) {
            boolean allgood = true;
            String username = ((AutoCompleteTextView)mView.findViewById(
                    R.id.fragment_register_dialog_username_text)).getText().toString();
            String password = ((AutoCompleteTextView) mView.findViewById(
                    R.id.fragment_register_dialog_password_text)).getText().toString();

            for(AutoCompleteTextView field: fieldList){
                field.setError(null); //Reset error every time
                if(field.getText().toString().trim().equals("")){
                    if(field.getVisibility() == View.GONE){
                        continue;
                    }
                    field.setError(impl.getString(MessageID.field_required_prompt, getContext()));
                    allgood = false;
                }
                if(field.getError() != null){
                    allgood = false;
                }
            }

            Hashtable fieldMap = new Hashtable();
            Set<Integer> extraFieldsSet = mPresenter.extraFieldsMap.keySet();
            Integer[] allFields =
                    extraFieldsSet.toArray(new Integer[extraFieldsSet.size()]);

            for(int field:allFields){
                String value = ((EditText)mView.findViewById(field)).getText().toString();
                fieldMap.put(field, value);
            }

            //register new user if validation all good
            if(allgood) {
                mPresenter.handleClickRegister(username, password, fieldMap, editMode);
            }
        }
    }

    private boolean validateFields(AutoCompleteTextView[] fields){
        for(int i=0; i<fields.length; i++){
            EditText currentField=fields[i];
            if(currentField.getText().toString().length()<=0){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(mPresenter != null)
            mPresenter.setResultListener(mResultListener);
    }
}


