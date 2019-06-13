package com.ustadmobile.port.android.view;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.Login2Presenter;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.port.android.sync.UmAppDatabaseSyncWorker;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Login2Activity extends UstadBaseActivity implements Login2View,
        FingerprintAuthenticationHelper.FingerprintHelperListener {

    private Login2Presenter mPresenter;

    private String mServerUrl;

    private TextView mUsernameTextView;

    private TextView mPasswordTextView;

    private TextView mErrorTextView;

    private ProgressBar mProgressBar;

    private Button mLoginButton;

    private CheckBox assignToFingerprintCB;

    private boolean assignToFingerprint = false;

    //Testing
    private ImageView fingerprintIV;
    // Declare a string variable for the key we’re going to use in our fingerprint authentication
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCheckLogout(false);

        setContentView(R.layout.activity_login2);
        setSupportActionBar(findViewById(R.id.um_toolbar));

        mUsernameTextView = findViewById(R.id.activity_login_username);
        mPasswordTextView = findViewById(R.id.activity_login_password);

        mPresenter = new Login2Presenter(this, UMAndroidUtil.bundleToHashtable(
                getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));


        mLoginButton = findViewById(R.id.activity_login_button_login);
        mErrorTextView = findViewById(R.id.activity_login_errormessage);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setScaleY(3f);

        assignToFingerprintCB = findViewById(R.id.activity_login2_assign_fingerprint_checbox);
        assignToFingerprintCB.setOnCheckedChangeListener((buttonView, isChecked) ->
                assignToFingerprint = isChecked);

        findViewById(R.id.activity_login_button_login).setOnClickListener(
                (evt) -> mPresenter.handleClickLogin(mUsernameTextView.getText().toString(),
                        mPasswordTextView.getText().toString(), mServerUrl, assignToFingerprint));

        fingerprintIV = findViewById(R.id.activity_login2_fingerprint_imageview);
        fingerprintIV.setOnClickListener(v -> mPresenter.handleClickFingerPrint());

        checkFingerprint();
    }

    private void checkFingerprint(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            keyguardManager =
                    (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager =
                    (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            boolean fpok=false;
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
                //Check whether the device has a fingerprint sensor//
                fingerprintIV.setVisibility(View.GONE);
                assignToFingerprintCB.setVisibility(View.GONE);
                //("Your device doesn't support fingerprint authentication");

            }else{
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    sendToast("No fingerprint configured. " +
                            "Please register at least one fingerprint in your device's Settings");
                }else {
                    fingerprintIV.setVisibility(View.VISIBLE);
                    assignToFingerprintCB.setVisibility(View.VISIBLE);
                    fpok=true;
                    //Check that the user has registered at least one fingerprint//
                }
            }
            //Check whether the user has granted your app the USE_FINGERPRINT permission//
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // If your app doesn't have this permission, then display the following text//
                sendToast("Please enable the fingerprint permission");
            }

            //Check that the lock screen is secured//
            if (!keyguardManager.isKeyguardSecure() && fpok == true) {
                sendToast("Please enable lock screen security in your device's Settings");
            } else {
                try {
                    generateKey();
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }

                if (initCipher()) {
                    //If the cipher is initialized successfully, then create a CryptoObject instance//
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    FingerprintAuthenticationHelper helper =
                            new FingerprintAuthenticationHelper(this, this);
                    helper.startAuth(fingerprintManager, cryptoObject);
                }
            }
        }
    }

    /**
     * Create the generateKey method that we’ll use to gain access to the Android
     *  keystore and generate the encryption key
     * @throws FingerprintException Exception fingerprint
     */
    private void generateKey() throws FingerprintException {
        try {
            // Obtain a reference to the Keystore
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore");

            //Initialize an empty KeyStore//
            keyStore.load(null);

            //Initialize the KeyGenerator//
            keyGenerator.init(new

                //Specify the operation(s) this key can be used for//
                KeyGenParameterSpec.Builder(KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT |
                        KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build()
            );

            //Generate the key//
            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    //Create a new method that we’ll use to initialize our cipher//
    public boolean initCipher() {
        try {

            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the cipher has been initialized successfully//
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {

            //Return false if cipher initialization failed//
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    @Override
    public void authenticationSuccess(FingerprintManager.AuthenticationResult result) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String fpUsername = UmAccountManager.getFingerprintUsername(getContext(), impl);
        String fpAuth = UmAccountManager.getFingerprintAuth(getContext(), impl);
        if(fpUsername!= null && !fpUsername.isEmpty()){
            mPresenter.loginOKFromOtherSource(mServerUrl, fpAuth);
        }else{
            sendToast("Login not registered with fingerprint.");
        }
    }

    @Override
    public void authenticationSuccess(FingerprintManager.AuthenticationResult result, UmAccount account) {
        //Does nothing.
    }

    @Override
    public void authenticationFailed(String error) {
        //Does nothing.
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }

    @Override
    public void setInProgress(boolean inProgress) {
        mProgressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        mPasswordTextView.setEnabled(!inProgress);
        mUsernameTextView.setEnabled(!inProgress);
        mLoginButton.setEnabled(!inProgress);
        mLoginButton.getBackground().setAlpha(inProgress ? 128 : 255 );
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        mErrorTextView.setText(errorMessage);
    }

    @Override
    public void setServerUrl(String serverUrl) {
        this.mServerUrl = serverUrl;
    }

    @Override
    public void setPassword(String password) {
        mPasswordTextView.setText(password);
    }

    public void sendToast(String message) {
        runOnUiThread(() -> Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show());
    }

    @Override
    public void forceSync() {
        WorkManager.getInstance().cancelAllWorkByTag(UmAppDatabaseSyncWorker.TAG);
        UmAppDatabaseSyncWorker.queueSyncWorkerWithPolicy(100, TimeUnit.MILLISECONDS,
                ExistingWorkPolicy.APPEND);
        sendToast("Sync started");
        WorkManager.getInstance().getWorkInfosByTagLiveData(UmAppDatabaseSyncWorker.TAG).observe(
                this, workInfos -> {
                    for(WorkInfo wi:workInfos){
                        if(wi.getState().isFinished()){
                            //sendToast("Sync finished");
                        }
                    }
                });
    }

    @Override
    public void updateLastActive() {
        AtomicLong systemTime = new AtomicLong(System.currentTimeMillis());
        updateLastActive(systemTime);
    }

    @Override
    public void updateUsername(String username) {
        mUsernameTextView.setText(username);
        mUsernameTextView.setFocusable(false);
        mPasswordTextView.setFocusable(true);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //mPasswordTextView.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    @Override
    public void setFinishAfficinityOnView() {
        runOnUiThread(() -> finishAffinity());
    }
}
