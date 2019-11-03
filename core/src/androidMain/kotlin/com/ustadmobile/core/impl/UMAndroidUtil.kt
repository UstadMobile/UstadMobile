package com.ustadmobile.core.impl

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.squareup.picasso.Picasso
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

/**
 * Created by mike on 9/21/15.
 */
object UMAndroidUtil {

    private val sNextGeneratedId = AtomicInteger(1)

    /**
     * Set the direction of a given view if we are running on a version of Android that supports
     * this : support for directionality in views was added in Android 4.2
     *
     * @param view
     * @param direction
     */
    fun setDirectionIfSupported(view: View, direction: Int) {
        if (Build.VERSION.SDK_INT >= 17) {
            view.layoutDirection = direction
        }
    }


    /**
     * Convert an Android bundle to a hashtable
     *
     * @param bundle
     * @return
     */
    fun bundleToHashtable(bundle: Bundle?): HashMap<String, String>? {
        if (bundle == null)
            return null

        val retVal = HashMap<String, String>()
        val keys = bundle.keySet()
        val iterator = keys.iterator()

        var key: String
        var keyVal : Any?
        while (iterator.hasNext()) {
            key = iterator.next()
            keyVal = bundle.get(key)
            //TODO: could this not simply be putAll?
            when (keyVal) {
                is String -> retVal[key] = keyVal
                is Int -> retVal[key] = keyVal.toString()
                is Long -> retVal[key] = keyVal.toString()
            }
        }

        return retVal
    }


    /**
     * @param map
     * @return
     */
    fun mapToBundle(map: Map<String, String?>): Bundle? {

        val bundle = Bundle()
        for ((key, value) in map) {
            bundle.putString(key, value)
        }

        return bundle
    }

    fun bundleToMap(bundle: Bundle?): Map<String, String> {
        if (bundle == null)
            return HashMap()

        val keys = bundle.keySet()
        val map = HashMap<String, String>()
        for (key in keys) {
            val `val` = bundle.get(key)
            if (`val` is String) {
                map[key] = `val`
            }
        }

        return map
    }


    /**
     * Android normally but not always surrounds an SSID with quotes on it's configuration objects.
     * This method simply removes the quotes, if they are there. Will also handle null safely.
     *
     * @param ssid
     * @return
     */
    fun normalizeAndroidWifiSsid(ssid: String?): String? {
        return ssid?.replace("\"", "") ?: ssid
    }


    /**
     * Get file mime type
     * @param context application context
     * @param uri Uri to be resolved
     * @return mime type of the file
     */
    fun getMimeType(context: Context, @NonNull uri: Uri): String? {
        val mimeType: String?
        mimeType = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cr = context.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase())
        }
        return mimeType
    }

    /**
     * Get current device locale
     * @param context Application context
     * @return current locale
     */
    @JvmStatic
    fun getCurrentLocale(context: Context): String {
        return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            context.resources.configuration.locales.get(0)
        else
            context.resources.configuration.locale).toString()
    }

    /**
     * Get current device language directionality
     * @param context Application context
     * @return current directionality i.e rtl or ltr
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @JvmStatic
    fun getDirectionality(context: Context): String {
        val config = context.resources.configuration
        return if (config.layoutDirection == View.LAYOUT_DIRECTION_RTL) "rtl" else "ltr"
    }

    /**
     * Get actionbar size
     * @param context application context
     * @return toolbar height
     */
    fun getActionBarSize(context: Context): Int {
        val tv = TypedValue()
        return if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data,
                    context.resources.displayMetrics)
        } else 0
    }

    /**
     * Get device display width
     * @param activity Active fragment activity
     * @return width
     */
    fun getDisplayWidth(activity: FragmentActivity): Int {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val density = activity.resources.displayMetrics.density
        return (outMetrics.widthPixels / density).roundToInt()
    }

    /**
     * Covert density pixels to pixels
     * @param dp density pixels to be converted
     * @return converted pixels
     */
    fun convertDpToPixel(dp: Int): Int {
        val metrics = Resources.getSystem().displayMetrics
        return (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    /**
     * Convert pixels to density pixels
     * @param px pixels to be converted
     * @return converted density pixels
     */
    fun convertPixelsToDp(px: Float): Int {
        val metrics = Resources.getSystem().displayMetrics
        return (px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    /**
     * Gets number of rows to be displayed as per screen size
     * @param width Width of a single item
     * @return number of columns which will fit the screen
     */
    fun getSpanCount(activity: FragmentActivity, @NonNull width: Int?): Int {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val density = activity.resources.displayMetrics.density
        val dpWidth = outMetrics.widthPixels / density
        return (dpWidth / width!!).roundToInt()
    }

    /**
     * Load image to an image view
     * @param imageUrl image remote URL
     * @param placeHolder place holder image in case of error/loading progress
     * @param targetView target view whenre image should be loaded to
     */
    fun loadImage(imageUrl: String?, placeHolder: Int, targetView: ImageView){
        Picasso.get()
                .load(if(imageUrl == null || imageUrl.isEmpty()) "empty" else imageUrl)
                .placeholder(placeHolder)
                .into(targetView)
    }

}
