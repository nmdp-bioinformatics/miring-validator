<?xml version="1.0" encoding="utf-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <ns prefix="hml" uri="http://schemas.nmdp.org/spec/hml/1.0.1" />

    <!--
    Rule 5.2.b
      -->
    <pattern name="variant Start and End">    
        <rule context="hml:variant">
            <assert test="number(@end) >= number(@start)">On a variant node, end attribute should be greater than or equal to the start attribute.</assert>
        </rule>
    </pattern>

    <!--
    Rule 5.2.d
      -->
    <pattern name="variant Start and End Within Refseq">
        <rule context="hml:variant">
            <let name="varRefSeqId" value="parent::*/@reference-sequence-id" />
            <let name="varStart" value="attribute(start)" />
            <let name="varEnd" value="attribute(end)" />
            
            <assert test="//hml:reference-sequence[@id = $varRefSeqId and @start &lt;= $varStart]">
            The start attribute on a variant node should be greater than or equal to the start attribute on the corresponding reference-sequence node.  variant:start=<value-of select="$varStart"/> ,RefSeq:start=<value-of select="//hml:reference-sequence[@id = $varRefSeqId]/@start"/>
            </assert>
            <assert test="//hml:reference-sequence[@id = $varRefSeqId and @end &gt;= $varEnd]">
            The end attribute on a variant node should be less than or equal to the end attribute on the corresponding reference-sequence node.  variant:end=<value-of select="$varEnd"/> ,RefSeq:end=<value-of select="//hml:reference-sequence[@id = $varRefSeqId]/@end"/>
            </assert>
        </rule>
    </pattern>
      
    <!--
    Rule 5.3.b and 5.3.c
      -->
     <pattern name="variant IDs">
        <rule context="hml:variant">
            <let name="varID" value="replace(number(attribute(id)),'NaN','?')" />
            <let name="prevVarID" value="replace(number(preceding-sibling::*[1]/@id),'NaN','?')" />
            <let name="nextVarID" value="replace(number(following-sibling::*[1]/@id),'NaN','?')" />
            
            <report test="$varID = '?'">The variant nodes under a single consensus-sequence-block must have id attributes that are integers ranging from 0:n-1, where n is the number of variants.111 previd=<value-of select="$prevVarID"/> id=<value-of select="$varID"/> nextid=<value-of select="$nextVarID"/></report>            
            <assert test="$nextVarID=($varID + 1) or $nextVarID='?'" >The variant nodes under a single consensus-sequence-block must have id attributes that are integers ranging from 0:n-1, where n is the number of variants.222 previd=<value-of select="$prevVarID"/> id=<value-of select="$varID"/> nextid=<value-of select="$nextVarID"/></assert>
            <assert test="$prevVarID=($varID - 1) or $prevVarID='?'" >The variant nodes under a single consensus-sequence-block must have id attributes that are integers ranging from 0:n-1, where n is the number of variants.333 previd=<value-of select="$prevVarID"/> id=<value-of select="$varID"/> nextid=<value-of select="$nextVarID"/></assert>
        </rule>
    </pattern>

</schema>