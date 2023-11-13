package com.ustadmobile.core.contentformats.epub

import com.ustadmobile.core.contentformats.epub.minxhtml.MinXhtmlDocument
import nl.adaptivity.xmlutil.serialization.XML
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.nodes.Entities
import org.jsoup.nodes.DocumentType
import org.jsoup.nodes.TextNode

class XhtmlFixerJsoup(
    private val xml: XML,
) : XhtmlFixer {
    override fun fixXhtml(xhtml: String): XhtmlFixResult {
        try {
            xml.decodeFromString(MinXhtmlDocument.serializer(), xhtml)
            return XhtmlFixResult(true, xhtml)
        }catch(e: Exception) {
            val doc = Jsoup.parse(xhtml, Parser.htmlParser())
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml)
            doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml)

            //Replace the DocumentType with standard HTML5 DocType
            doc.documentType()?.also {
                it.replaceWith(DocumentType("html", "", ""))

                //Somehow thanks to Storyweaver, we get a "]>" text at the start, so remove that if present
                doc.body().firstChild()
                    .let { it as? TextNode }
                    ?.takeIf { it.text().trim() == "]>" }
                    ?.remove()
            }

            //Remove StoryWeaver loader SVG that is not included in the epub itself
            doc.getElementById("pb-dictionary-loder")?.remove()

            return XhtmlFixResult(false, doc.html())
        }
    }
}
