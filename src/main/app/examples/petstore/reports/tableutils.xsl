<?xml version='1.0'?>

<!--
   Petstore - table utilities

   Copyright (C) 2011 Inqwell Ltd

   You may distribute under the terms of the Artistic License, as specified in
   the README file.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version='1.0'>

  <!--
  Tables Utilities
  -->
  
  <!--
  Create a table.
  Outputs a table whose data is selected from a nodeset referenced
  beneath $data.
  @param table a node whose children define any caption, the table columns
               and the nodeset within data section to use
  @param meta  the root of the metadata describing the data within the data
               section.
  @param data  the root of the data section, beneath which the report's data
               is found and within which the data for this table will be
               addressed.
  -->
  <xsl:template name="create-table">
    <xsl:param name="table"/>
    <xsl:param name="meta"/>
    <xsl:param name="data"/>

    <!--
    Output any caption
    -->
    <xsl:call-template name="create-caption">
      <xsl:with-param name="table" select="$table"/>
    </xsl:call-template>
    
    <fo:table width="100%"
              font-size="80%"
              text-align="center"
              table-layout="fixed"
              border-collapse="collapse">
      <!--
              font-family="Arial"
      -->
      
      <!--
      Define the columns
      -->
      <xsl:call-template name="create-columns">
        <xsl:with-param name="table" select="$table"/>
        <xsl:with-param name="meta" select="$meta"/>
      </xsl:call-template>
        
      <!--
      Output the header cells
      -->
      <xsl:call-template name="create-headers">
        <xsl:with-param name="table" select="$table"/>
        <xsl:with-param name="meta" select="$meta"/>
      </xsl:call-template>

      <!--
      Output the data cells.

      First grab the data we've been pointed at by finding the child
      of $data whose @nodeset attribute is equal to the @nodeset
      attribute of the <data> child of $table.
      -->
      <xsl:variable name="nodeset" select="$table/from/@nodeset"/>
      <xsl:variable name="tabledata" select="$data/*[@nodeset=$nodeset]"/>
      
      <fo:table-body>
        <!--
        Then loop over the data and within that the columns to create the cells
        (Note as this whole template stands it would produce an empty body
        if there were no data rows. This is illegal fo: and we should guard
        against that. TODO (but hopefully this script is not called by upper
        layers in that case...)
        -->
        <xsl:for-each select="$tabledata/child">
          <xsl:variable name="child" select="."/>
          <fo:table-row>
            <xsl:for-each select="$table/column">
              <xsl:variable name="typedef" select="@typedef"/>
              <xsl:variable name="field" select="@field"/>
              <xsl:variable name="content" select="$child/*[@typedef=$typedef]/*[@field=$field]"/>
              <fo:table-cell border="1pt solid black"
                             padding="2pt"
                             linefeed-treatment="preserve"
                             white-space-collapse="false"
                             wrap-option="wrap"
                             white-space-treatment="preserve">
                <fo:block>
                  <!--
                  When there is a total attribute on a child then apply bold
                  to all the cells. This is one kind of total handling, see also
                  another one below
                  -->
                  <xsl:if test="$child/@total">
                    <xsl:attribute name="font-weight">bold</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="$meta/INQtypedef[@INQfqname=$typedef]/INQfield[@INQname=$field]/INQisnumeric">
                    <xsl:attribute name="text-align"><xsl:value-of select="'end'"/></xsl:attribute>
                  </xsl:if>
                  <xsl:if test="@halign">
                    <xsl:attribute name="text-align"><xsl:value-of select="@halign"/></xsl:attribute>
                  </xsl:if>
                  <xsl:value-of select="$content"/>
                </fo:block>
              </fo:table-cell>
            </xsl:for-each>
          </fo:table-row>
        </xsl:for-each>
        <!--
        Another form of total. In this case, output bordered cells for any
        children that have content. Output border-less cells for those that
        do not.
        -->
        <xsl:if test="$tabledata/total">
          <xsl:variable name="child" select="$tabledata/total"/>
          <fo:table-row>
            <xsl:for-each select="$table/column">
              <xsl:variable name="typedef" select="@typedef"/>
              <xsl:variable name="field" select="@field"/>
              <xsl:variable name="cell" select="$child/*[@typedef=$typedef]/*[@field=$field]"/>
              <xsl:choose>
                <xsl:when test="string($cell) = ''">
                  <fo:table-cell>
                    <fo:block/>
                  </fo:table-cell>
                </xsl:when>
                <xsl:otherwise>
                  <fo:table-cell border="1pt solid black"
                                 padding="2pt"
                                 linefeed-treatment="preserve"
                                 white-space-collapse="false"
                                 wrap-option="wrap"
                                 white-space-treatment="preserve">
                    <fo:block font-weight="bold">
                      <xsl:if test="$meta/INQtypedef[@INQfqname=$typedef]/INQfield[@INQname=$field]/INQisnumeric">
                        <xsl:attribute name="text-align"><xsl:value-of select="'end'"/></xsl:attribute>
                      </xsl:if>
                      <xsl:if test="@halign">
                        <xsl:attribute name="text-align"><xsl:value-of select="@halign"/></xsl:attribute>
                      </xsl:if>
                      <xsl:value-of select="$cell"/>
                    </fo:block>
                  </fo:table-cell>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </fo:table-row>
        </xsl:if>
      </fo:table-body>
    </fo:table>
  </xsl:template>
  
  <!--
  Create a table comprising headers and a single row using 
  @param table a node whose children define any caption and the table columns.
  @param meta  the root of the metadata describing the data within the data
               section.
  @param data  the root of the data section, beneath which the report's data
               is found and within which the data for this table will be
               addressed.
  -->
  <xsl:template name="one-row">
    <xsl:param name="table"/>
    <xsl:param name="meta"/>
    <xsl:param name="data"/>

    <!--
    Output any caption
    -->
    <xsl:call-template name="create-caption">
      <xsl:with-param name="table" select="$table"/>
    </xsl:call-template>
    
    <fo:table width="100%"
              font-size="80%"
              text-align="center"
              table-layout="fixed"
              border-collapse="collapse">
      <!--
              font-family="Arial"
      -->
      
      <!--
      Define the columns
      -->
      <xsl:call-template name="create-columns">
        <xsl:with-param name="table" select="$table"/>
        <xsl:with-param name="meta" select="$meta"/>
      </xsl:call-template>
        
      <!--
      Output the header cells
      -->
      <xsl:call-template name="create-headers">
        <xsl:with-param name="table" select="$table"/>
        <xsl:with-param name="meta" select="$meta"/>
      </xsl:call-template>

      <!--
      Output the data cells. There is only one row
      -->

      <fo:table-body>
        <!--
        Loop over the columns to create the cells
        -->
        <fo:table-row>
          <xsl:for-each select="$table/column">
            <xsl:variable name="typedef" select="@typedef"/>
            <xsl:variable name="field" select="@field"/>
            <xsl:variable name="content" select="$data/*[@typedef=$typedef]/*[@field=$field]"/>
            <fo:table-cell border="1pt solid black"
                           padding="2pt"
                           linefeed-treatment="preserve"
                           white-space-collapse="false"
                           wrap-option="wrap"
                           white-space-treatment="preserve">
              <fo:block>
                <xsl:if test="$meta/INQtypedef[@INQfqname=$typedef]/INQfield[@INQname=$field]/INQisnumeric">
                  <xsl:attribute name="text-align"><xsl:value-of select="'end'"/></xsl:attribute>
                </xsl:if>
                <xsl:if test="@halign">
                  <xsl:attribute name="text-align"><xsl:value-of select="@halign"/></xsl:attribute>
                </xsl:if>
                <xsl:value-of select="$content"/>
              </fo:block>
            </fo:table-cell>
          </xsl:for-each>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
  </xsl:template>
  
  <xsl:template name="create-columns">
    <xsl:param name="table"/>
    <xsl:param name="meta"/>
    <xsl:for-each select="$table/column">
      <xsl:variable name="typedef" select="@typedef"/>
      <xsl:variable name="field" select="@field"/>
      <fo:table-column column-number="{position()}">
        <xsl:attribute name="column-width">
          <xsl:choose>
            <xsl:when test="@width">
              <xsl:value-of select="@width"/>%
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$meta/INQtypedef[@INQfqname=$typedef]/INQfield[@INQname=$field]/INQwidth"/>em
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </fo:table-column>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="create-headers">
    <xsl:param name="table"/>
    <xsl:param name="meta"/>
    <fo:table-header background-color="#D3D3D3"
                     text-align="center"
                     font-weight="bold">
      <fo:table-row>
        <xsl:for-each select="$table/column">
          <xsl:variable name="typedef" select="@typedef"/>
          <xsl:variable name="field" select="@field"/>
          <fo:table-cell border="1pt solid black"
                         padding="2pt"
                         linefeed-treatment="preserve"
                         white-space-collapse="false"
                         wrap-option="wrap"
                         display-align="center"
                         white-space-treatment="preserve">
            <fo:block>
              <xsl:value-of select="$meta/INQtypedef[@INQfqname=$typedef]/INQfield[@INQname=$field]/INQlabel"/>
            </fo:block>
          </fo:table-cell>
        </xsl:for-each>
      </fo:table-row>
    </fo:table-header>
  </xsl:template>
  
  <!--
  If there is a caption then output it in a paragraph. Note Apache FOP
  does not yet support table-and-caption
  -->
  <xsl:template name="create-caption">
    <xsl:param name="table"/>    
    <xsl:if test="$table/caption">
      <fo:block space-after="5pt">
        <xsl:value-of select="$table/caption"/>
      </fo:block>
    </xsl:if>
  </xsl:template>


</xsl:stylesheet>
