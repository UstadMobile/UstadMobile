/*
 *  Copyright ? 2008, 2010, Oracle and/or its affiliates. All rights reserved
 */
package com.sun.lwuit.browser;

import com.sun.lwuit.Button;
import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Image;
import com.sun.lwuit.TextField;
import com.sun.lwuit.animations.Motion;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.html.DocumentInfo;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.util.Resources;
import java.io.IOException;
import java.util.Vector;

/**
 * A navigation toolbar that adds the back,forward,stop,refresh and home functionality
 *
 * @author Ofir Leitner
 */
public class BrowserToolbar extends Container implements ActionListener {

    /**
     * The prefix of the button images in the resource file
     */
    private static String BUTTON_IMAGE_PREFIX="nav_button_";

    /**
     * The prefix of the selected button images in the resource file
     */
    private static String BUTTON_IMAGE_PREFIX_SELECTED="nav_button_sel_";

    /**
     * The prefix of the disabled button images in the resource file
     */
    private static String BUTTON_IMAGE_PREFIX_DISABLED="nav_button_disabled_";

    /**
     * The individual suffix names of the button images in the resource file
     */
    private static String[] imgFiles= {"back","stop","forward","refresh","home"};


    // Constants to identify the various navigation buttons
    static int BTN_BACK = 0;
    private static int BTN_CANCEL = 1;
    private static int BTN_NEXT = 2;
    private static int BTN_REFRESH = 3;
    private static int BTN_HOME = 4;

    /**
     * The duration it takes the toolbar to slide in and out
     */
    private static int SLIDE_DURATION = 300;

    Button[] navButtons = new Button[imgFiles.length];
    Image[] buttonsImages = new Image[imgFiles.length];
    TextField address;
    HTMLComponent htmlC;
    Vector back=new Vector();
    Vector forward=new Vector();
    String homePage;
    String currentURL="";
    Motion slide;
    boolean slidingOut;
    int prefH;

    private boolean backRequested=true;

    /**
     * A constant determining by which factor to scale the bar's buttons horizotally, specified as percentage of the original size (100 = original size).
     * This is useful especially when using touch screens with very high resolutions or high DPI ratio.
     * This value can be modified in the JAD property navbar_scalew
     */
    static int scaleHorizontal = 100;

    /**
     * A constant determining by which factor to scale the bar's buttons horizotally, specified as percentage of the original size (100 = original size).
     * This is useful especially when using touch screens with very high resolutions or high DPI ratio.
     * This value can be modified in the JAD property navbar_scaleh
     */
    static int scaleVertical = 100;

    BrowserForm form;


