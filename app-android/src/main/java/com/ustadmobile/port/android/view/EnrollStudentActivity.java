package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.controller.EnrollStudentController;
import com.ustadmobile.port.sharedse.view.EnrollStudentView;

import java.util.Hashtable;

/**
 * Created by varuna on 09/03/16.
 */
public class EnrollStudentActivity extends UstadBaseActivity implements EnrollStudentView,
        View.OnClickListener, CheckBox.OnCheckedChangeListener {

    private int viewId;

    private EnrollStudentController mEnrollStudentController;

    protected String mTitle;

    protected String mUsernameHint;

    protected String mPasswordHint;

    protected String mButtonText;

    protected String mRegisterPhoneNumberHint;

    protected String mRegisterNameHint;

    protected String mRegisterGenderMaleLabel;

    protected String mRegisterGenderFemaleLable;

    protected String mRegisterButtonText;

    protected String mRegisterUsernameHint;

    protected String mRegisterPasswordHint;

    protected String mRegisterEmailHint;

    private void setTextViewHint(int id, String hint) {
        ((TextView)this.findViewById(id)).setHint(hint);
    }

    @Override
    public void setController(EnrollStudentController mController) {
        setBaseController(mEnrollStudentController);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_enroll_student);
        UstadMobileSystemImplAndroid.getInstanceAndroid().handleActivityCreate(this, savedInstanceState);

        mEnrollStudentController = EnrollStudentController.makeControllerForView(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()));

        setBaseController(mEnrollStudentController);
        setUMToolbar(R.id.um_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Enroll Student");
        Button registerButton = (Button)this.findViewById(R.id.enroll_registerbutton);
        registerButton.setOnClickListener(this);

    }

    @Override
    public void setTitle(String title) {
        mTitle = title;
        super.setTitle(title);
    }

    @Override
    public void setUsernameHint(String loginHint) {
        mUsernameHint = loginHint;
        setTextViewHint(R.id.enroll_registerusername,
                mUsernameHint);

    }

    @Override
    public void setPasswordHint(String passwordHint) {
        mPasswordHint = passwordHint;
        setTextViewHint(R.id.enroll_registerpassword,
                mPasswordHint);
    }

    @Override
    public void setButtonText(String buttonText) {
        mButtonText = buttonText;
        setTextViewHint(R.id.enroll_registerbutton,
                mButtonText);
    }

    @Override
    public void setRegisterPhoneNumberHint(String phoneNumberHint) {
        mRegisterPhoneNumberHint = phoneNumberHint;
        setTextViewHint(R.id.enroll_registerphonenum,
                mRegisterPhoneNumberHint);
    }

    @Override
    public void setRegisterNameHint(String nameHint) {
        mRegisterNameHint = nameHint;
        setTextViewHint(R.id.enroll_registername,
                mRegisterNameHint);
    }

    @Override
    public void setRegisterUsernameHint(String usernameHint) {
        mRegisterUsernameHint = usernameHint;
        setTextViewHint(R.id.enroll_registerusername,
                mRegisterUsernameHint);
    }

    @Override
    public void setRegisterPasswordHint(String passwordHint) {
        mRegisterPasswordHint = passwordHint;
        setTextViewHint(R.id.enroll_registerpassword,
                mRegisterPasswordHint);
    }

    @Override
    public void setRegisterEmailHint(String registerEmailHint) {
        mRegisterEmailHint = registerEmailHint;
        setTextViewHint(R.id.enroll_registeremail,
                mRegisterEmailHint);
    }

    @Override
    public void setRegisterGenderMaleLabel(String maleLabel) {
        mRegisterGenderMaleLabel = maleLabel;
        setTextViewHint(R.id.enroll_register_radio_male,
                mRegisterGenderMaleLabel);
    }

    @Override
    public void setRegisterGenderFemaleLabel(String femaleLabel) {
        mRegisterGenderFemaleLable = femaleLabel;
        setTextViewHint(R.id.enroll_register_radio_female,
                mRegisterGenderFemaleLable);
    }

    @Override
    public void setRegisterButtonText(String registerButtonText) {
        mRegisterButtonText = registerButtonText;
        setTextViewHint(R.id.enroll_registerbutton,
                mRegisterButtonText);
    }

    @Override
    public Object getContext() {
        return this;
    }

    private String getEditTextVal(int viewId) {
        return ((EditText)findViewById(viewId)).getText().toString();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.enroll_registerbutton) {
            System.out.println("So you want to add this?");
            Hashtable userVals = new Hashtable();

            userVals.put(LoginController.REGISTER_PHONENUM,
                    ((EditText) findViewById(R.id.enroll_registerphonenum)).getText().toString());
            userVals.put(LoginController.REGISTER_NAME,
                    ((EditText)findViewById(R.id.enroll_registername)).getText().toString());
            int genderSelectedId = ((RadioGroup)findViewById(R.id.enroll_registergenderradiogroup)).getCheckedRadioButtonId();
            userVals.put(LoginController.REGISTER_GENDER,
                    genderSelectedId == R.id.enroll_register_radio_female ? "f" : "m");
            userVals.put(LoginController.REGISTER_USERNAME, getEditTextVal(R.id.enroll_registerusername));
            userVals.put(LoginController.REGISTER_PASSWORD, getEditTextVal(R.id.enroll_registerpassword));
            userVals.put(LoginController.REGISTER_EMAIL, getEditTextVal(R.id.enroll_registeremail));
            try {
                mEnrollStudentController.handleClickEnroll(userVals);
            }catch(Exception e){
                System.out.println("Exception in enrolling: " + e.toString());
            }
        }
    }
}
