/*
 *  Copyright ? 2008, 2010, Oracle and/or its affiliates. All rights reserved
 */
package com.sun.lwuit.browser;

import com.sun.lwuit.Button;
import com.sun.lwuit.Component;
import com.sun.lwuit.Display;
import com.sun.lwuit.Font;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.Painter;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.html.DocumentRequestHandler;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.html.HTMLElement;

/**
 * This extension of HTMLComponent adds a cursor/pointer to allow easier navigation in non-touch screens
 *
 * @author Ofir Leitner
 */
public class ExtHTMLComponent extends HTMLComponent{

    PointerGlassPane pointerPane = null;
    Image pointerIcon;
    Image pointerIconLink;

    ExtHTMLComponent(DocumentRequestHandler handler) {
        super(handler);
    }

    /**
     * Checks whether pointer mode is enabled
     *
     * @return true if pointer mode is on, false otherwise
     */
    public boolean isPointerEnabled() {
        return (pointerPane!=null);
    }

    /**
     * Toggles pointer mode
     *
     * @param enable true to enable pointer mode, false to disabled it
     */
    public void setPointerEnabled(boolean enable) {
        if ((enable) && (pointerPane==null)) {
            pointerPane=new PointerGlassPane(this);
            if (getComponentForm()!=null) {
                pointerPane.y=getScrollY();
                getComponentForm().setGlassPane(pointerPane);
                getComponentForm().setFocused(this);
            }


        } else if ((!enable) && (pointerPane!=null)) {
            if (getComponentForm()!=null) {
                getComponentForm().setGlassPane(null);
                if (pointerPane.lastFocused!=null) {
                    getComponentForm().setFocused(pointerPane.lastFocused);
                }
            }
            pointerPane=null;
        }
    }

    /**
     * Sets the icons to use as the pointer in regular mode and when it's over a link
     * If the linkIcon is null, the regularIcon will be used instead all the time
     * If both icons are null, a default painted triangle icon will be displayed.
     *
     * @param regularIcon The icon to use for the pointer when it is not hovering above a link
     * @param linkIcon The icon to use for the pointer when it is hovering above a link
     */
    public void setPointerIcon(Image regularIcon,Image linkIcon) {
        pointerIcon=regularIcon;
        pointerIconLink=linkIcon;
        if (pointerPane!=null) {
            repaint();
        }
    }

    /**
     * Should be called when the page status is PAGE_DISPLAYED, to enable reinstating the pointer glass pane if it's turned on
     */
    public void pageDisplayed() {
        if (getComponentForm()!=null) {
            if (pointerPane!=null) {
                getComponentForm().setGlassPane(pointerPane);
                getComponentForm().setFocused(this);

                pointerPane.x=0;
                pointerPane.y=0;
            }
        }
    }


    // ************
    // Component initialization and deinitialization
    // ************

    /**
     * Overrides initComponent to reinstate the pointer glass pane when needed
     */
    protected void initComponent() {
        super.initComponent();
        if (pointerPane!=null) {
            getComponentForm().setGlassPane(pointerPane);
            getComponentForm().setFocused(this);
        }
    }



    /**
     * Overrides deinitialize to remove the pointer glass pane
     */
    protected void deinitialize() {
        super.deinitialize();
        if (pointerPane!=null) {
            getComponentForm().setGlassPane(null);
        }
    }


    // ************
    // Input management - Overriding keys to relay them to the pointer glass pane
    // ************

    /**
     * {@inheritDoc}
     */
    public boolean handlesInput() {
        return (pointerPane!=null);
    }

    /**
     * {@inheritDoc}
     */
    public void keyPressed(int k) {
        if (pointerPane!=null) {
            int action=Display.getInstance().getGameAction(k);
            if (pointerPane.move(action)) {
                return;
            }
        }
        super.keyPressed(k);
    }

    /**
     * {@inheritDoc}
     */
    public void keyReleased(int k) {
        if (pointerPane!=null) {
            int action=Display.getInstance().getGameAction(k);
            if ((action==Display.GAME_DOWN) || (action==Display.GAME_UP) || (action==Display.GAME_LEFT) || (action==Display.GAME_RIGHT) ||
                    (action==Display.GAME_FIRE)) {
                return;
            }
        }
        super.keyReleased(k);
    }


    // ************
    // Scroll management
    // ************

    /** The following overrides the various public scroll methodsto update the pointer glass pane that the page was scrolled
     * And also to disable animated scrolling which is currently not supported in pointer mode
     */

    /**
     * {@inheritDoc}
     */
    public void scrollPages(int pages, boolean animate) {
        super.scrollPages(pages, ((animate) && (pointerPane==null)));
        scrollPane();
    }

    /**
     * {@inheritDoc}
     */
    public void scrollPixels(int pixels, boolean animate) {
        super.scrollPixels(pixels, ((animate) && (pointerPane==null)));
        scrollPane();
    }

