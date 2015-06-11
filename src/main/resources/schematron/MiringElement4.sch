<?xml version="1.0" encoding="utf-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <ns prefix="hml" uri="http://schemas.nmdp.org/spec/hml/1.0.1" />

    <!--
    Rule 4.2.3.b
    end >= start
      -->
    <pattern name="CSB Start and End">    
        <rule context="hml:consensus-sequence-block">
            <assert test="number(@end) >= number(@start)">On a consensus-sequence-block node, end attribute should be greater than or equal to the start attribute.</assert>
        </rule>
    </pattern>
      
    <!--
    Rule 4.2.3.d
    CSB:start >= refseq:start && CSB:end <= refseq:end
    I get the CSB information, then assert that there is a reference sequence with "start<csbStart" and "end>=csbEnd"  (with matching ID)
      -->
    <pattern name="CSB Start and End Within Refseq">
        <rule context="hml:consensus-sequence-block">
            <let name="csbRefSeqId" value="attribute(reference-sequence-id)" />
            <let name="csbStart" value="attribute(start)" />
            <let name="csbEnd" value="attribute(end)" />
            
            <assert test="//hml:reference-sequence[@id = $csbRefSeqId and @start &lt;= $csbStart]">
            The start attribute on a consensus sequence node should be greater than or equal to the start attribute on the corresponding reference-sequence node.  CSB:start=<value-of select="$csbStart"/> ,RefSeq:start=<value-of select="//hml:reference-sequence[@id = $csbRefSeqId]/@start"/>
            </assert>
            <assert test="//hml:reference-sequence[@id = $csbRefSeqId and @end &gt;= $csbEnd]">
            The end attribute on a consensus sequence node should be less than or equal to the end attribute on the corresponding reference-sequence node.  CSB:end=<value-of select="$csbEnd"/> ,RefSeq:end=<value-of select="//hml:reference-sequence[@id = $csbRefSeqId]/@end"/>
            </assert>
        </rule>
    </pattern>

    <!--
    Rule 4.2.3.e
    Length of sequence node text (trimmed) should be = end-start.
      -->
    <pattern name="CSB Sequence Length">
        <rule context="hml:consensus-sequence-block">
            <let name="csbStart" value="number(attribute(start))" />
            <let name="csbEnd" value="number(attribute(end))" />
            <let name="seq" value="./sequence" />
            <let name="seqLength" value="string-length(./sequence)" />

            <report test="//hml:sequence"> CSB: Sequence = <value-of select="$seq"/> length = <value-of select="$seqLength"/> </report>

        </rule>
    </pattern>
      
      
      
      
    <!--
    Rule 4.2.4.b
      -->
    <!--
    Rule 4.2.7.b
     -->



    <!-- 
    Rule 2.2.c
     -->
    <!-- <pattern name="Reference Sequence Start and End">    
        <rule context="hml:reference-sequence">            
            <assert test="number(@end) >= number(@start)">On a reference sequence node, end attribute should be greater than or equal to the start attribute.</assert>
        </rule>
    </pattern> -->
    
    <!--
    Rule 2.2.1.c
    Get every id belonging to a reference-sequence.
    Assert that there is a node with a idref of "@id" on a node named "consensus-sequence-block:
     -->
    <!-- <pattern name="Reference Sequence ID">    
        <rule context="hml:reference-sequence">            
            <let name="refSeqId" value="attribute(id)" />
            <assert test="//hml:consensus-sequence-block[@reference-sequence-id=$refSeqId]">A reference-sequence node has an id attribute with no corresponding consensus-sequence-block id attribute.</assert>
        </rule>
    </pattern> -->

</schema>