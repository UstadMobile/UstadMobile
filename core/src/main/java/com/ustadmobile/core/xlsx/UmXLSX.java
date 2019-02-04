package com.ustadmobile.core.xlsx;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.util.UMUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Creates an XLSX file.
 *
 * Tree structure of an xlsx file:
 *
 * [.xlsx]
 * ├── [Content_Types].xml              Contains all files content types (including worksheets xmls and layout xmls)
 * ├── docProps                         Document properties folder?
 * │   ├── app.xml                      Contains sheet title and count
 * │   └── core.xml                     Contains created date and last modified date
 * ├── _rels                            Relationship properties folder?
 * │   ├── .rels                        Contains relationships type (usually app.xml, core.xml and workbook.xml relationship declarations)
 * └── xl                               The excel data folder
 *     ├── _rels                        Relationship folder for sheets, values, styles and themes xml
 *     │   └── workbook.xml.rels        The relationship for every sheets and their relationship id is set here.
 *     ├── sharedStrings.xml            All values that goes inside the workbook goes here in order.
 *     ├── styles.xml                   Styles (doesn't really change)
 *     ├── theme                        Theme folder
 *     │   └── theme1.xml               Themes (doesn't really change)
 *     ├── workbook.xml                 Sheet titles and their relationship id is mapped in this file.
 *     └── worksheets                   The folder that contains all sheets value maps
 *         ├── sheet1.xml               Every sheet xml contains active cell, format, and most importantly
 *         ├── sheet2.xml                   every sheet contains, for every row and every row's columns,
 *         └── sheet3.xml                   value mapping based on the order ID of strings in sharedStrings.xml
 */
public class UmXLSX {

    String title;
    String author;
    String filePath;
    String workingPath;
    long dateCreated;
    long dateModified;
    HashMap<String, Integer> sharedStringMap;
    int sharedStringCount = 0;
    List<UmSheet> sheets;
    public static final String AUTHOR = "UstadMobile";

    private XmlPullParser parser;

    public static final String UTF8 = "UTF-8";
    public static final int SHEET_COUNT_START = 1;

    public static final String CONTENT_TYPE_FILE = "[Content_Types].xml";
    public static final String REL_FOLDER = "_rels";
    public static final String RELS_FILE = ".rels";
    public static final String APP_FILE = "app.xml";
    public static final String CORE_FILE = "core.xml";
    public static final String DOCPROPS_FOLDER = "docProps";
    public static final String XL_FOLDER = "xl";
    public static final String XL_RELS_FOLDER = "_rels";
    public static final String XL_RELS_WORKBOOK_RELS_FILE = "";
    public static final String XL_THEME_FOLDER = "theme";
    public static final String XL_THEME_THEMES_FILE = "theme1.xml";
    public static final String XL_WORKSHEET_FOLDER = "worksheets";
    public static final String SHARED_STRINGS_FILE = "sharedStrings.xml";
    public static final String STYLES_FILE = "styles.xml";
    public static final String WORKBOOK_FILE = "workbook.xml";
    public static final String XL_WORKSHEET_SHEET_FILE = "sheet1.xml";

    public static String CONTENT_TYPE_FILE_XML_DATA =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/><Override PartName=\"/xl/theme/theme1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.theme+xml\"/><Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/><Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/><Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/></Types>";
    public static String RELS_FILE_XML_DATA =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\" Target=\"docProps/app.xml\"/><Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" Target=\"docProps/core.xml\"/><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>";
    public static String APP_FILE_XML_DATA =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\" xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes\"><Application>Microsoft Excel</Application><DocSecurity>0</DocSecurity><ScaleCrop>false</ScaleCrop><HeadingPairs><vt:vector size=\"2\" baseType=\"variant\"><vt:variant><vt:lpstr>Worksheets</vt:lpstr></vt:variant><vt:variant><vt:i4>1</vt:i4></vt:variant></vt:vector></HeadingPairs><TitlesOfParts><vt:vector size=\"1\" baseType=\"lpstr\"><vt:lpstr>Sheet1</vt:lpstr></vt:vector></TitlesOfParts><Company></Company><LinksUpToDate>false</LinksUpToDate><SharedDoc>false</SharedDoc><HyperlinksChanged>false</HyperlinksChanged><AppVersion>16.0300</AppVersion></Properties>";
    public static String CORE_FILE_XML_DATA =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><dc:creator>Varuna Singh</dc:creator><cp:lastModifiedBy></cp:lastModifiedBy><dcterms:created xsi:type=\"dcterms:W3CDTF\">2015-06-05T18:17:20Z</dcterms:created><dcterms:modified xsi:type=\"dcterms:W3CDTF\">2015-06-05T18:17:26Z</dcterms:modified></cp:coreProperties>";
    public static String XL_RELS_WORKBOOK_RELS_FILE_XML_DATA =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/><Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme\" Target=\"theme/theme1.xml\"/><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/></Relationships>";
    public static String XL_THEME_THEMES_FILE_XML_DATA =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<a:theme xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" name=\"Office Theme\"><a:themeElements><a:clrScheme name=\"Office\"><a:dk1><a:sysClr val=\"windowText\" lastClr=\"000000\"/></a:dk1><a:lt1><a:sysClr val=\"window\" lastClr=\"FFFFFF\"/></a:lt1><a:dk2><a:srgbClr val=\"44546A\"/></a:dk2><a:lt2><a:srgbClr val=\"E7E6E6\"/></a:lt2><a:accent1><a:srgbClr val=\"5B9BD5\"/></a:accent1><a:accent2><a:srgbClr val=\"ED7D31\"/></a:accent2><a:accent3><a:srgbClr val=\"A5A5A5\"/></a:accent3><a:accent4><a:srgbClr val=\"FFC000\"/></a:accent4><a:accent5><a:srgbClr val=\"4472C4\"/></a:accent5><a:accent6><a:srgbClr val=\"70AD47\"/></a:accent6><a:hlink><a:srgbClr val=\"0563C1\"/></a:hlink><a:folHlink><a:srgbClr val=\"954F72\"/></a:folHlink></a:clrScheme><a:fontScheme name=\"Office\"><a:majorFont><a:latin typeface=\"Calibri Light\" panose=\"020F0302020204030204\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/><a:font script=\"Jpan\" typeface=\"游ゴシック Light\"/><a:font script=\"Hang\" typeface=\"맑은 고딕\"/><a:font script=\"Hans\" typeface=\"等线 Light\"/><a:font script=\"Hant\" typeface=\"新細明體\"/><a:font script=\"Arab\" typeface=\"Times New Roman\"/><a:font script=\"Hebr\" typeface=\"Times New Roman\"/><a:font script=\"Thai\" typeface=\"Tahoma\"/><a:font script=\"Ethi\" typeface=\"Nyala\"/><a:font script=\"Beng\" typeface=\"Vrinda\"/><a:font script=\"Gujr\" typeface=\"Shruti\"/><a:font script=\"Khmr\" typeface=\"MoolBoran\"/><a:font script=\"Knda\" typeface=\"Tunga\"/><a:font script=\"Guru\" typeface=\"Raavi\"/><a:font script=\"Cans\" typeface=\"Euphemia\"/><a:font script=\"Cher\" typeface=\"Plantagenet Cherokee\"/><a:font script=\"Yiii\" typeface=\"Microsoft Yi Baiti\"/><a:font script=\"Tibt\" typeface=\"Microsoft Himalaya\"/><a:font script=\"Thaa\" typeface=\"MV Boli\"/><a:font script=\"Deva\" typeface=\"Mangal\"/><a:font script=\"Telu\" typeface=\"Gautami\"/><a:font script=\"Taml\" typeface=\"Latha\"/><a:font script=\"Syrc\" typeface=\"Estrangelo Edessa\"/><a:font script=\"Orya\" typeface=\"Kalinga\"/><a:font script=\"Mlym\" typeface=\"Kartika\"/><a:font script=\"Laoo\" typeface=\"DokChampa\"/><a:font script=\"Sinh\" typeface=\"Iskoola Pota\"/><a:font script=\"Mong\" typeface=\"Mongolian Baiti\"/><a:font script=\"Viet\" typeface=\"Times New Roman\"/><a:font script=\"Uigh\" typeface=\"Microsoft Uighur\"/><a:font script=\"Geor\" typeface=\"Sylfaen\"/></a:majorFont><a:minorFont><a:latin typeface=\"Calibri\" panose=\"020F0502020204030204\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/><a:font script=\"Jpan\" typeface=\"游ゴシック\"/><a:font script=\"Hang\" typeface=\"맑은 고딕\"/><a:font script=\"Hans\" typeface=\"等线\"/><a:font script=\"Hant\" typeface=\"新細明體\"/><a:font script=\"Arab\" typeface=\"Arial\"/><a:font script=\"Hebr\" typeface=\"Arial\"/><a:font script=\"Thai\" typeface=\"Tahoma\"/><a:font script=\"Ethi\" typeface=\"Nyala\"/><a:font script=\"Beng\" typeface=\"Vrinda\"/><a:font script=\"Gujr\" typeface=\"Shruti\"/><a:font script=\"Khmr\" typeface=\"DaunPenh\"/><a:font script=\"Knda\" typeface=\"Tunga\"/><a:font script=\"Guru\" typeface=\"Raavi\"/><a:font script=\"Cans\" typeface=\"Euphemia\"/><a:font script=\"Cher\" typeface=\"Plantagenet Cherokee\"/><a:font script=\"Yiii\" typeface=\"Microsoft Yi Baiti\"/><a:font script=\"Tibt\" typeface=\"Microsoft Himalaya\"/><a:font script=\"Thaa\" typeface=\"MV Boli\"/><a:font script=\"Deva\" typeface=\"Mangal\"/><a:font script=\"Telu\" typeface=\"Gautami\"/><a:font script=\"Taml\" typeface=\"Latha\"/><a:font script=\"Syrc\" typeface=\"Estrangelo Edessa\"/><a:font script=\"Orya\" typeface=\"Kalinga\"/><a:font script=\"Mlym\" typeface=\"Kartika\"/><a:font script=\"Laoo\" typeface=\"DokChampa\"/><a:font script=\"Sinh\" typeface=\"Iskoola Pota\"/><a:font script=\"Mong\" typeface=\"Mongolian Baiti\"/><a:font script=\"Viet\" typeface=\"Arial\"/><a:font script=\"Uigh\" typeface=\"Microsoft Uighur\"/><a:font script=\"Geor\" typeface=\"Sylfaen\"/></a:minorFont></a:fontScheme><a:fmtScheme name=\"Office\"><a:fillStyleLst><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:gradFill rotWithShape=\"1\"><a:gsLst><a:gs pos=\"0\"><a:schemeClr val=\"phClr\"><a:lumMod val=\"110000\"/><a:satMod val=\"105000\"/><a:tint val=\"67000\"/></a:schemeClr></a:gs><a:gs pos=\"50000\"><a:schemeClr val=\"phClr\"><a:lumMod val=\"105000\"/><a:satMod val=\"103000\"/><a:tint val=\"73000\"/></a:schemeClr></a:gs><a:gs pos=\"100000\"><a:schemeClr val=\"phClr\"><a:lumMod val=\"105000\"/><a:satMod val=\"109000\"/><a:tint val=\"81000\"/></a:schemeClr></a:gs></a:gsLst><a:lin ang=\"5400000\" scaled=\"0\"/></a:gradFill><a:gradFill rotWithShape=\"1\"><a:gsLst><a:gs pos=\"0\"><a:schemeClr val=\"phClr\"><a:satMod val=\"103000\"/><a:lumMod val=\"102000\"/><a:tint val=\"94000\"/></a:schemeClr></a:gs><a:gs pos=\"50000\"><a:schemeClr val=\"phClr\"><a:satMod val=\"110000\"/><a:lumMod val=\"100000\"/><a:shade val=\"100000\"/></a:schemeClr></a:gs><a:gs pos=\"100000\"><a:schemeClr val=\"phClr\"><a:lumMod val=\"99000\"/><a:satMod val=\"120000\"/><a:shade val=\"78000\"/></a:schemeClr></a:gs></a:gsLst><a:lin ang=\"5400000\" scaled=\"0\"/></a:gradFill></a:fillStyleLst><a:lnStyleLst><a:ln w=\"6350\" cap=\"flat\" cmpd=\"sng\" algn=\"ctr\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:prstDash val=\"solid\"/><a:miter lim=\"800000\"/></a:ln><a:ln w=\"12700\" cap=\"flat\" cmpd=\"sng\" algn=\"ctr\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:prstDash val=\"solid\"/><a:miter lim=\"800000\"/></a:ln><a:ln w=\"19050\" cap=\"flat\" cmpd=\"sng\" algn=\"ctr\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:prstDash val=\"solid\"/><a:miter lim=\"800000\"/></a:ln></a:lnStyleLst><a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst><a:outerShdw blurRad=\"57150\" dist=\"19050\" dir=\"5400000\" algn=\"ctr\" rotWithShape=\"0\"><a:srgbClr val=\"000000\"><a:alpha val=\"63000\"/></a:srgbClr></a:outerShdw></a:effectLst></a:effectStyle></a:effectStyleLst><a:bgFillStyleLst><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:solidFill><a:schemeClr val=\"phClr\"><a:tint val=\"95000\"/><a:satMod val=\"170000\"/></a:schemeClr></a:solidFill><a:gradFill rotWithShape=\"1\"><a:gsLst><a:gs pos=\"0\"><a:schemeClr val=\"phClr\"><a:tint val=\"93000\"/><a:satMod val=\"150000\"/><a:shade val=\"98000\"/><a:lumMod val=\"102000\"/></a:schemeClr></a:gs><a:gs pos=\"50000\"><a:schemeClr val=\"phClr\"><a:tint val=\"98000\"/><a:satMod val=\"130000\"/><a:shade val=\"90000\"/><a:lumMod val=\"103000\"/></a:schemeClr></a:gs><a:gs pos=\"100000\"><a:schemeClr val=\"phClr\"><a:shade val=\"63000\"/><a:satMod val=\"120000\"/></a:schemeClr></a:gs></a:gsLst><a:lin ang=\"5400000\" scaled=\"0\"/></a:gradFill></a:bgFillStyleLst></a:fmtScheme></a:themeElements><a:objectDefaults/><a:extraClrSchemeLst/><a:extLst><a:ext uri=\"{05A4C25C-085E-4340-85A3-A5531E510DB2}\"><thm15:themeFamily xmlns:thm15=\"http://schemas.microsoft.com/office/thememl/2012/main\" name=\"Office Theme\" id=\"{62F939B6-93AF-4DB8-9C6B-D6C7DFDC589F}\" vid=\"{4A3C46E8-61CC-4603-A589-7422A47A8E4A}\"/></a:ext></a:extLst></a:theme>";
    public static String SHARED_STRINGS_FILE_XML_DATA =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"1\" uniqueCount=\"1\"><si><t umid=\"0\" >Hello</t></si></sst>";
    public static String STYLES_FILE_XML_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" mc:Ignorable=\"x14ac x16r2\" xmlns:x14ac=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac\" xmlns:x16r2=\"http://schemas.microsoft.com/office/spreadsheetml/2015/02/main\"><fonts count=\"1\" x14ac:knownFonts=\"1\"><font><sz val=\"11\"/><color theme=\"1\"/><name val=\"Calibri\"/><family val=\"2\"/><scheme val=\"minor\"/></font></fonts><fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills><borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs><cellXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/></cellXfs><cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles><dxfs count=\"0\"/><tableStyles count=\"0\" defaultTableStyle=\"TableStyleMedium2\" defaultPivotStyle=\"PivotStyleLight16\"/><extLst><ext uri=\"{EB79DEF2-80B8-43e5-95BD-54CBDDF9020C}\" xmlns:x14=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/main\"><x14:slicerStyles defaultSlicerStyle=\"SlicerStyleLight1\"/></ext><ext uri=\"{9260A510-F301-46a8-8635-F512D64BE5F5}\" xmlns:x15=\"http://schemas.microsoft.com/office/spreadsheetml/2010/11/main\"><x15:timelineStyles defaultTimelineStyle=\"TimeSlicerStyleLight1\"/></ext></extLst></styleSheet>";
    public static String WORKBOOK_FILE_XML_DATA =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" mc:Ignorable=\"x15\" xmlns:x15=\"http://schemas.microsoft.com/office/spreadsheetml/2010/11/main\"><fileVersion appName=\"xl\" lastEdited=\"6\" lowestEdited=\"6\" rupBuild=\"14420\"/><workbookPr filterPrivacy=\"1\" defaultThemeVersion=\"164011\"/><bookViews><workbookView xWindow=\"0\" yWindow=\"0\" windowWidth=\"22260\" windowHeight=\"12645\"/></bookViews><sheets><sheet name=\"Sheet1\" sheetId=\"1\" r:id=\"rId1\"/></sheets><calcPr calcId=\"162913\"/><extLst><ext uri=\"{140A7094-0E35-4892-8432-C4D2E57EDEB5}\" xmlns:x15=\"http://schemas.microsoft.com/office/spreadsheetml/2010/11/main\"><x15:workbookPr chartTrackingRefBase=\"1\"/></ext></extLst></workbook>";
    public static String XL_WORKSHEET_SHEET_FILE_XML_DATA = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:x14ac=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac\" mc:Ignorable=\"x14ac\">" +
                    "  <dimension ref=\"A1\"/>" +
                    "  <sheetViews>" +
                    "    <sheetView tabSelected=\"1\" workbookViewId=\"0\"/>" +
                    "  </sheetViews>" +
                    "  <sheetFormatPr defaultRowHeight=\"15\" x14ac:dyDescent=\"0.25\"/>" +
                    "  <sheetData/>" +
                    "  <pageMargins left=\"0.7\" right=\"0.7\" top=\"0.75\" bottom=\"0.75\" header=\"0.3\" footer=\"0.3\"/>" +
                    "</worksheet>";

    public static final String CONTENT_TYPE_TAG_VALUE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml";
    public static final String CONTENT_TYPE_PARTNAME = "PartName";
    public static final String CONTENT_TYPE_CONTENTTYPE = "ContentType";

    public UmXLSX(String title, String theFilePath, String theWorkingPath){
        this.title = title;
        this.author = AUTHOR;
        this.filePath = theFilePath;
        this.dateCreated = System.currentTimeMillis();
        this.dateModified = System.currentTimeMillis();
        this.workingPath = theWorkingPath;
        this.sheets = new ArrayList<>();
        try {
            parser = UstadMobileSystemImpl.getInstance().newPullParser();
            //initiateXmlObjects();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

    }

    private int addValueToSharedString(String newValue){
        if(!sharedStringMap.containsKey(newValue)){
            sharedStringMap.put(newValue, sharedStringCount);
            sharedStringCount++;
        }
        return sharedStringMap.get(newValue);
    }

    public void addNewSheet(String title){
        UmSheet newSheet = new UmSheet(title);
        sheets.add(newSheet);
    }

    public void addSheet(UmSheet newSheet){
        sheets.add(newSheet);
    }

    public void createXLSX(){

        //The current object is done. Create the files in the working directory.
        File workingDirFile = new File(workingPath);
        if(!workingDirFile.mkdir()){
            return;
        }

        //1. Modify [Content_Types].xml
        messWithIt(CONTENT_TYPE_FILE_XML_DATA);

        File content_types_file = new File(workingPath, CONTENT_TYPE_FILE);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            parser.setInput(new StringReader(CONTENT_TYPE_FILE_XML_DATA));
            XmlSerializer xs = UstadMobileSystemImpl.getInstance().newXMLSerializer();
            xs.setOutput(bout, UTF8);

            xs.startDocument(UTF8, false);
            UMUtil.passXmlThrough(parser, xs, true, new UMUtil.PassXmlThroughFilter() {
                @Override
                public boolean beforePassthrough(int evtType, XmlPullParser parser, XmlSerializer serializer) throws IOException, XmlPullParserException {

                    if(evtType == XmlPullParser.END_TAG && parser.getName().equals("Override")){
                        serializer.startTag(parser.getNamespace(), "Testing");

                    }

                    return true;
                }

                @Override
                public boolean afterPassthrough(int evtType, XmlPullParser parser, XmlSerializer serializer) throws IOException, XmlPullParserException {
                    return true;
                }
            });

            xs.endDocument();
            byte[] boutBA = bout.toByteArray();
            System.out.println("he");


        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

    }

    public List<Element> createContentTypeSheetEntry(Document doc){

        List<Element> contentEntryEntry = new ArrayList<>();

        int index = SHEET_COUNT_START;
        for(UmSheet everySheet:sheets){
            if(index < SHEET_COUNT_START + 1){
                index++;
            }else{
                String partName = "/xl/worksheets/sheet" + index + ".xml";

                Element el = doc.createElement("Override");
                el.setAttribute(CONTENT_TYPE_PARTNAME, partName);
                el.setAttribute(CONTENT_TYPE_CONTENTTYPE, CONTENT_TYPE_TAG_VALUE);

                contentEntryEntry.add(el);
            }
        }
        return contentEntryEntry;
    }

    private static String nodeToString(Node node) throws TransformerException {
        StringWriter buf = new StringWriter();
        Transformer xform = TransformerFactory.newInstance().newTransformer();
        xform.transform(new DOMSource(node), new StreamResult(buf));
        return buf.toString();
    }

    public void messWithIt(String data){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;

        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(data);
            NodeList types = doc.getElementsByTagName("Types");
            NodeList overrides = doc.getElementsByTagName("Override");

            for(Element everySheetEntry: createContentTypeSheetEntry(doc)){
                overrides.item(0).appendChild(everySheetEntry);
            }

            System.out.println(nodeToString(doc));

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }


    public void initiateXmlObjects(){

    }

    private void createContentTypeFile(){

    }

}