    public BrowserToolbar(HTMLComponent htmlComponent,BrowserForm form) {
        Resources toolBarRes=null;
        this.form=form;
        setUIID("NavToolbar");
        try {
            toolBarRes = Resources.open("/toolbar.res");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        htmlC=htmlComponent;
        setLayout(new BorderLayout());

        address = new TextField() {

            public void keyReleased(int keyCode) {
                int action=Display.getInstance().getGameAction(keyCode);
                super.keyReleased(keyCode);
                if (action==Display.GAME_FIRE) {
                        htmlC.setPage(address.getText());
                        setEnabled(false);
                }
            }

        };

        Container buttons=new Container();
        buttons.setHandlesInput(true); //to ignore the initial page load

        buttons.setLayout(new BoxLayout(BoxLayout.X_AXIS));
        for(int i=0;i<imgFiles.length;i++) {
            Image img=null;
            Image disabledImg=null;
            Image selImg=null;
            selImg=toolBarRes.getImage(BUTTON_IMAGE_PREFIX_SELECTED+imgFiles[i]);
            img=toolBarRes.getImage(BUTTON_IMAGE_PREFIX+imgFiles[i]);
            disabledImg=toolBarRes.getImage(BUTTON_IMAGE_PREFIX_DISABLED+imgFiles[i]);

            if ((scaleHorizontal!=100) || (scaleVertical!=100)) {
                img=img.scaled(img.getWidth()*scaleHorizontal/100, img.getHeight()*scaleVertical/100);
                disabledImg=disabledImg.scaled(disabledImg.getWidth()*scaleHorizontal/100, disabledImg.getHeight()*scaleVertical/100);
                selImg=selImg.scaled(selImg.getWidth()*scaleHorizontal/100, selImg.getHeight()*scaleVertical/100);
            }

            navButtons[i]=new NavButton(img,selImg,disabledImg);

            buttons.addComponent(navButtons[i]);
            navButtons[i].addActionListener(this);
            navButtons[i].setEnabled(false);

        }

        addComponent(BorderLayout.CENTER, address);
        addComponent(BorderLayout.SOUTH,buttons);

    }

    /**
     * Sets the homepage of this toolbar to the specified URL
     * 
     * @param url The homepage
     */
    public void setHomePage(String url) {
        homePage=url;
    }

    /**
     * Called when a page loading has started and sets the buttons disabled/enablked mode accordingly.
     */
    void notifyLoading() {
        address.setEnabled(false);
        navButtons[BTN_CANCEL].setEnabled(true);
        navButtons[BTN_BACK].setEnabled(false);
        navButtons[BTN_NEXT].setEnabled(false);
        navButtons[BTN_HOME].setEnabled(false);
        navButtons[BTN_REFRESH].setEnabled(false);
        form.setCancelCmdOnly();
    }

    /**
     * Called when a page loading has completed and sets the buttons disabled/enablked mode accordingly.
     */
    void notifyLoadCompleted(String url) {
            address.setEnabled(true);
            navButtons[BTN_CANCEL].setEnabled(false);
            navButtons[BTN_BACK].setEnabled(!back.isEmpty());
            navButtons[BTN_NEXT].setEnabled(!forward.isEmpty());
            navButtons[BTN_HOME].setEnabled(homePage!=null);
            navButtons[BTN_REFRESH].setEnabled(true);

            if (!backRequested) {
                back.addElement(currentURL);
                navButtons[BTN_BACK].setEnabled(true);
            } else {
                backRequested=false;
            }
            currentURL="";
            DocumentInfo docInfo=htmlC.getDocumentInfo();
            if (docInfo!=null) {
                currentURL=docInfo.getFullUrl();
            }
            address.setText(currentURL);

            form.addCommands(navButtons[BrowserToolbar.BTN_BACK].isEnabled());
    }


    public void actionPerformed(ActionEvent evt) {
         if (evt.getSource()==navButtons[BTN_BACK]) {
             back();
        } else if (evt.getSource()==navButtons[BTN_NEXT]) {
            forward();
        } else if (evt.getSource()==navButtons[BTN_HOME]) {
            home();
        } else if (evt.getSource()==navButtons[BTN_REFRESH]) {
            refresh();
        } else if (evt.getSource()==navButtons[BTN_CANCEL]) {
            stop();
        }

    }

    /**
     * Navigates to the previous page (if any)
     */
    public void back() {
            if (!back.isEmpty()) {
                String url=(String)back.lastElement();
                back.removeElementAt(back.size()-1);
                forward.addElement(currentURL);
                navButtons[BTN_NEXT].setEnabled(true);
                if (back.isEmpty()) {
                    navButtons[BTN_BACK].setEnabled(false);
                }
                backRequested=true;
                htmlC.setPage(url);
            }

    }

    /**
     * Navigates to the next page (if any)
     */
    public void forward() {
            if (!forward.isEmpty()) {
                String url=(String)forward.lastElement();
                forward.removeElementAt(forward.size()-1);
                back.addElement(url);
                if (forward.isEmpty()) {
                    navButtons[BTN_NEXT].setEnabled(false);
                }
                backRequested=true;
                htmlC.setPage(url);
            }
    }

    /**
     * Navigates to the home page (if any)
     */
    public void home() {
        if (homePage!=null) {
            htmlC.setPage(homePage);
        } else {
            System.out.println("No home page was set.");
        }
    }

    /**
     * Refreshes the current page
     */
    public void refresh() {
            backRequested=true;
            htmlC.setPage(currentURL);
    }

    /**
     * Stops the loading of the current page
     */
    public void stop() {
            System.out.println("Cancelling");
            htmlC.cancel();
    }




    /**
     * Starts the a slide in animation of the toolbar
     */
    public void slideIn() {
        slidingOut=false;
        if (prefH==0) {
            //prefH=getPreferredH();
            prefH=calcPreferredSize().getHeight();
        }
//        if (slide!=null) {
//            slide=Motion.createLinearMotion(slide.getValue(), prefH, SLIDE_DURATION);
//        } else {
            slide=Motion.createLinearMotion(0, prefH, SLIDE_DURATION);
            getComponentForm().registerAnimated(this);
            slide.start();
//        }
    }

    /**
     * Returns true if the toolbar is currently animating a slide, false otherwise
     * 
     * @return true if the toolbar is currently animating a slide, false otherwise
     */
    public boolean isSliding() {
        return (slide!=null);
    }

    /**
     * Starts the a slide out animation of the toolbar
     */
    public void slideOut() {
        slidingOut=true;
        if (prefH==0) {
            prefH=getPreferredH();
        }
//        if (slide!=null) {
//            slide=Motion.createLinearMotion(slide.getValue(), 0, SLIDE_DURATION);
//        } else {
            slide=Motion.createLinearMotion(prefH, 0, SLIDE_DURATION);
            getComponentForm().registerAnimated(this);
            slide.start();
//        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean animate() {
        if (slide!=null) {
            setPreferredH(slide.getValue());
            if (slide.isFinished()) {
                slide=null;
            }
            getParent().revalidate();
            return true;
        } else {
            getComponentForm().deregisterAnimated(this);
            if (slidingOut) {
                getComponentForm().removeComponent(this);
            } else {
                //getComponentForm().setFocused(this); // This seems unnecessary and it messes up pointer mode
            }
            return false;
        }
    }



}

/**
 * A navigation button., mostly adds the disabled image functionality to Button and an animation effect when focused.
 *
 * @author Ofir Leitner
 */
class NavButton extends Button{

