package com.ustadmobile.staging.core.xlsx

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import org.kmp.io.KMPPullParserException
import org.kmp.io.KMPXmlParser

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xml.sax.SAXException
//import org.xmlpull.v1.XmlPullParser
//import org.xmlpull.v1.XmlPullParserException

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.zip.ZipOutputStream

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

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
 * ├── _rels                        Relationship folder for sheets, values, styles and themes xml
 * │   └── workbook.xml.rels        The relationship for every sheets and their relationship id is set here.
 * ├── sharedStrings.xml            All values that goes inside the workbook goes here in order.
 * ├── styles.xml                   Styles (doesn't really change)
 * ├── theme                        Theme folder
 * │   └── theme1.xml               Themes (doesn't really change)
 * ├── workbook.xml                 Sheet titles and their relationship id is mapped in this file.
 * └── worksheets                   The folder that contains all sheets value maps
 * ├── sheet1.xml               Every sheet xml contains active cell, format, and most importantly
 * ├── sheet2.xml                   every sheet contains, for every row and every row's columns,
 * └── sheet3.xml                   value mapping based on the order ID of strings in sharedStrings.xml
 */
actual class UmXLSX {
    actual internal var title: String
    actual internal var filePath: String
    actual internal var workingPath: String

    actual constructor(title: String, filePath: String, workingPath: String) {
        this.title = title
        this.filePath = filePath
        this.workingPath = workingPath
        this.author = AUTHOR
        this.dateCreated = UMCalendarUtil.getDateInMilliPlusDays(0)
        this.dateModified = UMCalendarUtil.getDateInMilliPlusDays(0)
        this.sheets = ArrayList()
        try {
            parser = UstadMobileSystemImpl.instance.newPullParser()
            //initiateXmlObjects();
        } catch (e: KMPPullParserException) {
            print(e.message)
        }
        this.sharedStringMap = LinkedHashMap()
    }

    internal var author: String
    internal var dateCreated: Long = 0
    internal var dateModified: Long = 0
    internal var sharedStringMap: LinkedHashMap<String, Int>
    internal var sharedStringCount = 0
    internal var sheets: MutableList<UmSheet>

    private var parser: KMPXmlParser? = null

    private fun addValueToSharedString(newValue: String): Int {
        if (!sharedStringMap.containsKey(newValue)) {
            sharedStringMap[newValue] = sharedStringCount
            sharedStringCount++
        }
        return sharedStringMap[newValue]!!
    }

    fun addNewSheet(title: String) {
        val newSheet = UmSheet(title)
        sheets.add(newSheet)
    }

    actual fun addSheet(newSheet: UmSheet) {
        this.sheets.add(newSheet)
    }

    actual fun createXLSX() {

        //The current object is done. Create the files in the working directory.
        val workingDirFile = File(workingPath)

        if (!workingDirFile.exists()) {
            if (!workingDirFile.mkdir()) {
                return
            }
        }

        //1. Generate all XML data
        val contentTypeXML = generateContentTypeXMLDocument(CONTENT_TYPE_FILE_XML_DATA)
        val relsXML = generateXMLDocument(RELS_FILE_XML_DATA) //No change
        val appXML = generateAppXMLDocumnet(APP_FILE_XML_DATA)
        val coreXML = generateXMLDocument(CORE_FILE_XML_DATA)
        val xlRelsWorkbookXML = generateXlRelsWorkbookXMLDocument(XL_RELS_WORKBOOK_RELS_FILE_XML_DATA)
        val themeXML = generateXMLDocument(XL_THEME_THEMES_FILE_XML_DATA)
        val sheetXMLList = generateSheetXMLList(XL_WORKSHEET_SHEET_FILE_XML_DATA)
        val sharedStringXML = generateSharedStringXML(SHARED_STRINGS_FILE_XML_DATA)
        val stylesXML = generateXMLDocument(STYLES_FILE_XML_DATA)
        val workbookXML = generateWorkbookXMLDocument(WORKBOOK_FILE_XML_DATA)

        //2. Create the directory structure for the XML in the working Directory
        val relsFolder = File(workingPath, REL_FOLDER)
        val docPropsFolder = File(workingPath, DOCPROPS_FOLDER)
        val xlFolder = File(workingPath, XL_FOLDER)
        val contentTypesFile = File(workingPath, CONTENT_TYPE_FILE)
        val relsRelsFile = File(relsFolder.getAbsolutePath(), RELS_FILE)
        val appFile = File(docPropsFolder.getAbsolutePath(), APP_FILE)
        val coreFile = File(docPropsFolder, CORE_FILE)
        val xlRelsFolder = File(xlFolder.getAbsolutePath(), XL_RELS_FOLDER)
        val relsWorkbookFile = File(xlRelsFolder.getAbsolutePath(), XL_RELS_WORKBOOK_RELS_FILE)
        val themeFolder = File(xlFolder.getAbsolutePath(), XL_THEME_FOLDER)
        val themesFile = File(themeFolder.getAbsolutePath(), XL_THEME_THEMES_FILE)
        val worksheetFolder = File(xlFolder.getAbsolutePath(), XL_WORKSHEET_FOLDER)
        val sharedStringsFile = File(xlFolder.getAbsolutePath(), SHARED_STRINGS_FILE)
        val stylesFile = File(xlFolder.getAbsolutePath(), STYLES_FILE)
        val workbookFile = File(xlFolder.getAbsolutePath(), WORKBOOK_FILE)

        try {
            relsFolder.mkdir()
            docPropsFolder.mkdir()
            xlFolder.mkdir()
            contentTypesFile.createNewFile()
            relsRelsFile.createNewFile()
            appFile.createNewFile()
            coreFile.createNewFile()
            xlRelsFolder.mkdir()
            relsWorkbookFile.createNewFile()
            themeFolder.mkdir()
            themesFile.createNewFile()
            worksheetFolder.mkdir()

            var index = 0
            for (everySheetXML in sheetXMLList) {
                index++
                val thisSheet = File(worksheetFolder.getAbsolutePath(), "sheet$index.xml")
                thisSheet.createNewFile()
                writeDocumentToFile(everySheetXML, thisSheet)
            }

            sharedStringsFile.createNewFile()
            stylesFile.createNewFile()
            workbookFile.createNewFile()

        } catch (e: IOException) {
            print(e.message)
        }

        //3. Put the XML into Files
        writeDocumentToFile(contentTypeXML, contentTypesFile)
        writeDocumentToFile(relsXML, relsRelsFile)
        writeDocumentToFile(appXML, appFile)
        writeDocumentToFile(coreXML, coreFile)
        writeDocumentToFile(xlRelsWorkbookXML, relsWorkbookFile)
        writeDocumentToFile(themeXML, themesFile)
        writeDocumentToFile(sharedStringXML, sharedStringsFile)
        writeDocumentToFile(stylesXML, stylesFile)
        writeDocumentToFile(workbookXML, workbookFile)
        println("did it work?")

        //4. Zip the contents of working directory to a zip file (.xlsx file)
        val zipUtil = ZipUtil()
        zipUtil.zipThisFoldersContents("$workingPath/", filePath)

        //5. Remove working dir
        workingDirFile.delete()

        //6. Call view's export/share
        //View is already calling it.
    }

    fun createContentTypeSheetEntry(doc: Document): List<Element> {

        val contentEntryEntry = ArrayList<Element>()

        var index = SHEET_COUNT_START
        for (everySheet in sheets) {
            if (index < SHEET_COUNT_START + 1) {
                index++
            } else {
                val partName = "/xl/worksheets/sheet$index.xml"

                val el = doc.createElement("Override")
                el.setAttribute(CONTENT_TYPE_PARTNAME, partName)
                el.setAttribute(CONTENT_TYPE_CONTENTTYPE, CONTENT_TYPE_TAG_VALUE)

                contentEntryEntry.add(el)
                index++
            }
        }
        return contentEntryEntry
    }

    fun createAppSheetEntry(doc: Document): List<Element> {
        val appSheetEntry = ArrayList<Element>()
        for (everySheet in sheets) {
            val el = doc.createElement("vt:lpstr")
            el.setTextContent(everySheet.title)
            appSheetEntry.add(el)
        }
        return appSheetEntry
    }

    fun createXlRelsWorkbookSheetEntry(doc: Document): List<Element> {
        val sheetEntries = ArrayList<Element>()
        var index = SHEET_COUNT_START
        var relId = RELATIONSHIP_ID_EXTRA_START
        for (everySheet in sheets) {
            relId++
            val el = doc.createElement("Relationship")
            //el.setAttribute("Id", "rId"+relId );
            el.setAttribute("Id", "rId$index")
            el.setAttribute("Type",
                    "http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet")
            el.setAttribute("Target", "worksheets/sheet$index.xml")
            sheetEntries.add(el)
            index++

        }

        val ss = doc.createElement("Relationship")
        val st = doc.createElement("Relationship")
        val tt = doc.createElement("Relationship")

        //Add SharedStrings
        ss.setAttribute("Id", "rId" + index + 1)
        ss.setAttribute("Type",
                "http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings")
        ss.setAttribute("Target", "sharedStrings.xml")
        sheetEntries.add(ss)

        //Add Styles
        st.setAttribute("Id", "rId" + index + 2)
        st.setAttribute("Type",
                "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles")
        st.setAttribute("Target", "styles.xml")
        sheetEntries.add(st)

        //Add Theme
        tt.setAttribute("Id", "rId" + index + 3)
        tt.setAttribute("Type",
                "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme")
        tt.setAttribute("Target", "theme/theme1.xml")
        sheetEntries.add(tt)
        return sheetEntries
    }

    fun createXlWorkbookSheetEntry(doc: Document): List<Element> {
        val sheetEntries = ArrayList<Element>()
        var index = SHEET_COUNT_START
        var relId = RELATIONSHIP_ID_EXTRA_START
        for (everySheet in sheets) {

            relId++
            val el = doc.createElement("sheet")
            el.setAttribute("name", everySheet.title)
            el.setAttribute("sheetId", index.toString())
            //el.setAttribute("r:id", "rId"+relId);
            el.setAttribute("r:id", "rId$index")

            sheetEntries.add(el)

            index++

        }
        return sheetEntries
    }

    fun writeDocumentToFile(doc: Document?, xmlFile: File) {
        val transformer: Transformer
        try {
            transformer = TransformerFactory.newInstance().newTransformer()
            val output = StreamResult(FileOutputStream(xmlFile))
            val input = DOMSource(doc)

            transformer.transform(input, output)
        } catch (e: TransformerConfigurationException) {
            e.printStackTrace()
        } catch (e: TransformerException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

    }

    fun generateContentTypeXMLDocument(data: String): Document? {
        val kxml= UstadMobileSystemImpl.instance.newXMLSerializer()
        val kxmlp = UstadMobileSystemImpl.instance.newPullParser()

        val dbf = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder

        try {
            db = dbf.newDocumentBuilder()
            val doc = db.parse(InputSource(StringReader(data)))
            val types = doc.getElementsByTagName("Types")
            val overrides = doc.getElementsByTagName("Override")

            for (everySheetEntry in createContentTypeSheetEntry(doc)) {
                types.item(types.getLength() - 1).appendChild(everySheetEntry)
            }

            println(nodeToString(doc))
            return doc

        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: TransformerException) {
            e.printStackTrace()
        }

        return null
    }

    fun generateAppXMLDocumnet(data: String): Document? {
        val dbf = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder

        try {
            db = dbf.newDocumentBuilder()
            val doc = db.parse(InputSource(StringReader(data)))


            //1. Update Worksheet <vt:vector size="X"..> with sheet size.
            doc.getElementsByTagName("HeadingPairs").item(0).getFirstChild()
                    .getChildNodes().item(1).getChildNodes().item(0)
                    .setTextContent(sheets.size.toString())

            //2. Add sheet entries
            for (everySheetEntry in createAppSheetEntry(doc)) {
                doc.getElementsByTagName("TitlesOfParts").item(0).getFirstChild()
                        .appendChild(everySheetEntry)
            }

            //3. Update vt:vector size
            doc.getElementsByTagName("TitlesOfParts").item(0)
                    .getFirstChild().getAttributes().getNamedItem("size")
                    .setTextContent(sheets.size.toString())


            println(nodeToString(doc))
            return doc

        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: TransformerException) {
            e.printStackTrace()
        }

        return null
    }

    fun generateXlRelsWorkbookXMLDocument(data: String): Document? {
        val dbf = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder

        try {
            db = dbf.newDocumentBuilder()
            val doc = db.parse(InputSource(StringReader(data)))

            //1. Add Sheets to relationships
            val relationships = doc.getElementsByTagName("Relationships")
            for (everySheetEntry in createXlRelsWorkbookSheetEntry(doc)) {
                relationships.item(0).appendChild(everySheetEntry)
            }


            println(nodeToString(doc))
            return doc

        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: TransformerException) {
            e.printStackTrace()
        }

        return null
    }

    fun generateWorkbookXMLDocument(data: String): Document? {
        val dbf = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder

        try {
            db = dbf.newDocumentBuilder()
            val doc = db.parse(InputSource(StringReader(data)))

            //1. Add Sheets entry with relationship id
            val sheets = doc.getElementsByTagName("sheets")
            for (everySheetEntry in createXlWorkbookSheetEntry(doc)) {
                sheets.item(0).appendChild(everySheetEntry)
            }

            println(nodeToString(doc))
            return doc

        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: TransformerException) {
            e.printStackTrace()
        }

        return null
    }

    fun generateSharedStringXML(data: String): Document? {
        val dbf = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder

        try {
            db = dbf.newDocumentBuilder()
            val doc = db.parse(InputSource(StringReader(data)))

            //1.
            val siNodeList = doc.getElementsByTagName("sst")

            //Loop through all values

            val valueIterator = sharedStringMap.keys.iterator()

            while (valueIterator.hasNext()) {
                val value = valueIterator.next()
                val valueId = sharedStringMap[value]

                val newStringElement = doc.createElement("si")
                newStringElement.setAttribute("umid", valueId.toString())
                val newStringElementValue = doc.createElement("t")
                newStringElementValue.setTextContent(value)
                newStringElement.appendChild(newStringElementValue)
                siNodeList.item(siNodeList.getLength() - 1).appendChild(newStringElement)
            }

            println(nodeToString(doc))
            return doc

        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: TransformerException) {
            e.printStackTrace()
        }

        return null
    }

    fun generateSheetXMLList(data: String): List<Document> {
        val allSheets = ArrayList<Document>()
        val dbf = DocumentBuilderFactory.newInstance()
        var db: DocumentBuilder

        for (everySheet in sheets) {

            try {
                db = dbf.newDocumentBuilder()
                val doc = db.parse(InputSource(StringReader(data)))

                val rows = ArrayList<Element>()
                val rowMap = LinkedHashMap<Int, Element>()
                val rowColElements = LinkedHashMap<Int, MutableList<Element>>()
                //1. Add Values to sheet
                //List<UmSheet.TableValue> values = everySheet.getTableValueList();
                //for(UmSheet.TableValue everyValuePair:values){
                for (everyValuePair in everySheet.getSheetValues()) {

                    val r = everyValuePair.rowIndex
                    val c = everyValuePair.colIndex
                    val v = everyValuePair.value

                    val valueId = addValueToSharedString(v)

                    if (!rowMap.containsKey(r)) {
                        val newRowElement = doc.createElement("row")
                        newRowElement.setAttribute("r", (r + 1).toString())
                        newRowElement.setAttribute("spans", "1:")
                        newRowElement.setAttribute("x14ac:dyDescent", "0.25")
                        rowMap.put(r, newRowElement)
                    }

                    val newColElement = doc.createElement("c")
                    newColElement.setAttribute("r", toAlphabetic(c) + (r + 1))
                    newColElement.setAttribute("t", "s")

                    val newColValue = doc.createElement("v")
                    newColValue.setTextContent(valueId.toString())
                    newColElement.appendChild(newColValue)

                    val colEntries: MutableList<Element>?

                    if (!rowColElements.containsKey(r)) {
                        colEntries = ArrayList<Element>()
                    } else {
                        colEntries = rowColElements[r]
                    }
                    colEntries!!.add(newColElement)
                    rowColElements[r] = colEntries
                }

                //Update row's col count in "spans"
                val rowMapIterator = rowMap.keys.iterator()
                while (rowMapIterator.hasNext()) {
                    val rowth = rowMapIterator.next()
                    val rowColCount = rowColElements[rowth]!!.size
                    val thisRowElement = rowMap.get(rowth)
                    thisRowElement!!.setAttribute("spans", "1:$rowColCount")
                }

                //Update doc.
                val sheetDataNodeList = doc.getElementsByTagName("sheetData")
                val rowIterator = rowMap.keys.iterator()
                while (rowIterator.hasNext()) {
                    val rowKey = rowIterator.next()
                    val rowElement = rowMap.get(rowKey)
                    val colElements = rowColElements[rowKey]
                    for (everyColElement in colElements!!) {
                        rowElement!!.appendChild(everyColElement)
                    }

                    sheetDataNodeList.item(0).appendChild(rowElement)
                }

                println(nodeToString(doc))
                println("next")

                allSheets.add(doc)
            } catch (e: ParserConfigurationException) {
                e.printStackTrace()
            } catch (e: SAXException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: TransformerException) {
                e.printStackTrace()
            }

        }

        return allSheets
    }

    fun generateXMLDocument(data: String): Document? {
        val dbf = DocumentBuilderFactory.newInstance()
        var db: DocumentBuilder? = null

        try {
            db = dbf.newDocumentBuilder()
            return db!!.parse(InputSource(StringReader(data)))

        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    companion object {
        val AUTHOR = "UstadMobile"

        val UTF8 = "UTF-8"
        val SHEET_COUNT_START = 1
        val RELATIONSHIP_ID_EXTRA_START = 4

        val CONTENT_TYPE_FILE = "[Content_Types].xml"
        val REL_FOLDER = "_rels"
        val RELS_FILE = ".rels"
        val APP_FILE = "app.xml"
        val CORE_FILE = "core.xml"
        val DOCPROPS_FOLDER = "docProps"
        val XL_FOLDER = "xl"
        val XL_RELS_FOLDER = "_rels"
        val XL_RELS_WORKBOOK_RELS_FILE = "workbook.xml.rels"
        val XL_THEME_FOLDER = "theme"
        val XL_THEME_THEMES_FILE = "theme1.xml"
        val XL_WORKSHEET_FOLDER = "worksheets"
        val SHARED_STRINGS_FILE = "sharedStrings.xml"
        val STYLES_FILE = "styles.xml"
        val WORKBOOK_FILE = "workbook.xml"
        val XL_WORKSHEET_SHEET_FILE = "sheet1.xml"

        var CONTENT_TYPE_FILE_XML_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/><Override PartName=\"/xl/theme/theme1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.theme+xml\"/><Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/><Override PartName=\"/xl/sharedStrings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\"/><Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/><Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/></Types>"
        var RELS_FILE_XML_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\" Target=\"docProps/app.xml\"/><Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" Target=\"docProps/core.xml\"/><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>"
        var RELS_FILE_XML_DATA_OLD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\" Target=\"docProps/app.xml\"/><Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" Target=\"docProps/core.xml\"/><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>"
        var APP_FILE_XML_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\" xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes\"><Application>Microsoft Excel</Application><DocSecurity>0</DocSecurity><ScaleCrop>false</ScaleCrop><HeadingPairs><vt:vector size=\"2\" baseType=\"variant\"><vt:variant><vt:lpstr>Worksheets</vt:lpstr></vt:variant><vt:variant><vt:i4>1</vt:i4></vt:variant></vt:vector></HeadingPairs><TitlesOfParts><vt:vector size=\"1\" baseType=\"lpstr\"></vt:vector></TitlesOfParts><Company></Company><LinksUpToDate>false</LinksUpToDate><SharedDoc>false</SharedDoc><HyperlinksChanged>false</HyperlinksChanged><AppVersion>16.0300</AppVersion></Properties>"
        var CORE_FILE_XML_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><dc:creator>Varuna Singh</dc:creator><cp:lastModifiedBy></cp:lastModifiedBy><dcterms:created xsi:type=\"dcterms:W3CDTF\">2015-06-05T18:17:20Z</dcterms:created><dcterms:modified xsi:type=\"dcterms:W3CDTF\">2015-06-05T18:17:26Z</dcterms:modified></cp:coreProperties>"
        var XL_RELS_WORKBOOK_RELS_FILE_XML_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"></Relationships>"
        var XL_THEME_THEMES_FILE_XML_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<a:theme xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" name=\"Office Theme\"><a:themeElements><a:clrScheme name=\"Office\"><a:dk1><a:sysClr val=\"windowText\" lastClr=\"000000\"/></a:dk1><a:lt1><a:sysClr val=\"window\" lastClr=\"FFFFFF\"/></a:lt1><a:dk2><a:srgbClr val=\"44546A\"/></a:dk2><a:lt2><a:srgbClr val=\"E7E6E6\"/></a:lt2><a:accent1><a:srgbClr val=\"5B9BD5\"/></a:accent1><a:accent2><a:srgbClr val=\"ED7D31\"/></a:accent2><a:accent3><a:srgbClr val=\"A5A5A5\"/></a:accent3><a:accent4><a:srgbClr val=\"FFC000\"/></a:accent4><a:accent5><a:srgbClr val=\"4472C4\"/></a:accent5><a:accent6><a:srgbClr val=\"70AD47\"/></a:accent6><a:hlink><a:srgbClr val=\"0563C1\"/></a:hlink><a:folHlink><a:srgbClr val=\"954F72\"/></a:folHlink></a:clrScheme><a:fontScheme name=\"Office\"><a:majorFont><a:latin typeface=\"Calibri Light\" panose=\"020F0302020204030204\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/><a:font script=\"Jpan\" typeface=\"游ゴシック Light\"/><a:font script=\"Hang\" typeface=\"맑은 고딕\"/><a:font script=\"Hans\" typeface=\"等线 Light\"/><a:font script=\"Hant\" typeface=\"新細明體\"/><a:font script=\"Arab\" typeface=\"Times New Roman\"/><a:font script=\"Hebr\" typeface=\"Times New Roman\"/><a:font script=\"Thai\" typeface=\"Tahoma\"/><a:font script=\"Ethi\" typeface=\"Nyala\"/><a:font script=\"Beng\" typeface=\"Vrinda\"/><a:font script=\"Gujr\" typeface=\"Shruti\"/><a:font script=\"Khmr\" typeface=\"MoolBoran\"/><a:font script=\"Knda\" typeface=\"Tunga\"/><a:font script=\"Guru\" typeface=\"Raavi\"/><a:font script=\"Cans\" typeface=\"Euphemia\"/><a:font script=\"Cher\" typeface=\"Plantagenet Cherokee\"/><a:font script=\"Yiii\" typeface=\"Microsoft Yi Baiti\"/><a:font script=\"Tibt\" typeface=\"Microsoft Himalaya\"/><a:font script=\"Thaa\" typeface=\"MV Boli\"/><a:font script=\"Deva\" typeface=\"Mangal\"/><a:font script=\"Telu\" typeface=\"Gautami\"/><a:font script=\"Taml\" typeface=\"Latha\"/><a:font script=\"Syrc\" typeface=\"Estrangelo Edessa\"/><a:font script=\"Orya\" typeface=\"Kalinga\"/><a:font script=\"Mlym\" typeface=\"Kartika\"/><a:font script=\"Laoo\" typeface=\"DokChampa\"/><a:font script=\"Sinh\" typeface=\"Iskoola Pota\"/><a:font script=\"Mong\" typeface=\"Mongolian Baiti\"/><a:font script=\"Viet\" typeface=\"Times New Roman\"/><a:font script=\"Uigh\" typeface=\"Microsoft Uighur\"/><a:font script=\"Geor\" typeface=\"Sylfaen\"/></a:majorFont><a:minorFont><a:latin typeface=\"Calibri\" panose=\"020F0502020204030204\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/><a:font script=\"Jpan\" typeface=\"游ゴシック\"/><a:font script=\"Hang\" typeface=\"맑은 고딕\"/><a:font script=\"Hans\" typeface=\"等线\"/><a:font script=\"Hant\" typeface=\"新細明體\"/><a:font script=\"Arab\" typeface=\"Arial\"/><a:font script=\"Hebr\" typeface=\"Arial\"/><a:font script=\"Thai\" typeface=\"Tahoma\"/><a:font script=\"Ethi\" typeface=\"Nyala\"/><a:font script=\"Beng\" typeface=\"Vrinda\"/><a:font script=\"Gujr\" typeface=\"Shruti\"/><a:font script=\"Khmr\" typeface=\"DaunPenh\"/><a:font script=\"Knda\" typeface=\"Tunga\"/><a:font script=\"Guru\" typeface=\"Raavi\"/><a:font script=\"Cans\" typeface=\"Euphemia\"/><a:font script=\"Cher\" typeface=\"Plantagenet Cherokee\"/><a:font script=\"Yiii\" typeface=\"Microsoft Yi Baiti\"/><a:font script=\"Tibt\" typeface=\"Microsoft Himalaya\"/><a:font script=\"Thaa\" typeface=\"MV Boli\"/><a:font script=\"Deva\" typeface=\"Mangal\"/><a:font script=\"Telu\" typeface=\"Gautami\"/><a:font script=\"Taml\" typeface=\"Latha\"/><a:font script=\"Syrc\" typeface=\"Estrangelo Edessa\"/><a:font script=\"Orya\" typeface=\"Kalinga\"/><a:font script=\"Mlym\" typeface=\"Kartika\"/><a:font script=\"Laoo\" typeface=\"DokChampa\"/><a:font script=\"Sinh\" typeface=\"Iskoola Pota\"/><a:font script=\"Mong\" typeface=\"Mongolian Baiti\"/><a:font script=\"Viet\" typeface=\"Arial\"/><a:font script=\"Uigh\" typeface=\"Microsoft Uighur\"/><a:font script=\"Geor\" typeface=\"Sylfaen\"/></a:minorFont></a:fontScheme><a:fmtScheme name=\"Office\"><a:fillStyleLst><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:gradFill rotWithShape=\"1\"><a:gsLst><a:gs pos=\"0\"><a:schemeClr val=\"phClr\"><a:lumMod val=\"110000\"/><a:satMod val=\"105000\"/><a:tint val=\"67000\"/></a:schemeClr></a:gs><a:gs pos=\"50000\"><a:schemeClr val=\"phClr\"><a:lumMod val=\"105000\"/><a:satMod val=\"103000\"/><a:tint val=\"73000\"/></a:schemeClr></a:gs><a:gs pos=\"100000\"><a:schemeClr val=\"phClr\"><a:lumMod val=\"105000\"/><a:satMod val=\"109000\"/><a:tint val=\"81000\"/></a:schemeClr></a:gs></a:gsLst><a:lin ang=\"5400000\" scaled=\"0\"/></a:gradFill><a:gradFill rotWithShape=\"1\"><a:gsLst><a:gs pos=\"0\"><a:schemeClr val=\"phClr\"><a:satMod val=\"103000\"/><a:lumMod val=\"102000\"/><a:tint val=\"94000\"/></a:schemeClr></a:gs><a:gs pos=\"50000\"><a:schemeClr val=\"phClr\"><a:satMod val=\"110000\"/><a:lumMod val=\"100000\"/><a:shade val=\"100000\"/></a:schemeClr></a:gs><a:gs pos=\"100000\"><a:schemeClr val=\"phClr\"><a:lumMod val=\"99000\"/><a:satMod val=\"120000\"/><a:shade val=\"78000\"/></a:schemeClr></a:gs></a:gsLst><a:lin ang=\"5400000\" scaled=\"0\"/></a:gradFill></a:fillStyleLst><a:lnStyleLst><a:ln w=\"6350\" cap=\"flat\" cmpd=\"sng\" algn=\"ctr\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:prstDash val=\"solid\"/><a:miter lim=\"800000\"/></a:ln><a:ln w=\"12700\" cap=\"flat\" cmpd=\"sng\" algn=\"ctr\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:prstDash val=\"solid\"/><a:miter lim=\"800000\"/></a:ln><a:ln w=\"19050\" cap=\"flat\" cmpd=\"sng\" algn=\"ctr\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:prstDash val=\"solid\"/><a:miter lim=\"800000\"/></a:ln></a:lnStyleLst><a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst><a:outerShdw blurRad=\"57150\" dist=\"19050\" dir=\"5400000\" algn=\"ctr\" rotWithShape=\"0\"><a:srgbClr val=\"000000\"><a:alpha val=\"63000\"/></a:srgbClr></a:outerShdw></a:effectLst></a:effectStyle></a:effectStyleLst><a:bgFillStyleLst><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:solidFill><a:schemeClr val=\"phClr\"><a:tint val=\"95000\"/><a:satMod val=\"170000\"/></a:schemeClr></a:solidFill><a:gradFill rotWithShape=\"1\"><a:gsLst><a:gs pos=\"0\"><a:schemeClr val=\"phClr\"><a:tint val=\"93000\"/><a:satMod val=\"150000\"/><a:shade val=\"98000\"/><a:lumMod val=\"102000\"/></a:schemeClr></a:gs><a:gs pos=\"50000\"><a:schemeClr val=\"phClr\"><a:tint val=\"98000\"/><a:satMod val=\"130000\"/><a:shade val=\"90000\"/><a:lumMod val=\"103000\"/></a:schemeClr></a:gs><a:gs pos=\"100000\"><a:schemeClr val=\"phClr\"><a:shade val=\"63000\"/><a:satMod val=\"120000\"/></a:schemeClr></a:gs></a:gsLst><a:lin ang=\"5400000\" scaled=\"0\"/></a:gradFill></a:bgFillStyleLst></a:fmtScheme></a:themeElements><a:objectDefaults/><a:extraClrSchemeLst/><a:extLst><a:ext uri=\"{05A4C25C-085E-4340-85A3-A5531E510DB2}\"><thm15:themeFamily xmlns:thm15=\"http://schemas.microsoft.com/office/thememl/2012/main\" name=\"Office Theme\" id=\"{62F939B6-93AF-4DB8-9C6B-D6C7DFDC589F}\" vid=\"{4A3C46E8-61CC-4603-A589-7422A47A8E4A}\"/></a:ext></a:extLst></a:theme>"
        var SHARED_STRINGS_FILE_XML_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"1\" uniqueCount=\"1\"></sst>"
        var STYLES_FILE_XML_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" mc:Ignorable=\"x14ac x16r2\" xmlns:x14ac=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac\" xmlns:x16r2=\"http://schemas.microsoft.com/office/spreadsheetml/2015/02/main\"><fonts count=\"1\" x14ac:knownFonts=\"1\"><font><sz val=\"11\"/><color theme=\"1\"/><name val=\"Calibri\"/><family val=\"2\"/><scheme val=\"minor\"/></font></fonts><fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills><borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs><cellXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/></cellXfs><cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles><dxfs count=\"0\"/><tableStyles count=\"0\" defaultTableStyle=\"TableStyleMedium2\" defaultPivotStyle=\"PivotStyleLight16\"/><extLst><ext uri=\"{EB79DEF2-80B8-43e5-95BD-54CBDDF9020C}\" xmlns:x14=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/main\"><x14:slicerStyles defaultSlicerStyle=\"SlicerStyleLight1\"/></ext><ext uri=\"{9260A510-F301-46a8-8635-F512D64BE5F5}\" xmlns:x15=\"http://schemas.microsoft.com/office/spreadsheetml/2010/11/main\"><x15:timelineStyles defaultTimelineStyle=\"TimeSlicerStyleLight1\"/></ext></extLst></styleSheet>"
        var WORKBOOK_FILE_XML_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" mc:Ignorable=\"x15\" xmlns:x15=\"http://schemas.microsoft.com/office/spreadsheetml/2010/11/main\"><fileVersion appName=\"xl\" lastEdited=\"6\" lowestEdited=\"6\" rupBuild=\"14420\"/><workbookPr filterPrivacy=\"1\" defaultThemeVersion=\"164011\"/><bookViews><workbookView xWindow=\"0\" yWindow=\"0\" windowWidth=\"22260\" windowHeight=\"12645\"/></bookViews><sheets></sheets><calcPr calcId=\"162913\"/><extLst><ext uri=\"{140A7094-0E35-4892-8432-C4D2E57EDEB5}\" xmlns:x15=\"http://schemas.microsoft.com/office/spreadsheetml/2010/11/main\"><x15:workbookPr chartTrackingRefBase=\"1\"/></ext></extLst></workbook>"
        var XL_WORKSHEET_SHEET_FILE_XML_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:x14ac=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac\" mc:Ignorable=\"x14ac\">" +
                "  <dimension ref=\"A1\"/>" +
                "  <sheetViews>" +
                "    <sheetView tabSelected=\"1\" workbookViewId=\"0\"/>" +
                "  </sheetViews>" +
                "  <sheetFormatPr defaultRowHeight=\"15\" x14ac:dyDescent=\"0.25\"/>" +
                "  <sheetData/>" +
                "  <pageMargins left=\"0.7\" right=\"0.7\" top=\"0.75\" bottom=\"0.75\" header=\"0.3\" footer=\"0.3\"/>" +
                "</worksheet>"

        val CONTENT_TYPE_TAG_VALUE = "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"
        val CONTENT_TYPE_PARTNAME = "PartName"
        val CONTENT_TYPE_CONTENTTYPE = "ContentType"

        @Throws(TransformerException::class)
        private fun nodeToString(node: Node): String {
            val buf = StringWriter()
            val xform = TransformerFactory.newInstance().newTransformer()
            xform.transform(DOMSource(node), StreamResult(buf))
            return buf.toString()
        }

        fun toAlphabetic(i: Int): String {
            if (i < 0) {
                return "-" + toAlphabetic(-i - 1)
            }

            val quot = i / 26
            val rem = i % 26
            val letter = ('A'.toInt() + rem).toChar()
            return if (quot == 0) {
                "" + letter
            } else {
                toAlphabetic(quot - 1) + letter
            }
        }
    }

}
