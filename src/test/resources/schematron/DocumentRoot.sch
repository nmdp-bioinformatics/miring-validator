<?xml version="1.0" encoding="utf-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    
    <pattern name="Check Document Root">
        <rule context="/*">
            <assert test="name()='hml'">Root element is <name/>, not hml</assert>
            <report test="name()='hml'">Root element is hml</report>
        </rule>
    </pattern>

</schema>