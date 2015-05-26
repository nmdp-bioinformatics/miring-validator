<?xml version="1.0" encoding="utf-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <ns prefix="hml" uri="http://schemas.nmdp.org/spec/hml/1.0.1" />
    <pattern name="Check the HMLID">
        <rule context="hml:hml">
            <assert test="hmlid">There is an element for hmlid.</assert>
            <report test="hmlid">The hmlid element is not present, but required by MIRING.</report>
            <assert test="@version">HML root node must include a version number.</assert>
        </rule>
    </pattern>
</schema>