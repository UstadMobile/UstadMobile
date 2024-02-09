package com.ustadmobile.core.impl

import com.russhwolf.settings.Settings
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import kotlin.js.Date
import com.ustadmobile.door.DoorUri
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.provider.JsStringProvider
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLAnchorElement

/**
 * SystemImpl provides system methods for tasks such as copying files, reading
 * http streams etc. independently of the underlying system.
 *
 *
 * @author mike, kileha3
 * @param jsStringProvider Moko resources JsStringProvider
 */
actual open class UstadMobileSystemImpl(
    settings: Settings,
    langConfig: SupportedLanguagesConfig,
    private val jsStringProvider: JsStringProvider,
): UstadMobileSystemCommon(settings, langConfig) {

    override fun getString(stringResource: StringResource): String {
        return stringResource.localized(
            provider = jsStringProvider,
            locale = langConfig.displayedLocale
        )
    }

    override fun formatString(stringResource: StringResource, vararg args: Any): String {
        return stringResource.localized(
            provider = jsStringProvider,
            locale = langConfig.displayedLocale,
            args = args
        )
    }

    override fun formatPlural(pluralsResource: PluralsResource, number: Int): String {
        return pluralsResource.localized(
            provider = jsStringProvider,
            locale = langConfig.displayedLocale,
            quantity = number,
        )
    }

    /**
     * Return absolute path of the application setup file. Asynchronous.
     *
     * @param context System context
     * @param zip if true, the app setup file should be delivered within a zip.
     */
    actual override suspend fun getAppSetupFile(context: Any, zip: Boolean): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Gives a string with the version number
     *
     * @return String with version number
     */
    actual fun getVersion(context: Any): String {
        return ""
    }

    /**
     * Get the build timestamp
     *
     * @param context System context object
     *
     * @return Build timestamp in ms since epoch
     */
    actual fun getBuildTimestamp(context: Any): Long = Date().getTime().toLong()


    override fun openFileInDefaultViewer(
        context: Any,
        doorUri: DoorUri,
        mimeType: String?,
        fileName: String?,
    ) {
        val aElement = document.createElement("a") as HTMLAnchorElement
        GlobalScope.launch {
            aElement.asDynamic().style.display = "none"
            aElement.href = doorUri.toString()
            fileName?.also { aElement.download = it }
            aElement.click()
        }
    }


    /**
     * The main method used to go to a new view. This is implemented at the platform level. On
     * Android this involves starting a new activity with the arguments being turned into an
     * Android bundle. On J2ME it creates a new Form and shows it, on iOS it looks up the related
     * UIViewController.
     *
     * @param viewName The name of the view to go to: This should match the view's interface .VIEW_NAME constant
     * @param args (Optional) Hahstable of arguments for the new view (e.g. catalog/container url etc)
     * @param context System context object
     */
    actual override fun go(viewName: String, args: Map<String, String?>, context: Any,
                           flags: Int,
                           ustadGoOptions: UstadGoOptions) {
        throw IllegalStateException("Not supported on JS anymore!")
    }


    actual companion object {

    }
}