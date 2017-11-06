package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Performs some minor tweaks on HTML being served to enable EPUB pagination and handling html
 * autoplay:
 *  - Add a script immediately after the body tag so that it can, if desired, apply columns style.
 *    This is only applied to HTML which is a top level frame, allownig content in an iframe to work
 *    as expected.
 * - Add a meta viewport tag  -
 *
 */
public class EpubHtmlFilterSerializer {

    private String scriptSrcToAdd;

    private InputStream in;

    public EpubHtmlFilterSerializer() {

    }

    public void setIntput(InputStream in) {
        this.in  = in;
    }

    public String getScriptSrcToAdd() {
        return scriptSrcToAdd;
    }

    public void setScriptSrcToAdd(String scriptSrcToAdd) {
        this.scriptSrcToAdd = scriptSrcToAdd;
    }

    public byte[] getOutput() throws IOException, XmlPullParserException{
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XmlSerializer xs = UstadMobileSystemImpl.getInstance().newXMLSerializer();
        xs.setOutput(bout, "UTF-8");

        XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(in, "UTF-8");
        xs.startDocument("UTF-8", false);
        UMUtil.passXmlThrough(xpp, xs, true, new UMUtil.PassXmlThroughFilter() {
            @Override
            public boolean beforePassthrough(int evtType, XmlPullParser parser, XmlSerializer serializer) {
                return true;
            }

            @Override
            public boolean afterPassthrough(int evtType, XmlPullParser parser, XmlSerializer serializer)
            throws IOException, XmlPullParserException{
                if(evtType == XmlPullParser.START_TAG && parser.getName().equals("body")) {
                    //add the script
                    serializer.startTag(parser.getNamespace(), "script");
                    serializer.attribute(parser.getNamespace(), "src", scriptSrcToAdd);
                    serializer.attribute(parser.getNamespace(), "type", "text/javascript");
                    serializer.text(" ");
                    serializer.endTag(parser.getNamespace(), "script");
                }

                return true;
            }
        });

        xs.endDocument();
        bout.flush();
        return bout.toByteArray();
    }


}
