package com.ustadmobile.port.android.util

import android.content.Context
import android.content.SharedPreferences

class AppPreferences {

    fun setIsLoggedIn(value: Boolean?, context: Context) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        if (value != null) {
            editor?.putString(KEY_IS_LOGGED_IN, value.toString())
        } else {
            editor?.remove(KEY_IS_LOGGED_IN)
        }
        editor?.apply()
    }

    fun getIsLoggedIn(context: Context): Boolean? {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref?.getString(KEY_IS_LOGGED_IN, "false").toBoolean()
    }

     companion object {
         private const val KEY_IS_LOGGED_IN = "isLoggedIn"
         private var sharedPref: SharedPreferences? = null
         private const val PREF_NAME = "prefs"
     }
}

