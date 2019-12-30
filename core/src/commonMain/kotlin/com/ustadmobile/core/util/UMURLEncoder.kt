package com.ustadmobile.core.util

expect class UMURLEncoder {

   companion object{
       /**
        * Encode url string
        */
       fun encodeUTF8(text: String): String

       /**
        * Decode url string
        */
       fun decodeUTF8(text: String): String
   }

}