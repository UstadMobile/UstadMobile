package com.ustadmobile.core.util

import kotlin.jvm.JvmStatic

expect class URLEncoder {

   companion object{
       /**
        * Encode url string
        */
       fun encodeUTF8(text: String?): String

       /**
        * Decode url string
        */
       fun decodeUTF8(text: String?): String
   }

}