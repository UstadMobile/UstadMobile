/*
  This file is primarily made up of code from java.net.UUID and java.lang 
  packages.  It is under the GPLv2 license only as per the Oracle Java license.
  
*/
package com.ustadmobile.core.util
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlin.random.Random

/**
 *
 * Basic filler class here because this class is not present in J2ME built by
 * taking the relevant methods from java.util.UUID
 *
 * @author mike
 */
class UMUUID(var mostSigBits: Long, var leastSigBits: Long) {

    override fun toString(): String {
        return digits(mostSigBits shr 32, 8) + "-" +
                digits(mostSigBits shr 16, 4) + "-" +
                digits(mostSigBits, 4) + "-" +
                digits(leastSigBits shr 48, 4) + "-" +
                digits(leastSigBits, 12)
    }

    companion object {

        internal val cDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')

        fun randomUUID(): UMUUID {
            val r = Random(getSystemTimeInMillis())
            return UMUUID(r.nextLong(), r.nextLong())
        }

        private fun digits(`val`: Long, digits: Int): String {
            val hi = 1L shl digits * 4
            return longToHexString(hi or (`val` and hi - 1)).substring(1)
        }

        private fun longToHexString(i: Long): String {
            return toUnsignedString(i, 4)
        }

        private fun toUnsignedString(i: Long, shift: Int): String {
            var i = i
            val buf = CharArray(64)
            var charPos = 64
            val radix = 1 shl shift
            val mask = (radix - 1).toLong()
            do {
                buf[--charPos] = cDigits[(i and mask).toInt()]
                i = i ushr shift
            } while (i != 0L)
            return String(buf, charPos, 64 - charPos)
        }
    }

}
