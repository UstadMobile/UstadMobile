package com.ustadmobile.test.sharedse.view;

import android.content.Intent;
import android.support.test.espresso.intent.rule.IntentsTestRule;

import com.google.gson.Gson;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.Login2Presenter;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.port.android.view.Login2Activity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class Login2ActivityEspressoTest {

    @Rule
    public IntentsTestRule<Login2Activity> mActivityRule =
            new IntentsTestRule<>(Login2Activity.class, false, false);

    public static final String TEST_VALID_USERNAME = "username";

    public static final String TEST_VALID_PASSWORD = "secret";

    public static final String TEST_VALID_AUTH_TOKEN = "token";

    MockWebServer mockRestServer;

    @Before
    public void setUp() throws IOException {
        mockRestServer = new MockWebServer();
        mockRestServer.start();
        mockRestServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                HttpUrl url = request.getRequestUrl();
                if(TEST_VALID_USERNAME.equals(url.queryParameter("user"))
                        && TEST_VALID_PASSWORD.equals(url.queryParameter("password"))) {
                    UmAccount account = new UmAccount(1, TEST_VALID_USERNAME,
                            TEST_VALID_AUTH_TOKEN, "");
                    return new MockResponse()
                            .setHeader("Content-Type", "application/json")
                            .setBody(new Gson().toJson(account));
                }else {
                    return new MockResponse().setResponseCode(204);
                }
            }
        });
    }

    @Test
    public void givenValidUsernameAndPassword_whenLoginClicked_thenShouldFireIntent() {
        Intent launchIntent = new Intent();
        launchIntent.putExtra(Login2Presenter.ARG_SERVER_URL,
                mockRestServer.url("/").toString());
        mActivityRule.launchActivity(launchIntent);

        onView(withId(R.id.activity_login_username)).perform(typeText(TEST_VALID_USERNAME));
        onView(withId(R.id.activity_login_password)).perform(typeText(TEST_VALID_PASSWORD));
        onView(withId(R.id.activity_login_button_login)).perform(click());
    }

}
