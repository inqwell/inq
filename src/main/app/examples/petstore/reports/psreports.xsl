<?xml version='1.0'?>

<!--
   Petstore - header, footer and reports

   Copyright (C) 2011 Inqwell Ltd

   You may distribute under the terms of the Artistic License, as specified in
   the README file.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version='1.0'>

  <xsl:include href="pagelayout.xsl"/>
  <xsl:include href="tableutils.xsl"/>

  <!--
  Templates to transform report content provided in a XML file
  into xsl:fo for rendering by (eg) Apache FOP
  -->

  <!--
  Puts a report title, account name, date (left side) and logo (right side)
  in region-before. This is achieved using a table spanning the available
  width.
  @param report the root of the static content (whose descendant structure
                is assumed).
  -->
  <xsl:template name="report-header">
    <xsl:param name="report"/>
    <fo:static-content flow-name="xsl-region-before">
      <fo:table width="100%" table-layout="fixed">
        <fo:table-column column-width="70%"/>
        <fo:table-column column-width="30%"/>
        <fo:table-body>
          <fo:table-row>
            <fo:table-cell>
              <!--
              <fo:block space-after="14pt">
                <xsl:value-of select="$report/title"/>
              </fo:block>
              -->
              <fo:block space-after="14pt" font-size="100%">
                <fo:block><xsl:value-of select="data/Account/FirstName"/></fo:block>
                <fo:block><xsl:value-of select="data/Account/LastName"/></fo:block>
                <fo:block><xsl:value-of select="data/Account/Addr1"/></fo:block>
                <fo:block><xsl:value-of select="data/Account/Addr2"/></fo:block>
                <fo:block><xsl:value-of select="data/Account/City"/></fo:block>
                <fo:block><xsl:value-of select="data/Account/State"/></fo:block>
                <fo:block><xsl:value-of select="data/Account/ZIP"/></fo:block>
                <fo:block><xsl:value-of select="data/Account/Country"/></fo:block>
              </fo:block>
              <!--
              <fo:block font-size="80%">
                <xsl:value-of select="$report/date"/>
              </fo:block>
              -->
            </fo:table-cell>
            <fo:table-cell>
              <fo:block text-align="center">
                <fo:external-graphic vertical-align="top" src="url('app/examples/petstore/reports/petstore.png')"/>
              </fo:block>
              <fo:block text-align="center">
                Bringing Pets To You Since 1871
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-body>
      </fo:table>
    </fo:static-content>
  </xsl:template>

  <!--
  Footer to region-after
  @param report the root of the static content (whose descendant structure
                is assumed).
  -->
  <xsl:template name="report-footer">
    <xsl:param name="report"/>
    <fo:static-content flow-name="xsl-region-after">
      <fo:block line-height="14pt"
                font-size="10pt"
                text-align="end">Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/>
      </fo:block>
      <fo:block font-size="6pt"
                text-align="center"
                color="gray"
                margin-left="2cm"
                margin-right="2cm">
        <xsl:value-of select="$report/disclaimer"/>
      </fo:block>
    </fo:static-content>
  </xsl:template>

  <!--
  Order Summary Report
  -->
  <xsl:template match="ordersummary">
    <fo:root>
      <xsl:call-template name="page-layouts"/>
      <fo:page-sequence master-reference="A4">
        <xsl:call-template name="report-header">
          <xsl:with-param name="report" select="report"/>
        </xsl:call-template>
        <xsl:call-template name="report-footer">
          <xsl:with-param name="report" select="report"/>
        </xsl:call-template>
        <fo:flow flow-name="xsl-region-body">
          <fo:block>
            <xsl:call-template name="create-table">
              <xsl:with-param name="table" select="summarytable"/>
              <xsl:with-param name="meta" select="INQmetadata"/>
              <xsl:with-param name="data" select="data"/>
            </xsl:call-template>
          </fo:block>
          <fo:block id="last-page"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <!--
  Order Detail Report
  -->
  <xsl:template match="orderdetail">
    <fo:root>
      <xsl:call-template name="page-layouts"/>
      <fo:page-sequence master-reference="A4">
        <xsl:call-template name="report-header">
          <xsl:with-param name="report" select="report"/>
        </xsl:call-template>
        <xsl:call-template name="report-footer">
          <xsl:with-param name="report" select="report"/>
        </xsl:call-template>
        <fo:flow flow-name="xsl-region-body">
          <fo:block space-after="10pt">
            <xsl:call-template name="one-row">
              <xsl:with-param name="table" select="order"/>
              <xsl:with-param name="meta" select="INQmetadata"/>
              <xsl:with-param name="data" select="data"/>
            </xsl:call-template>
          </fo:block>
          <fo:block font-size="8pt">
            <xsl:call-template name="create-table">
              <xsl:with-param name="table" select="itemstable"/>
              <xsl:with-param name="meta" select="INQmetadata"/>
              <xsl:with-param name="data" select="data"/>
            </xsl:call-template>
          </fo:block>
          <fo:block id="last-page"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

</xsl:stylesheet>
