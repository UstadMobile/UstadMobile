/*
    JSPWiki - a JSP-based WikiWiki clone.

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.    
 */
package com.ustadmobile.core.util;


import java.io.UnsupportedEncodingException;


/**
 *  Contains a number of static utility methods.
 * Taken from 
 * http://www.java2s.com/Tutorial/Java/0320__Network/URLencoding.htm
 */
// FIXME3.0: Move to the "util" package
public final class URLTextUtil
{
    static final String   HEX_DIGITS = "0123456789ABCDEF";

    /**
     *  Private constructor prevents instantiation.
     */
    public URLTextUtil()
    {}
    
    /**
     *  java.net.URLEncoder.encode() method in JDK < 1.4 is buggy.  This duplicates
     *  its functionality.
     *  @param rs the string to encode
     *  @return the URL-encoded string
     */
    protected static String urlEncode( byte[] rs )
    {
        StringBuffer result = new StringBuffer(rs.length*2);

        // Does the URLEncoding.  We could use the java.net one, but
        // it does not eat byte[]s.

        for( int i = 0; i < rs.length; i++ )
        {
            char c = (char) rs[i];

            switch( c )
            {
              case '_':
              case '.':
              case '*':
              case '-':
              case '/':
                result.append( c );
                break;

              case ' ':
                result.append( '+' );
                break;

              default:
                if( (c >= 'a' && c <= 'z') ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= '0' && c <= '9') )
                {
                    result.append( c );
                }
                else
                {
                    result.append( '%' );
                    result.append( HEX_DIGITS.charAt( (c & 0xF0) >> 4 ) );
                    result.append( HEX_DIGITS.charAt( c & 0x0F ) );
                }
            }

        } // for

        return result.toString();
    }

    /**
     *  URL encoder does not handle all characters correctly.
     *  See <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4257115.html">
     *  Bug parade, bug #4257115</A> for more information.
     *  <P>
     *  Thanks to CJB for this fix.
     *  
     *  @param bytes The byte array containing the bytes of the string
     *  @param encoding The encoding in which the string should be interpreted
     *  @return A decoded String
     *  
     *  @throws UnsupportedEncodingException If the encoding is unknown.
     *  @throws IllegalArgumentException If the byte array is not a valid string.
     */
    protected static String urlDecode( byte[] bytes, String encoding )
        throws UnsupportedEncodingException,
               IllegalArgumentException
    {
        if(bytes == null)
        {
            return null;
        }

        byte[] decodeBytes   = new byte[bytes.length];
        int decodedByteCount = 0;

        try
        {
            for( int count = 0; count < bytes.length; count++ )
            {
                switch( bytes[count] )
                {
                  case '+':
                    decodeBytes[decodedByteCount++] = (byte) ' ';
                    break ;

                  case '%':
                    decodeBytes[decodedByteCount++] = (byte)((HEX_DIGITS.indexOf(bytes[++count]) << 4) +
                                                             (HEX_DIGITS.indexOf(bytes[++count])) );

                    break ;

                  default:
                    decodeBytes[decodedByteCount++] = bytes[count] ;
                }
            }

        }
        catch (IndexOutOfBoundsException ae)
        {
            throw new IllegalArgumentException( "Malformed UTF-8 string?" );
        }

        String processedPageName = null ;

        try
        {
            processedPageName = new String(decodeBytes, 0, decodedByteCount, encoding) ;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new UnsupportedEncodingException( "UTF-8 encoding not supported on this platform" );
        }

        return processedPageName;
    }

    /**
     *  As java.net.URLEncoder class, but this does it in UTF8 character set.
     *  
     *  @param text The text to decode
     *  @return An URLEncoded string.
     */
    public static String urlEncodeUTF8( String text )
    {
        // If text is null, just return an empty string
        if ( text == null )
        {
            return "";
        }

        byte[] rs;

        try
        {
            rs = text.getBytes("UTF-8");
            return urlEncode( rs );
        }
        catch( UnsupportedEncodingException e )
        {
            throw new RuntimeException("UTF-8 not supported!?!");
        }

    }

    /**
     *  As java.net.URLDecoder class, but for UTF-8 strings.  null is a safe
     *  value and returns null.
     *  
     *  @param utf8 The UTF-8 encoded string
     *  @return A plain, normal string.
     */
    public static String urlDecodeUTF8( String utf8 )
    {
        String rs = null;

        if( utf8 == null ) return null;

        try
        {
            rs = urlDecode( utf8.getBytes("ISO-8859-1"), "UTF-8" );
        }
        catch( UnsupportedEncodingException e )
        {
            throw new RuntimeException("UTF-8 or ISO-8859-1 not supported!?!");
        }

        return rs;
    }

    /**
     * Provides encoded version of string depending on encoding.
     * Encoding may be UTF-8 or ISO-8859-1 (default).
     *
     * <p>This implementation is the same as in
     * FileSystemProvider.mangleName().
     * 
     * @param data A string to encode
     * @param encoding The encoding in which to encode
     * @return An URL encoded string.
     */
    public static String urlEncode( String data, String encoding )
    {
        // Presumably, the same caveats apply as in FileSystemProvider.
        // Don't see why it would be horribly kludgy, though.
        if( "UTF-8".equals( encoding ) )
        {
            return URLTextUtil.urlEncodeUTF8( data );
        }

        try
        {
            return URLTextUtil.urlEncode( data.getBytes(encoding) );
        }
        catch (UnsupportedEncodingException uee)
        {
            throw new RuntimeException("Could not encode String into" + encoding);
        }
    }

    /**
     * Provides decoded version of string depending on encoding.
     * Encoding may be UTF-8 or ISO-8859-1 (default).
     *
     * <p>This implementation is the same as in
     * FileSystemProvider.unmangleName().
     * 
     * @param data The URL-encoded string to decode
     * @param encoding The encoding to use
     * @return A decoded string.
     * @throws UnsupportedEncodingException If the encoding is unknown
     * @throws IllegalArgumentException If the data cannot be decoded.
     */
    public static String urlDecode( String data, String encoding )
        throws UnsupportedEncodingException,
               IllegalArgumentException
    {
        // Presumably, the same caveats apply as in FileSystemProvider.
        // Don't see why it would be horribly kludgy, though.
        if( "UTF-8".equals( encoding ) )
        {
            return URLTextUtil.urlDecodeUTF8( data );
        }

        try
        {
            return URLTextUtil.urlDecode( data.getBytes(encoding), encoding );
        }
        catch (UnsupportedEncodingException uee)
        {
            throw new RuntimeException("Could not decode String into" + encoding);
        }

    }

}