/*
  This file is primarily made up of code from java.net.UUID and java.lang 
  packages.  It is under the GPLv2 license only as per the Oracle Java license.
  
*/
package com.ustadmobile.core.util;

import java.util.Random;

/**
 *
 * Basic filler class here because this class is not present in J2ME built by
 * taking the relevant methods from java.util.UUID
 * 
 * @author mike
 */
public class UMUUID {
    public long mostSigBits;
    public long leastSigBits;
    
    final static char[] cDigits = {
          '0' , '1' , '2' , '3' , '4' , '5' ,
          '6' , '7' , '8' , '9' , 'a' , 'b' ,
          'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
          'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
          'o' , 'p' , 'q' , 'r' , 's' , 't' ,
          'u' , 'v' , 'w' , 'x' , 'y' , 'z'
    };
    
    public UMUUID(long mostSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }
    
    public static UMUUID randomUUID() {
        Random r = new Random(System.currentTimeMillis());
        UMUUID uuid = new UMUUID(r.nextLong(), r.nextLong());
        return uuid;
    }
    
    public String toString() {
        return (digits(mostSigBits >> 32, 8) + "-" +
                digits(mostSigBits >> 16, 4) + "-" +
                digits(mostSigBits, 4) + "-" +
                digits(leastSigBits >> 48, 4) + "-" +
                digits(leastSigBits, 12));
    }
    
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }
    
    private static String longToHexString(long i) {
         return toUnsignedString(i, 4);
    }
    
    private static String toUnsignedString(long i, int shift) {
        char[] buf = new char[64];
         int charPos = 64;
         int radix = 1 << shift;
         long mask = radix - 1;
         do {
             buf[--charPos] = cDigits[(int)(i & mask)];
             i >>>= shift;
         } while (i != 0);
         return new String(buf, charPos, (64 - charPos));
    }
    
}
