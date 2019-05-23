package com.ustadmobile.port.android.util

import android.os.Build
import android.os.Bundle
import android.view.View
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

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
    fun bundleToHashtable(bundle: Bundle?): Hashtable<*, *>? {
        if (bundle == null)
            return null

        val retVal = Hashtable<String, Any>()
        val keys = bundle.keySet()
        val iterator = keys.iterator()

        var key: String
        var `val`: Any?
        while (iterator.hasNext()) {
            key = iterator.next()
            `val` = bundle.get(key)
            //TODO: could this not simply be putAll?
            if (`val` is String) {
                retVal.put(key, `val`)
            } else if (`val` is Int) {
                retVal.put(key, `val`)
            } else if (`val` is Array<*>) {
                retVal.put(key, `val`)
            } else if (`val` is Long) {
                retVal.put(key, `val`)
            }
        }

        return retVal
    }

    fun hashtableToBundle(table: Hashtable<*, *>?): Bundle? {
        if (table == null)
            return null

        val bundle = Bundle()

        val iterator = table.keys.iterator()
        var key: String
        var `val`: Any?
        while (iterator.hasNext()) {
            key = iterator.next() as String
            `val` = table[key]
            if (`val` is Int) {
                bundle.putInt(key, (`val` as Int?)!!)
            } else if (`val` is String) {
                bundle.putString(key, `val` as String?)
            } else if (`val` is Array<*>) {
                bundle.putStringArray(key, `val` as Array<String>?)
            } else if (`val` is Long) {
                bundle.putLong(key, (`val` as Long?)!!)
            }
        }
        return bundle

    }

    /**
     * @param map
     * @return
     */
    fun mapToBundle(map: Map<String, String>?): Bundle? {
        if (map == null)
            return null

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
