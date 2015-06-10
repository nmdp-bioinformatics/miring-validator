<?xml version="1.0" encoding="utf-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <ns prefix="hml" uri="http://schemas.nmdp.org/spec/hml/1.0.1" />

    <!-- 
    Rule 2.2.c
     -->
    <pattern name="Reference Sequence Start and End">    
        <rule context="hml:reference-sequence">            
            <assert test="number(@end) >= number(@start)">On a reference sequence node, end attribute should be greater than or equal to the start attribute.</assert>
        </rule>
    </pattern>
    
    <!--
    Rule 2.2.1.c
    Get every id belonging to a reference-sequence.
    Assert that there is a node with a idref of "@id" on a node named "consensus-sequence-block:
     -->
    <pattern name="Reference Sequence ID">    
        <rule context="hml:reference-sequence">            
            <let name="refSeqId" value="attribute(id)" />
            <assert test="//hml:consensus-sequence-block[@reference-sequence-id=$refSeqId]">A reference-sequence node has an id attribute with no corresponding consensus-sequence-block id attribute.</assert>
        </rule>
    </pattern>
</schema>