    /**
     * {@inheritDoc}
     */
    public void scrollToElement(HTMLElement element, boolean animate) {
        super.scrollToElement(element, ((animate) && (pointerPane==null)));
        scrollPane();
    }

    /**
     * Notifies the pointer pane that the scrollY location changed
     */
    private void scrollPane() {
        if (pointerPane!=null) {
            pointerPane.pageScrolled(getScrollY());
        }
    }


    // ************
    // The PointerGlassPane inner class
    // ************


    /**
     * PointerGlassPane is a glass pane that draws the pointer and handles its movements, link highlighting and more.
     * 
     * @author Ofir Leitner
     */
    class PointerGlassPane implements Painter {

        /**
         * The default pointer size in pixels
         */

        int step=Font.getDefaultFont().charWidth('W'); // The pointer's movement step (determined relatively to the system default font size)
        int defaultPointerSize = step;

        ExtHTMLComponent htmlC; // The associated ExtHTMLComponent
        Component lastFocused; // Last focused component
        int x,y; // pointer coordinates


        public PointerGlassPane(ExtHTMLComponent cmp) {
            this.htmlC=cmp;
        }

        /**
         * Updates the pointer according to a new scrollY poisition
         * 
         * @param newY The new scrollY
         */
        void pageScrolled(int newY) {
            y=newY;
            checkLinks();
        }

        /**
         * Moves the pointer according to the key action (if applicable)
         * 
         * @param action The key action
         * @return true if the key was handled by the pointer glass pane, false otherwise
         */
        boolean move(int action) {
            boolean actionKey=false;
            boolean moved=false;
            if (action==Display.GAME_LEFT) {
                actionKey=true;
                if (x>0) {
                    x=Math.max(0, x-step);
                    moved=true;
                }
            } else if (action==Display.GAME_RIGHT) {
                actionKey=true;
                if (x<Display.getInstance().getDisplayWidth()) {
                    x=Math.min(Display.getInstance().getDisplayWidth(), x+step);
                    moved=true;
                }
            } else if (action==Display.GAME_DOWN) {
                actionKey=true;
                if (y<htmlC.getPreferredH()) {
                    y=Math.min(htmlC.getPreferredH(), y+step);
                    moved=true;
                }
                htmlC.scrollRectToVisible(x, y, step, step*3, null);


            } else if (action==Display.GAME_UP) {
                actionKey=true;
                if (y>0) {
                    y=Math.max(0, y-step);
                    moved=true;
                }
                htmlC.scrollRectToVisible(x, Math.max(0,y-step*2), step, step, null);
            }  else if (action==Display.GAME_FIRE) {
                actionKey=true;
                System.out.println("Pointer FIRE at "+x+","+y);
                int cx=htmlC.getAbsoluteX()+x;
                int cy=htmlC.getAbsoluteY()+y;
                htmlC.getComponentForm().pointerPressed(cx, cy);
                htmlC.getComponentForm().pointerReleased(cx,cy);
                getComponentForm().setFocused(htmlC);
            }
            if (moved) {
                checkLinks();
            }

            return actionKey;
        }

        /**
         * Checks if the pointer is hovering over a link and handles focus of the links accordingly
         */
        private void checkLinks() {
            int cx=htmlC.getAbsoluteX()+x;
            int cy=htmlC.getAbsoluteY()+y;
            Component cmp=getComponentForm().getComponentAt(cx,cy);
            // Note that HTMLLink (which is a Button) can also be non-focusable (aside from the first word) - This relies on an implementation detail which is not ideal
            if ((cmp!=null) && ((cmp.isFocusable()) || (cmp instanceof Button))) {
                if (cmp!=lastFocused) {
                    if (lastFocused!=null) {
                        lastFocused.setFocus(false);
                    }
                    cmp.setFocus(true);
                    lastFocused=cmp;
                }
            } else if (lastFocused!=null) {
                lastFocused.setFocus(false);
                lastFocused=null;
            }
            repaint();

        }

        public void paint(Graphics g, Rectangle rect) {
            int cx=htmlC.getAbsoluteX()+x;
            int cy=htmlC.getAbsoluteY()+y;
            if (htmlC.pointerIcon!=null) {
                if ((lastFocused!=null) && (htmlC.pointerIconLink!=null)) {
                    g.drawImage(htmlC.pointerIconLink, cx, cy);
                } else {
                    g.drawImage(htmlC.pointerIcon, cx, cy);
                }
            } else { // default pointer (painted)
                if (lastFocused!=null) {
                    g.setColor(0x555555);
                } else {
                    g.setColor(0xaaaaaa);
                }
                
                g.fillTriangle(cx, cy, cx+defaultPointerSize, cy, cx, cy+defaultPointerSize);
                g.setColor(0);
                g.drawLine(cx, cy, cx+defaultPointerSize, cy);
                g.drawLine(cx+defaultPointerSize, cy, cx, cy+defaultPointerSize);
                g.drawLine(cx, cy, cx, cy+defaultPointerSize);
            }
        }

    }

}
