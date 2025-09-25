<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <fo:root>
            <fo:layout-master-set>
                <fo:simple-page-master master-name="A4" page-height="29.7cm" page-width="21cm" margin="1cm">
                    <fo:region-body margin-top="2cm"/>
                    <fo:region-before extent="2cm"/>
                </fo:simple-page-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="A4">
                <fo:static-content flow-name="xsl-region-before">
                    <fo:block font-size="16pt" font-weight="bold" text-align="center">
                        INVOICE
                    </fo:block>
                </fo:static-content>

                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-size="12pt">
                        Customer: <xsl:value-of select="/invoice/customer"/>
                    </fo:block>
                    <fo:block font-size="12pt">
                        Amount: $<xsl:value-of select="/invoice/amount"/>
                    </fo:block>
                    <fo:block font-size="12pt">
                        Date: <xsl:value-of select="/invoice/date"/>
                    </fo:block>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>