<?xml version='1.0'?>

<!--
   Petstore Page Layouts

   Copyright (C) 2011 Inqwell Ltd

   You may distribute under the terms of the Artistic License, as specified in
   the README file.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version='1.0'>

  <xsl:template name="page-layouts">
    <fo:layout-master-set>

      <fo:simple-page-master master-name="A4"
            page-width="21cm"
            page-height="29.7cm"
            margin-right="1.5cm"
            margin-left="1.5cm"
            margin-bottom="2cm"
            margin-top="1cm">
        <fo:region-body margin-top="8cm" margin-bottom="3cm"/>
        <fo:region-before extent="3cm"/>
        <fo:region-after extent="2cm"/>
      </fo:simple-page-master>

      <fo:simple-page-master master-name="A4L"
            reference-orientation="90"
            page-width="21cm"
            page-height="29.7cm"
            margin-right="1.5cm"
            margin-left="1.5cm"
            margin-bottom="2cm"
            margin-top="1cm">
        <fo:region-body margin-top="4cm" margin-bottom="3cm"/>
        <fo:region-before extent="3cm"/>
        <fo:region-after extent="2cm"/>
      </fo:simple-page-master>

    </fo:layout-master-set>

    <!--
    Add other simple page master templates for desired page sizes/layouts
    -->

  </xsl:template>

</xsl:stylesheet>
