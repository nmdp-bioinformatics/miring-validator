<?xml version="1.0" encoding="utf-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">

    <pattern name="Character $ forbidden">
        <rule context="*">
            <report test="contains(.,'$')">Text in element <name/> must not contain character $</report>
        </rule>
    </pattern>

</schema>