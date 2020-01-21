package com.ustadmobile.core.util

class ArgumentUtil{


    companion object {


        /**
         * Converts  given CSV String to a list of long
         */
        fun convertCSVStringToLongList(csString: String): List<Long> {
            val list = ArrayList<Long>()
            for (s in csString.split((",").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()) {
                val p = s.trim()
                list.add(p.toLong())
            }

            return list
        }

        /**
         * Convert given Long List to CSV String
         * @param longList    Long list
         */
        fun convertLongListToStringCSV(longList: List<Long>): String {
            return longList.toString().replace("\\[|\\]".toRegex(), "")
        }
    }
}