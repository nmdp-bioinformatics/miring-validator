<?xml version="1.0" encoding="utf-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <ns prefix="hml" uri="http://schemas.nmdp.org/spec/hml/1.0.1" />
    
    
    <pattern name="Reference Sequence Start and End">    
        <rule context="hml:reference-sequence">
        
            <assert test="@start">Reference Sequence node must include a @start attribute.</assert>
            
            <assert test="@end">Reference Sequence node must include a @end attribute.</assert>
            
            <assert test="number(@start) = '0'">start attribute on reference-sequence nodes should be 0.</assert>
            
            <assert test="number(@end) >= number(@start)">end attribute should be greater than or equal to the start attribute.</assert>

        </rule>        
    </pattern>
        
        
</schema>