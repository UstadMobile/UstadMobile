<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">

  <xsl:param name="strings_src"/>
  <xsl:output method="xml" indent="yes"/>
   <xsl:param name="stringsxml" select="document($strings_src)" />
  
   <xsl:template match="/">
       <xliff version="1.2" xmlns="urn:oasis:names:tc:xliff:document:1.2">
           <file source-language="en" datatype="plaintext" original="ng2.template">
                <body>
                  <xsl:for-each select="xliff:xliff/xliff:file/xliff:body/xliff:trans-unit">
                      <trans-unit datatype="html">
                          <xsl:attribute name="id">
                              <xsl:value-of select="@id"/>
                          </xsl:attribute>

                          <xsl:for-each select="*">
                              <xsl:copy-of select="."/>
                          </xsl:for-each>

                          <target>
                              <xsl:variable name="targetid" select="@id"/>
                              <xsl:choose>
                                <xsl:when test="$stringsxml/resources/string[@name=$targetid]">
                                  <xsl:value-of select="$stringsxml/resources/string[@name=$targetid]"/>
                                </xsl:when>

                                <xsl:otherwise>
                                  <xsl:value-of select="@id"/>
                                </xsl:otherwise>
                              </xsl:choose>
                          </target>

                      </trans-unit>
                  </xsl:for-each>
                </body>
           </file>
       </xliff>
        
   </xsl:template>
</xsl:stylesheet>
