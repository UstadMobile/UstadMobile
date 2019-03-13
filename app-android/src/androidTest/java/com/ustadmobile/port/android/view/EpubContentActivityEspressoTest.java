package com.ustadmobile.port.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument;
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.EpubContentView;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.port.sharedse.container.ContainerManager;
import com.ustadmobile.port.sharedse.util.UmFileUtilSe;
import com.ustadmobile.test.port.android.UmViewActions;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static android.support.test.espresso.web.sugar.Web.onWebView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.getText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AllOf.allOf;

public class EpubContentActivityEspressoTest {

    @Rule
    public IntentsTestRule<EpubContentActivity> mActivityRule =
            new IntentsTestRule<>(EpubContentActivity.class, false, false);

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private File epubTmpFile;

    private File containerTmpDir;

    private Container epubContainer;

    private ContainerManager epubContainerManager;

    private OpfDocument opfDocument;

    private EpubNavDocument navDocument;

    @Before
    public void setup() throws IOException, XmlPullParserException {
        db = UmAppDatabase.getInstance(InstrumentationRegistry.getTargetContext());
        repo = db.getRepository("http://localhost/dummy/", "");
        db.clearAllTables();

        Context context = InstrumentationRegistry.getTargetContext();

        File storageDir = ContextCompat.getExternalFilesDirs(context, null)[0];
        epubTmpFile = new File(storageDir, "test.epub");
        UmFileUtilSe.extractResourceToFile("/com/ustadmobile/port/android/view/test.epub",
                epubTmpFile);
        containerTmpDir = new File(storageDir, "containertmp");
        boolean dirMade = containerTmpDir.mkdirs();
        System.out.println(dirMade);

        epubContainer = new Container();
        epubContainer.setContainerUid(repo.getContainerDao().insert(epubContainer));

        epubContainerManager = new ContainerManager(epubContainer, db, repo,
                containerTmpDir.getAbsolutePath());
        ZipFile zipFile = new ZipFile(epubTmpFile);
        epubContainerManager.addEntriesFromZip(zipFile, ContainerManager.OPTION_COPY);
        zipFile.close();

        InputStream opfIn = epubContainerManager.getInputStream(
                epubContainerManager.getEntry("OEBPS/package.opf"));
        XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(opfIn,
                "UTF-8");
        opfDocument = new OpfDocument();
        opfDocument.loadFromOPF(xpp);
        opfIn.close();

        InputStream navDocIn = epubContainerManager.getInputStream(
                epubContainerManager.getEntry("OEBPS/nav.html"));
        navDocument = new EpubNavDocument();
        navDocument.load(UstadMobileSystemImpl.getInstance().newPullParser(navDocIn, "UTF-8"));
        navDocIn.close();
    }

    @After
    public void tearDown() {
        epubTmpFile.delete();
        UmFileUtilSe.deleteRecursively(containerTmpDir);
    }

    public void launchActivity() {
        Intent launchIntent = new Intent();
        launchIntent.putExtra(EpubContentView.ARG_CONTAINER_UID,
                String.valueOf(epubContainer.getContainerUid()));
        mActivityRule.launchActivity(launchIntent);
    }

    @Test
    public void givenValidEpub_whenOpened_thenShouldShowContentAndMenu() {
        launchActivity();

        //Ensure that Espresso can see the progress bar - so it waits for this to be idle
        SystemClock.sleep(1000);

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.um_toolbar))))
                .check(matches(withText(opfDocument.getTitle())));
        onView(withId(R.id.container_drawer_layout)).perform(DrawerActions.open(Gravity.END));

        String firstNavTitle = navDocument.getToc().getChild(0).getTitle();
        onView(allOf(withId(R.id.expandedListItem), withText(firstNavTitle)))
                .check(matches(isDisplayed()));
    }


    @Test
    public void givenValidEpubOpen_whenClickOnNavigationDrawItem_thenShouldOpenPage() {
        launchActivity();

        //Ensure that Espresso can see the progress bar - so it waits for this to be idle
        SystemClock.sleep(1000);

        onView(withId(R.id.container_drawer_layout)).perform(DrawerActions.open(Gravity.END));
        onView(allOf(withId(R.id.expandedListItem), withText("Page 3")))
                .perform(click());

        //Check that the requested page has loaded - look for the page number footer in this epub
        onWebView(allOf(withId(R.id.fragment_container_page_webview), withTagValue(equalTo(2))))
                .withElement(findElement(Locator.CLASS_NAME, "page_number"))
                .check(webMatches(getText(), containsString("3")));
    }

    @Test
    public void givenValidEpubOpen_whenSingleTapOnContent_thenActionBarShouldHideAndShow() {
        launchActivity();

        //Ensure that Espresso can see the progress bar - so it waits for this to be idle
        SystemClock.sleep(1000);
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.um_toolbar))))
                .check(matches(withText(opfDocument.getTitle())));

        //When we single tap on the content, the toolbar should go away
        onView(allOf(withId(R.id.fragment_container_page_webview), withTagValue(equalTo(0))))
                .perform(UmViewActions.singleTap(200, 200));

        SystemClock.sleep(1000);
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.um_toolbar))))
                .check(matches(not(isDisplayed())));

        //When we single tap again, the toolbar should come back
        SystemClock.sleep(1000);
        onView(allOf(withId(R.id.fragment_container_page_webview), withTagValue(equalTo(0))))
                .perform(UmViewActions.singleTap(200, 200));

        SystemClock.sleep(1000);
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.um_toolbar))))
                .check(matches(isDisplayed()));

    }





}
