/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */

package com.ustadmobile.port.j2me.view;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Painter;
import com.sun.lwuit.geom.Rectangle;

/**
 * Used by AppViewJ2ME to put a Notification box up
 * 
 * @author mike
 */
public class NotificationPainter implements Painter{

    private String text;
    
    private static final int BGCOLOR = 0x888888;
    private static final int FGCOLOR = 0xffffff;

    public synchronized String getText() {
        return text;
    }

    public synchronized void setText(String text) {
        this.text = text;
    }
    
    public static int PADDING_BOTTOM = 20;
    
    public void paint(Graphics g, Rectangle rect) {
        final int strHeight = g.getFont().getHeight();
        final String textStr = getText();
        final int strWidth = Math.min(g.getFont().stringWidth(textStr), 
                rect.getSize().getWidth());
        final int x = (rect.getSize().getWidth() - strWidth)/2;
        final int y = rect.getSize().getHeight() - strHeight - PADDING_BOTTOM;
        
        g.setColor(BGCOLOR);
        g.fillRect(x-2, y-2, strWidth+4, strHeight+4);
        
        g.setColor(FGCOLOR);
        g.drawString(textStr, x, y);
    }
    
}
