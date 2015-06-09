<?xml version="1.0" encoding="utf-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <ns prefix="hml" uri="http://schemas.nmdp.org/spec/hml/1.0.1" />

    
    
    <pattern name="Reference Sequence Start and End">    
        <rule context="hml:reference-sequence">            
            <assert test="number(@end) >= number(@start)">On a reference sequence node, end attribute should be greater than or equal to the start attribute.</assert>
        </rule>
    </pattern>

</schema>