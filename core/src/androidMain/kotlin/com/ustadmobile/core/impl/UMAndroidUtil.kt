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


    /**
     * @param map
     * @return
     */
    fun mapToBundle(map: Map<String, String?>): Bundle {
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


}
