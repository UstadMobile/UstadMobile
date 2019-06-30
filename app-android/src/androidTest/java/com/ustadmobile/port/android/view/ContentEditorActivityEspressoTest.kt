package com.ustadmobile.port.android.view


import android.Manifest
import android.content.Intent
import androidx.core.widget.NestedScrollView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.web.assertion.WebViewAssertions.webContent
import androidx.test.espresso.web.matcher.DomMatchers
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ContentEditorView
import com.ustadmobile.core.view.ContentEditorView.Companion.TEXT_FORMAT_TYPE_BOLD
import com.ustadmobile.core.view.ContentEditorView.Companion.TEXT_FORMAT_TYPE_ITALIC
import com.ustadmobile.lib.db.entities.ContentEntry
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Test class which tests [ContentEditorActivity] to make sure it behaves as expected
 * under different circumstances when editing mode is ON
 *
 * @author kileha3
 */

@RunWith(AndroidJUnit4::class)
class ContentEditorActivityEspressoTest {

    private lateinit var contentOptionBottomSheet: BottomSheetBehavior<NestedScrollView>

    private lateinit var multimediaSourceBottomSheet: BottomSheetBehavior<NestedScrollView>

    @get:Rule
    public val mActivityRule: ActivityTestRule<ContentEditorActivity> = object :
            ActivityTestRule<ContentEditorActivity>(ContentEditorActivity::class.java, false, true) {
        override fun getActivityIntent(): Intent {
            val mContext = InstrumentationRegistry.getInstrumentation().targetContext
            val intent = Intent(mContext, ContentEditorActivity::class.java)
            intent.putExtra(ContentEditorView.CONTENT_ENTRY_UID, CONTENT_ENTRY_UID.toString())
            intent.putExtra(ContentEditorView.CONTENT_STORAGE_OPTION , "")
            return intent
        }

    }

    @get:Rule
    public val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)
    @Before
    fun setUp() {
        contentOptionBottomSheet = mActivityRule.activity.contentOptionsBottomSheetBehavior!!
        multimediaSourceBottomSheet = mActivityRule.activity.mediaSourceBottomSheetBehavior!!

        val mContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database = UmAppDatabase.getInstance(mContext)
        database.clearAllTables()
        val entry = ContentEntry("Sample title", "Sample description", leaf = true, publik = true)
        entry.contentEntryUid = CONTENT_ENTRY_UID
        database.contentEntryDao.insert(entry)

        Thread.sleep(TimeUnit.SECONDS.toMillis(10))
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenButtonToSwitchEditingModeOn_whenClicked_thenShouldTurnEditingModeOn() {

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        assertTrue("Editing mode was switched ON successfully",
                mActivityRule.activity.isEditorInitialized)
    }


    @Test
    @Throws(InterruptedException::class)
    fun givenInsertContentIconIsClicked_whenEditingIsEnabledAndBottomSheetIsCollapsed_thenShouldExpandContentOptionBottomSheet() {

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))
        onView(withId(R.id.content_action_insert)).perform(click())

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        assertTrue("Editing mode was enabled",
                mActivityRule.activity.isEditorInitialized)

        assertEquals("Content option BottomSheet was expanded",
                contentOptionBottomSheet.state, BottomSheetBehavior.STATE_EXPANDED)
    }


    @Test
    @Throws(InterruptedException::class)
    fun givenFormatTypeMenuIsClicked_whenEditingIsEnabled_thenShouldActivateAndApplyFormatting() {


        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))
        //add content to the editor
        mActivityRule.activity.insertTestContent("Dummy Text on Editor")

        Thread.sleep(TimeUnit.SECONDS.toMillis(1))
        //Select all added content
        mActivityRule.activity.selectAllTestContent()

        //apply formatting (Bold & Italic)
        onView(withId(R.id.content_action_bold)).perform(click())
        onView(withId(R.id.content_action_italic)).perform(click())

        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        //Get formatting type reference
        val umFormatHelper = mActivityRule.activity.umFormatHelper!!
        val boldFormat = umFormatHelper.getFormatByCommand(TEXT_FORMAT_TYPE_BOLD)!!
        val italicFormat = umFormatHelper.getFormatByCommand(TEXT_FORMAT_TYPE_ITALIC)!!


        assertTrue("Editing mode was enabled",
                mActivityRule.activity.isEditorInitialized)

        assertTrue("Bold formatting was applied to the text", boldFormat.active)

        assertTrue("Italic formatting was applied to the text", italicFormat.active)
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenMultipleChoiceButtonIsClicked_whenEditingIsEnabled_thenShouldInsertMultipleChoiceTemplate() {


        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        mActivityRule.activity.clearAll()

        //open content chooser
        onView(withId(R.id.content_action_insert)).perform(click())
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        //insert multiple-choice question
        onView(withId(R.id.content_option_multiplechoice)).perform(click())
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        assertTrue("Editing mode was enabled",
                mActivityRule.activity.isEditorInitialized)


        onWebView().withTimeout(10000, TimeUnit.MILLISECONDS)
                .check(webContent(DomMatchers.hasElementWithXpath("//div[contains(@data-um-widget, 'multi-choice')]")))
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenInsertLinkButtonIsClicked_whenEditorIsEnabled_thenShouldInsertLinkToTheSelectedText() {

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        //clear field
        mActivityRule.activity.selectAllTestContent()
        mActivityRule.activity.clearAll()

        mActivityRule.activity.insertTestContent("Ustadmobile")

        //Select all content
        mActivityRule.activity.selectAllTestContent()
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        //open content chooser
        onView(withId(R.id.content_action_insert)).perform(click())
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        //insert link
        onView(withId(R.id.content_option_link)).perform(click())
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        onView(withId(R.id.linkUrl))
                .perform(typeText("ustadmobile.com"))
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        onView(withText(mActivityRule.activity.getString(R.string.content_editor_link_insert)))
                .inRoot(isDialog()).check(matches(isDisplayed())).perform(click())

        onWebView().withTimeout(10000, TimeUnit.MILLISECONDS)
                .check(webContent(DomMatchers.hasElementWithXpath("//a[contains(@href, 'ustadmobile.com')]")))

    }


    @Test
    @Throws(InterruptedException::class)
    fun givenFillInTheBlanksChoiceButtonIsClicked_whenEditingIsEnabled_thenShouldInsertFillInTheBlanksTemplate() {


        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        //open content chooser
        onView(withId(R.id.content_action_insert)).perform(click())
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        //insert fill in the blanks question
        onView(withId(R.id.content_option_filltheblanks)).perform(click())
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        assertTrue("Editing mode was enabled",
                mActivityRule.activity.isEditorInitialized)

        onWebView().withTimeout(10000, TimeUnit.MILLISECONDS)
                .check(webContent(DomMatchers.hasElementWithXpath("//div[contains(@data-um-widget, 'fill-the-blanks')]")))
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenMultimediaChoiceIsClicked_whenEditingIsEnabled_thenShouldLetUserChooseTheSource() {

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        //open content chooser
        onView(withId(R.id.content_action_insert)).perform(click())
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        //insert multiple-choice question
        onView(withId(R.id.content_option_multimedia)).perform(click())
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME.toLong()))

        assertTrue("Editing mode was enabled",
                mActivityRule.activity.isEditorInitialized)

        assertEquals("Multimedia source bottom sheen was expanded",
                multimediaSourceBottomSheet.state, BottomSheetBehavior.STATE_EXPANDED)

    }

    companion object {

        private const val MAX_WAIT_TIME = 50

        private const val CONTENT_ENTRY_UID = 12L
    }

}
