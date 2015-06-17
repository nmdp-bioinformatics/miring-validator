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
            
            <assert test="//hml:reference-sequence[@id = $csbRefSeqId and @start &lt;= $csbStart]">The start attribute on a consensus-sequence-block node should be greater than or equal to the start attribute on the corresponding reference-sequence node.</assert>
            <assert test="//hml:reference-sequence[@id = $csbRefSeqId and @end &gt;= $csbEnd]">The end attribute on a consensus-sequence-block node should be less than or equal to the end attribute on the corresponding reference-sequence node.</assert>
        </rule>
    </pattern>

    <!--
    Rule 4.2.3.e
    Length of sequence node text (trimmed) should be = end-start.
    There are sequence nodes elsewhere in HML.  Only want the children of CSBs
      -->
    <pattern name="CSB Sequence Length">
        <rule context="//hml:consensus-sequence-block/hml:sequence">
            <let name="seq" value="replace(normalize-space(.),' ','')" />
            <let name="seqLength" value="string-length($seq)" />
            <assert test="..[@end - @start = $seqLength]">For every consensus-sequence-block node, the child sequence node must have a length of (end - start).  The sequence has a length of <value-of select="$seqLength"/>. start=<value-of select="../@start"/>. end=<value-of select="../@end"/> </assert>
        </rule>
    </pattern>

    <!--
    Rule 4.2.4.b
      -->
    <pattern name="Depricated CSB phasing-group">
        <rule context="hml:consensus-sequence-block">
            <report test="@phasing-group">On a consensus-sequence-block node, the phasing-group attribute is deprecated.</report>
        </rule>
    </pattern>
      
    <!--
    Rule 4.2.7.b
     -->
    <pattern name="CSB continuity">
        <rule context="hml:consensus-sequence-block">
            <let name="csbCont" value="attribute(continuity)" />
            <let name="csbStart" value="attribute(start)" />
            <let name="csbRefSeqID" value="attribute(reference-sequence-id)" />
            <let name="csbPhaseSet" value="attribute(phase-set)" />
            <let name="csbPreviousEnd" value="preceding-sibling::*[@reference-sequence-id=$csbRefSeqID and @phase-set=$csbPhaseSet][1]/@end" />
            <report test="$csbCont='true' and $csbStart!=$csbPreviousEnd and $csbPreviousEnd!='' ">A consensus-sequence-block with attribute continuity="true" does not appear to be continuous with it's previous sibling consensus-sequence-block node, matched by reference-sequence-id and phase-set.</report>
        </rule>
    </pattern>
    
</schema>