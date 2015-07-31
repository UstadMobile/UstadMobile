/*
 *  Copyright ? 2008, 2010, Oracle and/or its affiliates. All rights reserved
 */
 package com.sun.lwuit.browser;

import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Font;
import com.sun.lwuit.Form;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.html.DocumentRequestHandler;
import com.sun.lwuit.html.HTMLCallback;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.layouts.BorderLayout;
import java.util.Hashtable;
import com.sun.lwuit.Display;
import com.sun.lwuit.Image;
import com.sun.lwuit.Painter;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.animations.Animation;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.html.HTMLElement;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.list.DefaultListCellRenderer;
import com.sun.lwuit.xml.Element;
import com.sun.lwuit.plaf.Border;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;
import java.io.IOException;
import java.util.Vector;

/**
 * Uses an HTMLComponent to create an XHTML-MP 1.0 browser. The BrowserForm i scomposed mainly of a toolbar (BrowserToolbar) and an HTMLComponent.
 * It also implements the HTMLCallback interface to receive events from the HTMLComponent and change the UI acordingly.
 *
 * @author Ofir Leitner
 */
public class BrowserForm extends Form implements HTMLCallback,ActionListener {

    /**
     * Set to 'true' to use bitmap fonts
     * This can be modified using the JAD property use_bitmap_fonts
     */
    static boolean useBitmapFonts=false;

    /**
     * If useBitmapFonts is set to true, the following list of fonts will be loaded
     * Note that too many fonts can cause memory problems
     */
    static String fonts[] = {"arial.12","arial.12.bold","arial.12.bold.italic","arial.12.italic","arial.14","courier.10",
                             "arial.10","h1.arial.16.bold","small-caps.12"};

    /**
     * Set to 'true' to turn on toolbar sliding animation (Doesn't work too good on some devices)
     */
    static boolean animateToolbar=true;

    Label titleLabel;
    Label dummyLabel = new Label();
    ExtHTMLComponent htmlC;
    static Hashtable autoCompleteCache = BrowserStorage.getFormData();
    LoadingGlassPane progressPane;

    HTMLElement scrollTo;
    
    /**
     * This address will be used as the homepage and as the first page accessed
     * This can be modified using the JAD property homepage
     */
    static String PAGE_HOME = "jar:///index.html";

    /**
     * The address of the help file
     */
    static final String PAGE_HELP = "jar:///help.html";

    BrowserToolbar toolBar;

    Command exitCmd=new Command("Exit");
    Command helpCmd=new Command("Help");
    Command clearCmd=new Command("Clear Cache");
    Command toolbarCmd=new Command("Toggle Navbar");
    Command backCmd=new Command("Back");
    Command forwardCmd=new Command("Forward");
    Command stopCmd=new Command("Stop");
    Command homeCmd=new Command("Home");
    Command refreshCmd=new Command("Refresh");
    Command imagesCmd=new Command("Toggle Images");
    Command toggleCSSCmd=new Command("Toggle CSS");
    Command searchCmd=new Command("Search");
    Command pointerCmd=new Command("Toggle Pointer");

    /**
     * If true the navigation bar will be shown on application start.
     * This can be modified using the JAD property navbar_display
     */
    static boolean displayNavBarOnInit = true;

    /**
     * If true the navigation bar will be either shown or not shown (according to displayNavBarOnInit) without the option to toggle it.
     * This can be modified using the JAD property navbar_lock
     */
    static boolean lockNavBar = false;

    /**
     * The menu font name, if null then a proportional,plain,medium system font will be used.
     * This can be modified using the JAD property menu_font
     */
    static String menuFontName;


    boolean showImages=true;
    boolean loadCSS=true;
    BrowserApp midlet;
    DocumentRequestHandler handler;

