package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.platform.app.InstrumentationRegistry
import com.toughra.ustadmobile.R
import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.test.port.android.UmViewActions
import org.hamcrest.CoreMatchers.*
import org.hamcrest.core.AllOf.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

class EpubContentActivityEspressoTest {

    @get:Rule
    var mActivityRule = IntentsTestRule(EpubContentActivity::class.java, false, false)

    private var db: UmAppDatabase? = null

    private var repo: UmAppDatabase? = null

    private var epubTmpFile: File? = null

    private var containerTmpDir: File? = null

    private var epubContainer: Container? = null

    private var epubContainerManager: ContainerManager? = null

    private var opfDocument: OpfDocument? = null

    private var navDocument: EpubNavDocument? = null

    @Before
    @Throws(IOException::class, XmlPullParserException::class)
    fun setup() {
        db = UmAppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().context)
        repo = UmAppDatabase.getInstance(InstrumentationRegistry.getInstrumentation().context) //db!!.getUmRepository("http://localhost/dummy/", "")
        db!!.clearAllTables()

        val context = InstrumentationRegistry.getInstrumentation().context

        val storageDir = ContextCompat.getExternalFilesDirs(context, null)[0]
        epubTmpFile = File(storageDir, "test.epub")
        UmFileUtilSe.extractResourceToFile("/com/ustadmobile/port/android/view/test.epub",
                epubTmpFile!!)
        containerTmpDir = File(storageDir, "containertmp")
        val dirMade = containerTmpDir!!.mkdirs()
        println(dirMade)

        epubContainer = Container()
        epubContainer!!.containerUid = repo!!.containerDao.insert(epubContainer!!)

        epubContainerManager = ContainerManager(epubContainer!!, db!!, repo!!,
                containerTmpDir!!.absolutePath)
        addEntriesFromZipToContainer(epubTmpFile!!.absolutePath, epubContainerManager!!)

        val opfIn = epubContainerManager!!.getInputStream(
                epubContainerManager!!.getEntry("OEBPS/package.opf")!!)
        val xpp = UstadMobileSystemImpl.instance.newPullParser(opfIn,
                "UTF-8")
        opfDocument = OpfDocument()
        opfDocument!!.loadFromOPF(xpp)
        opfIn.close()

        val navDocIn = epubContainerManager!!.getInputStream(
                epubContainerManager!!.getEntry("OEBPS/nav.html")!!)
        navDocument = EpubNavDocument()
        navDocument!!.load(UstadMobileSystemImpl.instance.newPullParser(navDocIn, "UTF-8"))
        navDocIn.close()
    }

    @After
    fun tearDown() {
        epubTmpFile!!.delete()
        UmFileUtilSe.deleteRecursively(containerTmpDir!!)
    }

    fun launchActivity() {
        val launchIntent = Intent()
        launchIntent.putExtra(EpubContentView.ARG_CONTAINER_UID,
                epubContainer!!.containerUid.toString())
        mActivityRule.launchActivity(launchIntent)
    }

    @Test
    fun givenValidEpub_whenOpened_thenShouldShowContentAndMenu() {
        launchActivity()

        //Ensure that Espresso can see the progress bar - so it waits for this to be idle
        SystemClock.sleep(1000)

        onView(allOf<View>(instanceOf<View>(AppCompatTextView::class.java), withParent(withId(R.id.um_toolbar))))
                .check(matches(withText(opfDocument!!.title)))
        onView(withId(R.id.container_drawer_layout)).perform(DrawerActions.open(Gravity.END))

        val firstNavTitle = navDocument!!.toc?.getChild(0)?.title
        onView(allOf<View>(withId(R.id.expandedListItem), withText(firstNavTitle)))
                .check(matches(isDisplayed()))
    }


    @Test
    fun givenValidEpubOpen_whenClickOnNavigationDrawItem_thenShouldOpenPage() {
        launchActivity()

        //Ensure that Espresso can see the progress bar - so it waits for this to be idle
        SystemClock.sleep(1000)

        onView(withId(R.id.container_drawer_layout)).perform(DrawerActions.open(Gravity.END))
        onView(allOf<View>(withId(R.id.expandedListItem), withText("Page 3")))
                .perform(click())

        //Check that the requested page has loaded - look for the page number footer in this epub
        onWebView(allOf<View>(withId(R.id.fragment_container_page_webview), withTagValue(equalTo(2))))
                .withElement(findElement(Locator.CLASS_NAME, "page_number"))
                .check(webMatches(getText(), containsString("3")))
    }

    @Test
    fun givenValidEpubOpen_whenSingleTapOnContent_thenActionBarShouldHideAndShow() {
        launchActivity()

        //Ensure that Espresso can see the progress bar - so it waits for this to be idle
        SystemClock.sleep(1000)
        onView(allOf<View>(instanceOf<View>(AppCompatTextView::class.java), withParent(withId(R.id.um_toolbar))))
                .check(matches(withText(opfDocument!!.title)))

        //When we single tap on the content, the toolbar should go away
        onView(allOf<View>(withId(R.id.fragment_container_page_webview), withTagValue(equalTo(0))))
                .perform(UmViewActions.singleTap(200f, 200f))

        SystemClock.sleep(1000)
        onView(allOf<View>(instanceOf<View>(AppCompatTextView::class.java), withParent(withId(R.id.um_toolbar))))
                .check(matches(not<View>(isDisplayed())))

        //When we single tap again, the toolbar should come back
        SystemClock.sleep(1000)
        onView(allOf<View>(withId(R.id.fragment_container_page_webview), withTagValue(equalTo(0))))
                .perform(UmViewActions.singleTap(200f, 200f))

        SystemClock.sleep(1000)
        onView(allOf<View>(instanceOf<View>(AppCompatTextView::class.java), withParent(withId(R.id.um_toolbar))))
                .check(matches(isDisplayed()))

    }


}