    Image icon,disabledIcon,selIcon;
    Image lastAnimatedIcon,currentAnimatedIcon;
    long animationStartTime;

    /**
     * The selected icon animated speed in ms
     */
    private static final int FOCUSED_ICON_ANIMATE_RATE = 400;

     NavButton(Image icon,Image selIcon,Image disabledIcon) {
         super(icon);
         this.icon=icon;
         this.selIcon=selIcon;
         this.disabledIcon=disabledIcon;
         setRolloverIcon(selIcon);
         setPressedIcon(selIcon);
         setDisabledStyle(getUnselectedStyle());
     }

    public String getUIID() {
        return "NavButton";
    }

    /**
     * {@inheritDoc}
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setIcon(icon);
        } else {
            setIcon(disabledIcon);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void focusGained() {
        super.focusGained();
        getComponentForm().registerAnimated(this);
        animationStartTime=System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    protected void focusLost() {
        super.focusLost();
        if (getComponentForm()!=null) {
            getComponentForm().deregisterAnimated(this);
        } //otherwise it deregistered in deinitialize
    }

    protected void deinitialize() {
        getComponentForm().deregisterAnimated(this);
        super.deinitialize();
    }

    public boolean animate() {
        if ((System.currentTimeMillis()-animationStartTime)%(FOCUSED_ICON_ANIMATE_RATE<<1)>FOCUSED_ICON_ANIMATE_RATE) {
            currentAnimatedIcon=selIcon;
        } else {
            currentAnimatedIcon=icon;
        }
        if (currentAnimatedIcon!=lastAnimatedIcon) {
            lastAnimatedIcon=currentAnimatedIcon;
            return true;
        }
        return false;
    }

    public Image getRolloverIcon() {
        return currentAnimatedIcon;
    }

}