    public BrowserForm(BrowserApp midlet) {
        this.midlet=midlet;
        setMenuCellRenderer(new DefaultListCellRenderer(false));
        setCyclicFocus(false);
        setMenuFont();
        HTMLComponent.addSpecialKey("send", 'z');

        setLayout(new BorderLayout());
        setScrollable(false); // The HTMLComponent itself will be scrollable, not the form
        setScrollableX(false); // The HTMLComponent itself will be scrollable, not the form

        // Creating the HTMLComponent and its handler
        handler=new HttpRequestHandler();
        htmlC = new ExtHTMLComponent(handler);
        htmlC.getStyle().setPadding(0, 0, 3, 3);

        // Following commented code enables pointer mode by default and loads icons (if they exist)
        //htmlC.setPointerEnabled(true);
        /*try {
            htmlC.setPointerIcon(Image.createImage("/icon1.png"), Image.createImage("/icon2.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/

        htmlC.setHTMLCallback(this);

        // If using bitmap fonts, load the fonts and make the first the default
        if (useBitmapFonts) {
            for(int i=0;i<fonts.length;i++) {
                System.out.println("Loading font "+fonts[i]);
                HTMLComponent.addFont(fonts[i], Font.getBitmapFont(fonts[i]));
            }
            htmlC.setDefaultFont(fonts[0], Font.getBitmapFont(fonts[0]));
        }


        // Creating the toolbar and editing it to the upper segment of the form
        toolBar=new BrowserToolbar(htmlC,this);
        toolBar.setHomePage(PAGE_HOME);
        if (displayNavBarOnInit) {
            addComponent(BorderLayout.NORTH, toolBar);
        }
        addComponent(BorderLayout.CENTER,htmlC);

        // Creation and setup of the title
        titleLabel = new Label("Loading...");
        titleLabel.setUnselectedStyle(getTitleStyle());
        titleLabel.setText(htmlC.getTitle());
        titleLabel.setTickerEnabled(false);
        setTitleComponent(titleLabel);

        // Adds the command to the form
        addCommands(false);

        // Sets the form as the command listener
        addCommandListener(this);

        // Loads the page
        htmlC.setPage(PAGE_HOME);
        
        // See below commented code for examples of setBodyText and setHTML:
        
        // Example #1: Set Body without title
        //htmlC.setBodyText("Testing HTML 123 <a href=\"test\">Test link</a><hr>");

        // Example #2: Set Body with title and an active
        //htmlC.setHTML("Testing HTML 123<br><a href=\"http://m.google.com\">Absolute link - Active</a><br><br><a href=\"test\">Relative link - Inactive</a><hr>",null,"Titletest",false); // Set body with title

        // Example #3: Set full HTML - relative links will not be active, only absolute ones
        /*InputStream is=getClass().getResourceAsStream("/index.html");
        try {
            byte[] buf = HttpRequestHandler.getBuffer(is);
            String body=new String(buf);
            System.out.println("body="+body);
            htmlC.setHTML(body,null,null,true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/

    }

    void addCommands(boolean addBack) {
        removeAllCommands();
        if (addBack) {
            addCommand(backCmd);
        }
        addCommand(exitCmd);
        addCommand(clearCmd);
        addCommand(helpCmd);
        addCommand(toolbarCmd);
        addCommand(stopCmd);
        addCommand(forwardCmd);
        addCommand(refreshCmd);
        addCommand(homeCmd);
        addCommand(imagesCmd);
        addCommand(toggleCSSCmd);
        addCommand(searchCmd);
        addCommand(pointerCmd);
    }

    void setCancelCmdOnly() {
        removeAllCommands();
        addCommand(stopCmd);
    }

    private void setMenuFont() {
        if (menuFontName!=null) {
            Font menuFont=null;
            menuFontName=menuFontName.toLowerCase();

            if (menuFontName.startsWith("system")) {
                int size=Font.SIZE_MEDIUM;
                int style=Font.STYLE_PLAIN;
                int face=Font.FACE_PROPORTIONAL;
                menuFontName=menuFontName.substring(6);
                if (menuFontName.indexOf("small")!=-1) {
                    size=Font.SIZE_SMALL;
                } else if (menuFontName.indexOf("large")!=-1) {
                    size=Font.SIZE_LARGE;
                }

                if (menuFontName.indexOf("bold")!=-1) {
                    style=Font.STYLE_BOLD;
                } else if (menuFontName.indexOf("italic")!=-1) {
                    style=Font.STYLE_ITALIC;
                }

                if (menuFontName.indexOf("system")!=-1) {
                    face=Font.FACE_SYSTEM;
                } else if (menuFontName.indexOf("mono")!=-1) {
                    face=Font.FACE_MONOSPACE;
                }
                menuFont=Font.createSystemFont(face, style, size);
            } else {
                menuFont=Font.getBitmapFont(menuFontName);
            }

            if (menuFont!=null) {
                int count=getSoftButtonCount();
                for(int i=0;i<count;i++) {
                    Style style=getSoftButton(i).getStyle();
                    style.setFont(menuFont);
                }
                getTitleStyle().setFont(menuFont);
            }

        }

    }

    /**
     * Overriden to catch the POUND, STAR and 0 keys that are used as shortcuts
     * @param keyCode
     */
    public void keyReleased(int keyCode) {
        super.keyReleased(keyCode);
        if (!(getFocused() instanceof TextField)) {
            switch(keyCode) {
                case Display.KEY_POUND:
                    toolBar.home();
                    break;
                case '*':
                    if (!lockNavBar) {
                        toggleToolbar();
                    }
                    break;
                case '0':
                    toolBar.stop();
                    break;
                case '2': // Focus on addressbar
                    setFocused(toolBar.address);
                    break;
                case '9': //Page Down
                    htmlC.scrollPages(1, true);
                    break;
                case '3' : //Page Up
                    htmlC.scrollPages(-1, true);
                    break;
                case '1' : //Home
                    htmlC.scrollPages(-1000, true);
                    break;
                case '7' : //End
                    htmlC.scrollPages(1000, true);
                    break;
                case '4' : //Back
                    toolBar.back();
                    break;
                case '5' : //Refresh
                    toolBar.refresh();
                    break;
                case '6' : //Foward
                    toolBar.forward();
                    break;
                case '8': //Toggle pointer mode
                    htmlC.setPointerEnabled(!htmlC.isPointerEnabled());
                    break;

            }
        }
    }


    // HTMLCallback implementation:

    /**
     * {@inheritDoc}
     */
    public void titleUpdated(HTMLComponent htmlC, String title) {
        if ((title==null) || (title.equals(""))) {
            title="Untitled";
        } else {
            String newTitle="";
            for(int i=0;i<title.length();i++) {
                if (title.charAt(i)>=32) {
                    newTitle+=title.charAt(i);
                }
            }
            title=newTitle;
        }
        
        titleLabel.setText(title);
    }

    /**
     * {@inheritDoc}
     */
    public boolean parsingError(int errorId, String tag, String attribute, String value, String description) {
        System.out.println(description);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void pageStatusChanged(HTMLComponent htmlC, int phase, String url) {
        //System.out.println("page status="+phase);
        if (phase==HTMLCallback.STATUS_REQUESTED) {
            titleLabel.setText("Loading...");
            toolBar.notifyLoading();
            progressPane=new ProgressGlassPane(htmlC);
            progressPane.installPane(this);
        } else if ((phase==HTMLCallback.STATUS_DISPLAYED) || //(phase==HTMLCallback.STATUS_COMPLETED) || 
                   (phase==HTMLCallback.STATUS_ERROR) || (phase==HTMLCallback.STATUS_CANCELLED)) {
            if (scrollTo!=null) {
                htmlC.scrollToElement(scrollTo, true);
                scrollTo=null;
            }
            toolBar.notifyLoadCompleted(url);
            removeAllCommands();
            addCommands(toolBar.navButtons[BrowserToolbar.BTN_BACK].isEnabled());
            if (progressPane!=null) {
                progressPane.uninstallPane(this);
                progressPane=null;
            }
            if (phase==HTMLCallback.STATUS_DISPLAYED) {
                this.htmlC.pageDisplayed();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String fieldSubmitted(HTMLComponent htmlC, TextArea ta, String actionURL, String id, String value, int type,String errorMsg) {
        if (errorMsg!=null) {
            System.out.println("Malformed text");
            Dialog.show(UIManager.getInstance().localize("html.format.errortitle", "Format error"), errorMsg, UIManager.getInstance().localize("ok", "OK"), null);

            htmlC.getComponentForm().scrollComponentToVisible(ta);
            ta.getUnselectedStyle().setBorder(Border.createLineBorder(2, 0xff0000));
            ta.getSelectedStyle().setBorder(Border.createLineBorder(2, 0xff0000));
            setFocused(ta);
            ta.repaint();
            return null;
        } else { // Restore original look&feel in case there was an error before
            if (ta instanceof TextArea) {
                ta.setUIID("TextArea");
            } else {
                ta.setUIID("TextField");
            }
        }

        Hashtable urlCache = (Hashtable)autoCompleteCache.get(actionURL);
        if (urlCache==null) {
            urlCache=new Hashtable();
            autoCompleteCache.put(actionURL,urlCache);
        }
        urlCache.put(id,value);
        BrowserStorage.addFormData(actionURL, id, value);

        return value;
    }

    /**
     * {@inheritDoc}
     */
    public String getAutoComplete(HTMLComponent htmlC, String actionURL, String id) {
        if (actionURL==null) {
            return null;
        }
        Hashtable urlCache = (Hashtable)autoCompleteCache.get(actionURL);
        String auto=null;
        if (urlCache!=null) {
            auto=(String)urlCache.get(id);
        }
        return auto;
        //return (String)autoCompleteCache.get(id);
    }

    /**
     * {@inheritDoc}
     */
    public int getLinkProperties(HTMLComponent htmlC, String url) {
        if (url==null) {
            System.out.println("NULL URL!!!!");
        }

        String domain=HttpRequestHandler.getDomainForLinks(url);

        if (domain==null) {
            return LINK_FORBIDDEN; //unknown protocol
        }
        Vector visited=(Vector)HttpRequestHandler.visitedLinks.get(domain);
        if (visited!=null) {
            if (visited.contains(url)) {
                return HTMLCallback.LINK_VISTED;
            }
        }
        return HTMLCallback.LINK_REGULAR;
    }

    public void actionPerformed(ActionEvent evt) {
        Command cmd=evt.getCommand();
        if (cmd==imagesCmd) {
            showImages=!showImages;
            htmlC.setShowImages(showImages);
            String status=showImages?"on":"off";
            Dialog.show("Images "+status,"Images have been turned "+status,null,Dialog.TYPE_INFO,null,2000);
        } else if (cmd==searchCmd) {
            search();
        } else if (cmd==toggleCSSCmd) {
            loadCSS=!loadCSS;
            htmlC.setIgnoreCSS(!loadCSS);
            String status=loadCSS?"on":"off";
            Dialog.show("CSS "+status,"CSS has been turned "+status,null,Dialog.TYPE_INFO,null,2000);
        } else if (cmd==pointerCmd) {
            htmlC.setPointerEnabled(!htmlC.isPointerEnabled());
        } else if (cmd==exitCmd) {
            midlet.saveToRMS();
            Display.getInstance().exitApplication();
        } else if (cmd==helpCmd) {
            htmlC.setPage(PAGE_HELP);
        } else if (cmd==backCmd) {
            toolBar.back();
        } else if (cmd==forwardCmd) {
            toolBar.forward();
        } else if (cmd==refreshCmd) {
            toolBar.refresh();
        } else if (cmd==stopCmd) {
            toolBar.stop();
        } else if (cmd==homeCmd) {
            toolBar.home();
        } else if (cmd==toolbarCmd) {
            toggleToolbar();
        } else if (cmd==clearCmd) {
            BrowserStorage.clearFormData();
            HttpRequestHandler.visitedLinks=BrowserStorage.clearHistory();

        }
    }

    /**
     * Performs a search of a user string in the current document
     */
    private void search() {
            Dialog searchDialog = new Dialog("Search");
            TextField searchField =new TextField();
            searchDialog.setLayout(new BoxLayout((BoxLayout.Y_AXIS)));
            searchDialog.addComponent(searchField);
            Command cancelCmd = new Command("Cancel");
            searchDialog.addCommand(cancelCmd);
            searchDialog.addCommand(searchCmd);
            Command result=searchDialog.showDialog();
            if (result==searchCmd) {
                HTMLElement doc=htmlC.getDOM();

                String searchTerm=searchField.getText().toLowerCase();
                Vector texts=doc.getTextDescendants(searchTerm, false, Element.DEPTH_INFINITE);
                for (int i=0;i<texts.size();i++) {
                    HTMLElement elem=(HTMLElement)texts.elementAt(i);
                    String txt = elem.getText();
                    System.out.println("txt="+txt);
                    int pos=txt.toLowerCase().indexOf(searchTerm);
                    //if (pos!=-1) { // pos will not be -1 since we searched for texts containing the word
                        String txt2=txt.substring(pos+searchTerm.length());
                        String space="";
                        if (txt2.startsWith(" ")) { // Spacing between words doesn't work well when the space is in the beginning of a words segment (Since in HTML all spaces before text are truncated), so we move the space to the element containing the search term
                            space=" ";
                        }
                        HTMLElement elem2 = new HTMLElement(txt2,true);
                        elem.setText(txt.substring(0, pos));
                        System.out.println("txt="+txt+"("+txt.length()+"), search="+searchTerm+", pos="+pos);
                        HTMLElement termElem = new HTMLElement(txt.substring(pos, pos+searchTerm.length())+space,true);
                        HTMLElement span = new HTMLElement("b");
                        span.setAttribute("style", "background-color: yellow");
                        span.addChild(termElem);

                        Element parent=elem.getParent();
                        int index=parent.getChildIndex(elem);
                        parent.insertChildAt(span, index+1);
                        parent.insertChildAt(elem2, index+2);
                        if (scrollTo==null) {
                            scrollTo=termElem;
                        }
                    //}
                }
                htmlC.refreshDOM();
            }
    }

    private void toggleToolbar() {
            if (toolBar.getParent()==null) {

                if (animateToolbar) {
                    toolBar.setPreferredH(0);
                    addComponent(BorderLayout.NORTH, toolBar);
                    toolBar.slideIn();
                } else {
                    addComponent(BorderLayout.NORTH, toolBar);
                    setFocused(toolBar);
                }

            } else {
                if (animateToolbar) {
                    toolBar.slideOut();
                } else {
                    removeComponent(toolBar);
                }
            }
            revalidate();

    }

    public boolean linkClicked(HTMLComponent htmlC, String url) {
        System.out.println("Link clicked: "+url);
        return true;
    }

    public void actionPerformed(ActionEvent evt, HTMLComponent htmlC, HTMLElement element) {
        // do nothing
    }

    public void focusGained(Component cmp, HTMLComponent htmlC, HTMLElement element) {
        // do nothing
    }

    public void focusLost(Component cmp, HTMLComponent htmlC, HTMLElement element) {
        // do nothing
    }

    public void selectionChanged(int oldSelected, int newSelected, HTMLComponent htmlC, List list, HTMLElement element) {
        // do nothing
    }

    public void dataChanged(int type, int index, HTMLComponent htmlC, TextField textField, HTMLElement element) {
        // do nothing
    }

 }

/**
 * A simple glass pane with a loading message
 * 
 * @author Ofir Leitner
 */
class LoadingGlassPane implements Painter {

    public void paint(Graphics g, Rectangle rect) {
        Font font=g.getFont();
        int color=g.getColor();
        g.setColor(0);
        g.setFont(Font.getDefaultFont());
        g.drawString("Loading...", 20, 120);
        g.setColor(color);
        g.setFont(font);
    }

    void installPane(Form f) {
        f.setGlassPane(this);
    }

    void uninstallPane(Form f) {
        f.setGlassPane(null);
    }
    
}

/**
 * An advanced glass pane with a loading message and lodaing animation
 * 
 * @author Ofir Leitner
 */
class ProgressGlassPane extends LoadingGlassPane implements Animation {

    int spacing = 20;
    int fontSpacing = 10;
    String loadMsg="Loading...";

    HTMLComponent htmlC;

    public ProgressGlassPane(HTMLComponent htmlC) {
        this.htmlC=htmlC;
    }

    public void paint(Graphics g, Rectangle rect) {
        int color=g.getColor();
        Font font=g.getFont();

        int pos=(int)((System.currentTimeMillis()%2700)/300);
        Font f=Font.getDefaultFont();
        int startX=htmlC.getAbsoluteX()+(htmlC.getWidth()/2)-spacing;
        int fontStartX=htmlC.getAbsoluteX()+(htmlC.getWidth()-f.stringWidth(loadMsg))/2;
        int startY=htmlC.getAbsoluteY()+(htmlC.getHeight()/2)-spacing-(f.getHeight()+fontSpacing)/2;
        int i=0;
        g.setColor(0xffffff);
        g.fillRect(Math.min(startX-3,fontStartX), startY-3, Math.max(spacing*2+7,f.stringWidth(loadMsg))+1, spacing*2+7+f.getHeight()+fontSpacing);
        g.setColor(0);
        g.setFont(f);
        g.drawString(loadMsg, fontStartX, startY);
        startY+=f.getHeight()+fontSpacing;
        for (int y=0;y<3;y++) {
            for(int x=0;x<3;x++) {
                int thickness=3;
                if (i==pos) {
                    thickness=7;
                } else if (i==pos-1) {
                    thickness=5;
                }
                g.fillRect(startX+x*spacing-(thickness/2), startY+y*spacing-(thickness/2), thickness, thickness);
                i++;
            }
        }
        g.setColor(color);
        g.setFont(font);
    }

    public boolean animate() {
        return true;
    }

    public void paint(Graphics g) {
        paint(g, null);
    }

    void installPane(Form f) {
        super.installPane(f);
        f.registerAnimated(this);

    }

    void uninstallPane(Form f) {
        super.uninstallPane(f);
        f.deregisterAnimated(this);
    }


}